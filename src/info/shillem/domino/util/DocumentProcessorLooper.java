package info.shillem.domino.util;

import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;

public class DocumentProcessorLooper {

	final DocumentProcessor processor;

	public DocumentProcessorLooper(DocumentProcessor processor) {
		this.processor = processor;
	}

	public void process(View view) throws DocumentProcessorException {
		Document doc = null;

		try {
			doc = view.getFirstDocument();

			while (doc != null) {
				Document nextDocument = view.getNextDocument(doc);
				
				doc.setPreferJavaDates(true);
				processor.process(doc);
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
