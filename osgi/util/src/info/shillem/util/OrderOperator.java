package info.shillem.util;

public enum OrderOperator {
    ASCENDING(true), DESCENDING(false);
    
    private final boolean flag;
    
    OrderOperator(boolean flag) {
        this.flag = flag;
    }
    
    public boolean asBoolean() {
        return flag;
    }
}
