package info.shillem.dao;

import java.util.Locale;
import java.util.Set;

import info.shillem.dto.BaseField;

public class Query<E extends Enum<E> & BaseField> {

    public static class Builder<E extends Enum<E> & BaseField>
            extends AbstractQueryBuilder<E, Builder<E>, Query<E>> {

        @Override
        public Query<E> build() {
            return new Query<>(this);
        }

    }

    private final boolean cached;
    private final boolean databaseUrl;
    private final Locale locale;
    private final int maxCount;
    private final Set<E> schema;

    protected Query(QueryBuilder<E, ?, ?> builder) {
        cached = builder.isFetchCached();
        databaseUrl = builder.isFetchDatabaseUrl();
        locale = builder.getLocale();
        maxCount = builder.getMaxCount();
        schema = builder.getSchema();
    }

    public Locale getLocale() {
        return locale;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public Set<E> getSchema() {
        return schema;
    }

    public boolean isFetchCached() {
        return cached;
    }

    public boolean isFetchDatabaseUrl() {
        return databaseUrl;
    }

}
