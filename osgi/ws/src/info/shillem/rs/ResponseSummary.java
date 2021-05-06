package info.shillem.rs;

import info.shillem.dao.Query;
import info.shillem.dao.Summary;

public class ResponseSummary {

    public Integer limit;
    public Integer offset;
    public Integer total;

    public ResponseSummary setLimit(Integer value) {
        limit = value;

        return this;
    }

    public ResponseSummary setOffset(Integer value) {
        offset = value;

        return this;
    }

    public ResponseSummary setTotal(Integer value) {
        total = value;

        return this;
    }

    public static ResponseSummary wrap(Query<?> query) {
        Summary summary = query.getSummary();

        return new ResponseSummary()
                .setLimit(summary.getLimit())
                .setOffset(summary.getOffset())
                .setTotal(summary.getTotal());
    }

}