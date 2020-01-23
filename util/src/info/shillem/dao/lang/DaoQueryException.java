package info.shillem.dao.lang;

public class DaoQueryException extends DaoException {

    private static final long serialVersionUID = 1L;

    private DaoQueryException(DaoErrorCode code) {
        super(code);
    }

    public static DaoQueryException asInvalid(String syntax) {
        DaoQueryException exception = new DaoQueryException(DaoErrorCode.INVALID_QUERY);

        exception.setProperty("syntax", syntax);

        return exception;
    }

}
