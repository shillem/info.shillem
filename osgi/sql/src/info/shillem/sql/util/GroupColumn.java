package info.shillem.sql.util;

import java.util.Objects;

public class GroupColumn extends AGroup {

    private final String name;

    public GroupColumn(String name) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
    }

    @Override
    public String output() {
        return findSchemaColumn(name)
                .map(this::outputSchemaColumn)
                .orElse(name);
    }

}
