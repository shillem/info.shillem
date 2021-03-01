package info.shillem.util.xsp.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

import com.ibm.xsp.model.DataObject;

import info.shillem.util.xsp.context.SerializableFunction;

public class ProxyDataObject implements DataObject, Serializable {

    private static final long serialVersionUID = 1L;

    private final Function<String, Object> fn;

    public ProxyDataObject(SerializableFunction<String, Object> fn) {
        this.fn = Objects.requireNonNull(fn, "Function cannot be null");
    }

    @Override
    public Class<?> getType(Object key) {
        Object value = getValue(key);

        return value != null ? value.getClass() : null;
    }

    @Override
    public Object getValue(Object key) {
        String k = key.toString();

        return fn.apply(k);
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
