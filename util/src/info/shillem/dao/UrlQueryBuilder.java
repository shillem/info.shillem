package info.shillem.dao;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import info.shillem.dto.BaseField;

public class UrlQueryBuilder<E extends Enum<E> & BaseField> {

    final QueryBuilder<E> base;
    
    String url;
    
    public UrlQueryBuilder(String url) {
        this.base = new QueryBuilder<>();
        this.url = Objects.requireNonNull(url, "Url cannot be null");
    }

    public UrlQuery<E> build() {
        return new UrlQuery<>(this);
    }

    public UrlQueryBuilder<E> fetch(E field) {
        base.fetch(field);
        
        return this;
    }

    public UrlQueryBuilder<E> fetch(E[] fields) {
        base.fetch(fields);
        
        return this;
    }

    public UrlQueryBuilder<E> fetch(Set<E> fields) {
        base.fetch(fields);
        
        return this;
    }

    public UrlQueryBuilder<E> fetchDatabaseUrl(boolean flag) {
        base.fetchDatabaseUrl(flag);
        
        return this;
    }

    public UrlQueryBuilder<E> setLocale(Locale locale) {
        base.setLocale(locale);
        
        return this;
    }

}
