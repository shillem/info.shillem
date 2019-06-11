package info.shillem.dao;

import java.util.Locale;
import java.util.Set;

import info.shillem.dto.BaseField;

public class Query<E extends Enum<E> & BaseField> {

    public static class Builder<E extends Enum<E> & BaseField>
            extends QueryBuilder<E, Builder<E>> {

        public Query<E> build() {
            return new Query<>(this);
        }

    }

    private final boolean databaseUrl;
    private final Locale locale;
    private final Set<E> schema;

    protected Query(QueryBuilder<E, ?> builder) {
        databaseUrl = builder.isFetchDatabaseUrl();
        locale = builder.getLocale();
        schema = builder.getSchema();
    }

    public Locale getLocale() {
        return locale;
    }

    public Set<E> getSchema() {
        return schema;
    }

    public boolean isFetchDatabaseUrl() {
        return databaseUrl;
    }

}
