package info.shillem.util;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MimeUtil {

    private MimeUtil() {
        throw new UnsupportedOperationException();
    }

    public static String getHeaderParam(String name, String value) {
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(value, "Value cannot be null");

        Pattern pattern = Pattern.compile(
                name.concat("=\\s*([\"'])*((?:(?!\\1).)*)\\1*"),
                Pattern.CASE_INSENSITIVE);

        return Stream.of(value.split(";"))
                .map((s) -> {
                    Matcher m = pattern.matcher(s);

                    return m.find() ? m.group(2) : null;
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public static String sanitizeFileName(String value) {
        return value.replaceAll("[^\\w-.]", "-");
    }

}
