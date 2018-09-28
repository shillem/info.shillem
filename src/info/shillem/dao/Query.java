package info.shillem.dao;

import java.util.Locale;
import java.util.Set;

import info.shillem.dto.BaseField;

public class Query {

    public static class Builder extends AbstractQueryBuilder<Query.Builder> {

        public Query build() {
            return new Query(this);
        }

    }

    private final Set<? extends BaseField> schema;
    private final Locale locale;
    private final boolean cache;

    protected Query(QueryBuilder<?> builder) {
        schema = builder.getSchema();
        locale = builder.getLocale();
        cache = builder.getCache();
    }

    public boolean getCache() {
        return cache;
    }

    public Locale getLocale() {
        return locale;
    }

    public Set<? extends BaseField> getSchema() {
        return schema;
    }

}
