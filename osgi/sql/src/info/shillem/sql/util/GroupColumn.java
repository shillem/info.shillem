package info.shillem.sql.util;

import java.util.Objects;

public class GroupColumn implements IGroup {

    private final String name;

    public GroupColumn(String name) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
    }

    @Override
    public String output(Schema schema) {
        return SelectQuery.getColumner(schema).apply(name);
    }

}
