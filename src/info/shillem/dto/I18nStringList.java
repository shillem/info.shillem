package info.shillem.dto;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class I18nStringList implements I18nValue, Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<String, String> map = new LinkedHashMap<>();

    public void add(I18nString string) {
        add(string.getValue(), string.getLabel());
    }

    public void add(I18nStringList stringList) {
        stringList.getMap().forEach((key, value) -> add(key, value));
    }

    public void add(I18nValue value) {
        if (value instanceof I18nString) {
            add((I18nString) value);
        } else {
            add((I18nStringList) value);
        }
    }

    public void add(String value, String label) {
        map.put(value, label);
    }

    public String getLabel(String value) {
        return map.get(value);
    }

    public Map<String, String> getMap() {
        return map;
    }

    @Override
    public String toString() {
        return map.toString();
    }

}
