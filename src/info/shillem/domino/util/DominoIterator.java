package info.shillem.domino.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;

import info.shillem.util.TFunction;
import lotus.domino.Base;

class DominoIterator<T extends Base> implements AutoCloseable, Iterator<T> {

    private final TFunction<T, T> advancer;

    private T current;
    private T next;

    DominoIterator(Supplier<T> starter, TFunction<T, T> advancer) {
        this.advancer = Objects.requireNonNull(advancer);

        this.next = Objects.requireNonNull(starter).get();
    }

    @Override
    public boolean hasNext() {
        return Objects.nonNull(next);
    }

    @Override
    public T next() {
        DominoUtil.recycle(current);

        current = next;

        if (Objects.isNull(current)) {
            throw new NoSuchElementException();
        }

        next = advancer.apply(current);

        return current;
    }

    public void close() {
        DominoUtil.recycle(current);
    }

}
