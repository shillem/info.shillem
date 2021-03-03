package info.shillem.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public interface BaseDto<E extends Enum<E> & BaseField> {

    public enum SchemaFilter {
        SET, UPDATED
    }

    void clear();

    void commit();

    void commit(Date commitDate);

    default Object computeValueIfAbsent(E key, Function<E, Object> fn) {
        Object value = getValue(key);

        if (value == null) {
            Object newValue = fn.apply(key);

            if (newValue != null) {
                setValue(key, newValue);

                return newValue;
            }
        }

        return value;
    }

    boolean containsField(E key);

    default E fieldOf(String name) {
        Class<E> cls = getFieldClass();

        for (E field : cls.getEnumConstants()) {
            if (field.name().equals(name)) {
                return field;
            }
        }

        return null;
    }

    BigDecimal getBigDecimal(E key);

    Boolean getBoolean(E key);

    String getDatabaseUrl();

    Date getDate(E key);

    Double getDouble(E key);

    Class<E> getFieldClass();

    String getId();

    Integer getInteger(E key);

    Date getLastModified();

    <T> List<T> getList(E key, Class<T> type);

    Set<E> getSchema(SchemaFilter schemaQuery);

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
