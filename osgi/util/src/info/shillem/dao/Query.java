package info.shillem.dao;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import info.shillem.dto.BaseField;

public class Query<E extends Enum<E> & BaseField> {

    private final String collection;
    private final Locale locale;
    private final Set<String> options;
    private final Set<E> schema;

    private QuerySummary summary;

    Query(QueryBuilder<E> builder) {
        collection = builder.collection;
        locale = builder.locale;
        options = builder.options;
        schema = builder.schema;
    }

    public boolean containsOption(String name) {
        return options.contains(name);
    }

    public QuerySummary createSummary() {
        if (summary == null) {
            summary = new QuerySummary();
        }

        return summary;
    }

    public String getCollection() {
        return collection;
    }

    public Locale getLocale() {
        return locale;
    }

    public Set<String> getOptions() {
        return options;
    }

    public Set<E> getSchema() {
        return schema;
    }

    public QuerySummary getSummary() {
        return summary;
    }

    protected Map<String, Object> toMap() {
        Map<String, Object> properties = new TreeMap<>();

        properties.put("collection", getCollection());
        properties.put("locale", getLocale());
        properties.put("options", getOptions());
        properties.put("schema", getSchema());
        properties.put("summary", getSummary());

        return properties;
    }

    @Override
    public String toString() {
        return toMap().toString();
    }

    public static void require(Query<?> query, Class<?> cls) {
        if (cls.isAssignableFrom(query.getClass())) {
            return;
        }

        throw unsupported(query);
    }

    public static UnsupportedOperationException unsupported(Query<?> query) {
        return new UnsupportedOperationException(query.getClass().getName() + " is not supported");
    }

}
