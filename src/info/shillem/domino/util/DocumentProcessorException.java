package info.shillem.domino.util;

public class DocumentProcessorException extends Exception {

	private static final long serialVersionUID = 1L;

	public DocumentProcessorException(Exception e) {
		super(e);
	}

	public DocumentProcessorException(String message) {
		super(message);
	}

}
