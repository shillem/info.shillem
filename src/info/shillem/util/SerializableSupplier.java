package info.shillem.util;

import java.io.Serializable;

public interface SerializableSupplier<T> extends Serializable, ThrowableSupplier<T> {

}
