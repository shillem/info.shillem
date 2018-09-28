package info.shillem.dao.lang;

import info.shillem.dto.BaseField;
import info.shillem.lang.ErrorCode;

public class DaoRecordException extends DaoException {

    private static final long serialVersionUID = 1L;

    private String id;
    private BaseField field;
    private Object value;

    private DaoRecordException(String message, ErrorCode code) {
        super(message, code);
    }

    public String getId() {
        return id;
    }

    public BaseField getField() {
        return field;
    }

    public Object getValue() {
        return value;
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
                DaoErrorCode.MISSING);
        
        exception.field = field;
        exception.value = value;
        
        return exception;
    }
    
    public static DaoRecordException asMissing(String id) {
        DaoRecordException exception = new DaoRecordException(
                String.format("The resource with id %s is missing", id),
                DaoErrorCode.MISSING);
        
        exception.id = id;
        
        return exception;
    }

}
