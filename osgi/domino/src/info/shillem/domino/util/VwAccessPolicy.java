package info.shillem.domino.util;

import java.util.Set;

public enum VwAccessPolicy {
    FRESH,
    STALE;

    public static VwAccessPolicy valueOf(Set<String> values) {
        return values.contains("FETCH_STALE") ? STALE : FRESH;
    }

}
