package info.shillem.lang;

public class DaoException extends Exception {

	private static final long serialVersionUID = 1L;

	public DaoException(String message) {
		super(message);
	}
	
	public DaoException(Throwable cause) {
		super(cause);
	}

	public DaoException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
