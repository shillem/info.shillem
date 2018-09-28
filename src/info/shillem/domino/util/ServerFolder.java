package info.shillem.domino.util;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

public final class ServerFolder implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Pattern SYNTAX_SEPARATOR_PATTERN = Pattern.compile("[\\/]");
    private static final Pattern PATH_END_PATTERN = Pattern.compile("[\\/]$");

    private final String serverName;
    private final String folderPath;

    public ServerFolder(String serverName, String folderPath) {
        Objects.requireNonNull(serverName);
        Objects.requireNonNull(folderPath);

        this.serverName = serverName;
        this.folderPath = formatFolderPath(folderPath);
    }

    private String formatFolderPath(String folderPath) {
        if (PATH_END_PATTERN.matcher(folderPath).find()) {
            SYNTAX_SEPARATOR_PATTERN
                    .matcher(folderPath)
                    .replaceAll(File.separator);
        }

        return SYNTAX_SEPARATOR_PATTERN
                .matcher(folderPath)
                .replaceAll(File.separator)
                .concat(File.separator);
    }

    public String getFolderPath() {
        return folderPath;
    }

    public String getServerName() {
        return serverName;
    }

}