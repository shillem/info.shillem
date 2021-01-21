package info.shillem.domino.util;

import java.util.Set;

public enum ViewAccessPolicy {
    FRESH,
    STALE;

    public static ViewAccessPolicy valueOf(Set<String> values) {
        return values.contains("FETCH_STALE") ? STALE : FRESH;
    }

}
