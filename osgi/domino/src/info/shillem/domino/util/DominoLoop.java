package info.shillem.domino.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import info.shillem.util.Unthrow.ThrowableFunction;
import info.shillem.util.Unthrow.ThrowableSupplier;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.NotesError;
import lotus.domino.NotesException;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;
import lotus.domino.ViewNavigator;

public class DominoLoop {

    public static class DocumentOptions<T> extends Options {

        Consumer<Document> reader;
        Function<Document, T> converter;

        public DocumentOptions<T> setConverter(Function<Document, T> converter) {
            this.converter = converter;

            return this;
        }

        public DocumentOptions<T> setFetchTotal(boolean value) {
            fetchTotal = value;

            return this;
        }

        public DocumentOptions<T> setLimit(int value) {
            limit = value;

            return this;
        }

        public DocumentOptions<T> setOffset(int value) {
            offset = value;

            return this;
        }

        public DocumentOptions<T> setReader(Consumer<Document> reader) {
            this.reader = reader;

            return this;
        }

    }

    public enum OptionEntry {
        ANY, CATEGORY, DOCUMENT;
    }

    static class Options {
        int limit;
        int offset;
        boolean fetchTotal;

        boolean isWithinLimit(int count) {
            return limit == 0 || count < limit;
        }
    }

    public static class Result<T> {

        private List<T> data;
        private Integer total;

        private Result() {
            data = new ArrayList<>();
        }

        public List<T> getData() {
            return data;
        }

        public Integer getTotal() {
            return total;
        }

    }

    public static class ViewEntryOptions<T> extends Options {

        Consumer<ViewEntry> reader;
        Function<ViewEntry, T> converter;
        OptionEntry kind;

        public ViewEntryOptions() {
            kind = OptionEntry.ANY;
        }

        public ViewEntryOptions<T> setConverter(Function<ViewEntry, T> converter) {
            this.converter = converter;

            return this;
        }

        public ViewEntryOptions<T> setFetchTotal(boolean value) {
            fetchTotal = value;

            return this;
        }

        public ViewEntryOptions<T> setKind(OptionEntry kind) {
            this.kind = kind;

            return this;
        }

        public ViewEntryOptions<T> setLimit(int value) {
            limit = value;

            return this;
        }

        public ViewEntryOptions<T> setOffset(int value) {
            offset = value;

            return this;
        }

        public ViewEntryOptions<T> setReader(Consumer<ViewEntry> reader) {
            this.reader = reader;

            return this;
        }

    }

    private DominoLoop() {

    }

    public static <T> Result<T> read(DocumentCollection coll, DocumentOptions<T> options)
            throws NotesException {
        Objects.requireNonNull(coll, "Document collection cannot be null");
        Objects.requireNonNull(options, "Document options cannot be null");

        return readDocuments(
                options.offset > 0
                        ? () -> coll.getNthDocument(options.offset + 1)
                        : coll::getFirstDocument,
                (current) -> coll.getNextDocument(),
                coll::getCount,
                options);
    }

    public static <T> Result<T> read(View view, DocumentOptions<T> options)
            throws NotesException {
        Objects.requireNonNull(view, "View cannot be null");
        Objects.requireNonNull(options, "Document options cannot be null");

        return readDocuments(
                options.offset > 0
                        ? () -> view.getNthDocument(options.offset + 1)
                        : view::getFirstDocument,
                view::getNextDocument,
                view::getEntryCount,
                options);
    }

    public static <T> Result<T> read(ViewEntryCollection coll, ViewEntryOptions<T> options)
            throws NotesException {
        Objects.requireNonNull(coll, "View entry collection cannot be null");
        Objects.requireNonNull(options, "View entry options cannot be null");

        return readViewEntries(
                options.offset > 0
                        ? () -> coll.getNthEntry(options.offset + 1)
                        : coll::getFirstEntry,
                (current) -> coll.getNextEntry(),
                coll::getCount,
                options);
    }

