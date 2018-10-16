package info.shillem.dao.lang;

import java.util.Date;

import info.shillem.dto.BaseField;
import info.shillem.lang.ErrorCode;

public class DaoRecordException extends DaoException {

    private static final long serialVersionUID = 1L;

    private String id;
    private Date storedDate;
    private String referenceId;
    private BaseField field;
    private Object value;

    private DaoRecordException(String message, ErrorCode code) {
        super(message, code);
    }

    public BaseField getField() {
        return field;
    }

    public String getId() {
        return id;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public Date getStoredDate() {
        return storedDate;
    }

    public Object getValue() {
        return value;
    }

    public DaoRecordException setReferenceId(String referenceId) {
        this.referenceId = referenceId;

        return this;
    }

    public static DaoRecordException asDirty(String id, Date inMemoryDate, Date storedDate) {
        DaoRecordException exception = new DaoRecordException(String.format(
                "The in-memory resource %s with saved date %s has already been updated on %s"
                        + " and therefore needs to be refreshed",
                id, inMemoryDate, storedDate),
                DaoErrorCode.DIRTY_RECORD);

        exception.id = id;
        exception.storedDate = storedDate;

        return exception;
    }

    public static DaoRecordException asDuplicate(BaseField field, Object value) {
        DaoRecordException exception = new DaoRecordException(
                String.format("The resource with field %s and value %s is duplicate", field, value),
                DaoErrorCode.DUPLICATE_RECORD);

        exception.field = field;
        exception.value = value;

        return exception;
    }

    public static DaoRecordException asInvalidValue(BaseField field, Object value) {
        DaoRecordException exception = new DaoRecordException(
                String.format("The resource with field %s and value %s is invalid", field, value),
                DaoErrorCode.INVALID_FIELD_VALUE);

        exception.field = field;
        exception.value = value;

        return exception;
    }

    public static DaoRecordException asMissing(BaseField field, Object value) {
        DaoRecordException exception = new DaoRecordException(
                String.format("The resource with field %s and value %s is missing", field, value),
                DaoErrorCode.MISSING_RECORD);

        exception.field = field;
        exception.value = value;

        return exception;
    }

    public static DaoRecordException asMissing(String id) {
        DaoRecordException exception = new DaoRecordException(
                String.format("The resource with id %s is missing", id),
                DaoErrorCode.MISSING_RECORD);

        exception.id = id;

        return exception;
    }

}
