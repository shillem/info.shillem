package info.shillem.synchronizer.util;

public class Field {

    public enum Type {
        BOOLEAN, DATE, DECIMAL, DOUBLE, INTEGER, STRING
    }

    private final String name;
    private final Type type;

    public Field(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return name + ":" + type;
    }

}
