package info.shillem.dao.lang;

import java.util.Date;

import info.shillem.dao.Query;
import info.shillem.dto.BaseField;

public class DaoRecordException extends DaoException {

    private static final long serialVersionUID = 1L;

    private DaoRecordException(DaoErrorCode code) {
        super(code);
    }

    public DaoRecordException setReference(Object value) {
        setProperty("reference", value);

        return this;
    }

    public static DaoRecordException asDirty(String id, Date inMemoryDate, Date storedDate) {
        DaoRecordException exception = new DaoRecordException(DaoErrorCode.DIRTY_RECORD);

        exception.setProperty("id", id);
        exception.setProperty("inMemoryDate", inMemoryDate);
        exception.setProperty("storedDate", storedDate);

        return exception;
    }

    public static DaoRecordException asDuplicate(BaseField field, Object value) {
        DaoRecordException exception = new DaoRecordException(DaoErrorCode.DUPLICATE_RECORD);

        exception.setProperty("field", field);
        exception.setProperty("value", value);

        return exception;
    }

    public static DaoRecordException asInvalidField(BaseField field, Object value) {
        DaoRecordException exception = new DaoRecordException(DaoErrorCode.INVALID_FIELD);

        exception.setProperty("field", field);
        exception.setProperty("value", value);

        return exception;
    }

    public static DaoRecordException asMissing(BaseField field, Object value) {
        DaoRecordException exception = new DaoRecordException(DaoErrorCode.MISSING_RECORD);

        exception.setProperty("field", field);
        exception.setProperty("value", value);

        return exception;
    }

    public static DaoRecordException asMissing(Query<?> query) {
        DaoRecordException exception = new DaoRecordException(DaoErrorCode.MISSING_RECORD);

        exception.setProperty("query", query);

        return exception;
    }

    public static DaoRecordException asMissing(String identifier) {
        DaoRecordException exception = new DaoRecordException(DaoErrorCode.MISSING_RECORD);

        exception.setProperty("identifier", identifier);

        return exception;
    }

    public static DaoRecordException asMissingField(BaseField field) {
        DaoRecordException exception = new DaoRecordException(DaoErrorCode.MISSING_FIELD);

        exception.setProperty("field", field);

        return exception;
    }

}
