package info.shillem.dao;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import info.shillem.dao.Query.Clause;
import info.shillem.dao.Query.Group;
import info.shillem.dao.Query.Logical;
import info.shillem.dao.Query.Type;
import info.shillem.dto.BaseField;
import info.shillem.util.OrderOperator;

public class QueryBuilder<E extends Enum<E> & BaseField> {

    Group clause;
    String collection;
    Map<E, Object> filters;
    String id;
    Locale locale;
    int limit;
    int offset;
    Set<String> options;
    Set<E> schema;
    Map<E, OrderOperator> sorters;
    String url;

    public QueryBuilder<E> addClause(Logical logical, Clause piece) {
        Objects.requireNonNull(logical, "Logical cannot be null");
        Objects.requireNonNull(piece, "Clause cannot be null");
        
        if (clause == null) {
            clause = new Group();
        }
        
        clause.add(logical, piece);

        return this;
    }

    public QueryBuilder<E> addOption(String value) {
        if (options == null) {
            options = new HashSet<>();
        }
        
        options.add(Objects.requireNonNull(value, "Option cannot be null"));

        return this;
    }

    public QueryBuilder<E> addOption(String value, String... values) {
        addOption(value);

        if (values != null) {
            Stream.of(values).forEach(options::add);
        }

        return this;
    }

    public QueryBuilder<E> andClause(Clause piece) {
        return addClause(Logical.AND, piece);
    }

    public Query<E> build() {
        return new Query<>(this);
    }

    public QueryBuilder<E> fetch(E field) {
        Objects.requireNonNull(field, "Field cannot be null");
        
        if (schema == null) {
            schema = new HashSet<>();
        }
        
        schema.add(field);

        return this;
    }

    public QueryBuilder<E> fetch(E[] fields) {
        Objects.requireNonNull(fields, "Fields cannot be null");
        
        if (schema == null) {
            schema = new HashSet<>();
        }

        Stream.of(fields).forEach(schema::add);

        return this;
    }

    public QueryBuilder<E> fetch(Set<E> fields) {
        Objects.requireNonNull(fields, "Fields cannot be null");
        
        if (schema == null) {
            schema = new HashSet<>();
        }

        schema.addAll(fields);

        return this;
    }

    public QueryBuilder<E> filter(E field, Object value) {
        Objects.requireNonNull(field, "Field cannot be null");
        Objects.requireNonNull(value, "Value cannot be null");
        
        if (filters == null) {
            filters = new LinkedHashMap<>();
        }
        
        filters.put(field, value);

        return this;
    }

    // If any changes are made here
    // they must also be ported Query constructor logic
    public Query.Type getCurrentQueryType() {
        if (clause != null) {
            return Type.SEARCH;
        }
        
        if (!filters.isEmpty()) {
            return Type.FILTER;
        }
        
        if (id != null) {
            return Type.ID;
        }
        
        if (url != null) {
            return Type.URL;
        }
        
        return Type.FLAT;
    }
    
    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }
    
    public QueryBuilder<E> orClause(Clause piece) {
        return addClause(Logical.OR, piece);
    }

    public QueryBuilder<E> setCollection(String value) {
        collection = value;
        
        return this;
    }

    public QueryBuilder<E> setId(String value) {
        id = value;
        
        return this;
    }
    
    public QueryBuilder<E> setLimit(int value) {
        limit = value;

        return this;
    }
    
    public QueryBuilder<E> setLocale(Locale value) {
        locale = value;

        return this;
    }

    public QueryBuilder<E> setOffset(int value) {
        offset = value;

        return this;
    }

    public QueryBuilder<E> setUrl(String value) {
        url = value;
        
        return this;
    }

    public QueryBuilder<E> sort(E field, OrderOperator order) {
        Objects.requireNonNull(field, "Field cannot be null");
        Objects.requireNonNull(order, "Order cannot be null");
        
        if (sorters == null) {
            sorters = new LinkedHashMap<>();
        }
        
        sorters.put(field, order);
        
        return this;
    }

}
