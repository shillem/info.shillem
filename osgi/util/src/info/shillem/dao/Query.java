package info.shillem.dao;

import java.util.Locale;
import java.util.Set;

import info.shillem.dto.BaseField;

public class Query<E extends Enum<E> & BaseField> {

    private final boolean databaseUrl;
    private final Locale locale;
    private final Set<E> schema;

    Query(QueryBuilder<E> builder) {
        databaseUrl = builder.databaseUrl;
        locale = builder.locale;
        schema = builder.schema;
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

    public static void isOrElseThrowException(Query<?> query, Class<?>... classes) {
        if (classes != null) {
            for (Class<?> cls : classes) {
                if (cls.isAssignableFrom(query.getClass())) {
                    return;
                }
            }
        }

        throw unsupported(query);
    }

    public static UnsupportedOperationException unsupported(Query<?> query) {
        return new UnsupportedOperationException(query.getClass().getName() + " is not supported");
    }

}
