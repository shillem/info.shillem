package info.shillem.dao;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import info.shillem.dto.BaseField;

public class IdQueryBuilder<E extends Enum<E> & BaseField> {

    final QueryBuilder<E> base;
    
    String id;
    
    public IdQueryBuilder(String id) {
        this.base = new QueryBuilder<>();
        this.id = Objects.requireNonNull(id, "Id cannot be null");
    }

    public IdQuery<E> build() {
        return new IdQuery<>(this);
    }

    public IdQueryBuilder<E> fetch(E field) {
        base.fetch(field);
        
        return this;
    }

    public IdQueryBuilder<E> fetch(E[] fields) {
        base.fetch(fields);
        
        return this;
    }

    public IdQueryBuilder<E> fetch(Set<E> fields) {
        base.fetch(fields);
        
        return this;
    }

    public IdQueryBuilder<E> fetchDatabaseUrl(boolean flag) {
        base.fetchDatabaseUrl(flag);
        
        return this;
    }

    public IdQueryBuilder<E> setLocale(Locale locale) {
        base.setLocale(locale);
        
        return this;
    }

}
