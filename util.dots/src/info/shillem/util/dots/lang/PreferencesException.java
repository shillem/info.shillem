package info.shillem.util.dots.lang;

public class PreferencesException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PreferencesException(Exception e) {
        super(e);
    }

    public PreferencesException(String message) {
        super(message);
    }

    public PreferencesException(String message, Exception e) {
        super(message, e);
    }

}
