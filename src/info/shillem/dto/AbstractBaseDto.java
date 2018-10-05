package info.shillem.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractBaseDto implements BaseDto, Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String databaseUrl;
    private Date lastModified;

    private final Map<BaseField, ValueHolder> values = new HashMap<>();

    @Override
    public void clear() {
        id = null;
        lastModified = null;
        values.clear();
    }

    @Override
    public void commit() {
        commit(null);
    }

    @Override
    public void commit(Date commitDate) {
        if (id == null) {
            throw new IllegalStateException("Cannot commit on null id");
        }

        values.values().forEach(ValueHolder::commit);
        setLastModified(commitDate);
    }

    @Override
    public boolean contains(BaseField key) {
        return values.containsKey(key);
    }

    @Override
    public Object get(BaseField key) {
        ValueHolder valueHolder = values.get(key);

        return valueHolder != null ? valueHolder.getValue() : null;
    }

    @Override
    public <T> T get(BaseField key, Class<T> type) {
        Object value = get(key);

        try {
            return type.cast(value);
        } catch (ClassCastException e) {
            if (key.getProperties().isList()) {
                throw new IllegalArgumentException(
                        String.format("%s value type is List<%s> and not %s",
                                key,
                                key.getProperties().getType().getName(),
                                type.getName()));
            } else {
                throw new IllegalArgumentException(
                        String.format("%s value type is %s and not %s",
                                key,
                                key.getProperties().getType().getName(),
                                type.getName()));
            }
        }
    }

    @Override
    public Boolean getBoolean(BaseField key) {
        return get(key, Boolean.class);
    }

    @Override
    public String getDatabaseUrl() {
        return databaseUrl;
    }

    @Override
    public final Date getDate(BaseField key) {
        return get(key, Date.class);
    }

    @Override
    public final Double getDouble(BaseField key) {
        return get(key, Double.class);
    }

    @Override
    public Set<? extends BaseField> getFields() {
        return values.keySet();
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final Integer getInteger(BaseField key) {
        return get(key, Integer.class);
    }

    @Override
    public final Date getLastModified() {
        return lastModified != null ? new Date(lastModified.getTime()) : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> getList(BaseField key, Class<T> type) {
        Object value = get(key);

        try {
            return value != null ? (List<T>) value : Collections.emptyList();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    String.format("%s value type is List<%s> and not %s",
                            key,
                            key.getProperties().getType().getName(),
                            type.getName()));
        }
    }

    @Override
    public final String getString(BaseField key) {
        return get(key, String.class);
    }

    @Override
    public final boolean isNew() {
        return id == null;
    }

    @Override
    public boolean isTrue(BaseField key) {
        Boolean flag = getBoolean(key);

        return flag != null && flag;
    }

    @Override
    public boolean isUpdated(BaseField key) {
        ValueHolder valueHolder = values.get(key);

        return valueHolder != null && valueHolder.isUpdated();
    }

    @Override
    public void preset(BaseField key, Object value) {
        ValueHolder valueHolder = values.get(key);

        if (valueHolder != null) {
            throw new IllegalStateException(key + " value is already set");
        }

        values.put(key, ValueHolder.newSavedValue(value, key.getProperties().getFullType()));
    }

    @Override
    public void rollback() {
        values.values().forEach(ValueHolder::rollback);
    }

    @Override
    public final void set(BaseField key, Object value) {
        ValueHolder valueHolder = values.get(key);
        Class<?> type = key.getProperties().getFullType();

        if (valueHolder != null) {
            valueHolder.updateValue(value, type);
        } else {
            values.put(key, ValueHolder.newValue(value, type));
        }
    }

    @Override
    public void setAsUpdated(BaseField key) {
        ValueHolder valueHolder = values.get(key);

        if (valueHolder != null) {
            valueHolder.setAsUpdated();
        }
    }

    @Override
    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    @Override
    public final void setId(String id) {
        this.id = id;
    }

    @Override
    public final void setLastModified(Date lastModified) {
        this.lastModified = lastModified != null ? new Date(lastModified.getTime()) : null;
    }

    @Override
    public void transact(BaseField key, Object value) {
        ValueHolder valueHolder = values.get(key);
        Class<?> type = key.getProperties().getFullType();

        if (valueHolder != null) {
            valueHolder.transactValue(value, type);
        } else {
            values.put(key, ValueHolder.newTransactionValue(value, type));
        }
    }

}
