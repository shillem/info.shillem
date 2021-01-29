package info.shillem.dao;

import java.util.Map;

import info.shillem.dto.BaseField;
import info.shillem.util.OrderOperator;

public class PageQuery<E extends Enum<E> & BaseField> extends Query<E> {

    private final int limit;
    private final Map<E, OrderOperator> sorters;

    private int offset;

    PageQuery(PageQueryBuilder<E> page) {
        super(page.base);

        limit = page.limit;
        offset = page.offset;
        sorters = page.sorters;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public Map<E, OrderOperator> getSorters() {
        return sorters;
    }
    
    public boolean isUnknownOffset() {
        return offset == Integer.MAX_VALUE;
    }

    public int recalculateOffset(int lastRow) {
        if (!isUnknownOffset()) {
            throw new IllegalStateException(
                    "Offset cannot be recalculated unless offset at build time equals to Integer.MAX_VALUE");
        }

        int modulus = lastRow % limit;

        offset = lastRow - (modulus > 0 ? modulus : limit);

        return offset;
    }

}
