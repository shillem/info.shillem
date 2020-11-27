package info.shillem.sql.util;

public interface TablePath {
    
    default String composeColumn(String name) {
        return getName() + "." + name;
    }

    default String composeFrom() {
        return composeFrom(null);
    }

    default String composeFrom(String alias) {
        return "FROM " + getName() + (alias != null ? " " + alias : "");
    }

    String getName();

}
