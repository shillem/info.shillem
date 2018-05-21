package info.shillem.domino.util;

import lotus.domino.Document;

public interface DocumentProcessor {

	void process(Document doc) throws DocumentProcessorException;

}
