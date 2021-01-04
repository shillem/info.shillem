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
import java.util.Objects;
import java.util.function.Supplier;

import info.shillem.dao.FilterQuery;
import info.shillem.dao.PageQuery;
import info.shillem.dao.Query;
import info.shillem.dao.SearchQuery;
import info.shillem.dao.SearchQuery.Group;
import info.shillem.dao.SearchQuery.Logical;
import info.shillem.dao.SearchQuery.Value;
import info.shillem.dao.SearchQuery.Values;
import info.shillem.dto.BaseDto;
import info.shillem.dto.BaseField;
import info.shillem.sql.factory.SqlFactory;
import info.shillem.sql.util.SelectQuery;
import info.shillem.sql.util.SelectQuery.LWhere;
import info.shillem.sql.util.WhereColumn;
import info.shillem.sql.util.WhereGroup;
import info.shillem.sql.util.WhereLogic;
import info.shillem.util.ComparisonOperator;
import info.shillem.util.LogicalOperator;
import info.shillem.util.StringUtil;

public abstract class AbstractSqlDao<T extends BaseDto<E>, E extends Enum<E> & BaseField> {

    protected final SqlFactory factory;

    protected AbstractSqlDao(SqlFactory factory) {
        this.factory = Objects.requireNonNull(factory, "Factory cannot be null");
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

    protected void populateSelectQuery(SelectQuery select, PageQuery<E> query) {
        query.getSchema().forEach((f) -> select.column(f.name()));

        if (query instanceof FilterQuery) {
            FilterQuery<E> q = (FilterQuery<E>) query;

            LWhere wheres = select.wheres();

            q.getFilters().forEach((f, value) -> wheres
                    .and(new WhereColumn(f.name(), ComparisonOperator.EQUAL, value)));
        }

        if (query instanceof SearchQuery) {
            SearchQuery<E> q = (SearchQuery<E>) query;

            LWhere wheres = select.wheres();

            q.getPieces().forEach((piece) -> populateSelectQueryWhere(wheres, piece));
        }

        query.getSorters().forEach((f, op) -> select.order(f.name(), op));
    }

    @SuppressWarnings("unchecked")
    private void populateSelectQueryWhere(LWhere wheres, SearchQuery.Piece piece) {
        if (piece instanceof Group) {
            SearchQuery.Group group = (SearchQuery.Group) piece;

            if (group.isEmpty()) {
                return;
            }

            WhereGroup wg = new WhereGroup();

            group.getPieces().forEach((p) -> populateSelectQueryWhere(wg.wheres(), piece));

            wheres.add(wg);
        }

        if (piece instanceof Logical) {
            SearchQuery.Logical p = (SearchQuery.Logical) piece;

            wheres.add(p.getOperator() == LogicalOperator.AND ? WhereLogic.AND : WhereLogic.OR);
        }

        if (piece instanceof Value) {
            SearchQuery.Value<E> p = (SearchQuery.Value<E>) piece;

            wheres.add(new WhereColumn(p.getField().name(), p.getOperator(), p.getValue()));
        }

        if (piece instanceof Values) {
            SearchQuery.Values<E> p = (SearchQuery.Values<E>) piece;

            wheres.add(new WhereColumn(p.getField().name(), p.getOperator(), p.getValues()));
        }

        throw new UnsupportedOperationException(piece.getClass().getName()
                .concat(" joining is not implemented"));
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

    protected RuntimeException wrappedPullColumnException(Exception e, E field) {
        return new RuntimeException(String.format("Unable to pull column %s", field.name()), e);
    }

    protected T wrapRow(ResultSet resultSet, Supplier<T> supplier, Query<E> query)
            throws SQLException {
        T wrapper = supplier.get();

        pullRow(resultSet, wrapper, query);

        return wrapper;
    }

    protected List<T> wrapStatement(String syntax, Supplier<T> supplier, PageQuery<E> query)
            throws SQLException {
        try (
                Statement statement = factory.getConnection().createStatement(
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = statement.executeQuery(syntax)) {
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
        }
    }

}
