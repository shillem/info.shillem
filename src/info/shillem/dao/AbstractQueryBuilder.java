package info.shillem.dao;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import info.shillem.dto.BaseField;

public abstract class AbstractQueryBuilder<T extends AbstractQueryBuilder<?, ?>, R extends Query>
        implements QueryBuilder<T, R> {

    private boolean cache;
    private boolean databaseUrl;
    private Locale locale;
    private int maxCount;
    private Set<BaseField> schema;

    @SuppressWarnings("unchecked")
    private T autocast() {
        return (T) this;
    }

    @Override
    public T fetch(BaseField... fields) {
        Objects.requireNonNull(fields, "Field cannot be null");

        if (schema == null) {
            schema = new HashSet<>();
        }

        Stream.of(fields).forEach(schema::add);

        return autocast();
    }

    @Override
    public T fetch(Set<? extends BaseField> fields) {
        Objects.requireNonNull(fields, "Fields cannot be null");

        if (schema == null) {
            schema = new HashSet<>();
        }

        schema.addAll(fields);

        return autocast();
    }

    @Override
    public T fetchCached(boolean flag) {
        this.cache = flag;

        return autocast();
    }

    @Override
    public T fetchDatabaseUrl(boolean flag) {
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
    public Set<BaseField> getSchema() {
        return schema != null ? schema : Collections.emptySet();
    }

    @Override
    public boolean isFetchCached() {
        return cache;
    }

    @Override
    public boolean isFetchDatabaseUrl() {
        return databaseUrl;
    }

    @Override
    public T setLocale(Locale locale) {
        this.locale = locale;

        return autocast();
    }
    
    @Override
    public T setMaxCount(int maxCount) {
        this.maxCount = maxCount;
        
        return autocast();
    }

}
