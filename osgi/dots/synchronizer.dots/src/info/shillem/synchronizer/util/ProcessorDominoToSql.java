package info.shillem.synchronizer.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import info.shillem.domino.util.DominoUtil;
import info.shillem.domino.util.ViewAccessPolicy;
import info.shillem.synchronizer.dots.Program.Nature;
import info.shillem.synchronizer.dto.Record;
import info.shillem.synchronizer.dto.ValueChange;
import info.shillem.synchronizer.lang.ProcessorException;
import info.shillem.synchronizer.util.ProcessorHelper.Mode;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;

public class ProcessorDominoToSql<T extends Record> extends Processor<T> {

    private class SqlColumn {

        private final String name;
        private final int precision;
        private boolean nullable;

        public SqlColumn(String name, int precision, int nullable) {
            this.name = name;
            this.precision = precision;
            this.nullable = nullable > 0;
        }

        @Override
        public String toString() {
            return name + " " + precision + " " + nullable;
        }

    }

    private class SqlHelper implements AutoCloseable {

        private final int batchSize;
        private final Map<Object, T> batch;
        private final Connection conn;

        private String columnNames;
        private String columnValuePlaceholders;
        private PreparedStatement insertStatement;
        private PreparedStatement updateStatement;
        private Map<String, SqlColumn> columnMetas;

        private SqlHelper(Connection conn) throws SQLException {
            this(300, conn);
        }

        private SqlHelper(int batchSize, Connection conn)
                throws SQLException {
            this.batchSize = batchSize;
            this.batch = new HashMap<>(batchSize);
            this.conn = conn;

            this.conn.setAutoCommit(false);
        }

        @Override
        public void close() {
            if (insertStatement != null) {
                try {
                    insertStatement.close();
                } catch (SQLException e) {
                    // Do nothing
                }
            }

            if (updateStatement != null) {
                try {
                    updateStatement.close();
                } catch (SQLException e) {
                    // Do nothing
                }
            }
        }

        public void commit() throws SQLException {
            if (batch.isEmpty()) {
                return;
            }

            List<Object> batchKeys = new ArrayList<>(batch.keySet());

            try (PreparedStatement readStatement = newReadStatement()) {
                for (int i = 0; i < batchKeys.size(); i++) {
                    readStatement.setObject(i + 1, batchKeys.get(i));
                }

                try (ResultSet resultSet = readStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Object keyValue = transformValue(resultSet.getObject(
                                getKeyField().getName()),
                                getKeyField().getType());

                        T record = batch.get(keyValue);

                        if (record != null) {
                            Map<String, ValueChange> changes = pushRecord(record, resultSet);

                            if (!changes.isEmpty()) {
                                PreparedStatement updateStatement =
                                        prepareStatement(getUpdateStatement(), record);

                                updateStatement.setObject(
                                        helper.getFieldPairs().size() + 1, keyValue);

                                updateStatement.addBatch();

                                helper.getTracker().addModified();
                            }

                            batch.remove(keyValue);
                        }
                    }

                    if (!batch.isEmpty()) {
                        if (helper.getRecordPolicy() == RecordPolicy.UPSERT) {
                            for (T record : batch.values()) {
                                prepareStatement(getInsertStatement(), record).addBatch();

                                helper.getTracker().addCreated();
                            }
                        } else {
                            batch.forEach((key, value) -> helper.getTracker().addSkipped());
                        }
                    }

                    if (!helper.isMode(Mode.TEST)) {
                        boolean commit = false;

                        if (insertStatement != null
                                && insertStatement.executeBatch().length > 0) {
                            insertStatement.executeBatch();
                            commit = true;
                        }

                        if (updateStatement != null
                                && updateStatement.executeBatch().length > 0) {
                            updateStatement.executeBatch();
                            commit = true;
                        }

                        if (commit) {
                            conn.commit();
                        }
                    }
                }
            }

            batch.clear();
        }

        private Map<String, SqlColumn> getColumnMetas() throws SQLException {
            if (columnMetas == null) {
                Map<String, SqlColumn> columnMetas = new LinkedHashMap<>();

                try (ResultSet columns = conn.getMetaData().getColumns(
                        null, null, helper.getQueryReferenceTable(), null)) {
                    while (columns.next()) {

                        SqlColumn col = new SqlColumn(
                                columns.getString("COLUMN_NAME"),
                                columns.getInt("COLUMN_SIZE"),
                                columns.getInt("NULLABLE"));

                        columnMetas.put(col.name, col);
                    }
                }

                this.columnMetas = columnMetas;
            }

            return columnMetas;
        }

        private String getColumnNames() {
            if (columnNames == null) {
                columnNames = helper.getFieldPairs().stream()
                        .map((pair) -> pair.getTo().getName())
                        .collect(Collectors.joining(", "));
            }

            return columnNames;
        }

        private String getColumnValuePlaceholders() {
            if (columnValuePlaceholders == null) {
                columnValuePlaceholders = helper.getFieldPairs().stream()
                        .map((pair) -> "?")
                        .collect(Collectors.joining(", "));
            }

            return columnValuePlaceholders;
        }

        private PreparedStatement getInsertStatement() throws SQLException {
            if (insertStatement == null) {
                insertStatement = conn.prepareStatement(
                        String.format("INSERT INTO %s (%s) VALUES (%s)",
                                helper.getQueryReferenceTable(),
                                getColumnNames(),
                                getColumnValuePlaceholders()));
            }

            return insertStatement;
        }

