package info.shillem.dto;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface BaseDto<E extends Enum<E> & BaseField> {

    void clear();

    void commit();

    void commit(Date commitDate);

    boolean containsField(E key);

    Boolean getBoolean(E key);

    String getDatabaseUrl();

    Date getDate(E key);

    Double getDouble(E key);
    
    E getField(String name);

    Set<E> getFields();

    String getId();

    Integer getInteger(E key);

    Date getLastModified();

    <T> List<T> getList(E key, Class<T> type);

    String getString(E key);

    Object getValue(E key);

    <T> T getValue(E key, Class<T> type);

    boolean isNew();

    boolean isValueTrue(E key);

    boolean isValueUpdated(E key);
    
    void presetValue(E key, Object value);

    void rollback();

    void setDatabaseUrl(String databaseUrl);

    void setId(String id);

    void setLastModified(Date lastModified);

    void setValue(E key, Object value);

    void setValueAsUpdated(E key);
    
    void transactValue(E key, Object value);

}
