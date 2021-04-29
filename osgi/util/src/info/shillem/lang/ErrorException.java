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

    public Object getProperty(String name) {
        return getProperty(name, Object.class);
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

    public static boolean causeIs(Throwable e, ErrorCode code) {
        return causeIsAny(e, code);
    }

    public static boolean causeIsAny(Throwable e, ErrorCode... codes) {
        ErrorException ee = getCause(e);

        if (ee == null) {
            return false;
        }

        for (ErrorCode code : codes) {
            if (code.equals(ee.getCode())) {
                return true;
            }
        }

        return false;
    }

    public static Object causeProperty(Throwable e, String name) {
        return causeProperty(e, name, Object.class);
    }

    public static <T> T causeProperty(Throwable e, String name, Class<T> cls) {
        ErrorException ee = getCause(e);

        if (ee == null) {
            return null;
        }

        return ee.getProperty(name, cls);
    }

    private static ErrorException getCause(Throwable e) {
        if (e.getCause() == null || !(e.getCause() instanceof ErrorException)) {
            return null;
        }

        return (ErrorException) e.getCause();
    }

}
