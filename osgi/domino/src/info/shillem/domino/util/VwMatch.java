package info.shillem.domino.util;

public enum VwMatch {
    EXACT(true), PARTIAL(false);
    
    private final boolean flag;
    
    VwMatch(boolean flag) {
        this.flag = flag;
    }
    
    public boolean asBoolean() {
        return flag;
    }
    
}
