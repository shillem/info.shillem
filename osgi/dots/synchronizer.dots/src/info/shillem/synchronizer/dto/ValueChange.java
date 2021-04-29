package info.shillem.synchronizer.dto;

import java.util.Optional;

public class ValueChange {

    private final Object before;
    private final Object after;

    public ValueChange(Object before, Object after) {
        this.before = before;
        this.after = after;
    }

    public Object getAfter() {
        return after;
    }

    public Object getBefore() {
        return before;
    }

    public String toString() {
        return String.format("[from]=%s (%s) [to]=%s (%s)",
                String.valueOf(getBefore()), Optional
                        .ofNullable(getBefore()).map((val) -> val.getClass().getName())
                        .orElse("null"),
                String.valueOf(getAfter()), Optional
                        .ofNullable(getAfter()).map((val) -> val.getClass().getName())
                        .orElse("null"));
    }

}
