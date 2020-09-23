package info.shillem.util;

import java.util.Collection;
import java.util.Vector;

public enum CollectionUtil {
    ;

    @SuppressWarnings("unchecked")
    public static <T> Vector<T> asVector(T value) {
        if (value instanceof Vector) {
            return (Vector<T>) value;
        }

        if (value instanceof Collection) {
            return new Vector<>((Collection<T>) value);
        }

        Vector<T> values = new Vector<T>();

        values.add(value);

        return values;
    }

}
