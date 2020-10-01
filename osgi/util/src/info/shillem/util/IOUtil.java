package info.shillem.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

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

        if (others != null) {
            Arrays.stream(others).forEach(IOUtil::close);
        }
    }

    public static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        byte[] data = new byte[4096];
        int len;

        while ((len = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, len);
        }

        return buffer.toByteArray();
    }

}