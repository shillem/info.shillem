package info.shillem.dao.lang;

import info.shillem.lang.ErrorCode;
import info.shillem.lang.ErrorCodedException;

public class DaoException extends Exception implements ErrorCodedException {

    private static final long serialVersionUID = 1L;

    private final ErrorCode code;

    public DaoException() {
        this(DaoErrorCode.DEFAULT);
    }

    public DaoException(ErrorCode code) {
        this(code.toString(), code);
    }

    public DaoException(ErrorCode code, Throwable cause) {
        this(code.toString(), code, cause);
    }

    public DaoException(String message, ErrorCode code) {
        super(message);

        this.code = code;
    }

    public DaoException(String message, ErrorCode code, Throwable cause) {
        super(code.toString(), cause);

        this.code = code;
    }

    public DaoException(Throwable cause) {
        this(DaoErrorCode.DEFAULT, cause);
    }

    @Override
    public ErrorCode getCode() {
        return code;
    }

}