package info.shillem.synchronizer.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import info.shillem.domino.util.DominoUtil;
import info.shillem.domino.util.ViewAccessPolicy;
import info.shillem.synchronizer.dots.Program.Nature;
import info.shillem.synchronizer.dto.Record;
import info.shillem.synchronizer.dto.ValueChange;
import info.shillem.synchronizer.lang.ProcessorException;
import info.shillem.synchronizer.util.ProcessorHelper.Mode;
import info.shillem.util.Unthrow;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.NotesException;
import lotus.domino.View;

public class ProcessorSqlToDomino<T extends Record> extends Processor<T> {

    private ViewAccessPolicy vap;

    public ProcessorSqlToDomino(ProcessorHelper helper, Supplier<T> recordSupplier) {
        super(helper, recordSupplier);

        vap = ViewAccessPolicy.FRESH;
    }

    protected void afterExecution() throws ProcessorException {

    }

    protected void beforeExecution() throws ProcessorException {

    }

    protected Document createDocument() throws NotesException {
        return createDocument(getDominoSilo().getDatabase());
    }

    protected Document createDocument(Database db) throws NotesException {
        return helper.getDominoFactory().setDefaults(db.createDocument());
    }

    protected void deleteDocument(Document doc) throws NotesException {
        if (getDominoSilo().isDocumentLockingEnabled()) {
            if (!doc.lock()) {
                throw new RuntimeException("Unable to acquire lock on note " + doc.getNoteID());
            }
        }

        doc.removePermanently(true);
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

                while (resultSet.next()) {
                    if (helper.isExecutionCanceled()) {
                        return false;
                    }

                    T record = newRecord();

                    pullResultSet(resultSet, record);

                    Document doc = null;

                    try {
                        doc = findDocument(record).orElse(null);

                        if (record.isDeleted()) {
                            if (doc != null && !helper.isMode(Mode.TEST)) {
                                deleteDocument(doc);

                                helper.logVerboseMessage("Deleted record " + getKeyValue(record));

                                setViewAccessPolicy(ViewAccessPolicy.FRESH);
                            }

                            tracker.addDeleted();

                            continue;
                        }

                        RecordPolicy policy = helper.getRecordPolicy();
                        
                        if (doc == null) {
                            if (policy == RecordPolicy.UPDATE) {
                                tracker.addSkipped();

                                continue;
                            }
                            
                            doc = initializeDocument(record);

                            record.setNew(true);
                        } else if (policy == RecordPolicy.INSERT) {
                            tracker.addSkipped();

                            continue;
                        }

                        Map<String, ValueChange> changes = pushRecord(record, doc);

                        finalizeDocument(doc, record, changes);

                        if (changes.isEmpty()) {
                            tracker.addUnmodified();

                            continue;
                        }

                        helper.logVerboseMessage(() -> {
                            String header = String.format(
                                    "%s record %s",
                                    record.isNew() ? "New" : "Updated",
                                    getKeyValue(record));
                            String fields = changes
                                    .entrySet()
                                    .stream()
                                    .map((e) -> String.format("\t%s %s", e.getKey(), e.getValue()))
                                    .collect(Collectors.joining("\n"));

                            return header + "\n" + fields;
                        });

                        if (!helper.isMode(Mode.TEST)) {
                            doc.save();

                            setViewAccessPolicy(ViewAccessPolicy.FRESH);
                        }

                        tracker.addModified(record.isNew());
                    } catch (NotesException e) {
                        throw new RuntimeException(e);
                    } finally {
                        DominoUtil.recycle(doc);
                    }
                }
            }

            afterExecution();

            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void finalizeDocument(
            Document doc,
            T record,
            Map<String, ValueChange> changes)
            throws ProcessorException {

    }

    protected Optional<Document> findDocument(T record) throws NotesException {
        View view = getView(getViewAccessPolicy());

        if (!isStaleView()) {
            setViewAccessPolicy(ViewAccessPolicy.STALE);
        }

        return findDocument(view, getKeyValue(record));
    }

    protected final Optional<Document> findDocument(View view, Object key) throws NotesException {
        if (key == null) {
            throw new IllegalArgumentException("View key cannot be null");
        }

        Document doc = key instanceof Vector
                ? view.getDocumentByKey((Vector<?>) key, true)
                : view.getDocumentByKey(key, true);

        if (doc == null) {
            return Optional.empty();
        }

        return Optional.of(helper.getDominoFactory().setDefaults(doc));
    }

    protected final ViewAccessPolicy getViewAccessPolicy() {
        return vap;
    }

    protected Document initializeDocument(T record) throws NotesException {
        Document doc = createDocument();

        helper.getVariable("Form")
                .ifPresent((formName) -> Unthrow.on(() -> {
                    Item itm = doc.replaceItemValue("Form", formName);

                    DominoUtil.recycle(itm);
                }));

        return doc;
    }

    @Override
    public final boolean isNature(Nature nature) {
        return nature == Nature.SQL_TO_DOMINO;
    }

    protected final boolean isStaleView() {
        return vap == ViewAccessPolicy.STALE;
    }

    protected void pullResultSet(ResultSet resultSet, T record) {
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

    private Map<String, ValueChange> pushRecord(T record, Document doc) {
        Map<String, ValueChange> changes = new HashMap<>();

        for (FieldPair pair : helper.getFieldPairs()) {
            ValueChange change = pushRecordValue(record, doc, pair);

            if (change != null) {
                changes.put(pair.getTo().getName(), change);
            }
        }

        return changes;
    }

    protected ValueChange pushRecordValue(T record, Document doc, FieldPair pair) {
        Field to = pair.getTo();

        if (helper.getFieldTemporary().containsKey(to.getName())) {
            return null;
        }

        Object recordValue = record.getValue(to.getName());

        try {
            Object documentValue = transformValue(
                    DominoUtil.getItemValue(doc, to.getName()), to.getType());

            if (recordValue == null) {
                if (documentValue == null
                        || (documentValue instanceof String
                                && ((String) documentValue).isEmpty())) {
                    return null;
                }

                doc.removeItem(to.getName());

                return new ValueChange(documentValue, recordValue);
            }

            if (recordValue instanceof String
                    && ((String) recordValue).isEmpty()
                    && documentValue == null) {
                return null;
            }

            if (documentValue == null || !recordValue.equals(documentValue)) {
                if (recordValue instanceof Boolean) {
                    doc.replaceItemValue(to.getName(), ((Boolean) recordValue).toString());
                } else if (recordValue instanceof Date) {
                    DominoUtil.setDate(
                            helper.getDominoFactory().getSession(),
                            doc, to.getName(), (Date) recordValue);
                } else if (recordValue instanceof BigDecimal) {
                    doc.replaceItemValue(to.getName(), ((BigDecimal) recordValue).doubleValue());
                } else {
                    doc.replaceItemValue(to.getName(), recordValue);
                }

                return new ValueChange(documentValue, recordValue);
            }

            return null;
        } catch (Exception e) {
            String docId;

            try {
                docId = record.isNew() ? "[NEW]" : doc.getNoteID();
            } catch (NotesException ne) {
                docId = e.getMessage();
            }

            throw new RuntimeException(String.format(
                    "Error while pushing %s with value %s on record %s",
                    to, String.valueOf(recordValue), docId), e);
        }
    }

    protected final void setViewAccessPolicy(ViewAccessPolicy value) {
        vap = Objects.requireNonNull(value, "View access policy cannot be null");
    }

}
