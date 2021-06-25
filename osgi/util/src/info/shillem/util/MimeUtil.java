package info.shillem.util;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MimeUtil {

    private MimeUtil() {
        throw new UnsupportedOperationException();
    }

    public static String getHeaderProperty(String name, String value) {
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(value, "Value cannot be null");

        Pattern pattern = Pattern.compile(
                name + "=['\"]*((?:.|\\s)+)['\"](?:;|$)*",
                Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(value);

        return m.find() ? m.group(1) : null;
    }

    public static String sanitizeFileName(String value) {
        return value.replaceAll("[^\\w-.]", "-");
    }

}
