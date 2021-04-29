package info.shillem.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;

public class MapResourceBundle extends ResourceBundle {

    private final Map<String, Object> lookup;

    public MapResourceBundle(Map<String, String> properties) {
        lookup = new HashMap<>(properties);
    }

    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(lookup.keySet());
    }

    @Override
    protected Object handleGetObject(String key) {
        Objects.requireNonNull(key);

        if (lookup.containsKey(key)) {
            return lookup.get(key);
        }

        if (parent != null) {
            return parent.getObject(key);
        }

        return String.format("[%s:%s]", key, getLocale().getLanguage());
    }

    @Override
    protected Set<String> handleKeySet() {
        return lookup.keySet();
    }

}
