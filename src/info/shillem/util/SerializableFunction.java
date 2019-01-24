package info.shillem.util;

import java.io.Serializable;

public interface SerializableFunction<T, R> extends Serializable, ThrowableFunction<T, R> {

}
