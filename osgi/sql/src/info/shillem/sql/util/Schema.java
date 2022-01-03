package info.shillem.sql.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class Schema {

    public static class Column {

        private final Table table;
        private final String name;
        private final ColumnAs nameAs;

        private String alias;

        Column(Table table, ColumnAs nameAs) {
            this.table = table;
            this.name = null;
            this.nameAs = nameAs;
        }

        Column(Table table, String name) {
            this.table = table;
            this.name = name;
            this.nameAs = null;
        }

        public String getAlias() {
            return alias;
        }

        public String getName() {
            return name;
        }

        public ColumnAs getNameAs() {
            return nameAs;
        }

        public Table getTable() {
            return table;
        }

        public boolean isAliased() {
            return alias != null;
        }

        public boolean isNameAs() {
            return nameAs != null;
        }

        Column setAlias(String value) {
            alias = value;

            return this;
        }

    }

    public static class ColumnAs {

        private final Object value;

        public ColumnAs(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

    }

    public class Table {

        private final String name;
        private final List<Column> columns;

        private String alias;
        private boolean frozen;

        Table(String name) {
            this.name = name;
            this.columns = new ArrayList<>();
        }

        private void checkFrozen() {
            if (frozen) {
                throw new IllegalStateException("Cannot add a column after the table was frozen");
            }
        }

        public Table column(ColumnAs nameAs, String alias) {
            checkFrozen();

            columns.add(new Column(this, nameAs).setAlias(alias));

            return this;
        }

        public Table column(String name) {
            return column(name, (String) null);
        }

        public Table column(String name, String alias) {
            Objects.requireNonNull(name, "Table column name cannot be null");

            checkFrozen();

            columns.add(new Column(this, name).setAlias(alias));

            return this;
        }

        public boolean containsColumn(Column column) {
            return columns.contains(column);
        }

        Table freeze() {
            frozen = true;

            return this;
        }

        public String getAlias() {
            return alias;
        }

        public String getName() {
            return name;
        }

        public boolean isAliased() {
            return alias != null;
        }

        Table setAlias(String value) {
            alias = value;

            return this;
        }

    }

    private final List<Table> tables;

    private boolean frozen;

    public Schema() {
        tables = new ArrayList<>();
    }

    public Optional<Column> findColumn(Predicate<Column> predicate) {
        Objects.requireNonNull(predicate, "Predicate cannot be null");

        for (Table table : tables) {
            Optional<Column> optional = table.columns.stream().filter(predicate).findFirst();

            if (optional.isPresent()) {
                return optional;
            }
        }

        return Optional.empty();
    }

    public Optional<Column> findColumnByAlias(String alias) {
        Objects.requireNonNull(alias, "Alias cannot be null");

        return findColumn((c) -> alias.equals(c.getAlias()));
    }

    public Optional<Table> findTable(Predicate<Table> predicate) {
        return tables.stream().filter(predicate).findFirst();
    }

    public Optional<Table> findTableByAlias(String alias) {
        Objects.requireNonNull(alias, "Alias cannot be null");

        return findTable((t) -> alias.equals(t.getAlias()));
    }

    public Schema freeze() {
        tables.forEach(Table::freeze);

        frozen = true;

        return this;
    }

    public Table table(String name) {
        return table(name, null);
    }

    public Table table(String name, String alias) {
        Objects.requireNonNull(alias, "Schema table namea cannot be null");

        if (frozen) {
            throw new IllegalStateException("Cannot add a table after the schema was frozen");
        }

        Table t = new Table(name).setAlias(alias);

        tables.add(t);

        return t;
    }

}
