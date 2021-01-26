package info.shillem.util;

import java.util.Objects;

public class StringUtil {

    private StringUtil() {
        throw new UnsupportedOperationException();
    }

    public static String concat(String s1, String... s2) {
        return StringUtil.concat(' ', s1, s2);
    }

    public static String concat(char separator, String s1, String... s2) {
        StringBuilder builder = new StringBuilder();

        if (s1 != null)
            builder.append(s1);

        for (String s : s2) {
            if (s != null) {
                if (builder.length() > 0 && builder.charAt(builder.length() - 1) != separator) {
                    builder.append(separator);
                }

                builder.append(s);
            }
        }

        if (builder.length() > 0) {
            return builder.toString();
        }

        return null;
    }

    public static Enum<?> enumFromString(Class<?> cls, String s) throws IllegalArgumentException {
        Objects.requireNonNull(cls, "Class cannot be null");

        if (StringUtil.isEmpty(s)) {
            return null;
        }

        for (Object constant : cls.getEnumConstants()) {
            Enum<?> enumConstant = (Enum<?>) constant;

            if (enumConstant.name().equals(s)) {
                return enumConstant;
            }
        }

        throw new IllegalArgumentException(
                String.format("Cannot resolve enum %s from string %s", cls.getName(), s));
    }

    private static String firstCharToCase(String s, char c) {
        return c + s.substring(1);
    }

    public static String firstCharToLowerCase(String s) {
        return firstCharToCase(s, Character.toLowerCase(s.charAt(0)));
    }

    public static String firstCharToUpperCase(String s) {
        return firstCharToCase(s, Character.toUpperCase(s.charAt(0)));
    }

    public static String getExtension(String s) {
        int index = s.lastIndexOf('.');

        if (index < 0) {
            return "";
        }

        return s.substring(index + 1);
    }

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    public static String right(String s, int len) {
        if (isEmpty(s)) {
            return s;
        }

        return s.substring(Math.max(0, s.length() - len));
    }

    public static String toSafeName(String s) {
        if (isEmpty(s)) {
            return s;
        }

        return s.replaceAll("[^a-zA-Z0-9-]", "-").toLowerCase();
    }
}
