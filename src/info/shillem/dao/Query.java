package info.shillem.dao;

import java.util.Locale;
import java.util.Set;

import info.shillem.dto.BaseField;

public class Query {

    public static class Builder extends AbstractQueryBuilder<Query.Builder, Query> {

        @Override
        public Query build() {
            return new Query(this);
        }

    }

    private final boolean cached;
    private final boolean databaseUrl;
    private final Locale locale;
    private final int maxCount;
    private final Set<? extends BaseField> schema;

    protected Query(QueryBuilder<?, ?> builder) {
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

    public Set<? extends BaseField> getSchema() {
        return schema;
    }

    public boolean isFetchCached() {
        return cached;
    }
    
    public boolean isFetchDatabaseUrl() {
        return databaseUrl;
    }

}
