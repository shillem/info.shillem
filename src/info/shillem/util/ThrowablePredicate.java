package info.shillem.util;

import java.util.function.Predicate;

@FunctionalInterface
public interface ThrowablePredicate<T> extends Predicate<T> {
    
    @Override
    default boolean test(T t) {
        try {
            return testOrThrow(t);
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
    }

    boolean testOrThrow(T t) throws Throwable;

}
