package info.shillem.dao;

import java.util.Locale;
import java.util.Set;

import info.shillem.dto.BaseField;

public interface QueryBuilder<T> {
    
	T addField(BaseField... fields);
	
	T addField(Set<? extends BaseField> fields);
	
	boolean getCache();
	
	Locale getLocale();
	
	Set<? extends BaseField> getSchema();
	
	T setCache(boolean flag);
	
	T setLocale(Locale locale);
	
}
