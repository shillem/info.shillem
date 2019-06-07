package info.shillem.dao;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import info.shillem.dto.BaseField;

public abstract class AbstractQueryBuilder<E extends Enum<E> & BaseField, B extends QueryBuilder<E, B, T>, T extends Query<E>>
        implements QueryBuilder<E, B, T> {

    private boolean cached;
    private boolean databaseUrl;
    private Locale locale;
    private int maxCount;
    private Set<E> schema;

    @SuppressWarnings("unchecked")
    private B autocast() {
        return (B) this;
    }

    @Override
    public B fetch(E field) {
        Objects.requireNonNull(field, "Field cannot be null");

        if (schema == null) {
            schema = new HashSet<>();
        }
        
        schema.add(field);

        return autocast();
    }

    @Override
    public B fetch(E[] fields) {
        Objects.requireNonNull(fields, "Fields cannot be null");

        if (schema == null) {
            schema = new HashSet<>();
        }

        Stream.of(fields).forEach(schema::add);

        return autocast();
    }

    @Override
    public B fetch(Set<E> fields) {
        Objects.requireNonNull(fields, "Fields cannot be null");

        if (schema == null) {
            schema = new HashSet<>();
        }

        schema.addAll(fields);

        return autocast();
    }

    @Override
    public B fetchDatabaseUrl(boolean flag) {
        this.databaseUrl = flag;

        return autocast();
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public int getMaxCount() {
        return maxCount;
    }

    @Override
    public Set<E> getSchema() {
        return schema != null ? schema : Collections.emptySet();
    }

    @Override
    public boolean isCached() {
        return cached;
    }

    @Override
    public boolean isFetchDatabaseUrl() {
        return databaseUrl;
    }

    @Override
    public B setCache(boolean flag) {
        this.cached = flag;

        return autocast();
    }

    @Override
    public B setLocale(Locale locale) {
        this.locale = locale;

        return autocast();
    }

    @Override
    public B setMaxCount(int maxCount) {
        this.maxCount = maxCount;

        return autocast();
    }

}
