package info.shillem.domino.util;

public enum ViewMatch {
    EXACT(true), PARTIAL(false);
    
    private final boolean flag;
    
    ViewMatch(boolean flag) {
        this.flag = flag;
    }
    
    public boolean isExact() {
        return flag;
    }
    
}
