package info.shillem.util.xsp.model;

import info.shillem.util.SFunction;

public class WriteableDataObject<K, V> extends TypedDataObject<K, V> {

    private static final long serialVersionUID = 1L;

    public WriteableDataObject(SFunction<K, V> fn) {
        super(fn);
    }

    @Override
    public boolean isReadOnly(Object key) {
        return false;
    }

    public void removeValue(Object key) {
        values.remove(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setValue(Object key, Object value) {
        values.put((K) key, (V) value);
    }

}