        private PreparedStatement getUpdateStatement() throws SQLException {
            if (updateStatement == null) {
                updateStatement = conn.prepareStatement(
                        String.format("UPDATE %s SET %s WHERE %s = ?",
                                helper.getQueryReferenceTable(),
                                helper.getFieldPairs().stream()
                                        .map((pair) -> pair.getTo().getName() + " = ?")
                                        .collect(Collectors.joining(", ")),
                                getKeyField().getName()));
            }

            return updateStatement;
        }

        private PreparedStatement newReadStatement() throws SQLException {
            return conn.prepareStatement(
                    String.format("SELECT %s FROM %s WHERE %s IN (?)",
                            getColumnNames(),
                            helper.getQueryReferenceTable(),
                            getKeyField().getName())
                            .replace("?", batch.keySet()
                                    .stream()
                                    .map((key) -> "?")
                                    .collect(Collectors.joining(", "))));
        }

        private PreparedStatement prepareStatement(PreparedStatement statement, T record)
                throws SQLException {
            List<FieldPair> pairs = helper.getFieldPairs();
            Map<String, SqlColumn> columnMetas = getColumnMetas();

            for (int i = 0; i < pairs.size(); i++) {
                Field field = pairs.get(i).getTo();

                statement.setObject(i + 1, secureValue(field, record, columnMetas));
            }

            return statement;
        }

        public void queue(T record) throws SQLException {
            batch.put(record.getValue(getKeyField().getName()), record);

            if (batch.size() >= batchSize) {
                commit();
            }
        }

        public Object secureValue(Field field, T record, Map<String, SqlColumn> columnMetas) {
            Object value = record.getValue(field.getName());
            SqlColumn col = columnMetas.get(field.getName());

            if (value == null && !col.nullable) {
                switch (field.getType()) {
                case DECIMAL:
                    return BigDecimal.ZERO;
                case DOUBLE:
                    return 0.0D;
                case INTEGER:
                    return 0;
                case STRING:
                    return "";
                default:
                    return value;
                }
            }

            if (value instanceof String) {
                String stringValue = (String) value;

                if (stringValue.length() > col.precision) {
                    return stringValue.substring(0, col.precision);
                }
            }

            return value;
        }

    }

    public ProcessorDominoToSql(ProcessorHelper helper, Supplier<T> recordSupplier) {
        super(helper, recordSupplier);
    }

    protected void afterExecution() throws ProcessorException {

    }

    protected void beforeExecution() throws ProcessorException {

    }

    @Override
    public final boolean execute() throws ProcessorException {
        beforeExecution();

        try (SqlHelper sqlHelper = new SqlHelper(helper.getSqlFactory().getConnection())) {
            helper.logMessage("Processing records...");

            View view = getView(ViewAccessPolicy.FRESH);
            Document doc = null;

            try {
                doc = view.getFirstDocument();

                while (doc != null) {
                    if (helper.isExecutionCanceled()) {
                        return false;
                    }

                    T record = newRecord();

                    pullDocument(doc, record);

                    sqlHelper.queue(record);

                    Document temp = view.getNextDocument(doc);
                    DominoUtil.recycle(doc);
                    doc = temp;
                }

                sqlHelper.commit();
            } finally {
                DominoUtil.recycle(doc);
            }
        } catch (NotesException | SQLException e) {
            throw new RuntimeException(e);
        }

        afterExecution();

        return true;
    }

    @Override
    public final boolean isNature(Nature nature) {
        return nature == Nature.DOMINO_TO_SQL;
    }

    protected void pullDocument(Document doc, T record) throws ProcessorException {
        for (FieldPair pair : helper.getFieldPairs()) {
            Field from = pair.getFrom();
            Field to = pair.getTo();

            try {
                switch (from.getType()) {
                case BOOLEAN:
                    record.setValue(to.getName(), DominoUtil.getItemBoolean(doc, from.getName()));

                    break;
                case DATE:
                    record.setValue(to.getName(), DominoUtil.getItemDate(doc, from.getName()));

                    break;
                case DECIMAL:
                    record.setValue(to.getName(), DominoUtil.getItemDecimal(doc, from.getName()));

                    break;
                case DOUBLE:
                    record.setValue(to.getName(), DominoUtil.getItemDouble(doc, from.getName()));

                    break;
                case INTEGER:
                    record.setValue(to.getName(), DominoUtil.getItemInteger(doc, from.getName()));

                    break;
                case STRING:
                    record.setValue(to.getName(), DominoUtil.getItemString(doc, from.getName()));

                    break;
                }
            } catch (Exception e) {
                throw new RuntimeException(String.format(
                        "Error while pulling %s to %s", from, to), e);
            }
        }
    }

    protected Map<String, ValueChange> pushRecord(T record, ResultSet resultSet) {
        Map<String, ValueChange> changes = new HashMap<>();

        for (FieldPair pair : helper.getFieldPairs()) {
            Field to = pair.getTo();

            if (helper.getFieldTemporary().containsKey(to.getName())) {
                continue;
            }

            Object recordValue = record.getValue(to.getName());

            try {
                Object resultSetValue = transformValue(
                        resultSet.getObject(to.getName()), to.getType());

                if (!Objects.equals(recordValue, resultSetValue)) {
                    changes.put(to.getName(), new ValueChange(resultSetValue, recordValue));
                }
            } catch (Exception e) {
                throw new RuntimeException(String.format(
                        "Error while pushing %s with value %s",
                        to, String.valueOf(recordValue)), e);
            }
        }

        return changes;
    }

}
