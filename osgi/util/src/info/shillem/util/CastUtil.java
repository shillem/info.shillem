package info.shillem.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

@SuppressWarnings("unchecked")
public class CastUtil {

    private CastUtil() {
        throw new UnsupportedOperationException();
    }

    public static <T> Class<T> toAnyClass(Class<?> value) {
        return (Class<T>) value;
    }

    public static <T> Collection<T> toAnyCollection(Collection<?> value) {
        return (Collection<T>) value;
    }

    public static <T> List<T> toAnyList(List<?> value) {
        return (List<T>) value;
    }

    public static <T, U> Map<T, U> toAnyMap(Map<?, ?> value) {
        return (Map<T, U>) value;
    }

    public static <T> Set<T> toAnySet(Set<?> value) {
        return (Set<T>) value;
    }

    public static <T> Vector<T> toAnyVector(Vector<?> value) {
        return (Vector<T>) value;
    }

}
