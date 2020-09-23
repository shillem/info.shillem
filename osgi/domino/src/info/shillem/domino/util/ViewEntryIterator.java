package info.shillem.domino.util;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;

import lotus.domino.ViewEntry;

class ViewEntryIterator implements DominoIterator<ViewEntry> {

    private final Supplier<ViewEntry> advancer;

    private ViewEntry current;
    private ViewEntry next;

    ViewEntryIterator(Supplier<ViewEntry> starter, Supplier<ViewEntry> advancer) {
        this.advancer = Objects.requireNonNull(advancer);

        this.next = Objects.requireNonNull(starter).get();
    }

    @Override
    public boolean hasNext() {
        return Objects.nonNull(next);
    }

    @Override
    public ViewEntry next() {
        DominoUtil.recycle(current);

        current = next;

        if (Objects.isNull(current)) {
            throw new NoSuchElementException();
        }

        next = advancer.get();

        return current;
    }

    @Override
    public void close() throws Exception {
        DominoUtil.recycle(current);
    }

}
