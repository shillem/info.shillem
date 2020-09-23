package info.shillem.synchronizer.lang;

public class ProcessorException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public ProcessorException(String message) {
	    super(message);
	}
	
	@Override
	public Throwable fillInStackTrace() {
	    return this;
	}

}
