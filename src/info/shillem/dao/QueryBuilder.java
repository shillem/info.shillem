package info.shillem.dao;

import java.util.Locale;
import java.util.Set;

import info.shillem.dto.BaseField;

public interface QueryBuilder<T extends QueryBuilder<?, ?>, R extends Query> {

    R build();

    T fetch(BaseField... fields);

    T fetch(Set<? extends BaseField> fields);

    T fetchCached(boolean flag);

    T fetchDatabaseUrl(boolean flag);

    Locale getLocale();
    
    int getMaxCount();

    Set<? extends BaseField> getSchema();

    boolean isFetchCached();

    boolean isFetchDatabaseUrl();

    T setLocale(Locale locale);
    
    T setMaxCount(int maxCount);

}
