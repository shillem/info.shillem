package info.shillem.domino.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import info.shillem.dao.Summary;
import info.shillem.util.Unthrow.ThrowableConsumer;
import info.shillem.util.Unthrow.ThrowableFunction;
import info.shillem.util.Unthrow.ThrowableSupplier;
import lotus.domino.Base;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.NotesError;
import lotus.domino.NotesException;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;
import lotus.domino.ViewNavigator;

public class DominoLoop {

    public enum OptionEntry {
        ANY, CATEGORY, DOCUMENT;
    }

    public static class Options<T extends Base, R> {

        ThrowableFunction<T, R> converter;
        int limit;
        int offset;
        ThrowableConsumer<T> reader;
        OptionTotal total;

        boolean isWithinLimit(int value) {
            return limit == 0 || value < limit;
        }

        public void setConverter(ThrowableFunction<T, R> value) {
            converter = value;
        }

        public void setLimit(int value) {
            limit = value;
        }

        public void setOffset(int value) {
            offset = value;
        }

        public void setReader(ThrowableConsumer<T> value) {
            reader = value;
        }

        public void setTotal(OptionTotal value) {
            total = value;
        }

    }

    public static class OptionsDocument<R> extends Options<Document, R> {

    }

    public static class OptionsViewEntry<R> extends Options<ViewEntry, R> {

        boolean disableColumnData;
        OptionEntry kind;
        int precount;

        public OptionsViewEntry() {
            super();

            setKind(OptionEntry.ANY);
        }

        public void setDisableColumnData(boolean value) {
            disableColumnData = value;
        }

        public void setKind(OptionEntry value) {
            kind = value;
        }

        public void setPrecount(int value) {
            precount = value;
        }

    }

    public enum OptionTotal {
        READ, READ_ONLY;
    }

    public static class Result<T> {

        private final List<T> data;
        private final Summary summary;

        private Result(Options<?, ?> options) {
            data = new ArrayList<>();
            summary = new Summary(options.limit, options.offset);
        }

        public List<T> getData() {
            return data;
        }

        public Summary getSummary() {
            return summary;
        }

    }

    private static final int DEFAULT_CACHE_SIZE = 300;

    private DominoLoop() {

    }

    private static int getCacheSize(Options<?, ?> options) {
        if (options.limit == 0) {
            return DEFAULT_CACHE_SIZE;
        }

        return Math.min(options.limit, DEFAULT_CACHE_SIZE);
    }

    public static <R> Result<R> read(
            DocumentCollection coll,
            OptionsDocument<R> options)
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

