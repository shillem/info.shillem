package info.shillem.util;

import java.io.Closeable;

public enum IOUtil {
	;
	
	public static void close(Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (Exception e) {
				// Do nothing
			}
		}
	}

	public static void close(Closeable c, Closeable... others) {
		close(c);

		for (Closeable o : others) {
			close(o);
		}
	}
}
