package info.shillem.util.dots.lang;

public class PreferencesException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PreferencesException(String message) {
        super(message);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
    
}
