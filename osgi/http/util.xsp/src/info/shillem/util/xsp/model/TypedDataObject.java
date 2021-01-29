package info.shillem.util.xsp.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import com.ibm.xsp.model.DataObject;

import info.shillem.util.xsp.context.SerializableBiFunction;

public class TypedDataObject<T> implements DataObject, Serializable {

    private static final long serialVersionUID = 1L;

    private final Class<T> cls;
    private final BiFunction<Class<T>, String, T> fn;
    private final Map<String, T> values;

    public TypedDataObject(Class<T> cls, SerializableBiFunction<Class<T>, String, T> fn) {
        this.cls = Objects.requireNonNull(cls, "Class cannot be null");
        this.fn = Objects.requireNonNull(fn, "Function cannot be null");
        this.values = new HashMap<>();
    }

    public boolean containsValue(Object key) {
        return values.containsKey(Objects.requireNonNull(key).toString());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getType(Object key) {
        Object value = getValue(Objects.requireNonNull(key));

        return value != null ? (Class<T>) value.getClass() : null;
    }

    @Override
    public T getValue(Object key) {
        return values.computeIfAbsent(key.toString(), (k) -> fn.apply(cls, k));
    }

    @Override
    public boolean isReadOnly(Object key) {
        return true;
    }

    void removeValue(Object key) {
        values.remove(key);
    }

    @Override
    public void setValue(Object key, Object value) {
        values.put((String) key, cls.cast(value));
    }

}
