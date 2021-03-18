package info.shillem.util.xsp.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

import com.ibm.xsp.model.DataObject;

import info.shillem.util.xsp.context.SFunction;

public class ProxyDataObject<K, V> implements DataObject, Serializable {

    private static final long serialVersionUID = 1L;

    private final Function<K, V> fn;

    public ProxyDataObject(SFunction<K, V> fn) {
        this.fn = Objects.requireNonNull(fn, "Function cannot be null");
    }

    @Override
    public Class<V> getType(Object key) {
        Object value = getValue(key);

        @SuppressWarnings("unchecked")
        Class<V> valueClass = (Class<V>) (value != null ? value.getClass() : null);

        return valueClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V getValue(Object key) {        
        return fn.apply((K) key);
    }

    @Override
    public boolean isReadOnly(Object key) {
        return false;
    }

    @Override
    public void setValue(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

}
