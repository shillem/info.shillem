package info.shillem.domino.util;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DatabasePath implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Pattern PATH_PATTERN = Pattern.compile("([^!]*)!!(.*)");

	private final String serverName;
	private final String filePath;

	public DatabasePath(String s) {
		if (s == null) {
			throw new NullPointerException("Path cannot be null");
		}
		
		Matcher matcher = PATH_PATTERN.matcher(s);

		if (!matcher.find()) {
			throw new IllegalArgumentException("Cannot match path " + s);
		}

		this.serverName = matcher.group(1);
		this.filePath = formatFilePath(matcher.group(2));
	}

	public DatabasePath(String serverName, String filePath) {
		if (serverName == null || filePath == null) {
			throw new NullPointerException("Path cannot be null");
		}
		
		this.serverName = serverName;
		this.filePath = formatFilePath(filePath);
	}

	public DatabasePath(String[] arr) {
		if (arr == null) {
			throw new NullPointerException("Path cannot be null");
		}
		
		if (arr.length != 2) {
			throw new IllegalArgumentException();
		}

		this.serverName = arr[0];
		this.filePath = formatFilePath(arr[1]);
	}

	private String formatFilePath(String filePath) {
		if (!filePath.toLowerCase().endsWith(".nsf")) {
			return filePath + ".nsf";
		}

		return filePath;

	}

	public String getServerName() {
		return serverName;
	}

	public String getFilePath() {
		return filePath;
	}

	public String getUrlPath() {
		return filePath.replace('\\', '/');
	}

	public String getApiPath() {
		return serverName + "!!" + filePath;
	}

}
