package info.shillem.dao.lang;

import info.shillem.lang.ErrorCode;

public enum DaoErrorCode implements ErrorCode {

    DEFAULT,
    DIRTY_RECORD,
    DUPLICATE_RECORD,
    INVALID_FIELD_VALUE,
    MISSING_RECORD,
    UNRESOLVABLE_NOTES_URL

}
