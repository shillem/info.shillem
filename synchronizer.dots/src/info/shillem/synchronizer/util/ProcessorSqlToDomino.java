package info.shillem.synchronizer.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import info.shillem.domino.util.DominoUtil;
import info.shillem.synchronizer.dots.Program.Nature;
import info.shillem.synchronizer.dto.Record;
import info.shillem.synchronizer.dto.ValueChange;
import info.shillem.synchronizer.lang.ProcessorException;
import info.shillem.synchronizer.util.ProcessorHelper.Mode;
import info.shillem.util.Unthrow;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.NotesException;
import lotus.domino.View;

public class ProcessorSqlToDomino<T extends Record> extends Processor<T> {

    private boolean refreshView;

    public ProcessorSqlToDomino(ProcessorHelper helper, Supplier<T> recordSupplier) {
        super(helper, recordSupplier);
    }

    protected void afterExecution() throws ProcessorException {

    }

    protected void beforeExecution() throws ProcessorException {

    }

    protected void deleteDocument(Document doc) throws ProcessorException {
        try {
            if (getDominoSilo().isDocumentLockingEnabled()) {
                if (!doc.lock()) {
                    throw new RuntimeException("Unable to acquire lock");
                }

                doc.lock();
            }

            doc.removePermanently(true);
        } catch (NotesException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final boolean execute() throws ProcessorException {
        beforeExecution();

        try {
            Connection conn = helper.getSqlFactory().getConnection();
            PreparedStatement statement = conn.prepareStatement(helper.getQueryStatement());
            statement.setQueryTimeout(helper.getQueryTimeout());

            helper.logMessage("Performing query...");

            try (ResultSet resultSet = statement.executeQuery()) {
                ProcessorTracker tracker = helper.getTracker();

                helper.logMessage("Processing records...");

                String keyFieldName = getKeyField().getName();

                while (resultSet.next()) {
                    if (helper.isExecutionCanceled()) {
                        return false;
                    }

                    T record = newRecord();

                    pullResultSet(resultSet, record);

                    Document doc = null;

                    try {
                        doc = findDocument(record).orElse(null);

                        if (doc == null && helper.getRecordPolicy() == RecordPolicy.UPDATE) {
                            tracker.addSkipped();

                            continue;
                        }

                        if (record.isDeleted()) {
                            if (doc != null && !helper.isMode(Mode.TEST)) {
                                deleteDocument(doc);

                                helper.logVerboseMessage(() -> Unthrow.on(
                                        () -> "Deleted record " + record.getValue(keyFieldName)));

                                setRefreshView(true);
                            }

                            tracker.addDeleted();

                            continue;
                        }

                        if (doc == null) {
                            doc = initializeDocument(record);

                            record.setNew(true);
                        }

                        Map<String, ValueChange> changes = pushRecord(record, doc);

                        if (changes.isEmpty()) {
                            tracker.addUnmodified();

                            continue;
                        }

                        helper.logVerboseMessage(() -> Unthrow.on(() -> {
                            StringBuilder summary = new StringBuilder(
                                    (record.isNew() ? "New" : "Updated")
                                            + " record " + record.getValue(keyFieldName));

                            changes.forEach((name, change) -> summary.append(
                                    String.format("\n\t%s %s", name, change)));

                            return summary.toString();
                        }));

                        if (!helper.isMode(Mode.TEST)) {
                            doc.save();

                            setRefreshView(true);
                        }

                        tracker.addModified(record.isNew());
                    } catch (NotesException e) {
                        throw new RuntimeException(e);
                    } finally {
                        DominoUtil.recycle(doc);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        afterExecution();

        return true;
    }

    protected void finalizeDocument(Document doc, T record, Map<String, ValueChange> changes)
            throws ProcessorException {

    }

    protected Optional<Document> findDocument(T record) throws ProcessorException {
        try {
            View view = getView();

            if (isRefreshView()) {
                view.refresh();

                setRefreshView(false);
            }

            Document doc = view.getDocumentByKey(getKeyValue(record), true);

            if (doc == null) {
                return Optional.empty();
            }

            return Optional.of(helper.getDominoFactory().setDefaults(doc));
        } catch (NotesException e) {
            throw new RuntimeException(e);
        }
    }

    protected Document initializeDocument(T record) throws ProcessorException {
        try {
            Document doc = helper.getDominoFactory().setDefaults(
                    getDominoSilo().getDatabase().createDocument());

            helper.getVariable("Form")
                    .ifPresent((formName) -> Unthrow.on(() -> {
                        Item itm = doc.replaceItemValue("Form", formName);

                        DominoUtil.recycle(itm);
                    }));

            return doc;
        } catch (NotesException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final boolean isNature(Nature nature) {
        return nature == Nature.SQL_TO_DOMINO;
    }

    protected final boolean isRefreshView() {
        return refreshView;
    }

    protected void pullResultSet(ResultSet resultSet, T record) throws ProcessorException {
        for (FieldPair pair : helper.getFieldPairs()) {
            Field from = pair.getFrom();
            Field to = pair.getTo();

            try {
                switch (from.getType()) {
                case BOOLEAN:
                    record.setValue(to.getName(), transformValue(
                            resultSet.getBoolean(from.getName()), to.getType()));

                    break;
                case DATE:
                    // There's a fix here because Domino doesn't read millisecs
                    record.setValue(to.getName(), transformValue(
                            Optional.ofNullable(resultSet.getTimestamp(from.getName()))
                                    .map(t -> new java.util.Date(t.getTime() / 1000 * 1000))
                                    .orElse(null),
                            to.getType()));

                    break;
                case DECIMAL:
                    // getObject actually returns null if the SQL value is null
                    // unlike getBigDecimal that returns 0.0
                    record.setValue(to.getName(), transformValue(
                            resultSet.getObject(from.getName()), to.getType()));

                    break;
                case DOUBLE:
                    // getObject actually returns null if the SQL value is null
                    // unlike getDouble that returns 0.0
                    record.setValue(to.getName(), transformValue(
                            resultSet.getObject(from.getName()), to.getType()));

                    break;
                case INTEGER:
                    // getObject actually returns null if the SQL value is null
                    // unlike getInteger that returns 0
                    record.setValue(to.getName(), transformValue(
                            resultSet.getObject(from.getName()), to.getType()));

                    break;
                case STRING:
                    record.setValue(to.getName(), transformValue(
                            resultSet.getString(from.getName()), to.getType()));

                    break;
                }
            } catch (Exception e) {
                throw new RuntimeException(String.format(
                        "Error while pulling %s to %s", from, to), e);
            }
        }
    }

    protected Map<String, ValueChange> pushRecord(T record, Document doc)
            throws ProcessorException {
        Map<String, ValueChange> changes = new HashMap<>();

        for (FieldPair pair : helper.getFieldPairs()) {
            Field to = pair.getTo();

            if (helper.getFieldTemporary().containsKey(to.getName())) {
                continue;
            }

            Object recordValue = record.getValue(to.getName());

            try {
                Object documentValue = transformValue(
                        DominoUtil.getItemValue(doc, to.getName()), to.getType());

                if (recordValue == null) {
                    if (documentValue == null
                            || (documentValue instanceof String
                                    && ((String) documentValue).isEmpty())) {
                        continue;
                    }

                    doc.removeItem(to.getName());

                    changes.put(to.getName(), new ValueChange(documentValue, recordValue));

                    continue;
                }

                if (recordValue instanceof String
                        && ((String) recordValue).isEmpty()
                        && documentValue == null) {
                    continue;
                }

                if (documentValue == null
                        || !recordValue.equals(documentValue)) {
                    if (recordValue instanceof Boolean) {
                        doc.replaceItemValue(to.getName(), ((Boolean) recordValue).toString());
                    } else if (recordValue instanceof Date) {
                        DominoUtil.setDate(
                                helper.getDominoFactory().getSession(),
                                doc, to.getName(), (Date) recordValue);
                    } else if (recordValue instanceof BigDecimal) {
                        doc.replaceItemValue(to.getName(),
                                ((BigDecimal) recordValue).doubleValue());
                    } else {
                        doc.replaceItemValue(to.getName(), recordValue);
                    }

                    changes.put(to.getName(), new ValueChange(documentValue, recordValue));
                }
            } catch (Exception e) {
                throw new RuntimeException(String.format(
                        "Error while pushing %s with value %s",
                        to, String.valueOf(recordValue)), e);
            }
        }

        return changes;
    }

    protected final void setRefreshView(boolean flag) {
        this.refreshView = flag;
    }

}