    public static <T> Result<T> read(ViewNavigator nav, ViewEntryOptions<T> options)
            throws NotesException {
        Objects.requireNonNull(nav, "View navigator cannot be null");
        Objects.requireNonNull(options, "View entry options cannot be null");

        ThrowableSupplier<ViewEntry> starter = null;
        ThrowableFunction<ViewEntry, ViewEntry> advancer = null;
        ThrowableSupplier<Integer> counter = null;

        switch (options.kind) {
        case ANY: {
            starter = options.offset > 0
                    ? () -> {
                        nav.gotoFirst();
                        nav.skip(options.offset);
                        return nav.getCurrent();
                    }
                    : nav::getFirst;
            advancer = (current) -> nav.getNext();
            counter = () -> {
                nav.gotoFirst();
                int count = nav.skip(Integer.MAX_VALUE);
                ViewEntry entry = nav.getCurrent();

                if (entry != null) {
                    count++;
                    DominoUtil.recycle(entry);
                }

                return count;
            };

            break;
        }
        case CATEGORY: {
            starter = () -> {
                if (!nav.gotoFirst()) {
                    return null;
                }

                ViewEntry entry = nav.getCurrent();

                setCacheGuidance(nav);

                if (entry.isCategory()) {
                    return entry;
                }

                ViewEntry nextEntry = nav.getNextCategory();

                DominoUtil.recycle(entry);

                return nextEntry;
            };
            advancer = (current) -> nav.getNextCategory();
            counter = () -> null;

            break;
        }
        case DOCUMENT: {
            starter = () -> {
                if (!nav.gotoFirst()) {
                    return null;
                }

                ViewEntry entry = nav.getCurrent();

                setCacheGuidance(nav);

                if (entry.isDocument()) {
                    return entry;
                }

                ViewEntry nextEntry = nav.getNextDocument();

                DominoUtil.recycle(entry);

                return nextEntry;
            };
            advancer = (current) -> nav.getNextDocument();
            counter = () -> null;
        }
        }

        return readViewEntries(starter, advancer, counter, options);
    }

    private static <T> Result<T> readDocuments(
            ThrowableSupplier<Document> starter,
            ThrowableFunction<Document, Document> advancer,
            ThrowableSupplier<Integer> counter,
            DocumentOptions<T> options) throws NotesException {
        Result<T> result = new Result<>();

        Document doc = null;

        try {
            int count = 0;

            doc = starter.get();

            while (doc != null) {
                if (options.reader != null) {
                    options.reader.accept(doc);
                }

                if (options.converter != null) {
                    result.data.add(options.converter.apply(doc));
                }

                if (!options.isWithinLimit(++count)) {
                    break;
                }

                Document temp = advancer.apply(doc);
                DominoUtil.recycle(doc);
                doc = temp;
            }

            if (options.fetchTotal) {
                result.total = counter.get();
            }

            return result;
        } finally {
            DominoUtil.recycle(doc);
        }
    }

    private static <T> Result<T> readViewEntries(
            ThrowableSupplier<ViewEntry> starter,
            ThrowableFunction<ViewEntry, ViewEntry> advancer,
            ThrowableSupplier<Integer> counter,
            ViewEntryOptions<T> options) throws NotesException {
        Result<T> result = new Result<>();

        ViewEntry entry = null;

        try {
            int count = 0;

            entry = starter.get();

            while (entry != null) {
                if (options.reader != null) {
                    options.reader.accept(entry);
                }

                if (options.converter != null) {
                    result.data.add(options.converter.apply(entry));
                }

                if (!options.isWithinLimit(++count)) {
                    break;
                }

                ViewEntry temp = advancer.apply(entry);
                DominoUtil.recycle(entry);
                entry = temp;
            }

            if (options.fetchTotal) {
                result.total = counter.get();
            }

            return result;
        } finally {
            DominoUtil.recycle(entry);
        }
    }

    private static void setCacheGuidance(ViewNavigator nav) throws NotesException {
        try {
            nav.setCacheGuidance(300, ViewNavigator.VN_CACHEGUIDANCE_READSELECTIVE);
        } catch (NotesException e) {
            if (e.id != NotesError.NOTES_ERR_NOT_IMPLEMENTED) {
                throw e;
            }
        }
    }

}
