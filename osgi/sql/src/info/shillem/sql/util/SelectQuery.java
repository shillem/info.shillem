package info.shillem.sql.util;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import info.shillem.util.OrderOperator;

public class SelectQuery {

    public static class Column {

        private final String name;

        private String alias;
        private String function;

        public Column(String name) {
            this.name = Objects.requireNonNull(name, "Column name cannot be null");
        }

        public String getName() {
            return name;
        }

        public Column withAlias(String alias) {
            this.alias = alias;

            return this;
        }

        public Column withFunction(String function) {
            this.function = Objects.requireNonNull(function, "Function cannot be null");

            return this;
        }

    }

    abstract static class L<T> {

        protected List<T> instructions = new ArrayList<>();

        boolean isEmpty() {
            return instructions.isEmpty();
        }

        public abstract String output(Schema schema);

    }

    public static class LGroup extends L<IGroup> {

        LGroup() {

        }

        public LGroup add(IGroup instruction) {
            instructions.add(Objects.requireNonNull(instruction, "Instruction cannot be null"));

            return this;
        }

        @Override
        public String output(Schema schema) {
            return new StringBuilder()
                    .append(instructions.stream()
                            .map((i) -> i.output(schema))
                            .collect(Collectors.joining(", ")))
                    .toString();
        }

    }

    public static class LJoin extends L<IJoin> {

        LJoin() {

        }

        public LJoin add(IJoin instruction) {
            instructions.add(Objects.requireNonNull(instruction, "Instruction cannot be null"));

            return this;
        }

        @Override
        public String output(Schema schema) {
            return instructions.stream()
                    .map((i) -> i.output(schema))
                    .collect(Collectors.joining("\n"));
        }

    }

    public static class LOrder extends L<IOrder> {

        LOrder() {

        }

        public LOrder add(IOrder instruction) {
            instructions.add(Objects.requireNonNull(instruction, "Instruction cannot be null"));

            return this;
        }

        @Override
        public String output(Schema schema) {
            return new StringBuilder()
                    .append(instructions.stream()
                            .map((i) -> i.output(schema))
                            .collect(Collectors.joining(", ")))
                    .toString();
        }

    }

    public static class LWhere extends L<IWhere> {

        LWhere() {

        }

        public LWhere add(IWhere instruction, IWhere... instrs) {
            Objects.requireNonNull(instruction, "Instruction cannot be null");

            if (instruction instanceof WhereLogic) {
                if (!instructions.isEmpty()) {
                    instructions.add(instruction);
                }
            } else {
                instructions.add(instruction);
            }

            if (instrs != null) {
                for (IWhere instr : instrs) {
                    instructions.add(instr);
                }
            }

            return this;
        }

        public LWhere and(IWhere instruction) {
            add(WhereLogic.AND, instruction);

            return this;
        }

        public LWhere or(IWhere instruction) {
            add(WhereLogic.OR, instruction);

            return this;
        }

        @Override
        public String output(Schema schema) {
            StringBuilder builder = new StringBuilder();

            for (IWhere instruction : instructions) {
                if (instruction instanceof WhereLogic) {
                    builder
                            .append("\n\t")
                            .append(instruction.output(schema))
                            .append(" ");
                } else {
                    builder.append(instruction.output(schema));
                }
            }

            return builder.toString();
        }

    }

    private static final Logger LOGGER =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final List<Column> columns;
    private final String from;

    private Boolean distinct;
    private LGroup groups;
    private LJoin joins;
    private LOrder orders;
    private Schema schema;
    private Integer top;
    private LWhere wheres;
    private List<SelectQuery> unions;
    private String wrapper;

    public SelectQuery(String from) {
        this.columns = new ArrayList<>();
        this.from = Objects.requireNonNull(from, "From from cannot be null");
    }

    public SelectQuery column(Column column) {
        columns.add(Objects.requireNonNull(column, "Column cannot be null"));

        return this;
    }

    public SelectQuery column(String name) {
        return column(new Column(name));
    }

    public SelectQuery group(String name) {
        groups().add(new GroupColumn(name));

        return this;
    }

    public LGroup groups() {
        if (groups == null) {
            groups = new LGroup();
        }

        return groups;
    }

    public LJoin joins() {
        if (joins == null) {
            joins = new LJoin();
        }

        return joins;
    }

    public SelectQuery order(String name, OrderOperator operator) {
        orders().add(new OrderColumn(name, operator));

        return this;
    }

