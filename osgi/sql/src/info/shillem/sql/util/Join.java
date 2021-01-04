package info.shillem.sql.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import info.shillem.sql.util.Schema.Table;

public class Join implements IJoin {

    class Column implements Instruction {

        private final String acol;
        private final String btab;
        private final String bcol;

        Column(String acol, String btab) {
            this(acol, btab, null);
        }

        Column(String acol, String btab, String bcol) {
            this.acol = Objects.requireNonNull(acol, "Join source column cannot be null");
            this.btab = Objects.requireNonNull(btab, "Join destination table cannot be null");
            this.bcol = bcol;
        }

        @Override
        public String output(Schema schema) {
            Function<String, String> columner;

            if (schema != null) {
                columner = (n) -> {
                    Schema.Column c = schema.getColumn(n);

                    return c != null ? c.getName() : n;
                };
            } else {
                columner = (n) -> n;
            }

            return new StringBuilder()
                    .append(table)
                    .append(".")
                    .append(columner.apply(acol))
                    .append(" = ")
                    .append(btab)
                    .append(".")
                    .append(columner.apply(Optional.ofNullable(bcol).orElse(acol)))
                    .toString();
        }

    }

    interface Instruction {

        String output(Schema schema);

    }

    private final List<Instruction> instructions;
    private final String table;
    private final SelectQuery tableCustom;
    private final Type type;

    public Join(Type type, String table) {
        this(type, Objects.requireNonNull(table, "Table cannot be null"), null);
    }

    public Join(Type type, String table, SelectQuery tableCustom) {
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.table = Objects.requireNonNull(table, "Table cannot be null");
        this.tableCustom = tableCustom;

        this.instructions = new ArrayList<>();
    }

    public Join on(String table, String column) {
        instructions.add(new Column(column, table));

        return this;
    }

    @Override
    public String output(Schema schema) {
        StringBuilder builder = new StringBuilder("\t")
                .append(outputType())
                .append(" ")
                .append(outputTable(schema))
                .append(" ON ")
                .append(instructions.stream()
                        .map((i) -> i.output(schema))
                        .collect(Collectors.joining("\n\t\tAND ")));

        return builder.toString();
    }

    private String outputTable(Schema schema) {
        if (tableCustom != null) {
            return "(".concat(tableCustom.output()).concat(") ").concat(table);
        }

        if (schema != null) {
            Table t = schema.getTable(table);

            if (t != null) {
                return t.getName().concat(" ").concat(table);
            }
        }

        return table;
    }

    private String outputType() {
        switch (type) {
        case INNER_JOIN:
            return "INNER JOIN";
        case LEFT_JOIN:
            return "LEFT JOIN";
        default:
            throw new UnsupportedOperationException(type.name());
        }
    }

}
