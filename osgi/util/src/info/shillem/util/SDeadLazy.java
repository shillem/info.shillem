package info.shillem.util;

import java.io.Serializable;

public class SDeadLazy<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final T value;

    public SDeadLazy(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

}
