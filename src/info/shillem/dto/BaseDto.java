package info.shillem.dto;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface BaseDto {

    void clear();

    void commit();

    void commit(Date commitDate);

    boolean contains(BaseField key);

    Object get(BaseField key);

    <T> T get(BaseField key, Class<T> type);

    Boolean getBoolean(BaseField key);

    String getDatabaseUrl();

    Date getDate(BaseField key);

    Double getDouble(BaseField key);

    Set<? extends BaseField> getFields();

    String getId();

    Integer getInteger(BaseField key);

    Date getLastModified();

    <T> List<T> getList(BaseField key, Class<T> type);

    String getString(BaseField key);

    boolean isNew();

    boolean isTrue(BaseField key);

    boolean isUpdated(BaseField key);
    
    void preset(BaseField key, Object value);

    void rollback();

    void set(BaseField key, Object value);

    void setAsUpdated(BaseField key);

    void setDatabaseUrl(String databaseUrl);

    void setId(String id);

    void setLastModified(Date lastModified);
    
    void transact(BaseField key, Object value);

}
