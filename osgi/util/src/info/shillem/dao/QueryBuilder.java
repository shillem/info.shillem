package info.shillem.dao;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import info.shillem.dto.BaseField;

public class QueryBuilder<E extends Enum<E> & BaseField> {

    final Set<String> options;
    final Set<E> schema;

    String collection;
    Locale locale;

    public QueryBuilder() {
        options = new HashSet<>();
        schema = new HashSet<>();
    }

    public QueryBuilder<E> addOption(String value) {
        options.add(Objects.requireNonNull(value, "Option cannot be null"));

        return this;
    }

    public QueryBuilder<E> addOption(String value, String... values) {
        addOption(value);

        if (options != null) {
            Stream.of(values).forEach(this::addOption);
        }

        return this;
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

    public QueryBuilder<E> setCollection(String value) {
        collection = value;
        
        return this;
    }
    
    public QueryBuilder<E> setLocale(Locale value) {
        locale = value;

        return this;
    }

}
