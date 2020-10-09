package info.shillem.dao;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import info.shillem.dto.BaseField;
import info.shillem.util.OrderOperator;

class PageQueryBuilder<E extends Enum<E> & BaseField> {

    boolean cached;
    int limit;
    int offset;
    Map<E, OrderOperator> sorters;
    boolean total;

    PageQueryBuilder() {
        sorters = new LinkedHashMap<>();
    }
    
    public void fetchTotal(boolean flag) {
        this.total = flag;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public boolean isCached() {
        return cached;
    }

    public void setCache(boolean flag) {
        this.cached = flag;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void sort(E field, OrderOperator order) {
        sorters.put(
                Objects.requireNonNull(field, "Field cannot be null"),
                Objects.requireNonNull(order, "Order cannot be null"));
    }

}
