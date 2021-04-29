package info.shillem.domino.util;

public enum DeletionType {
    HARD(true), SOFT(false);
    
    private final boolean flag;
    
    DeletionType(boolean flag) {
        this.flag = flag;
    }
    
    public boolean asBoolean() {
        return flag;
    }
    
}
