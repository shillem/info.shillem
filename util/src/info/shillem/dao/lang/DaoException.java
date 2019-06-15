package info.shillem.dao.lang;

import info.shillem.lang.ErrorCode;
import info.shillem.lang.ErrorException;

public class DaoException extends ErrorException {

    private static final long serialVersionUID = 1L;
    
    public DaoException(ErrorCode code) {
        super(code);
    }

}
