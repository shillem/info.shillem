package info.shillem.sql.util;

import java.util.Objects;

public class SelectColumn extends SelectQueryLinked implements IComponent {

    private final String name;

    private String alias;
    private String function;

    public SelectColumn(String name) {
        this.name = Objects.requireNonNull(name, "Column name cannot be null");
    }

    public String getName() {
        return name;
    }

    @Override
    public String output() {
        return findSchemaColumn(alias != null ? alias : name)
                .map((c) -> outputColumn(outputSchemaColumn(c), c.getAlias()))
                .orElseGet(() -> outputColumn(name, alias));
    }

    private String outputColumn(String name, String alias) {
        StringBuilder builder =
                new StringBuilder(function != null ? String.format(function, name) : name);

        if (alias != null && !isSelectNested()) {
            builder.append(" AS ").append(alias);
        }

        return builder.toString();
    }

    public SelectColumn withAlias(String alias) {
        this.alias = alias;

        return this;
    }

    public SelectColumn withFunction(String function) {
        this.function = Objects.requireNonNull(function, "Function cannot be null");

        return this;
    }

}