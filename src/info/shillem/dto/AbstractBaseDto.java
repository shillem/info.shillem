package info.shillem.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractBaseDto implements BaseDto, Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private long lastModified;
	private boolean isNewRecord;

	private final Map<BaseField, Object> values = new HashMap<BaseField, Object>();
	private final Map<BaseField, ValueHolder> changes = new HashMap<BaseField, ValueHolder>();

	@Override
	public void clear() {
		id = null;
		lastModified = 0;
		values.clear();
		changes.clear();
	}

	@Override
	public void commit() {
		commit(null);
	}

	@Override
	public void commit(Date commitDate) {
		if (id == null) {
			throw new NullPointerException("Invalid id");
		}

		for (Iterator<Map.Entry<BaseField, ValueHolder>> iter = changes.entrySet().iterator(); iter
				.hasNext();) {
			Map.Entry<BaseField, ValueHolder> entry = iter.next();

			values.put(entry.getKey(), entry.getValue().getValue());

			iter.remove();
		}

		isNewRecord = false;

		setLastModified(commitDate);
	}

	@Override
	public boolean containsChange(BaseField key) {
		return changes.containsKey(key);
	}

	@Override
	public boolean containsField(BaseField key) {
		return values.containsKey(key) || changes.containsKey(key);
	}

	@Override
	public Object get(BaseField key) {
		return changes.containsKey(key) ? changes.get(key).getValue() : values.get(key);
	}

	@Override
	public <T> T get(BaseField key, Class<T> type) {
		Object value = get(key);

		if (type != key.getProperties().getType()) {
			throw invalidTypeException(key, type);
		}

		return type.cast(value);
	}

	@Override
	public Boolean getBoolean(BaseField key) {
		return get(key, Boolean.class);
	}

	@Override
	public Set<? extends BaseField> getChanges() {
		return changes.keySet();
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
		Set<BaseField> allFields = new HashSet<>();

		allFields.addAll(values.keySet());
		allFields.addAll(changes.keySet());

		return allFields;
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
		if (lastModified > 0) {
			return new Date(lastModified);
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getList(BaseField key, Class<T> listType) {
		if (!key.getProperties().isList()) {
			throw new IllegalArgumentException(String.format("%s is not a list type value", key));
		}

		if (listType != key.getProperties().getType()) {
			throw invalidTypeException(key, listType);
		}

		Object value = get(key);

		if (value != null) {
			return (List<T>) value;
		}

		return Collections.<T>emptyList();
	}

	@Override
	public final String getString(BaseField key) {
		return get(key, String.class);
	}

	private ClassCastException invalidTypeException(BaseField field, Class<?> c) {
		return new ClassCastException(String.format("%s value type is %s and not %s", field, field
				.getProperties().getType(), c));
	}

	@Override
	public final boolean isNewRecord() {
		return id == null || isNewRecord;
	}

	@Override
	public void rollback() {
		if (isNewRecord()) {
			id = null;
		}

		for (Iterator<Map.Entry<BaseField, ValueHolder>> iter = changes.entrySet().iterator(); iter
				.hasNext();) {
			Map.Entry<BaseField, ValueHolder> entry = iter.next();

			if (entry.getValue().getOperation() == ValueOperation.TRANSACTION_VALUE) {
				iter.remove();
			}
		}
	}

	@Override
	public final void set(BaseField key, Object value) {
		set(key, value, ValueOperation.UPDATE_VALUE);
	}

	@Override
	public final void set(BaseField key, Object value, ValueOperation operation) {
		Object newValue = null;
		boolean newChange = false;

		if (!values.containsKey(key) && value == null) {
			newChange = true;
		} else {
			newChange = !changes.containsKey(key);
			Object storedValue = newChange ? values.get(key) : changes.get(key).getValue();

			if (value == storedValue) {
				return;
			} else if (key.getProperties().isList()) {
				if (value != null) {
					if (!(value instanceof List)) {
						throw invalidTypeException(key, value.getClass());
					}

					List<?> storedValues = (List<?>) storedValue;
					List<?> newValues = (List<?>) value;

					if (storedValue != null && storedValues.size() == newValues.size()) {
						for (int i = 0; i < newValues.size(); i++) {
							Object o = newValues.get(i);

							if (o.getClass() != key.getProperties().getType()) {
								throw invalidTypeException(key, o.getClass());
							}

							if (newValues.get(i).equals(storedValues.get(i))) {
								newValue = newValues;

								break;
							}
						}
					} else {
						newValue = newValues;
					}
				}
			} else {
				newValue = value;

				if (newValue != null) {
					if (newValue.getClass() != key.getProperties().getType()) {
						throw invalidTypeException(key, newValue.getClass());
					}

					if (newValue.equals(storedValue)) {
						return;
					}
				}
			}
		}

		switch (operation) {
		case DEFAULT_VALUE:
			values.put(key, newValue);
			break;
		case UPDATE_VALUE:
		case TRANSACTION_VALUE:
			if (newChange) {
				changes.put(key, new ValueHolder(newValue, operation));
			} else {
				changes.get(key).updateValue(newValue, operation);
			}

			break;
		}
	}

	@Override
	public final void setId(String id) {
		this.id = id;
	}

	@Override
	public final void setLastModified(Date lastModified) {
		if (lastModified != null) {
			this.lastModified = lastModified.getTime();
		} else {
			this.lastModified = 0;
		}
	}

	@Override
	public void trackChange(BaseField key) {
		if (!changes.containsKey(key)) {
			changes.put(key, new ValueHolder(values.get(key), ValueOperation.UPDATE_VALUE));
		}
	}

}
