package info.shillem.domino.util;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DbPath implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Pattern PATH_END_PATTERN =
            Pattern.compile("\\.n[st]f$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SYNTAX_PATTERN =
            Pattern.compile("(.*)!![\\/]*(.+)");
    private static final Pattern SERVER_NAME_PATTERN =
            Pattern.compile("(?:CN=)*([^\\/]+)", Pattern.CASE_INSENSITIVE);

    private final String serverName;
    private final String filePath;

    public DbPath(String apiPath) {
        Objects.requireNonNull(apiPath, "Api path cannot be null");

        Matcher matcher = SYNTAX_PATTERN.matcher(apiPath);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot match path: ".concat(apiPath));
        }

        this.serverName = matcher.group(1);
        this.filePath = formatFilePath(matcher.group(2));
    }

    public DbPath(String serverName, String filePath) {
        Objects.requireNonNull(serverName, "Server name cannot be null");
        Objects.requireNonNull(filePath, "File path cannot be null");

        this.serverName = serverName;
        this.filePath = formatFilePath(filePath);
    }

    public DbPath(String[] apiPath) {
        Objects.requireNonNull(apiPath);

        if (apiPath.length != 2) {
            throw new IllegalArgumentException(
                    "Api path must contain a 2-value array with server name and path");
        }

        this.serverName = apiPath[0];
        this.filePath = formatFilePath(apiPath[1]);
    }

    private String formatFilePath(String filePath) {
        if (PATH_END_PATTERN.matcher(filePath).find()) {
            return filePath;
        }

        return filePath.concat(".nsf");
    }

    public String getApiPath() {
        return serverName.concat("!!").concat(filePath);
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFilePathAsUrl() {
        return "/" + filePath.replace('\\', '/');
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerNameAsUrl() {
        Matcher matcher = SERVER_NAME_PATTERN.matcher(serverName);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    @Override
    public String toString() {
        return getApiPath();
    }

}