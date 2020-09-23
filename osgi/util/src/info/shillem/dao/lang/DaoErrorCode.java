package info.shillem.dao.lang;

import info.shillem.lang.ErrorCode;

public enum DaoErrorCode implements ErrorCode {

    DIRTY_RECORD,
    DUPLICATE_RECORD,
    INVALID_FIELD_VALUE,
    INVALID_QUERY,
    MISSING_RECORD,
    UNRESOLVABLE

}
