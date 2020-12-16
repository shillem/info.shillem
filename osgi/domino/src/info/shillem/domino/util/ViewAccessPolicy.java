package info.shillem.domino.util;

import java.util.Set;

import info.shillem.dao.QueryOption;
import info.shillem.domino.dao.DominoQueryOption;

public enum ViewAccessPolicy {
    FRESH,
	STALE;
	
	public static ViewAccessPolicy valueOf(Set<Enum<? extends QueryOption>> set) {
	    return set.contains(DominoQueryOption.FETCH_STALE) ? STALE : FRESH;
	}
	
}
