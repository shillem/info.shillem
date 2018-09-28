package info.shillem.domino.util;

import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.NotesException;
import lotus.domino.View;

public class DocumentProcessorLooper {

    private final DocumentProcessor processor;

    public DocumentProcessorLooper(DocumentProcessor processor) {
        this.processor = processor;
    }

    public void process(Document doc) throws DocumentProcessorException {
        processor.process(doc);
    }

    public void process(DocumentCollection coll) throws DocumentProcessorException {
        Document doc = null;

        try {
            doc = coll.getFirstDocument();

            while (doc != null) {
                Document nextDocument = coll.getNextDocument(doc);

                doc.setPreferJavaDates(true);
                process(doc);
                DominoUtil.recycle(doc);

                doc = nextDocument;
            }
        } catch (NotesException e) {
            throw new DocumentProcessorException(e);
        } finally {
            DominoUtil.recycle(doc);
        }
    }

    public void process(View view) throws DocumentProcessorException {
        Document doc = null;

        try {
            doc = view.getFirstDocument();

            while (doc != null) {
                Document nextDocument = view.getNextDocument(doc);

                doc.setPreferJavaDates(true);
                process(doc);
                DominoUtil.recycle(doc);

                doc = nextDocument;
            }
        } catch (NotesException e) {
            throw new DocumentProcessorException(e);
        } finally {
            DominoUtil.recycle(doc);
        }
    }

}
