package info.shillem.util;

import java.util.function.Function;

@FunctionalInterface
public interface TFunction<T, R> extends Function<T, R> {

    @Override
    default R apply(T t) {
        try {
            return applyOrThrow(t);
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
    }

    R applyOrThrow(T t) throws Throwable;

}
