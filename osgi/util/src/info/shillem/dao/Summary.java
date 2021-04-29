package info.shillem.dao;

public class Summary {

    private final Integer limit;

    private Integer offset;
    private Integer total;

    public Summary(int limit, int offset) {
        this.limit = limit;
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public Integer getTotal() {
        return total;
    }

    public boolean isMaxOffset() {
        return offset == Integer.MAX_VALUE;
    }

    public Integer recalculateOffset(int lastRow) {
        if (!isMaxOffset()) {
            throw new IllegalStateException(
                    "Offset cannot be recalculated unless it equals to Integer.MAX_VALUE");
        }

        int modulus = lastRow % limit;

        offset = lastRow - (modulus > 0 ? modulus : limit);

        return offset;
    }

    public void setTotal(Integer value) {
        total = value;
    }

}
