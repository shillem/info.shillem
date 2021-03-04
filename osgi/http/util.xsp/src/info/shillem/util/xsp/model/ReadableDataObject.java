package info.shillem.util.xsp.model;

import info.shillem.util.xsp.context.SerializableBiFunction;

public class ReadableDataObject<K, V> extends TypedDataObject<K, V> {

    private static final long serialVersionUID = 1L;

    public ReadableDataObject(Class<V> cls, SerializableBiFunction<K, Class<V>, V> fn) {
        super(cls, fn);
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
