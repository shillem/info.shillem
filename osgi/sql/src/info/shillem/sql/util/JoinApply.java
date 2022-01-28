package info.shillem.sql.util;

import java.util.Objects;

public class JoinApply extends AJoin {

    public enum Type {
        CROSS_APPLY,
        OUTER_APPLY;
    }

    private final String alias;
    private final SelectQuery innerTable;
    private final Type type;

    public JoinApply(Type type, SelectQuery innerTable, String alias) {
        this.alias = Objects.requireNonNull(alias, "Alias cannot be null");
        this.innerTable = Objects.requireNonNull(innerTable, "Table cannot be null");
        this.type = Objects.requireNonNull(type, "Type cannot be null");
    }

    @Override
    public String output() {
        StringBuilder builder = new StringBuilder("\t")
                .append(outputType())
                .append(" ")
                .append(outputTable());

        return builder.toString();
    }

    private String outputTable() {
        return "(".concat(innerTable.output()).concat(") ").concat(alias);
    }

    private String outputType() {
        switch (type) {
        case CROSS_APPLY:
            return "CROSS APPLY";
        case OUTER_APPLY:
            return "OUTER APPLY";
        default:
            throw new UnsupportedOperationException(type.name());
        }
    }

}
