package info.shillem.domino.util;

import java.util.Objects;
import java.util.function.BiConsumer;

import lotus.domino.NotesException;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;
import lotus.domino.ViewNavigator;

public class ViewEntryLooper {

    public static class Driver extends AbstractLooperDriver<ViewEntry> {

        ViewEntry moveToFirst(ViewEntryCollection entries, boolean reverse) throws NotesException {
            return moveTo(reverse
                    ? entries.getLastEntry()
                    : entries.getFirstEntry());
        }

        ViewEntry moveToFirst(ViewNavigator nav, ViewEntryFilter filter, boolean reverse)
                throws NotesException {
            switch (filter) {
            case DOCUMENT:
                return moveTo(reverse
                        ? nav.getLastDocument()
                        : nav.getFirstDocument());
            default:
                return moveTo(reverse
                        ? nav.getLast()
                        : nav.getFirst());
            }
        }

        ViewEntry moveToNext(ViewEntryCollection entries, boolean reverse) throws NotesException {
            return moveTo(reverse
                    ? entries.getPrevEntry(getBase())
                    : entries.getNextEntry(getBase()));
        }

        ViewEntry moveToNext(ViewNavigator nav, ViewEntryFilter filter, boolean reverse)
                throws NotesException {
            switch (filter) {
            case ANY:
                return moveTo(reverse
                        ? nav.getPrev(getBase())
                        : nav.getNext(getBase()));
            case CATEGORY:
                return moveTo(reverse
                        ? nav.getPrevCategory()
                        : nav.getNextCategory());
            case DOCUMENT:
                return moveTo(reverse
                        ? nav.getPrevDocument()
                        : nav.getNextDocument());
            case HIERARCHY:
                return moveTo(reverse
                        ? nav.getParent()
                        : nav.getChild());
            case SIBLING:
                return moveTo(reverse
                        ? nav.getPrevSibling(getBase())
                        : nav.getNextSibling(getBase()));
            default:
                throw new UnsupportedOperationException(filter.name());
            }
        }

    }

    private ViewEntryFilter filter;
    private boolean reverse;
    private int maxCount;

    public ViewEntryLooper() {
        filter = ViewEntryFilter.ANY;
    }

    public void loop(ViewEntryCollection entries, BiConsumer<ViewEntry, Driver> consumer)
            throws NotesException {
        Objects.requireNonNull(entries, "Entry collection cannot be null");
        Objects.requireNonNull(consumer, "Consumer cannot be null");

        Driver driver = new Driver();

        try {
            int count = 0;

            ViewEntry entry = driver.moveToFirst(entries, reverse);

            while (entry != null) {
                if ((maxCount > 0 && count >= maxCount) || driver.isAborted()) {
                    break;
                }

                DominoUtil.setEncouragedOptions(entry);
                consumer.accept(entry, driver);

                count++;

                entry = driver.moveToNext(entries, reverse);
            }
        } finally {
            driver.recycle();
        }
    }

    public void loop(ViewNavigator nav, BiConsumer<ViewEntry, Driver> consumer)
            throws NotesException {
        Objects.requireNonNull(nav, "View navigator cannot be null");
        Objects.requireNonNull(consumer, "Consumer cannot be null");

        Driver driver = new Driver();

        try {
            nav.setCacheGuidance(400, ViewNavigator.VN_CACHEGUIDANCE_READALL);

            int count = 0;

            ViewEntry entry = driver.moveToFirst(nav, filter, reverse);

            while (entry != null) {
                if ((maxCount > 0 && count >= maxCount) || driver.isAborted()) {
                    break;
                }

                DominoUtil.setEncouragedOptions(entry);
                consumer.accept(entry, driver);

                count++;

                entry = driver.moveToNext(nav, filter, reverse);
            }
        } finally {
            driver.recycle();
        }
    }

    public void setFilter(ViewEntryFilter filter) {
        this.filter = Objects.requireNonNull(filter, "Filter cannot be null");
    }

    public ViewEntryLooper setMaxCount(int maxCount) {
        if (maxCount < 0) {
            throw new IllegalArgumentException("Max count cannot be lower than 0");
        }

        this.maxCount = maxCount;

        return this;
    }

    public ViewEntryLooper setReverse(boolean flag) {
        this.reverse = flag;

        return this;
    }

}
