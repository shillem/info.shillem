package info.shillem.util;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Unthrow {

    @FunctionalInterface
    public interface ThrowableBiConsumer<T, U> extends BiConsumer<T, U> {
        @Override
        default void accept(T t, U u) {
            try {
                acceptOrThrow(t, u);
            } catch (Throwable e) {
                rethrow(e);
            }
        }

        void acceptOrThrow(T t, U u) throws Throwable;
    }
    
    @FunctionalInterface
    public interface ThrowableBiFunction<T, U, R> extends BiFunction<T, U, R> {
        @Override
        default R apply(T t, U u) {
            try {
                return applyOrThrow(t, u);
            } catch (Throwable e) {
                return rethrow(e);
            }
        }

        R applyOrThrow(T t, U u) throws Throwable;
    }

    @FunctionalInterface
    public interface ThrowableConsumer<T> extends Consumer<T> {
        @Override
        default void accept(T t) {
            try {
                acceptOrThrow(t);
            } catch (Throwable e) {
                rethrow(e);
            }
        }

        void acceptOrThrow(T t) throws Throwable;
    }

    @FunctionalInterface
    public interface ThrowableFunction<T, R> extends Function<T, R> {
        @Override
        default R apply(T t) {
            try {
                return applyOrThrow(t);
            } catch (Throwable e) {
                return rethrow(e);
            }
        }

        R applyOrThrow(T t) throws Throwable;
    }

    @FunctionalInterface
    public interface ThrowablePredicate<T> extends Predicate<T> {

        @Override
        default boolean test(T t) {
            try {
                return testOrThrow(t);
            } catch (Throwable e) {
                return rethrow(e);
            }
        }

        boolean testOrThrow(T t) throws Throwable;

    }

    @FunctionalInterface
    public interface ThrowableRunnable extends Runnable {
        @Override
        default void run() {
            try {
                runOrThrow();
            } catch (Throwable e) {
                rethrow(e);
            }
        }

        void runOrThrow() throws Throwable;
    }

    @FunctionalInterface
    public interface ThrowableSupplier<T> extends Supplier<T> {

        @Override
        default T get() {
            try {
                return getOrThrow();
            } catch (Throwable e) {
                return rethrow(e);
            }
        }

        T getOrThrow() throws Throwable;

    }

    private Unthrow() {
        throw new UnsupportedOperationException();
    }

    public static <T, U> void on(ThrowableBiConsumer<T, U> lambda, T t, U u) {
        lambda.accept(t, u);
    }

    public static <T, U, R> R on(ThrowableBiFunction<T, U, R> lambda, T t, U u) {
        return lambda.apply(t, u);
    }

    public static <T> void on(ThrowableConsumer<T> lambda, T t) {
        lambda.accept(t);
    }

    public static <T, R> R on(ThrowableFunction<T, R> lambda, T t) {
        return lambda.apply(t);
    }

    public static <T> boolean on(ThrowablePredicate<T> lambda, T t) {
        return lambda.test(t);
    }

    public static void on(ThrowableRunnable lambda) {
        lambda.run();
    }
    
    public static <T> T on(ThrowableSupplier<T> lambda) {
        return lambda.get();
    }

    @SuppressWarnings("unchecked")
    private static <R, T extends Throwable> R rethrow(Throwable t) throws T {
        throw (T) t;
    }

}
