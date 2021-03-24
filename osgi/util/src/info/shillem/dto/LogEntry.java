package info.shillem.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class LogEntry implements Comparable<LogEntry>, JsonValue, Serializable {

    public interface Property {
        String name();
    }

    private static final long serialVersionUID = 1L;

    private final long timestamp;

    private transient Date date;
    private Map<String, Object> properties;

    public LogEntry() {
        this(System.currentTimeMillis());
    }

    public LogEntry(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(LogEntry e) {
        return (int) (e.timestamp - timestamp);
    }

    public Date getDate() {
        if (date == null) {
            date = new Date(timestamp);
        }

        return date;
    }

    public Map<String, Object> getProperties() {
        return properties != null ? properties : Collections.emptyMap();
    }

    public Object getProperty(Property name) {
        return getProperties().get(name.name());
    }

    public long getTimestamp() {
        return timestamp;
    }

    public LogEntry setProperty(Property name, Object value) {
        if (properties == null) {
            properties = new LinkedHashMap<>();
        }

        properties.put(name.name(), value);

        return this;
    }

}
