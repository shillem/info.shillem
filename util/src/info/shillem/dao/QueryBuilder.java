package info.shillem.dao;

import java.util.Locale;
import java.util.Set;

import info.shillem.dto.BaseField;

public interface QueryBuilder<E extends Enum<E> & BaseField, B extends QueryBuilder<E, B, T>, T extends Query<E>> {

    T build();

    B fetch(E field);
    
    B fetch(E[] field);

    B fetch(Set<E> fields);

    B setCache(boolean flag);

    B fetchDatabaseUrl(boolean flag);

    Locale getLocale();

    int getMaxCount();

    Set<E> getSchema();

    boolean isCached();

    boolean isFetchDatabaseUrl();

    B setLocale(Locale locale);

    B setMaxCount(int maxCount);

}
