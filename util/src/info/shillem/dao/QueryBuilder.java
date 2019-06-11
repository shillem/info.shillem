package info.shillem.dao;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import info.shillem.dto.BaseField;

abstract class QueryBuilder<E extends Enum<E> & BaseField, T extends QueryBuilder<E, T>> {

    private boolean databaseUrl;
    private Locale locale;
    private Set<E> schema;

    @SuppressWarnings("unchecked")
    private T autocast() {
        return (T) this;
    }

    public T fetch(E field) {
        Objects.requireNonNull(field, "Field cannot be null");

        if (schema == null) {
            schema = new HashSet<>();
        }

        schema.add(field);

        return autocast();
    }

    public T fetch(E[] fields) {
        Objects.requireNonNull(fields, "Fields cannot be null");

        if (schema == null) {
            schema = new HashSet<>();
        }

        Stream.of(fields).forEach(schema::add);

        return autocast();
    }

    public T fetch(Set<E> fields) {
        Objects.requireNonNull(fields, "Fields cannot be null");

        if (schema == null) {
            schema = new HashSet<>();
        }

        schema.addAll(fields);

        return autocast();
    }

    public T fetchDatabaseUrl(boolean flag) {
        this.databaseUrl = flag;

        return autocast();
    }

    public Locale getLocale() {
        return locale;
    }

    public Set<E> getSchema() {
        return schema != null ? schema : Collections.emptySet();
    }

    public boolean isFetchDatabaseUrl() {
        return databaseUrl;
    }

    public T setLocale(Locale locale) {
        this.locale = locale;

        return autocast();
    }

}
