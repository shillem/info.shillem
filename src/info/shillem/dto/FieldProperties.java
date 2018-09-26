package info.shillem.dto;

import java.io.Serializable;

public class FieldProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Class<? extends Serializable> type;

    public FieldProperties(Class<? extends Serializable> type) {
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

}
