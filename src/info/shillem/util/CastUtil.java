package info.shillem.util;

import java.util.List;
import java.util.Map;
import java.util.Vector;

public enum CastUtil {
    ;
    
    @SuppressWarnings("unchecked")
    public static <T> Class<T> toAnyClass(Class<?> value) {
        return (Class<T>) value;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> List<T> toAnyList(List<?> value) {
        return (List<T>) value;
    }

    @SuppressWarnings("unchecked")
    public static <T> Vector<T> toAnyVector(Vector<?> value) {
        return (Vector<T>) value;
    }
    
    @SuppressWarnings("unchecked")
    public static <T, U> Map<T, U> toAnyMap(Map<?, ?> value) {
        return (Map<T, U>) value;
    }
    
}
