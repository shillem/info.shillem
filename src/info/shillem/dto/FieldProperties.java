package info.shillem.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FieldProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Map<Class<? extends Serializable>, FieldProperties> ALL = new HashMap<>();

    private final Class<? extends Serializable> type;

    private FieldProperties(Class<? extends Serializable> type) {
        this.type = type;
    }

    public Class<? extends Serializable> getFullType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Serializable> getType() {
        return isList() ? (Class<? extends Serializable>) type.getComponentType() : type;
    }

    public boolean isList() {
        return type.isArray();
    }

    public static FieldProperties getInstance(Class<? extends Serializable> cls) {
        FieldProperties properties = ALL.get(cls);

        if (properties == null) {
            properties = new FieldProperties(cls);

            ALL.put(cls, properties);
        }

        return properties;
    }

}
