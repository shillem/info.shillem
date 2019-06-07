package info.shillem.domino.util;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import lotus.domino.Document;

class DocumentIterator implements DominoIterator<Document> {

    private final Function<Document, Document> advancer;

    private Document current;
    private Document next;

    DocumentIterator(Supplier<Document> starter, Function<Document, Document> advancer) {
        this.advancer = Objects.requireNonNull(advancer);

        this.next = Objects.requireNonNull(starter).get();
    }

    @Override
    public boolean hasNext() {
        return Objects.nonNull(next);
    }

    @Override
    public Document next() {
        DominoUtil.recycle(current);

        current = next;

        if (Objects.isNull(current)) {
            throw new NoSuchElementException();
        }

        next = advancer.apply(current);

        return current;
    }

    @Override
    public void close() {
        DominoUtil.recycle(current);
    }

}
