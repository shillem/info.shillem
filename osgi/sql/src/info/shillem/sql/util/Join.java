package info.shillem.sql.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import info.shillem.sql.util.Schema.Table;

public class Join implements IJoin {

    public class Column implements Instruction {

        private final String acol;
        private final String btab;
        private final String bcol;

        private String afun;
        private String bfun;

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
            BiFunction<String, String, String> columner;

            if (schema != null) {
                columner = (n, fn) -> {
                    String name = Optional
                            .ofNullable(schema.getColumn(n))
                            .map(Schema.Column::getName)
                            .orElse(n);

                    return fn != null ? String.format(fn, name) : name;
                };
            } else {
                columner = (n, fn) -> fn != null ? String.format(fn, n) : n;
            }

            return new StringBuilder()
                    .append(columner.apply(table.concat(".").concat(acol), afun))
                    .append(" = ")
                    .append(columner.apply(btab.concat(".")
                            .concat(Optional.ofNullable(bcol).orElse(acol)), bfun))
                    .toString();
        }

        private String validateFunction(String function) {
            if (function != null && !function.contains("%s")) {
                throw new IllegalArgumentException(
                        "Function must contain '%s' token for replacement");
            }

            return function;
        }

        public Column withDestinationFunction(String function) {
            this.bfun = validateFunction(function);

            return this;
        }

        public Column withSourceFunction(String function) {
            this.afun = validateFunction(function);

            return this;
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

    private Column on(Column c) {
        instructions.add(c);
        
        return c;        
    }
    
    public Column on(String acol, String btab) {
        return on(new Column(acol, btab));
    }
    
    public Column on(String acol, String btab, String bcol) {
        return on(new Column(acol, btab, bcol));
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
