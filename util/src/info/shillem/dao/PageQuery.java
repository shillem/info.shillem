package info.shillem.dao;

import java.util.Map;

import info.shillem.dto.BaseField;
import info.shillem.util.OrderOperator;

public class PageQuery<E extends Enum<E> & BaseField> extends Query<E> {

    private final boolean cached;
    private final int maxCount;
    private final Map<E, OrderOperator> sorters;

    private int offset;

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

    public boolean isUnknownOffset() {
        return offset == Integer.MAX_VALUE;
    }

    public int recalculateOffset(int lastRow) {
        if (!isUnknownOffset()) {
            throw new IllegalStateException(
                    "Offset cannot be recalculated unless offset at build time equals to Integer.MAX_VALUE");
        }

        int modulus = lastRow % maxCount;

        offset = lastRow - (modulus > 0 ? modulus : maxCount);

        return offset;
    }

}
