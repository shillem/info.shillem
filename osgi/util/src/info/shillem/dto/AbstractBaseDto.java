package info.shillem.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import info.shillem.util.CastUtil;

public abstract class AbstractBaseDto<E extends Enum<E> & BaseField>
        implements BaseDto<E>, Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String databaseUrl;
    private Date lastModified;

    private final Map<E, ValueHolder> values = new HashMap<>();

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
    public boolean containsField(E key) {
        return values.containsKey(key);
    }

    @Override
    public Boolean getBoolean(E key) {
        return getValue(key, Boolean.class);
    }

    @Override
    public String getDatabaseUrl() {
        return databaseUrl;
    }

    @Override
    public Date getDate(E key) {
        return getValue(key, Date.class);
    }

    @Override
    public Double getDouble(E key) {
        return getValue(key, Double.class);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Integer getInteger(E key) {
        return getValue(key, Integer.class);
    }

    @Override
    public Date getLastModified() {
        return lastModified != null ? new Date(lastModified.getTime()) : null;
    }

    @Override
    public <T> List<T> getList(E key, Class<T> type) {
        try {
            List<?> value = (List<?>) getValue(key);

            return value != null ? CastUtil.toAnyList(value) : Collections.emptyList();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    String.format("%s value type is List<%s> and not %s",
                            key,
                            key.getProperties().getType().getName(),
                            type.getName()));
        }
    }

    @Override
    public Set<E> getSchema(SchemaFilter filter) {
        switch (Objects.requireNonNull(filter, "Schema filter cannot be null")) {
        case UPDATED:
            return values
                    .keySet()
                    .stream()
                    .filter(this::isValueUpdated)
                    .collect(Collectors.toSet());
        case SET:
            return values.keySet();
        default:
            throw new UnsupportedOperationException(filter + " is not implemented");
        }
    }

    @Override
    public String getString(E key) {
        return getValue(key, String.class);
    }

    @Override
    public Object getValue(E key) {
        ValueHolder holder = getValueHolder(key);

        return holder != null ? holder.getValue() : null;
    }

    @Override
    public <T> T getValue(E key, Class<T> type) {
        Object value = getValue(key);

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

    private ValueHolder getValueHolder(E key) {
        return values.get(Objects.requireNonNull(key, "Key cannot be null"));
    }

    @Override
    public boolean isNew() {
        return id == null;
    }

    @Override
    public boolean isValueTrue(E key) {
        Boolean flag = getBoolean(key);

        return flag != null && flag;
    }

    @Override
    public boolean isValueUpdated(E key) {
        ValueHolder holder = getValueHolder(key);

        return holder != null && holder.isUpdated();
    }

    @Override
    public void presetValue(E key, Object value) {
        ValueHolder holder = getValueHolder(key);

        if (holder != null) {
            throw new IllegalStateException(key + " value is already set");
        }

        values.put(key, ValueHolder.newSavedValue(value, key.getProperties().getFullType()));
    }

    @Override
    public void rollback() {
        values.values().forEach(ValueHolder::rollback);
    }

    @Override
    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified != null ? new Date(lastModified.getTime()) : null;
    }

    @Override
    public void setValue(E key, Object value) {
        ValueHolder holder = getValueHolder(key);
        Class<?> type = key.getProperties().getFullType();

        if (holder != null) {
            holder.updateValue(value, type);
        } else {
            values.put(key, ValueHolder.newValue(value, type));
        }
    }

    @Override
    public void setValueAsUpdated(E key) {
        ValueHolder holder = getValueHolder(key);

        if (holder != null) {
            holder.setAsUpdated();
        }
    }

    @Override
    public void transactValue(E key, Object value) {
        ValueHolder holder = getValueHolder(key);
        Class<?> type = key.getProperties().getFullType();

        if (holder != null) {
            holder.transactValue(value, type);
        } else {
            values.put(key, ValueHolder.newTransactionValue(value, type));
        }
    }

}
