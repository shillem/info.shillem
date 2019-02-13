package info.shillem.domino.util;

import java.util.Objects;
import java.util.function.BiConsumer;

import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.NotesException;
import lotus.domino.View;

public class DocumentLooper {

    public static class Driver extends AbstractLooperDriver<Document> {

        private Document moveToFirst(DocumentCollection coll, boolean reverse)
                throws NotesException {
            return moveTo(reverse
                    ? coll.getLastDocument()
                    : coll.getFirstDocument());
        }

        private Document moveToFirst(View vw, boolean reverse) throws NotesException {
            return moveTo(reverse
                    ? vw.getLastDocument()
                    : vw.getFirstDocument());
        }

        private Document moveToNext(DocumentCollection coll, boolean reverse)
                throws NotesException {
            return moveTo(reverse
                    ? coll.getPrevDocument(getBase())
                    : coll.getNextDocument(getBase()));
        }

        private Document moveToNext(View vw, boolean reverse) throws NotesException {
            return moveTo(reverse
                    ? vw.getPrevDocument(getBase())
                    : vw.getNextDocument(getBase()));
        }

    }

    private boolean reverse;
    private int maxCount;

    public void loop(DocumentCollection coll, BiConsumer<Document, Driver> consumer)
            throws NotesException {
        Objects.requireNonNull(coll, "Document collection cannot be null");
        Objects.requireNonNull(consumer, "Consumer cannot be null");

        Driver driver = new Driver();

        try {
            int count = 0;

            Document doc = driver.moveToFirst(coll, reverse);

            while (doc != null) {
                if ((maxCount > 0 && count >= maxCount) || driver.isAborted()) {
                    break;
                }

                DominoUtil.setEncouragedOptions(doc);
                consumer.accept(doc, driver);

                doc = driver.moveToNext(coll, reverse);
            }
        } finally {
            driver.recycle();
        }
    }

    public void loop(View view, BiConsumer<Document, Driver> consumer)
            throws NotesException {
        Objects.requireNonNull(view, "View cannot be null");
        Objects.requireNonNull(consumer, "Consumer cannot be null");

        Driver driver = new Driver();

        try {
            int count = 0;

            Document doc = driver.moveToFirst(view, reverse);

            while (doc != null) {
                if ((maxCount > 0 && count >= maxCount) || driver.isAborted()) {
                    break;
                }
                
                DominoUtil.setEncouragedOptions(doc);
                consumer.accept(doc, driver);

                count++;

                doc = driver.moveToNext(view, reverse);
            }
        } finally {
            driver.recycle();
        }
    }

    public DocumentLooper setMaxCount(int maxCount) {
        if (maxCount < 0) {
            throw new IllegalArgumentException("Max count cannot be lower than 0");
        }

        this.maxCount = maxCount;

        return this;
    }

    public DocumentLooper setReverse(boolean flag) {
        this.reverse = flag;

        return this;
    }

}
