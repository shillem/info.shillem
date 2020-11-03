package info.shillem.rest;

import java.io.Serializable;

import info.shillem.lang.ErrorCode;

public class ResponseError implements Serializable {

    private static final long serialVersionUID = 1L;

    public Object data;
    public ErrorCode errorCode;
    public String message;
    public String stack;
    public Integer statusCode;

    public ResponseError setData(Object value) {
        data = value;

        return this;
    }

    public ResponseError setErrorCode(ErrorCode value) {
        errorCode = value;

        return this;
    }

    public ResponseError setMessage(String value) {
        message = value;

        return this;
    }

    public ResponseError setStack(String value) {
        stack = value;

        return this;
    }

    public ResponseError setStatusCode(Integer value) {
        statusCode = value;

        return this;
    }

}
