package info.shillem.lang;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ErrorException extends Exception {

    private static final long serialVersionUID = 1L;

    private final ErrorCode code;
    private Map<String, Object> properties;

    public ErrorException(ErrorCode code) {
        super();

        this.code = code;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    public ErrorCode getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return code + " " + properties;
    }

    public Map<String, Object> getProperties() {
        if (properties == null) {
            properties = new HashMap<>();
        }

        return properties;
    }

    public <T> T getProperty(String name, Class<T> cls) {
        return cls.cast(getProperties().get(name));
    }

    public boolean isCode(ErrorCode code) {
        return Objects.equals(code, this.code);
    }

    public void setProperty(String name, Object value) {
        getProperties().put(name, value);
    }

    public static boolean exceptionCauseIs(Throwable e, ErrorCode code) {
        if (e.getCause() == null) {
            return false;
        }
        
        Throwable cause = e.getCause();
        
        if (!(cause instanceof ErrorException)) {
            return false;
        }
        
        ErrorException ee = (ErrorException) cause;
        
        return code.equals(ee.getCode());
    }

}
