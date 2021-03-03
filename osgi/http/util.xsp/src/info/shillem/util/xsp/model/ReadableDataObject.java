package info.shillem.util.xsp.model;

import info.shillem.util.xsp.context.SerializableBiFunction;

public class ReadableDataObject<T> extends TypedDataObject<T> {

    private static final long serialVersionUID = 1L;

    public ReadableDataObject(Class<T> cls, SerializableBiFunction<Class<T>, String, T> fn) {
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
