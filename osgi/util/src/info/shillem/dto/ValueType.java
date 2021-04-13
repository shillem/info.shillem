package info.shillem.dto;

import java.io.Serializable;
import java.util.Collection;

public class ValueType implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private final Class<? extends Serializable> cls;
    private final Class<? extends Collection<?>> ccls;

    private ValueType(
            Class<? extends Serializable> cls,
            Class<? extends Collection<?>> ccls) {
        this.cls = cls;
        this.ccls = ccls;
    }

    private <S extends Serializable> ValueType(Class<S> cls) {
        this(cls, null);
    }

    public Class<? extends Collection<?>> getCollectionClass() {
        return ccls;
    }

    public Class<? extends Serializable> getValueClass() {
        return cls;
    }

    public boolean isCollection() {
        return ccls != null;
    }

    @SuppressWarnings("unchecked")
    public <T> Collection<T> newCollection() {
        try {
            return (Collection<T>) ccls.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> T newValue() {
        try {
            return (T) cls.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        if (isCollection()) {
            return String.format("%s<%s>", ccls.getName(), cls.getName());
        }

        return cls.getName();
    }

    public static <S extends Serializable> ValueType of(
            Class<? extends Serializable> cls) {
        return of(cls, null);
    }

    @SuppressWarnings("unchecked")
    public static ValueType of(
            Class<? extends Serializable> cls,
            Class<? extends Serializable> ccls) {
        return new ValueType(cls, (Class<? extends Collection<?>>) ccls);
    }

}
