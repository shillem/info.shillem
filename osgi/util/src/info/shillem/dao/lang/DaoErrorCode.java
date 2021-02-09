package info.shillem.dao.lang;

import info.shillem.lang.ErrorCode;

public enum DaoErrorCode implements ErrorCode {

    DIRTY_RECORD,
    DUPLICATE_RECORD,
    INVALID_FIELD,
    INVALID_QUERY,
    GENERIC,
    MISSING_FIELD,
    MISSING_RECORD,
    UNRESOLVABLE

}
