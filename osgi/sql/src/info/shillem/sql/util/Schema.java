package info.shillem.sql.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Schema {

    public class Column {

        private final String name;

        Column(String name) {
            this.name = Objects.requireNonNull(name, "Column name cannot be null");
        }

        public String getName() {
            return name;
        }

    }

    public class Table {

        private final String name;
        private final Map<String, Column> columns;

        private boolean frozen;

        Table(String name) {
            this.name = Objects.requireNonNull(name, "Table name cannot be null");
            this.columns = new HashMap<>();
        }

        public Table column(String name) {
            return column(name, name);
        }

        public Table column(String alias, String name) {
            if (frozen) {
                throw new IllegalStateException("Cannot add a column after the table was frozen");
            }

            columns.put(
                    Objects.requireNonNull(alias, "Table column alias cannot be null"),
                    new Column(name));

            return this;
        }

        public boolean containsColumn(Column column) {
            return columns.containsValue(column);
        }

        Table freeze() {
            frozen = true;

            return this;
        }

        public Column getColumn(String alias) {
            return columns.get(alias);
        }

        public String getName() {
            return name;
        }

    }

    private final Map<String, Table> tables;

    private boolean frozen;

    public Schema() {
        tables = new HashMap<>();
    }

    public Schema freeze() {
        tables.values().forEach(Table::freeze);

        frozen = true;

        return this;
    }

    public Column getColumn(String alias) {
        for (Table table : tables.values()) {
            Column col = table.getColumn(alias);

            if (col != null) {
                return col;
            }
        }

        return null;
    }

    public Map.Entry<String, Table> getColumnTable(Column col) {
        for (Map.Entry<String, Table> entry : tables.entrySet()) {
            if (entry.getValue().containsColumn(col)) {
                return entry;
            }
        }

        return null;
    }

    public Map.Entry<String, Table> getColumnTable(String alias) {
        for (Map.Entry<String, Table> entry : tables.entrySet()) {
            if (entry.getValue().getColumn(alias) != null) {
                return entry;
            }
        }

        return null;
    }

    public Table getTable(String alias) {
        return tables.get(alias);
    }

    public Table table(String alias, String name) {
        if (frozen) {
            throw new IllegalStateException("Cannot add a table after the schema was frozen");
        }

        Objects.requireNonNull(alias, "Schema table alias cannot be null");

        Table t = new Table(name);

        tables.put(alias, t);

        return t;
    }

}
