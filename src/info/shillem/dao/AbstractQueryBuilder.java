package info.shillem.dao;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import info.shillem.dto.BaseField;

public abstract class AbstractQueryBuilder<T extends AbstractQueryBuilder<?>>
        implements QueryBuilder<T> {

    private boolean cache;
    private Set<BaseField> schema;
    private Locale locale;

    @Override
    public T addField(BaseField... fields) {
        Objects.requireNonNull(fields, "Field cannot be null");
        
        if (schema == null) {
            schema = new HashSet<>();
        }
        
        Stream.of(fields).forEach(schema::add);
        
        return autocast();
    }

    @Override
    public T addField(Set<? extends BaseField> fields) {
        Objects.requireNonNull(fields, "Fields cannot be null");
        
        if (schema == null) {
            schema = new HashSet<>();
        }

        schema.addAll(fields);

        return autocast();
    }

    @SuppressWarnings("unchecked")
    private T autocast() {
        return (T) this;
    }

    @Override
    public boolean getCache() {
        return cache;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public Set<BaseField> getSchema() {
        return schema != null ? schema : Collections.emptySet();
    }

    @Override
    public T setCache(boolean flag) {
        this.cache = flag;

        return autocast();
    }

    @Override
    public T setLocale(Locale locale) {
        this.locale = locale;

        return autocast();
    }

}
