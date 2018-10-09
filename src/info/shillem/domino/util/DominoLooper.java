package info.shillem.domino.util;

import java.util.Objects;
import java.util.function.Consumer;

import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.NotesException;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;
import lotus.domino.ViewNavigator;

public class DominoLooper {

    private boolean invertedLoop;
    private int maxCount;

    private Document getFirstDocument(DocumentCollection coll) throws NotesException {
        return invertedLoop ? coll.getLastDocument() : coll.getFirstDocument();
    }

    private Document getFirstDocument(View vw) throws NotesException {
        return invertedLoop ? vw.getLastDocument() : vw.getFirstDocument();
    }

    private ViewEntry getFirstEntry(ViewEntryCollection entries) throws NotesException {
        return invertedLoop ? entries.getLastEntry() : entries.getFirstEntry();
    }

    private ViewEntry getFirstEntry(ViewNavigator nav) throws NotesException {
        return invertedLoop ? nav.getLast() : nav.getFirst();
    }

    private Document getNextDocument(DocumentCollection coll, Document doc) throws NotesException {
        return invertedLoop ? coll.getPrevDocument(doc) : coll.getNextDocument(doc);
    }

    private Document getNextDocument(View vw, Document doc) throws NotesException {
        return invertedLoop ? vw.getPrevDocument(doc) : vw.getNextDocument(doc);
    }
    
    private ViewEntry getNextEntry(ViewEntryCollection entries, ViewEntry entry)
            throws NotesException {
        return invertedLoop ? entries.getPrevEntry(entry) : entries.getNextEntry(entry);
    }
    
    private ViewEntry getNextEntry(ViewNavigator nav, ViewEntry entry)
            throws NotesException {
        return invertedLoop ? nav.getPrev(entry) : nav.getNext(entry);
    }

    public void loopDocuments(DocumentCollection coll, Consumer<Document> consumer)
            throws NotesException {
        Objects.requireNonNull(coll, "Document collection cannot be null");
        Objects.requireNonNull(consumer, "Consumer cannot be null");

        Document doc = null;

        int count = 0;

        try {
            doc = getFirstDocument(coll);

            while (doc != null) {
                if (maxCount > 0 && count >= maxCount) {
                    break;
                }

                Document nextDocument = getNextDocument(coll, doc);

                DominoUtil.setEncouragedOptions(doc);
                consumer.accept(doc);
                DominoUtil.recycle(doc);

                doc = nextDocument;

                count++;
            }
        } finally {
            DominoUtil.recycle(doc);
        }
    }

    public void loopDocuments(View view, Consumer<Document> consumer) throws NotesException {
        Objects.requireNonNull(view, "View cannot be null");
        Objects.requireNonNull(consumer, "Consumer cannot be null");

        Document doc = null;

        int count = 0;

        try {
            doc = getFirstDocument(view);

            while (doc != null) {
                if (maxCount > 0 && count >= maxCount) {
                    break;
                }

                Document nextDocument = getNextDocument(view, doc);

                DominoUtil.setEncouragedOptions(doc);
                consumer.accept(doc);
                DominoUtil.recycle(doc);

                doc = nextDocument;

                count++;
            }
        } finally {
            DominoUtil.recycle(doc);
        }
    }

    public void loopDocuments(ViewEntryCollection entries, Consumer<Document> consumer)
            throws NotesException {
        Objects.requireNonNull(entries, "Entry collection cannot be null");
        Objects.requireNonNull(consumer, "Consumer cannot be null");
        
        ViewEntry entry = null;
        Document doc = null;
        
        int count = 0;
        
        try {
            entry = getFirstEntry(entries);
            
            while (entry != null) {
                if (maxCount > 0 && count >= maxCount) {
                    break;
                }
                
                ViewEntry nextEntry = getNextEntry(entries, entry);
                
                doc = entry.getDocument();
                DominoUtil.setEncouragedOptions(doc);
                consumer.accept(doc);
                DominoUtil.recycle(doc, entry);
                
                entry = nextEntry;
                
                count++;
            }
        } finally {
            DominoUtil.recycle(doc, entry);
        }
    }
    
    public void loopDocuments(ViewNavigator nav, Consumer<Document> consumer) throws NotesException {
        Objects.requireNonNull(nav, "View navigator cannot be null");
        Objects.requireNonNull(consumer, "Consumer cannot be null");
        
        ViewEntry entry = null;
        Document doc = null;
        
        int count = 0;
        
        try {
            nav.setCacheGuidance(400, ViewNavigator.VN_CACHEGUIDANCE_READALL);
            
            entry = getFirstEntry(nav);
            
            while (entry != null) {
                if (maxCount > 0 && count >= maxCount) {
                    break;
                }
                
                ViewEntry nextEntry = getNextEntry(nav, entry);
                
                doc = entry.getDocument();
                DominoUtil.setEncouragedOptions(doc);
                consumer.accept(doc);
                DominoUtil.recycle(doc, entry);
                
                entry = nextEntry;
                
                count++;
            }
        } finally {
            DominoUtil.recycle(doc, entry);
        }
    }

    public void loopEntries(ViewEntryCollection entries, Consumer<ViewEntry> consumer)
            throws NotesException {
        Objects.requireNonNull(entries, "Entry collection cannot be null");
        Objects.requireNonNull(consumer, "Consumer cannot be null");

        ViewEntry entry = null;

        int count = 0;

        try {
            entry = getFirstEntry(entries);

            while (entry != null) {
                if (maxCount > 0 && count >= maxCount) {
                    break;
                }

                ViewEntry nextEntry = getNextEntry(entries, entry);

                DominoUtil.setEncouragedOptions(entry);
                consumer.accept(entry);
                DominoUtil.recycle(entry);

                entry = nextEntry;

                count++;
            }
        } finally {
            DominoUtil.recycle(entry);
        }
    }
    
    public void loopEntries(ViewNavigator nav, Consumer<ViewEntry> consumer) throws NotesException {
        Objects.requireNonNull(nav, "View navigator cannot be null");
        Objects.requireNonNull(consumer, "Consumer cannot be null");

        ViewEntry entry = null;

        int count = 0;

        try {
            nav.setCacheGuidance(400, ViewNavigator.VN_CACHEGUIDANCE_READALL);

            entry = getFirstEntry(nav);

            while (entry != null) {
                if (maxCount > 0 && count >= maxCount) {
                    break;
                }

                ViewEntry nextEntry = getNextEntry(nav, entry);

                DominoUtil.setEncouragedOptions(entry);
                consumer.accept(entry);
                DominoUtil.recycle(entry);

                entry = nextEntry;

                count++;
            }
        } finally {
            DominoUtil.recycle(entry);
        }
    }

    public DominoLooper setInvertedLoop(boolean invertedLoop) {
        this.invertedLoop = invertedLoop;

        return this;
    }

    public DominoLooper setMaxCount(int maxCount) {
        if (maxCount < 0) {
            throw new IllegalArgumentException("Max count cannot be lower than 0");
        }

        this.maxCount = maxCount;

        return this;
    }

}