    public static <R> Result<R> read(View view, OptionsDocument<R> options) throws NotesException {
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

    public static <R> Result<R> read(
            ViewEntryCollection coll,
            OptionsViewEntry<R> options)
            throws NotesException {
        Objects.requireNonNull(coll, "View entry collection cannot be null");
        Objects.requireNonNull(options, "View entry options cannot be null");

        return readViewEntries(
                (result) -> {
                    ViewEntry entry = options.offset > 0
                            ? coll.getNthEntry(options.offset + 1)
                            : coll.getFirstEntry();

                    Summary summary = result.summary;

                    if (summary.isMaxOffset() && options.precount > 0) {
                        summary.recalculateOffset(options.precount);
                        summary.setTotal(options.precount);
                    }

                    return entry;
                },
                (current) -> coll.getNextEntry(),
                coll::getCount,
                options);
    }

    public static <R> Result<R> read(
            ViewNavigator nav,
            OptionsViewEntry<R> options)
            throws NotesException {
        Objects.requireNonNull(nav, "View navigator cannot be null");
        Objects.requireNonNull(options, "View entry options cannot be null");

        ThrowableFunction<Result<R>, ViewEntry> starter = null;
        ThrowableFunction<ViewEntry, ViewEntry> advancer = null;
        ThrowableSupplier<Integer> counter = null;

        switch (options.kind) {
        case ANY: {
            starter = (result) -> {
                ViewEntry entry;
                Summary summary = result.summary;

                if (options.offset > 0) {
                    nav.gotoFirst();

                    if (summary.isMaxOffset()) {
                        summary.recalculateOffset(nav.skip(summary.getOffset()));
                        summary.setTotal(summary.getOffset());
                    }

                    nav.skip(result.summary.getOffset());

                    entry = nav.getCurrent();
                } else {
                    entry = nav.getFirst();
                }

                if (options.disableColumnData) {
                    setEntryOptions(nav, ViewNavigator.VN_ENTRYOPT_NOCOLUMNVALUES);
                }

                setCacheGuidance(
                        nav,
                        getCacheSize(options),
                        ViewNavigator.VN_CACHEGUIDANCE_READALL);

                return entry;
            };
            advancer = (current) -> nav.getNext();
            counter = () -> nav.skip(Integer.MAX_VALUE);

            break;
        }
        case CATEGORY: {
            starter = (result) -> {
                if (!nav.gotoFirst()) {
                    return null;
                }

                ViewEntry entry = nav.getCurrent();

                if (options.disableColumnData) {
                    setEntryOptions(nav, ViewNavigator.VN_ENTRYOPT_NOCOLUMNVALUES);
                }

                setCacheGuidance(
                        nav,
                        getCacheSize(options),
                        ViewNavigator.VN_CACHEGUIDANCE_READSELECTIVE);

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
            starter = (result) -> {
                if (!nav.gotoFirst()) {
                    return null;
                }

                ViewEntry entry = nav.getCurrent();

                if (options.disableColumnData) {
                    setEntryOptions(nav, ViewNavigator.VN_ENTRYOPT_NOCOLUMNVALUES);
                }

                setCacheGuidance(
                        nav,
                        getCacheSize(options),
                        ViewNavigator.VN_CACHEGUIDANCE_READSELECTIVE);

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

    private static <R> Result<R> readDocuments(
            ThrowableSupplier<Document> starter,
            ThrowableFunction<Document, Document> advancer,
            ThrowableSupplier<Integer> counter,
            OptionsDocument<R> options)
            throws NotesException {
        Result<R> result = new Result<>(options);

        Document doc = null;
        Document temp = null;

        try {
            if (options.total != OptionTotal.READ_ONLY) {
                int count = 0;
                doc = starter.get();

                while (doc != null) {
                    temp = advancer.apply(doc);

                    if (options.reader != null) {
                        options.reader.accept(doc);
                    }

                    if (options.converter != null) {
                        result.data.add(options.converter.apply(doc));
                    }

                    if (!options.isWithinLimit(++count)) {
                        break;
                    }

                    doc.recycle();
                    doc = temp;
                }
            }

            if (options.total == OptionTotal.READ || options.total == OptionTotal.READ_ONLY) {
                if (result.summary.getTotal() == null) {
                    result.summary.setTotal(counter.get());
                }
            }

            return result;
        } finally {
            DominoUtil.recycle(temp, doc);
        }
    }

    private static <R> Result<R> readViewEntries(
            ThrowableFunction<Result<R>, ViewEntry> starter,
            ThrowableFunction<ViewEntry, ViewEntry> advancer,
            ThrowableSupplier<Integer> counter,
            OptionsViewEntry<R> options)
            throws NotesException {
        Result<R> result = new Result<>(options);

        ViewEntry entry = null;
        ViewEntry temp = null;

        try {
            if (options.total != OptionTotal.READ_ONLY) {
                int count = 0;
                entry = starter.apply(result);

                while (entry != null) {
                    temp = advancer.apply(entry);

                    if (options.reader != null) {
                        options.reader.accept(entry);
                    }

                    if (options.converter != null) {
                        result.data.add(options.converter.apply(entry));
                    }

                    if (!options.isWithinLimit(++count)) {
                        break;
                    }

                    entry.recycle();
                    entry = temp;
                }
            }

            if (options.total == OptionTotal.READ || options.total == OptionTotal.READ_ONLY) {
                if (result.summary.getTotal() == null) {
                    result.summary.setTotal(counter.get());
                }
            }

            return result;
        } finally {
            DominoUtil.recycle(temp, entry);
        }
    }

    private static void setCacheGuidance(
            ViewNavigator nav,
            int size,
            int options)
            throws NotesException {
        try {
            nav.setCacheGuidance(size, options);
        } catch (NotesException e) {
            if (e.id != NotesError.NOTES_ERR_NOT_IMPLEMENTED) {
                throw e;
            }
        }
    }

    private static void setEntryOptions(ViewNavigator nav, int options) throws NotesException {
        try {
            nav.setEntryOptions(options);
        } catch (NotesException e) {
            if (e.id != NotesError.NOTES_ERR_NOT_IMPLEMENTED) {
                throw e;
            }
        }
    }

}
