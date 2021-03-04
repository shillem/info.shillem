package info.shillem.util.xsp.model;

import info.shillem.util.xsp.context.SerializableBiFunction;

public class WriteableDataObject<K, V> extends TypedDataObject<K, V> {

    private static final long serialVersionUID = 1L;

    public WriteableDataObject(Class<V> cls, SerializableBiFunction<K, Class<V>, V> fn) {
        super(cls, fn);
    }

    @Override
    public boolean isReadOnly(Object key) {
        return false;
    }

    public void removeValue(Object key) {
        values.remove(key);
    }

    @Override
    public void setValue(Object key, Object value) {
        @SuppressWarnings("unchecked")
        K k = (K) key;
        
        values.put(k, cls.cast(value));
    }

}
