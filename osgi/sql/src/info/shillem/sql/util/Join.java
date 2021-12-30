package info.shillem.sql.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class Join extends AJoin {

    public static class Column extends Instruction {

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
        public String output() {
            String left = getJoin()
                    .findSchemaColumn(acol)
                    .map(Schema.Column::getName)
                    .orElse(acol);

            String right = getJoin()
                    .findSchemaColumn(Optional.ofNullable(bcol).orElse(acol))
                    .map(Schema.Column::getName)
                    .orElse(Optional.ofNullable(bcol).orElse(acol));

            BiFunction<String, String, String> wrapper =
                    (fn, name) -> fn != null ? String.format(fn, name) : name;

            return new StringBuilder()
                    .append(wrapper.apply(getJoin().table.concat(".").concat(left), afun))
                    .append(" = ")
                    .append(wrapper.apply(btab.concat(".").concat(right), bfun))
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

    public static class CustomExpression extends Instruction {

        private final String value;

        public CustomExpression(String value) {
            this.value = Objects.requireNonNull(value, "Custome expression value cannot be null");
        }

        @Override
        public String output() {
            return value;
        }

    }

    public static abstract class Instruction {

        private Join join;

        public Join getJoin() {
            return join;
        }

        protected final void link(Join join) {
            if (this.join != null) {
                throw new IllegalStateException(
                        getClass().getName().concat(" is already linked to ")
                                .concat(join.getClass().getName()));
            }

            this.join = join;
        }

        public abstract String output();

    }

    public enum Type {
        INNER_JOIN,
        LEFT_JOIN;
    }

    private final List<Instruction> instructions;
    private final String table;
    private final SelectQuery innerTable;
    private final Type type;

    public Join(Type type, String table) {
        this(type, table, null);
    }

    public Join(Type type, String table, SelectQuery innerTable) {
        this.innerTable = innerTable;
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.table = Objects.requireNonNull(table, "Table cannot be null");
        this.instructions = new ArrayList<>();
    }

    public Instruction on(Instruction c) {
        c.link(this);

        instructions.add(c);

        return c;
    }

    public Column on(String acol, String btab) {
        return (Column) on(new Column(acol, btab));
    }

    public Column on(String acol, String btab, String bcol) {
        return (Column) on(new Column(acol, btab, bcol));
    }

    @Override
    public String output() {
        StringBuilder builder = new StringBuilder("\t")
                .append(outputType())
                .append(" ")
                .append(outputTable())
                .append(" ON ")
                .append(instructions.stream()
                        .map((i) -> i.output())
                        .collect(Collectors.joining("\n\t\tAND ")));

        return builder.toString();
    }

    private String outputTable() {
        if (innerTable != null) {
            return "(".concat(innerTable.output()).concat(") ").concat(table);
        }

        return findSchemaTable(table)
                .map((t) -> t.getName().concat(" ").concat(table))
                .orElse(table);
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
