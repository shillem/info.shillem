package info.shillem.util.xsp.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.ibm.xsp.model.DataObject;

import info.shillem.util.xsp.context.SerializableFunction;

public abstract class TypedDataObject<K, V> implements DataObject, Serializable {

    private static final long serialVersionUID = 1L;

    protected final SerializableFunction<K, V> fn;
    protected final Map<K, V> values;

    protected TypedDataObject(SerializableFunction<K, V> fn) {
        this.fn = Objects.requireNonNull(fn, "Function cannot be null");
        this.values = new HashMap<>();
    }

    @Override
    public Class<V> getType(Object key) {
        V value = getValue(key);

        @SuppressWarnings("unchecked")
        Class<V> valueClass = (Class<V>) (value != null ? value.getClass() : null);

        return valueClass;
    }

    @Override
    public V getValue(Object key) {
        @SuppressWarnings("unchecked")
        K k = (K) key;

        if (values.containsKey(k)) {
            return values.get(k);
        }

        V value = fn.apply(k);

        values.put(k, value);

        return value;
    }

}