    public LOrder orders() {
        if (orders == null) {
            orders = new LOrder();
        }

        return orders;
    }

    public String output() {
        StringBuilder builder = new StringBuilder();

        builder
                .append(outputSelect())
                .append("\n")
                .append(outputFrom());

        if (joins != null && !joins.isEmpty()) {
            builder.append("\n").append(joins.output(schema));
        }

        if (wheres != null && !wheres.isEmpty()) {
            builder.append("\nWHERE ").append(wheres.output(schema));
        }

        if (groups != null && !groups.isEmpty()) {
            builder.append("\nGROUP BY ").append(groups.output(schema));
        }

        if (orders != null && !orders.isEmpty()) {
            builder.append("\nORDER BY ").append(orders.output(schema));
        }

        if (unions != null && !unions.isEmpty()) {
            for (SelectQuery q : unions) {
                builder.append("\nUNION ALL\n").append(q.output());
            }
        }

        if (wrapper != null) {
            builder.insert(0, "WITH ".concat(wrapper).concat(" AS (\n")).append(")");
        }

        String result = builder.toString();

        LOGGER.debug(result);

        return result;
    }

    public String outputFrom() {
        StringBuilder builder = new StringBuilder("FROM ");

        if (schema != null) {
            Schema.Table t = schema.getTable(from);

            if (t != null) {
                builder.append(t.getName()).append(" ").append(from);
            }
        } else {
            builder.append(from);
        }

        return builder.toString();
    }

    public String outputSelect() {
        StringBuilder builder = new StringBuilder("SELECT");

        if (distinct != null && distinct) {
            builder.append(" DISTINCT");
        }

        if (top != null) {
            builder.append(" TOP ").append(top);
        }

        if (columns.isEmpty()) {
            return builder.append("*").toString();
        }

        Function<Column, String> columner;

        if (schema != null) {
            columner = (selectColumn) -> {
                Schema.Column schemaColumn = schema.getColumn(selectColumn.getName());

                if (schemaColumn == null) {
                    return outputSelectColumn(
                            selectColumn.getName(),
                            selectColumn.alias,
                            selectColumn.function);
                }

                return outputSelectColumn(
                        joins != null
                                ? schema.getColumnTable(schemaColumn).getKey()
                                        .concat(".")
                                        .concat(schemaColumn.getName())
                                : schemaColumn.getName(),
                        Optional.ofNullable(selectColumn.alias).orElse(selectColumn.getName()),
                        selectColumn.function);
            };
        } else {
            columner = (c) -> outputSelectColumn(c.getName(), c.alias, c.function);
        }

        builder.append(columns.stream()
                .map(columner)
                .collect(Collectors.joining(",")));

        return builder.toString();
    }

    private String outputSelectColumn(String name, String alias, String function) {
        StringBuilder builder = new StringBuilder("\n\t");

        builder.append(function != null ? String.format(function, name) : name);

        if (alias != null) {
            builder.append(" AS ").append(alias);
        }

        return builder.toString();
    }

    public boolean requiresAnyTable(String name, String... names) {
        if (schema != null) {
            List<String> n = new ArrayList<>();

            n.add(name);

            if (names != null) {
                n.addAll(Arrays.asList(names));
            }

            for (Column col : columns) {
                Map.Entry<String, Schema.Table> t = schema.getColumnTable(col.getName());

                if (t != null && n.contains(t.getKey())) {
                    return true;
                }
            }
        }

        return false;
    }

    public SelectQuery distinct() {
        return distinct(true);
    }

    public SelectQuery distinct(Boolean value) {
        distinct = value;

        return this;
    }

    public SelectQuery top(Integer value) {
        top = value;

        return this;
    }

    public SelectQuery union(SelectQuery query) {
        if (unions == null) {
            unions = new ArrayList<>();
        }

        unions.add(query);

        return this;
    }

    public LWhere wheres() {
        if (wheres == null) {
            wheres = new LWhere();
        }

        return wheres;
    }

    public SelectQuery withSchema(Schema schema) {
        this.schema = schema;

        return this;
    }

    public SelectQuery wrap(String value) {
        wrapper = value;

        return this;
    }

    static Function<String, String> getColumner(Schema schema) {
        if (schema == null) {
            return Function.identity();
        }

        return (n) -> {
            Schema.Column c = schema.getColumn(n);

            if (c == null) {
                return n;
            }

            return new StringBuilder()
                    .append(schema.getColumnTable(c).getKey())
                    .append(".")
                    .append(c.getName())
                    .toString();
        };

    }

}
