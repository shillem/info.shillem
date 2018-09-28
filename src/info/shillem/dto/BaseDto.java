package info.shillem.dto;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface BaseDto {

	void clear();

	void commit();

	void commit(Date commitDate);

	boolean containsChange(BaseField key);

	boolean containsField(BaseField key);

	Object get(BaseField key);
	
	<T> T get(BaseField key, Class<T> type);

	Boolean getBoolean(BaseField key);

	Set<? extends BaseField> getChanges();
	
	String getDatabaseUrl();

	Date getDate(BaseField key);

	Double getDouble(BaseField key);

	Set<? extends BaseField> getFields();

	String getId();

	Integer getInteger(BaseField key);

	Date getLastModified();

	<T> List<T> getList(BaseField key, Class<T> type);

	String getString(BaseField key);
	
	boolean is(BaseField key);

	boolean isNewRecord();

	void rollback();

	void set(BaseField key, Object value);

	void set(BaseField key, Object value, ValueOperation operation);

	void setId(String id);

	void setLastModified(Date lastModified);
	
	void setDatabaseUrl(String databaseUrl);

	void trackChange(BaseField key);

}
