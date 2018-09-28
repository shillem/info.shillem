package info.shillem.domino.util;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DatabasePath implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Pattern PATH_END_PATTERN =
            Pattern.compile("\\.n[st]f$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SYNTAX_PATTERN =
            Pattern.compile("([^!]*)!!(.*)");

    private final String serverName;
    private final String filePath;

    public DatabasePath(String filePath) {
        Objects.requireNonNull(filePath);

        Matcher matcher = SYNTAX_PATTERN.matcher(filePath);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot match path: " + filePath);
        }

        this.serverName = matcher.group(1);
        this.filePath = formatFilePath(matcher.group(2));
    }

    public DatabasePath(String serverName, String filePath) {
        Objects.requireNonNull(serverName);
        Objects.requireNonNull(filePath);

        this.serverName = serverName;
        this.filePath = formatFilePath(filePath);
    }

    public DatabasePath(String[] filePath) {
        Objects.requireNonNull(filePath);

        if (filePath.length != 2) {
            throw new IllegalArgumentException(
                    "Path must contain a 2-value array with server name and path");
        }

        this.serverName = filePath[0];
        this.filePath = formatFilePath(filePath[1]);
    }

    private String formatFilePath(String filePath) {
        if (PATH_END_PATTERN.matcher(filePath).find()) {
            return filePath;
        }

        return filePath + ".nsf";
    }

    public String getApiPath() {
        return serverName + "!!" + filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getServerName() {
        return serverName;
    }

    public String getUrlPath() {
        return filePath.replace('\\', '/');
    }

}