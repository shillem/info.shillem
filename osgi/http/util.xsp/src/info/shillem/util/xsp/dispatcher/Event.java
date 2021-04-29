package info.shillem.util.xsp.dispatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Event {
    
    public interface Id {
        String name();
    }

    private final Enum<? extends Id> id;
    
    private Map<String, Object> properties;

    public Event(Enum<? extends Id> id) {
        this.id = Objects.requireNonNull(id, "Id cannot be null");
        this.properties = new HashMap<>();
    }

    public Enum<? extends Id> getId() {
        return id;
    }

    public Object getProperty(String key) {        
        return properties.get(key);
    }

    public boolean is(Enum<? extends Id> id) {
        return this.id == id;
    }

    public Event setProperty(String key, Object value) {
        properties.put(key, value);

        return this;
    }
    
    @Override
    public String toString() {
        return id.toString() + " : " + properties.toString();
    }

}
