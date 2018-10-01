package info.shillem.dao;

import java.util.Locale;
import java.util.Set;

import info.shillem.dto.BaseField;

public interface QueryBuilder<T> {
    
	T fetch(BaseField... fields);
	
	T fetch(Set<? extends BaseField> fields);
	
	T fetchCached(boolean flag);
	
	T fetchDatabaseUrl(boolean flag);
	
	Locale getLocale();
	
	Set<? extends BaseField> getSchema();
    
    boolean isFetchCached();
    
    boolean isFetchDatabaseUrl();
	
	T setLocale(Locale locale);
	
}
