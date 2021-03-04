package info.shillem.util.xsp.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.ibm.xsp.model.DataObject;

import info.shillem.util.xsp.context.SerializableBiFunction;

public abstract class TypedDataObject<K, V> implements DataObject, Serializable {

    private static final long serialVersionUID = 1L;

    protected final Class<V> cls;
    protected final SerializableBiFunction<K, Class<V>, V> fn;
    protected final Map<K, V> values;

    protected TypedDataObject(Class<V> cls, SerializableBiFunction<K, Class<V>, V> fn) {
        this.cls = Objects.requireNonNull(cls, "Class cannot be null");
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
        
        if (values.containsKey(key)) {
            return values.get(key);
        }

        V value = fn.apply(k, cls);

        values.put(k, value);

        return value;
    }

}
