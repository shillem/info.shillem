package info.shillem.util;

import java.util.Vector;

public enum ListUtil {
	;
	
	public static Vector<Object> toVector(Object... objects) {
		if (objects.length == 0) {
			throw new NullPointerException("Vectorization requires at least one object");
		}

		Vector<Object> vector = new Vector<>();

		for (Object o : objects) {
			vector.add(o);
		}

		return vector;
	}
	
}
