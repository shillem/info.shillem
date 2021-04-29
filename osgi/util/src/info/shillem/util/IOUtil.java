package info.shillem.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class IOUtil {
    
    private IOUtil() {
        throw new UnsupportedOperationException();
    }

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

    public static byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;

        while ((len = is.read(buffer)) > 0) {
            os.write(buffer, 0, len);
        }

        return os.toByteArray();
    }

    public static <T> File toZipFile(
            String name,
            List<T> values,
            Function<T, String> namer,
            Function<T, InputStream> streamer) {
        File file = null;

        try {
            file = File.createTempFile(name.concat("-"), ".zip");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (
                FileOutputStream fos = new FileOutputStream(file);
                ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (T value : values) {
                zos.putNextEntry(new ZipEntry(namer.apply(value)));

                try (InputStream in = streamer.apply(value)) {
                    byte[] buffer = new byte[1024];
                    int len;

                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                }

                zos.closeEntry();
            }

            return file;
        } catch (Exception e) {
            if (file != null) {
                file.delete();
            }

            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }

            throw new RuntimeException(e);
        }
    }

}
