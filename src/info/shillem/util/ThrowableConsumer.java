package info.shillem.util;

import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowableConsumer<T> extends Consumer<T> {

    @Override
    default void accept(final T t) {
        try {
            acceptOrThrow(t);
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
    }

    void acceptOrThrow(T t) throws Throwable;

}
