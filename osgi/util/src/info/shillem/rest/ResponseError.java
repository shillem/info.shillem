package info.shillem.rest;

import java.io.Serializable;

public class ResponseError implements Serializable {

    private static final long serialVersionUID = 1L;

    public String code;
    public Object data;
    public String message;

    public ResponseError setCode(String value) {
        code = value;

        return this;
    }

    public ResponseError setData(Object value) {
        data = value;

        return this;
    }

    public ResponseError setMessage(String value) {
        message = value;

        return this;
    }

}
