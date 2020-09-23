package info.shillem.dao;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import info.shillem.dto.BaseField;

public class QueryBuilder<E extends Enum<E> & BaseField> {

    final Set<E> schema;
    
    boolean databaseUrl;
    Locale locale;
    
    public QueryBuilder() {
        schema = new HashSet<>();
    }

    public Query<E> build() {
        return new Query<>(this);
    }

    public QueryBuilder<E> fetch(E field) {
        schema.add(Objects.requireNonNull(field, "Field cannot be null"));

        return this;
    }

    public QueryBuilder<E> fetch(E[] fields) {
        Objects.requireNonNull(fields, "Fields cannot be null");
        
        Stream.of(fields).forEach(schema::add);

        return this;
    }

    public QueryBuilder<E> fetch(Set<E> fields) {
        Objects.requireNonNull(fields, "Fields cannot be null");

        schema.addAll(fields);

        return this;
    }

    public QueryBuilder<E> fetchDatabaseUrl(boolean flag) {
        this.databaseUrl = flag;

        return this;
    }
    
    public QueryBuilder<E> setLocale(Locale locale) {
        this.locale = locale;

        return this;
    }

}
