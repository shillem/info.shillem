package info.shillem.util;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface ThrowableBiConsumer<T, U> extends BiConsumer<T, U> {

    @Override
    default void accept(final T t, final U u) {
        try {
            acceptOrThrow(t, u);
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
    }

    void acceptOrThrow(T t, U u) throws Throwable;

}
