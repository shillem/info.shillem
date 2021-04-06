package info.shillem.rest;

import java.util.List;

public class ResponseEnvelope {

    public Object data;
    public List<? extends ResponseError> errors;
    public ResponseSummary summary;

    public ResponseEnvelope setData(Object value) {
        data = value;

        return this;
    }

    public ResponseEnvelope setErrors(List<? extends ResponseError> values) {
        errors = values;

        return this;
    }

    public ResponseEnvelope setSummary(ResponseSummary value) {
        summary = value;

        return this;
    }

}
