package info.shillem.util.xsp.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.ibm.xsp.model.DataObject;

import info.shillem.util.xsp.context.SerializableBiFunction;

public abstract class TypedDataObject<T> implements DataObject, Serializable {

    private static final long serialVersionUID = 1L;

    protected final Class<T> cls;
    protected final SerializableBiFunction<Class<T>, String, T> fn;
    protected final Map<String, T> values;

    protected TypedDataObject(Class<T> cls, SerializableBiFunction<Class<T>, String, T> fn) {
        this.cls = Objects.requireNonNull(cls, "Class cannot be null");
        this.fn = Objects.requireNonNull(fn, "Function cannot be null");
        this.values = new HashMap<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getType(Object key) {
        Object value = getValue(key);

        return value != null ? (Class<T>) value.getClass() : null;
    }

    @Override
    public T getValue(Object key) {
        String k = key.toString();

        if (values.containsKey(k)) {
            return values.get(k);
        }

        T value = fn.apply(cls, k);

        values.put(k, value);

        return value;
    }

}
