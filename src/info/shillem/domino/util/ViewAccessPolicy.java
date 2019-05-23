package info.shillem.domino.util;

public enum ViewAccessPolicy {
	CACHE,
	REFRESH;
	
	public static ViewAccessPolicy withCache(boolean flag) {
	    return flag ? CACHE : REFRESH;
	}
	
}
