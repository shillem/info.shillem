package info.shillem.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtil {

    public enum Case {
        KEBAB {
            @Override
            public String toKebabCase(String value) {
                return value;
            }
            
            @Override
            public String toLowerCamelCase(String value) {
                String v = toUpperCamelCase(value);

                return Character.toLowerCase(v.charAt(0)) + v.substring(1);
            }

            @Override
            public String toUpperCamelCase(String value) {
                return Stream.of(value.split("-"))
                        .map(v -> Character.toUpperCase(
                                v.charAt(0)) + (v.substring(1).toLowerCase()))
                        .collect(Collectors.joining());
            }
        };

        public abstract String toKebabCase(String value);
        
        public abstract String toLowerCamelCase(String value);

        public abstract String toUpperCamelCase(String value);
    }

    private StringUtil() {
        throw new UnsupportedOperationException();
    }

    public static String after(String value, String part) {
        int index = value.indexOf(part);

        return index < 0 ? value : value.substring(index + part.length());
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

    public static String concat(String s1, String... s2) {
        return StringUtil.concat(' ', s1, s2);
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

    public static String getDigest(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            digest.update(value.getBytes());

            StringBuilder builder = new StringBuilder();

            for (byte b : digest.digest()) {
                builder.append(String.format("%02x", b & 0xff));
            }

            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
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

    public static String padLeft(String value, int size, char c) {
        return padString(value, size, (b) -> b.insert(0, c));
    }

    public static String padRight(String value, int size, char c) {
        return padString(value, size, (b) -> b.append(c));
    }

    private static String padString(String value, int size, Consumer<StringBuilder> consumer) {
        StringBuilder builder = new StringBuilder(value);

        while (builder.length() < size) {
            consumer.accept(builder);
        }

        return builder.toString();
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
