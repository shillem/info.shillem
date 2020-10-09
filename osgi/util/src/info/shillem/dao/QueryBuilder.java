package info.shillem.dao;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import info.shillem.dto.BaseField;

public class QueryBuilder<E extends Enum<E> & BaseField> {

    final Set<Enum<? extends QueryOption>> options;
    final Set<E> schema;

    Locale locale;

    public QueryBuilder() {
        options = new HashSet<>();
        schema = new HashSet<>();
    }

    public QueryBuilder<E> addOption(Enum<? extends QueryOption> value) {
        options.add(Objects.requireNonNull(value, "Option cannot be null"));

        return this;
    }

    public QueryBuilder<E> addOption(
            Enum<? extends QueryOption> value,
            @SuppressWarnings("unchecked") Enum<? extends QueryOption>... values) {
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

    public QueryBuilder<E> setLocale(Locale locale) {
        this.locale = locale;

        return this;
    }

}
