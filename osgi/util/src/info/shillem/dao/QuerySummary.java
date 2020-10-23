package info.shillem.dao;

public class QuerySummary {

    Integer limit;
    Integer total;

    QuerySummary() {

    }

    public Integer getLimit() {
        return limit;
    }
    
    public Integer getTotal() {
        return total;
    }

    public QuerySummary setLimit(Integer value) {
        limit = value;
        
        return this;
    }

    public QuerySummary setTotal(Integer value) {
        total = value;

        return this;
    }

}
