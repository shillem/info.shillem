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

import info.shillem.dao.Query;
import info.shillem.dao.Summary;
import info.shillem.dto.BaseDto;
import info.shillem.dto.BaseField;
import info.shillem.sql.factory.SqlFactory;
import info.shillem.sql.util.SelectQuery;
import info.shillem.sql.util.SelectQuery.Column;
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

    protected Column createSelectQueryColumn(E field) {
        return new Column(field.name());
    }

    protected WhereColumn createSelectQueryWhereColumn(
            E field,
            ComparisonOperator operator,
            Object value) {
        WhereColumn column = new WhereColumn(
                field.name(),
                operator,
                value instanceof Enum ? ((Enum<?>) value).name() : value);

        if (factory.containsOption(SqlFactory.Option.PREFER_LIKE_INSENSITIVE)
                && operator == ComparisonOperator.LIKE) {
            column.addNameFunction("LOWER(%s)").addValueFunction("LOWER(%s)");
        }

        return column;
    }

    protected void populateSelectQuery(SelectQuery select, Query<E> query) {
        query.getSchema().forEach((f) -> select.column(createSelectQueryColumn(f)));

        LWhere wheres = select.wheres();

        switch (query.getType()) {
        case FILTER:
            query.getFilters().forEach((f, value) -> wheres.and(
                    createSelectQueryWhereColumn(f, ComparisonOperator.EQUAL, value)));
            break;

        case SEARCH:
            query.getClauses().forEach((clause) -> populateSelectQueryWhere(wheres, clause));
        default:
            // Do nothing
        }

        query.getSorters().forEach((f, op) -> select.order(f.name(), op));

        if (query.getLimit() > 0
                && query.getOffset() == 0
                && !query.containsOption("FETCH_TOTAL")
                && !query.containsOption("FETCH_TOTAL_ONLY")) {
            select.top(query.getLimit());
        }
        
        if (query.containsOption("DISTINCT")) {
            select.distinct();
        }
    }

    protected void populateSelectQueryWhere(LWhere wheres, Query.Clause clause) {
        if (clause instanceof Query.Group) {
            Query.Group group = (Query.Group) clause;

            if (group.isEmpty()) {
                return;
            }

            WhereGroup wg = new WhereGroup();

            group.getClauses().forEach((p) -> populateSelectQueryWhere(wg.wheres(), p));

            wheres.add(wg);

            return;
        }

        if (clause instanceof Query.Logical) {
            Query.Logical p = (Query.Logical) clause;

            wheres.add(p.getOperator() == LogicalOperator.AND ? WhereLogic.AND : WhereLogic.OR);

            return;
        }

        if (clause instanceof Query.Value) {
            @SuppressWarnings("unchecked")
            Query.Value<E> p = (Query.Value<E>) clause;

            wheres.add(createSelectQueryWhereColumn(p.getField(), p.getOperator(), p.getValue()));

            return;
        }

        if (clause instanceof Query.Values) {
            @SuppressWarnings("unchecked")
            Query.Values<E> p = (Query.Values<E>) clause;

            wheres.add(createSelectQueryWhereColumn(p.getField(), p.getOperator(), p.getValues()));

            return;
        }

        throw new UnsupportedOperationException(
                clause.getClass().getName().concat(" joining is not implemented"));
    }

    protected void pullColumn(
            E field,
            ResultSet resultSet,
            T wrapper,
            Locale locale)
            throws SQLException {
        Class<? extends Serializable> type = field.getValueType().getValueClass();

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

    protected RuntimeException wrappedPullColumnException(Exception e, E field) {
        return new RuntimeException(String.format("Unable to pull column %s", field.name()), e);
    }

    protected T wrapRow(ResultSet resultSet, Supplier<T> supplier, Query<E> query)
            throws SQLException {
        T wrapper = supplier.get();

        pullRow(resultSet, wrapper, query);

        return wrapper;
    }

    protected List<T> wrapStatement(
            String syntax,
            Supplier<T> supplier,
            Query<E> query)
            throws SQLException {
        try (
                Statement statement = factory.getConnection().createStatement(
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = statement.executeQuery(syntax)) {
            Summary summary = query.getSummary();

            if (query.isMaxOffset()) {
                resultSet.absolute(-1);
                resultSet.absolute(summary.recalculateOffset(resultSet.getRow()));

                summary.setTotal(summary.getOffset() + 1);
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
