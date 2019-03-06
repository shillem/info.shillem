package info.shillem.util;

import java.util.function.Supplier;

@FunctionalInterface
public interface TSupplier<T> extends Supplier<T> {

    @Override
    default T get() {
        try {
            return getOrThrow();
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
    }

    T getOrThrow() throws Throwable;

}

