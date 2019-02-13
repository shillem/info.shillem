package info.shillem.domino.util;

import java.util.Objects;
import java.util.function.BiConsumer;

import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;
import lotus.domino.ViewNavigator;

public class ViewEntryDocumentLooper {

    public static class Driver extends AbstractLooperDriver<ViewEntry> {

        ViewEntry moveToFirst(ViewEntryCollection entries, boolean reverse) throws NotesException {
            return moveTo(reverse
                    ? entries.getLastEntry()
                    : entries.getFirstEntry());
        }

        ViewEntry moveToFirst(ViewNavigator nav, boolean reverse) throws NotesException {
            return moveTo(reverse
                    ? nav.getLastDocument()
                    : nav.getFirstDocument());
        }

        ViewEntry moveToNext(ViewEntryCollection entries, boolean reverse) throws NotesException {
            return moveTo(reverse
                    ? entries.getPrevEntry(getBase())
                    : entries.getNextEntry(getBase()));
        }

        ViewEntry moveToNext(ViewNavigator nav, boolean reverse) throws NotesException {
            return moveTo(reverse
                    ? nav.getPrevDocument()
                    : nav.getNextDocument());
        }

    }

    private boolean reverse;
    private int maxCount;

    public void loop(ViewEntryCollection entries, BiConsumer<Document, Driver> consumer)
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

                Document doc = entry.getDocument();

                if (doc != null) {
                    DominoUtil.setEncouragedOptions(doc);

                    consumer.accept(doc, driver);

                    count++;
                }

                entry = driver.moveToNext(entries, reverse);
            }
        } finally {
            driver.recycle();
        }
    }

    public void loop(ViewNavigator nav, BiConsumer<Document, Driver> consumer)
            throws NotesException {
        Objects.requireNonNull(nav, "View navigator cannot be null");
        Objects.requireNonNull(consumer, "Consumer cannot be null");

        Driver driver = new Driver();

        try {
            nav.setCacheGuidance(400, ViewNavigator.VN_CACHEGUIDANCE_READALL);

            int count = 0;

            ViewEntry entry = driver.moveToFirst(nav, reverse);

            while (entry != null) {
                if ((maxCount > 0 && count >= maxCount) || driver.isAborted()) {
                    break;
                }

                Document doc = entry.getDocument();
                DominoUtil.setEncouragedOptions(doc);
                consumer.accept(doc, driver);
                count++;

                entry = driver.moveToNext(nav, reverse);
            }
        } finally {
            driver.recycle();
        }
    }

    public ViewEntryDocumentLooper setMaxCount(int maxCount) {
        if (maxCount < 0) {
            throw new IllegalArgumentException("Max count cannot be lower than 0");
        }

        this.maxCount = maxCount;

        return this;
    }

    public ViewEntryDocumentLooper setReverse(boolean flag) {
        this.reverse = flag;

        return this;
    }

}
