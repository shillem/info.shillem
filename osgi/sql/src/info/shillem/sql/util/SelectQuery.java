package info.shillem.sql.util;

import java.lang.invoke.MethodHandles;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.shillem.sql.util.Schema.Column;
import info.shillem.util.OrderOperator;

public class SelectQuery {

    abstract static class L<T extends IComponent> extends SelectQueryLinked {

        protected final List<T> instructions;
        protected final String delimiter;

        L(String delimiter) {
            this.instructions = new ArrayList<>();
            this.delimiter = delimiter;
        }

        L<T> addInstruction(T instruction) {
            instructions.add(Objects.requireNonNull(instruction, "Instruction cannot be null"));

            if (instruction instanceof SelectQueryLinked) {
                ((SelectQueryLinked) instruction).link(getSelect());
            }

            return this;
        }

        boolean isEmpty() {
            return instructions.isEmpty();
        }

        public String output() {
            return instructions.stream()
                    .map((i) -> i.output())
                    .collect(Collectors.joining(delimiter));
        }

    }

    public static class LGroup extends L<AGroup> {

        LGroup() {
            super(", ");
        }

        public LGroup add(AGroup instruction) {
            return (LGroup) addInstruction(instruction);
        }

    }

    public static class LJoin extends L<AJoin> {

        LJoin() {
            super("\n");
        }

        public LJoin add(AJoin instruction) {
            return (LJoin) addInstruction(instruction);
        }

    }

    public static class LOrder extends L<AOrder> {

        LOrder() {
            super(", ");
        }

        public LOrder add(AOrder instruction) {
            return (LOrder) addInstruction(instruction);
        }

    }

    public static class LWhere extends L<AWhere> {

        LWhere() {
            super(" ");
        }

        public LWhere add(AWhere instruction, AWhere... instrs) {
            Objects.requireNonNull(instruction, "Instruction cannot be null");

            if (instruction instanceof WhereLogic) {
                if (!isEmpty()) {
                    addInstruction(instruction);
                }
            } else {
                addInstruction(instruction);
            }

            if (instrs != null) {
                for (AWhere instr : instrs) {
                    addInstruction(instr);
                }
            }

            return this;
        }

        public LWhere and(AWhere instruction) {
            add(WhereLogic.AND, instruction);

            return this;
        }

        @Override
        public void link(SelectQuery select) {
            super.link(select);

            instructions.forEach((i) -> i.link(select));
        }

        public LWhere or(AWhere instruction) {
            add(WhereLogic.OR, instruction);

            return this;
        }

        @Override
        public String output() {
            StringBuilder builder = new StringBuilder();

            for (AWhere instruction : instructions) {
                if (instruction instanceof WhereLogic) {
                    builder
                            .append("\n\t")
                            .append(instruction.output())
                            .append(delimiter);
                } else {
                    builder.append(instruction.output());
                }
            }

            return builder.toString();
        }

    }

    static final DateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final Logger LOGGER =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final List<SelectColumn> columns;
    private final String from;

    private Boolean distinct;
    private LGroup groups;
    private LJoin joins;
    private boolean nested;
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

    public SelectQuery column(SelectColumn column) {
        columns.add(Objects.requireNonNull(column, "Column cannot be null"));
        column.link(this);

        return this;
    }

    public SelectQuery column(String name) {
        return column(new SelectColumn(name));
    }

    public SelectQuery distinct() {
        return distinct(true);
    }

    public SelectQuery distinct(Boolean value) {
        distinct = value;

        return this;
    }

    Optional<Schema.Column> findSchemaColumn(String identifier) {
        if (schema == null) {
            return Optional.empty();
        }

        return schema.findColumnByAlias(identifier);
    }

    Optional<Schema.Table> findSchemaTable(String identifier) {
        if (schema == null) {
            return Optional.empty();
        }

        return schema.findTableByAlias(identifier);
    }

    public SelectQuery group(String name) {
        groups().add(new GroupColumn(name));

        return this;
    }

    public LGroup groups() {
        if (groups == null) {
            groups = new LGroup();
            groups.link(this);
        }

        return groups;
    }

    boolean isNested() {
        return nested;
    }

    public LJoin joins() {
        if (joins == null) {
            joins = new LJoin();
            joins.link(this);
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
            orders.link(this);
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
            builder.append("\n").append(joins.output());
        }

        if (wheres != null && !wheres.isEmpty()) {
            builder.append("\nWHERE ").append(wheres.output());
        }

        if (groups != null && !groups.isEmpty()) {
            builder.append("\nGROUP BY ").append(groups.output());
        }

        if (orders != null && !orders.isEmpty()) {
            builder.append("\nORDER BY ").append(orders.output());
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

    private String outputFrom() {
        StringBuilder builder = new StringBuilder("FROM ");

        if (schema != null) {
            findSchemaTable(from).ifPresent((t) -> builder.append(t.getName()).append(" "));
        }

        return builder.append(from).toString();
    }

    private String outputSelect() {
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

        builder.append("\n").append(columns.stream()
                .map((c) -> c.output())
                .collect(Collectors.joining(",\n")));

        return builder.toString();
    }

    public boolean requiresAnyTable(String name, String... names) {
        if (schema != null) {
            List<String> n = new ArrayList<>();

            n.add(name);

            if (names != null) {
                n.addAll(Arrays.asList(names));
            }

            for (SelectColumn col : columns) {
                Optional<Column> opt = schema.findColumnByAlias(col.getName());

                if (opt.isPresent() && n.contains(opt.get().getTable().getAlias())) {
                    return true;
                }
            }
        }

        return false;
    }

    void setAsNested() {
        nested = true;
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
            wheres.link(this);
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

}
