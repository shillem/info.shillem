package info.shillem.sql.dao;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import info.shillem.dao.FilterQuery;
import info.shillem.dao.PageQuery;
import info.shillem.dao.Query;
import info.shillem.dao.SearchQuery;
import info.shillem.dto.BaseDto;
import info.shillem.dto.BaseField;
import info.shillem.sql.factory.SqlFactory;
import info.shillem.sql.util.QueryConverter;
import info.shillem.sql.util.SqlSearchQueryConverter;
import info.shillem.util.ComparisonOperator;
import info.shillem.util.LogicalOperator;
import info.shillem.util.OrderOperator;
import info.shillem.util.StringUtil;

public abstract class AbstractSqlDao<T extends BaseDto<E>, E extends Enum<E> & BaseField> {

    protected final SqlFactory factory;

    protected AbstractSqlDao(SqlFactory factory) {
        this.factory = factory;
    }

    protected String composeSelect(Query<E> query) {
        return composeSelect(query, null);
    }

    protected String composeSelect(Query<E> query, Integer top) {
        return "SELECT"
                + (top != null ? " TOP " + top : "")
                + " " + query.getSchema().stream()
                        .map((field) -> getColumnName(field) + " AS " + field)
                        .collect(Collectors.joining(", "));
    }

    protected String composeWhere(PageQuery<E> query) {
        if (query instanceof FilterQuery) {
            FilterQuery<E> q = (FilterQuery<E>) query;

            if (q.getFilters().isEmpty()) {
                return null;
            }

            return "WHERE " + q.getFilters().entrySet().stream()
                    .map((e) -> getColumnName(e.getKey())
                            + QueryConverter.formatComparisonOperator(ComparisonOperator.EQUAL)
                            + QueryConverter.formatValue(e.getValue()))
                    .collect(Collectors.joining(
                            QueryConverter.formatLogicalOperator(LogicalOperator.AND)));
        }

        if (query instanceof SearchQuery) {
            SearchQuery<E> q = (SearchQuery<E>) query;

            return "WHERE " + new SqlSearchQueryConverter<>(q, this::getColumnName).toString();
        }

        throw new UnsupportedOperationException(
                "WHERE composition for " + query.getClass().getName() + " is not supported");
    }

    protected String composeOrder(PageQuery<E> query) {
        if (query.getSorters().isEmpty()) {
            return null;
        }

        return "ORDER BY " + query.getSorters().entrySet().stream()
                .map((e) -> getColumnName(e.getKey())
                        + " " + (e.getValue() == OrderOperator.ASCENDING ? "ASC" : "DESC"))
                .collect(Collectors.joining(", "));
    }

    protected String getColumnName(E field) {
        return field.name();
    }

    protected void pullColumn(
            E field,
            ResultSet resultSet,
            T wrapper,
            Locale locale)
            throws SQLException {
        Class<? extends Serializable> type = field.getProperties().getType();

        try {
            Object value = resultSet.getObject(field.toString());

            wrapper.presetValue(field, pullValue(type, value));
        } catch (Exception e) {
            throw wrappedPullColumnException(e, field);
        }
    }

    protected void pullRow(ResultSet resultSet, T wrapper, Query<E> query) throws SQLException {
        for (E field : query.getSchema()) {
            pullColumn(field, resultSet, wrapper, query.getLocale());
        }
    }

    private <V> V pullValue(Class<V> type, Object value) {
        if (type.isEnum()) {
            if (value instanceof String) {
                return type.cast(StringUtil.enumFromString(type, (String) value));
            }
        } else if (type == Boolean.class) {
            return type.cast(Boolean.valueOf((String) value));
        } else if (Number.class.isAssignableFrom(type)) {
            if (value == null) {
                return null;
            }

            Number num = (Number) value;

            if (type == Integer.class) {
                return type.cast(num.intValue());
            }

            if (type == Long.class) {
                return type.cast(num.longValue());
            }

            if (type == Double.class) {
                return type.cast(num.doubleValue());
            }

            if (type == BigDecimal.class) {
                return type.cast(new BigDecimal(num.toString()));
            }
        } else if (type == Date.class && value instanceof Timestamp) {
            return type.cast(new Date(((Timestamp) value).getTime()));
        }

        return type.cast(value);
    }

    protected T wrapRow(ResultSet resultSet, Supplier<T> supplier, Query<E> query)
            throws SQLException {
        T wrapper = supplier.get();

        pullRow(resultSet, wrapper, query);

        return wrapper;
    }

    protected List<T> wrapStatement(String plan, Supplier<T> supplier, PageQuery<E> query) {
        try (
                Statement statement = factory.getConnection().createStatement(
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = statement.executeQuery(plan)) {
            if (query.isUnknownOffset()) {
                resultSet.absolute(-1);

                int lastPageOffset = query.recalculateOffset(resultSet.getRow());

                resultSet.absolute(lastPageOffset);
            } else {
                resultSet.absolute(query.getOffset());
            }

            List<T> wrappers = new ArrayList<>();
            int limit = query.getLimit();
            int count = 0;

            while (resultSet.next() && (limit == 0 || count < limit)) {
                wrappers.add(wrapRow(resultSet, supplier, query));

                count++;
            }

            return wrappers;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected RuntimeException wrappedPullColumnException(Exception e, E field) {
        return new RuntimeException(String.format("Unable to pull column %s", field.name()), e);
    }

}
