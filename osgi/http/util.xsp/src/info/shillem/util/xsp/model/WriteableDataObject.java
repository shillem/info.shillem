package info.shillem.util.xsp.model;

import info.shillem.util.xsp.context.SerializableBiFunction;

public class WriteableDataObject<T> extends TypedDataObject<T> {

    private static final long serialVersionUID = 1L;

    public WriteableDataObject(Class<T> cls, SerializableBiFunction<Class<T>, String, T> fn) {
        super(cls, fn);
    }

    @Override
    public boolean isReadOnly(Object key) {
        return false;
    }

    public void removeValue(Object key) {
        values.remove(key.toString());
    }

    @Override
    public void setValue(Object key, Object value) {
        values.put(key.toString(), cls.cast(value));
    }

}
