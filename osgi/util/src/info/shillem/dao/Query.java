package info.shillem.dao;

import java.util.Locale;
import java.util.Set;

import info.shillem.dto.BaseField;

public class Query<E extends Enum<E> & BaseField> {

    private final Locale locale;
    private final Set<Enum<? extends QueryOption>> options;
    private final Set<E> schema;

    private QuerySummary summary;

    Query(QueryBuilder<E> builder) {
        locale = builder.locale;
        options = builder.options;
        schema = builder.schema;
    }

    public boolean containsOption(Enum<? extends QueryOption> value) {
        return options.contains(value);
    }

    public QuerySummary createSummary() {
        if (summary == null) {
            summary = new QuerySummary();
        }

        return summary;
    }

    public Locale getLocale() {
        return locale;
    }

    public Set<Enum<? extends QueryOption>> getOptions() {
        return options;
    }

    public Set<E> getSchema() {
        return schema;
    }

    public QuerySummary getSummary() {
        return summary;
    }

    public static void require(Query<?> query, Class<?>... classes) {
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
