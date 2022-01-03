package info.shillem.dao;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import info.shillem.dto.BaseField;
import info.shillem.util.ComparisonOperator;
import info.shillem.util.LogicalOperator;
import info.shillem.util.OrderOperator;

public class Query<E extends Enum<E> & BaseField> {

    public interface Clause {

    }

    public static class Group implements Clause {

        private final Deque<Clause> deque = new ArrayDeque<>();

        public Group add(Logical logicalValue, Clause newPiece) {
            Clause lastPiece = deque.peekLast();

            if (lastPiece != null && !(lastPiece instanceof Logical)) {
                deque.add(logicalValue);
            }

            deque.add(Objects.requireNonNull(newPiece, "Piece cannot be null"));

            return this;
        }

        public Group and(Clause piece) {
            return add(Logical.AND, piece);
        }

        public Collection<Clause> getClauses() {
            return deque;
        }

        public boolean isEmpty() {
            return deque.isEmpty();
        }

        public Group or(Clause piece) {
            return add(Logical.OR, piece);
        }

        @Override
        public String toString() {
            return deque.toString();
        }

    }

    public static class Logical implements Clause {

        public static final Logical AND = new Logical(LogicalOperator.AND);
        public static final Logical OR = new Logical(LogicalOperator.OR);

        private final LogicalOperator operator;

        private Logical(LogicalOperator operator) {
            this.operator = operator;
        }

        public LogicalOperator getOperator() {
            return operator;
        }

        @Override
        public String toString() {
            return operator.toString();
        }

    }

    public enum Type {
        FILTER, FLAT, ID, SEARCH, URL;
    }

    public static class Value<E extends Enum<E> & BaseField> implements Clause {

        private final E field;
        private final ComparisonOperator operator;
        private final Object value;

        public Value(E field, Object value) {
            this(field, value, ComparisonOperator.EQUAL);
        }

        public Value(E field, Object value, ComparisonOperator operator) {
            this.field = Objects.requireNonNull(field, "Field cannot be null");
            this.operator = Objects.requireNonNull(operator, "Operator cannot be null");
            this.value = value;
        }

        public E getField() {
            return field;
        }

        public ComparisonOperator getOperator() {
            return operator;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "{" + getField() + " " + getOperator() + " " + value + "}";
        }

    }

    public static class Values<E extends Enum<E> & BaseField> implements Clause {

        private final E field;
        private final ComparisonOperator operator;
        private final Set<?> values;

        public Values(E field, List<?> values) {
            this(field, new HashSet<>(values));
        }
        
        public Values(E field, List<?> values, ComparisonOperator operator) {
            this(field, new HashSet<>(values), operator);
        }
        
        public Values(E field, Set<?> values) {
            this(field, values, ComparisonOperator.IN);
        }

        public Values(E field, Set<?> values, ComparisonOperator operator) {
            this.field = Objects.requireNonNull(field, "Field cannot be null");
            this.operator = Objects.requireNonNull(operator, "Operator cannot be null");

            Objects.requireNonNull(operator, "Values cannot be null");

            if (values.isEmpty()) {
                throw new IllegalArgumentException("Values cannot be empty");
            }

            this.values = values;
        }

        public E getField() {
            return field;
        }

        public ComparisonOperator getOperator() {
            return operator;
        }

        public Set<?> getValues() {
            return values;
        }

        @Override
        public String toString() {
            return "{" + getField() + " " + getOperator() + " " + values + "}";
        }

    }

    private final Group clause;
    private final String collection;
    private final Map<E, Object> filters;
    private final String id;
    private final int limit;
    private final Locale locale;
    private final int offset;
    private final Set<String> options;
    private final Set<E> schema;
    private final Map<E, OrderOperator> sorters;
    private final Type type;
    private final String url;

    private Summary summary;

    Query(QueryBuilder<E> builder) {
        clause = builder.clause;
        collection = builder.collection;
        filters = Optional.ofNullable(builder.filters).orElse(Collections.emptyMap());
        id = builder.id;
        limit = builder.limit;
        locale = builder.locale;
        offset = builder.offset;
        options = Optional.ofNullable(builder.options).orElse(Collections.emptySet());
        schema = Optional.ofNullable(builder.schema).orElse(Collections.emptySet());
        sorters = Optional.ofNullable(builder.sorters).orElse(Collections.emptyMap());
        url = builder.url;

        // If any changes are made here
        // they must also be ported QueryBuilder.getCurrentQueryType method
        if (clause != null) {
            type = Type.SEARCH;
        } else if (!filters.isEmpty()) {
            type = Type.FILTER;
        } else if (id != null) {
            type = Type.ID;
        } else if (url != null) {
            type = Type.URL;
        } else {
            type = Type.FLAT;
        }
    }

    public boolean containsOption(String name) {
        return options.contains(name);
    }

    public Collection<Clause> getClauses() {
        return clause != null ? clause.getClauses() : Collections.emptyList();
    }

    public String getCollection() {
        return collection;
    }

    public Map.Entry<E, Object> getFilter() {
        if (filters.isEmpty()) {
            return null;
        }

        return filters.entrySet().iterator().next();
    }

    public Map<E, Object> getFilters() {
        return filters;
    }

    public String getId() {
        return id;
    }

    public int getLimit() {
        return limit;
    }

    public Locale getLocale() {
        return locale;
    }

    public int getOffset() {
        return offset;
    }

    public Set<String> getOptions() {
        return options;
    }

    public Set<E> getSchema() {
        return schema;
    }

    public Map<E, OrderOperator> getSorters() {
        return sorters;
    }

    public Summary getSummary() {
        if (summary == null) {
            summary = new Summary(limit, offset);
        }

        return summary;
    }

    public Type getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public boolean isMaxOffset() {
        return offset == Integer.MAX_VALUE;
    }

    public Query<E> require(Type value) {
        if (type == value) {
            return this;
        }

        throw unsupported();
    }

    public void setSummary(Summary value) {
        summary = value;
    }

    protected Map<String, Object> toMap() {
        Map<String, Object> properties = new TreeMap<>();

        BiConsumer<String, Object> consumer = (key, value) -> {
            if (value == null) {
                return;
            }

            if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
                return;
            }

            properties.put(key, value);
        };

        consumer.accept("collection", getCollection());
        consumer.accept("clauses", getClauses());
        consumer.accept("filters", getFilters());
        consumer.accept("id", getId());
        consumer.accept("limit", getLimit());
        consumer.accept("locale", getLocale());
        consumer.accept("offset", getOffset());
        consumer.accept("options", getOptions());
        consumer.accept("schema", getSchema());
        consumer.accept("sorters", getSorters());
        consumer.accept("type", getType());

        return properties;
    }

    @Override
    public String toString() {
        return toMap().toString();
    }

    public UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException(toString());
    }

    public UnsupportedOperationException unsupported(String message) {
        return new UnsupportedOperationException(message.concat(": ").concat(toString()));
    }

}
