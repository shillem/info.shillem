package info.shillem.dao;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import info.shillem.dto.BaseField;
import info.shillem.util.OrderOperator;

class PageQueryBuilder<E extends Enum<E> & BaseField> {

    boolean cached;
    int maxCount;
    int offset;
    Map<E, OrderOperator> sorters;

    PageQueryBuilder() {
        sorters = new LinkedHashMap<>();
    }

    public int getMaxCount() {
        return maxCount;
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

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
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
