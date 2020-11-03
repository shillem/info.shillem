package info.shillem.rest;

import info.shillem.dao.QuerySummary;

public class ResponseSummary {

    public Integer limit;
    public Integer total;

    public ResponseSummary setLimit(Integer value) {
        limit = value;

        return this;
    }

    public ResponseSummary setTotal(Integer value) {
        total = value;

        return this;
    }

    public static ResponseSummary wrap(QuerySummary summary) {
        if (summary == null) {            
            return null;
        }
        
        return new ResponseSummary().setLimit(summary.getLimit()).setTotal(summary.getTotal());
    }

}