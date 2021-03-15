package info.shillem.util.xsp.model;

import info.shillem.util.xsp.context.SerializableFunction;

public class ReadableDataObject<K, V> extends TypedDataObject<K, V> {

    private static final long serialVersionUID = 1L;

    public ReadableDataObject(SerializableFunction<K, V> fn) {
        super(fn);
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
