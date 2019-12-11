package info.shillem.dao;

import java.util.Map;

import info.shillem.dto.BaseField;
import info.shillem.util.OrderOperator;

public class PageQuery<E extends Enum<E> & BaseField> extends Query<E> {

    private final boolean cached;
    private final int maxCount;
    private final int offset;
    private final Map<E, OrderOperator> sorters;

    PageQuery(QueryBuilder<E> base, PageQueryBuilder<E> page) {
        super(base);

        cached = page.cached;
        maxCount = page.maxCount;
        offset = page.offset;
        sorters = page.sorters;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public int getOffset() {
        return offset;
    }

    public Map<E, OrderOperator> getSorters() {
        return sorters;
    }
    
    public boolean isCached() {
        return cached;
    }

}
