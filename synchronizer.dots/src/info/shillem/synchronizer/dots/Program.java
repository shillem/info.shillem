package info.shillem.synchronizer.dots;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import info.shillem.domino.util.DatabasePath;
import info.shillem.domino.util.DominoUtil;
import info.shillem.synchronizer.util.Field;
import info.shillem.synchronizer.util.FieldPair;
import info.shillem.synchronizer.util.RecordPolicy;
import info.shillem.util.StringUtil;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.RichTextItem;
import lotus.domino.RichTextStyle;
import lotus.domino.Session;

public class Program {

    public static class Builder {

        private static final Pattern FIELD_MAPPING_PATTERN =
                Pattern.compile("(\\w*)\\:(\\w*)\\|(\\w*)\\:(\\w*)");

        private String id;
        private String connectionDriverClassName;
        private String connectionUrl;
        private DatabasePath databasePath;
        private LocalDateTime lastSuccessfullyStarted;
        private LocalDateTime lastSuccessfullyStopped;
        private RunMode runMode;
        private Integer intervalInMinutes;
        private FieldPair fieldDeletion;
        private FieldPair fieldKey;
        private List<FieldPair> fieldPairs;
        private Map<String, FieldPair> fieldTemporary;
        private Nature nature;
        private String notesUrl;
        private boolean printSummary;
        private String processorBuilderClassName;
        private RecordPolicy processorRecordPolicy;
        private Map<String, String> processorVariables;
        private String queryReferenceTable;
        private String queryStatement;
        private Integer queryTimeout;
        private Status status;
        private Integer[] timeFrame;
        private String title;
        private String viewName;

        public Builder(Document programDoc, Document connectionDoc) {
            Objects.requireNonNull(programDoc, "Program document cannot be null");
            Objects.requireNonNull(connectionDoc, "Connection document cannot be null");

            try {
                programDoc.setPreferJavaDates(true);
                connectionDoc.setPreferJavaDates(true);

                setGeneralPreferences(programDoc);
                setMappingPreferences(programDoc);
                setConnectionPreferences(programDoc, connectionDoc);
            } catch (NotesException e) {
                throw new RuntimeException(e);
            }
        }

        public Program build() {
            return new Program(this);
        }

        private void setConnectionPreferences(Document programDoc, Document connectionDoc)
                throws NotesException {
            connectionDriverClassName = DominoUtil.getItemString(connectionDoc, "driver");
            connectionUrl = DominoUtil.getItemString(connectionDoc, "url");
            queryReferenceTable = DominoUtil.getItemString(programDoc, "query_reference_table");
            queryStatement = DominoUtil.getItemString(programDoc, "query_stmt");
            queryTimeout = DominoUtil.getItemInteger(programDoc, "query_timeout");
        }

        private void setGeneralPreferences(Document doc) throws NotesException {
            id = Objects.requireNonNull(
                    DominoUtil.getItemString(doc, "uuid"),
                    "Document cannot be null");

            databasePath = new DatabasePath(
                    DominoUtil.getItemString(doc, "database"));

            intervalInMinutes = DominoUtil.getItemValue(
                    doc, "interval", (val) -> Integer.valueOf((String) val));

            lastSuccessfullyStarted = getLastSuccessfullyStartedDate(doc);
            lastSuccessfullyStopped = getLastSuccessfullyStoppedDate(doc);

            nature = DominoUtil.getItemValue(doc, "nature", (val) -> Nature.valueOf((String) val));

            notesUrl = doc.getNotesURL();

            printSummary = Optional
                    .ofNullable(DominoUtil.getItemBoolean(doc, "printSummary"))
                    .orElse(false);

            processorBuilderClassName = DominoUtil.getItemString(doc, "processorBuilderClassName");

            processorRecordPolicy = DominoUtil.getItemValue(
                    doc, "processorRecordPolicy", (val) -> RecordPolicy.valueOf((String) val));

            processorVariables = DominoUtil.getItemStrings(doc, "processorVariables")
                    .stream()
                    .map((val) -> val.split("="))
                    .collect(Collectors.toMap((val) -> val[0], (val) -> val[1]));

            runMode = DominoUtil.getItemValue(
                    doc, "runMode", (val) -> RunMode.valueOf((String) val));

            status = DominoUtil.getItemValue(
                    doc, "status", (val) -> Status.valueOf((String) val));

            List<Integer> tempTimeFrame = DominoUtil.getItemValues(doc, "timeFrame",
                    (val) -> ((Date) val)
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .getHour());

            timeFrame = (tempTimeFrame.isEmpty()
                    ? Arrays.asList(0, 23)
                    : tempTimeFrame)
                            .toArray(new Integer[0]);

            title = getTitle(doc);

            viewName = Objects.requireNonNull(
                    DominoUtil.getItemString(doc, "viewName"), "View name cannot be null");
        }

        private void setMappingPreferences(Document doc) throws NotesException {
            FieldEvaluation fieldEvaluation = DominoUtil.getItemValue(
                    doc, "fieldEvaluation", (val) -> FieldEvaluation.valueOf((String) val));

            Map<String, FieldPair> fieldPairs = DominoUtil.getItemValues(doc, "mapping", (val) -> {
                Matcher m = FIELD_MAPPING_PATTERN.matcher((String) val);

                if (!m.matches()) {
                    return null;
                }

                Field pf1 = new Field(
                        StringUtil.isEmpty(m.group(1)) ? m.group(3) : m.group(1),
                        Field.Type.valueOf(m.group(2)));
                Field pf2 = new Field(
                        StringUtil.isEmpty(m.group(3)) ? m.group(1) : m.group(3),
                        Field.Type.valueOf(m.group(4)));

                return fieldEvaluation == Program.FieldEvaluation.LEFT_TO_RIGHT
                        ? new FieldPair(pf1, pf2)
                        : new FieldPair(pf2, pf1);
            })
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap((p) -> p.getFrom().getName(), Function.identity()));

            fieldDeletion = Optional
                    .ofNullable(DominoUtil.getItemString(doc, "deletionControlField"))
                    .map(fieldPairs::get)
                    .orElse(null);

            fieldKey = Optional
                    .ofNullable(DominoUtil.getItemString(doc, "keyField"))
                    .map(fieldPairs::get)
                    .orElse(null);

            List<String> tempFieldNames = DominoUtil.getItemStrings(doc, "temporaryFields");
            fieldTemporary = fieldPairs.entrySet()
                    .stream()
                    .filter((e) -> tempFieldNames.contains(e.getKey()))
                    .collect(Collectors.toMap(
                            (e) -> e.getValue().getTo().getName(),
                            (e) -> e.getValue()));

            this.fieldPairs = new ArrayList<>(fieldPairs.values());
        }

        public static String getConnectionName(Document doc) throws NotesException {
            return DominoUtil.getItemString(doc, "connectionName");
        }

        public static String getTitle(Document doc) throws NotesException {
            return DominoUtil.getItemString(doc, "title");
        }

    }

    public enum FieldEvaluation {
        LEFT_TO_RIGHT, RIGHT_TO_LEFT
    }

    public enum Nature {
        DOMINO_TO_SQL, SQL_TO_DOMINO
    }

    public enum RunMode {
        DISABLED, ENABLED, DRY_RUN
    }

    public enum Status {
        STARTED, STOPPED
    }

    private final String id;
    private final String connectionDriverClassName;
    private final String connectionUrl;
    private final DatabasePath databasePath;
    private final FieldPair fieldDeletion;
    private final FieldPair fieldKey;
    private final List<FieldPair> fieldList;
    private final Map<String, FieldPair> fieldTemporary;
    private final Integer intervalInMinutes;
    private final Nature nature;
    private final String notesUrl;
    private final boolean printSummary;
    private final String processorBuilderClassName;
    private final RecordPolicy processorRecordPolicy;
    private final Map<String, String> processorVariables;
    private final String queryReferenceTable;
    private final String queryStatement;
    private final Integer queryTimeout;
    private final RunMode runMode;
    private final Integer[] timeFrame;
    private final String title;
    private final String viewName;

    private Status status;
    private LocalDateTime lastSuccessfullyStarted;
    private LocalDateTime lastSuccessfullyStopped;

    public Program(Builder builder) {
        id = builder.id;
        connectionDriverClassName = builder.connectionDriverClassName;
        connectionUrl = builder.connectionUrl;
        databasePath = builder.databasePath;
        fieldDeletion = builder.fieldDeletion;
        fieldKey = builder.fieldKey;
        fieldList = Collections.unmodifiableList(builder.fieldPairs);
        fieldTemporary = Collections.unmodifiableMap(builder.fieldTemporary);
        intervalInMinutes = builder.intervalInMinutes;
        lastSuccessfullyStarted = builder.lastSuccessfullyStarted;
        lastSuccessfullyStopped = builder.lastSuccessfullyStopped;
        nature = builder.nature;
        notesUrl = builder.notesUrl;
        printSummary = builder.printSummary;
        processorBuilderClassName = builder.processorBuilderClassName;
        processorRecordPolicy = builder.processorRecordPolicy;
        processorVariables = builder.processorVariables;
        queryReferenceTable = builder.queryReferenceTable;
        queryStatement = builder.queryStatement;
        queryTimeout = builder.queryTimeout;
        runMode = builder.runMode;
        status = builder.status;
        timeFrame = builder.timeFrame;
        title = builder.title;
        viewName = builder.viewName;
    }

    public String getConnectionDriverClassName() {
        return connectionDriverClassName;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public DatabasePath getDatabasePath() {
        return databasePath;
    }

    public FieldPair getFieldDeletion() {
        return fieldDeletion;
    }

    public FieldPair getFieldKey() {
        return fieldKey;
    }

    public List<FieldPair> getFieldPairs() {
        return fieldList;
    }

    public Map<String, FieldPair> getFieldTemporary() {
        return fieldTemporary;
    }

    public String getId() {
        return id;
    }

    public Integer getIntervalInMinutes() {
        return intervalInMinutes;
    }

    public Nature getNature() {
        return nature;
    }

    public String getProcessorBuilderClassName() {
        return processorBuilderClassName;
    }

    public RecordPolicy getProcessorRecordPolicy() {
        return processorRecordPolicy;
    }

    public Optional<String> getProcessorVariable(String key) {
        if (!processorVariables.containsKey(key)) {
            return Optional.empty();
        }

        return Optional.of(processorVariables.get(key));
    }

    public String getQueryReferenceTable() {
        return queryReferenceTable;
    }

    public String getQueryStatement() {
        return queryStatement;
    }

    public Integer getQueryTimeout() {
        return queryTimeout;
    }

    public RunMode getRunMode() {
        return runMode;
    }

    public Status getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getViewName() {
        return viewName;
    }

    public boolean isDue() {
        if (isRunning()
                || runMode != RunMode.ENABLED) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        int currentHour = now.getHour();

        if (currentHour < timeFrame[0] || currentHour > timeFrame[1]) {
            return false;
        }

        if (lastSuccessfullyStarted == null) {
            return true;
        }

        return lastSuccessfullyStarted.plusMinutes(intervalInMinutes).isBefore(now);
    }

    public boolean isPrintSummary() {
        return printSummary;
    }

    public boolean isRunMode(RunMode mode) {
        return runMode == mode;
    }

    public boolean isRunning() {
        return status == Program.Status.STARTED;
    }

    public synchronized boolean setAsStarted(Session session) {
        if (isRunning()) {
            return false;
        }

        Document doc = null;

        try {
            doc = (Document) session.resolve(notesUrl);
            doc.setPreferJavaDates(true);

            Status newStatus = Program.Status.STARTED;

            doc.replaceItemValue("status", newStatus.name());
            DominoUtil.setDate(session, doc, "started", new Date());
            doc.replaceItemValue("stopped", null);
            doc.save(true, false);

            status = newStatus;

            return true;
        } catch (NotesException e) {
            throw new RuntimeException(e);
        } finally {
            DominoUtil.recycle(doc);
        }
    }

    public synchronized boolean setAsStopped(Session session, String log, boolean failed) {
        if (!isRunning()) {
            return false;
        }

        Document doc = null;
        RichTextItem rtItem = null;
        RichTextStyle rtStyle = null;

        try {
            doc = (Document) session.resolve(notesUrl);
            doc.setPreferJavaDates(true);

            rtItem = (RichTextItem) doc.getFirstItem("log");

            if (rtItem != null) {
                rtItem.remove();
            }

            rtStyle = session.createRichTextStyle();
            rtStyle.setFont(RichTextStyle.FONT_ROMAN);
            rtStyle.setFontSize(10);

            rtItem = doc.createRichTextItem("log");
            rtItem.appendStyle(rtStyle);
            rtItem.appendText(log);

            Status newStatus = Program.Status.STOPPED;

            doc.replaceItemValue("status", newStatus.name());

            if (failed) {
                DominoUtil.setDate(
                        session, doc, "started", Program.toDate(lastSuccessfullyStarted));
                DominoUtil.setDate(
                        session, doc, "stopped", Program.toDate(lastSuccessfullyStopped));
            } else {
                lastSuccessfullyStarted = getLastSuccessfullyStartedDate(doc);

                lastSuccessfullyStopped = LocalDateTime.now();

                DominoUtil.setDate(
                        session, doc, "stopped", Program.toDate(lastSuccessfullyStopped));
            }

            doc.save(true, false);

            status = newStatus;

            return true;
        } catch (NotesException e) {
            throw new RuntimeException(e);
        } finally {
            DominoUtil.recycle(rtStyle, rtItem, doc);
        }
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", title, id);
    }

    private static LocalDateTime getLastSuccessfullyStartedDate(Document doc)
            throws NotesException {
        return DominoUtil.getItemValue(doc, "started", (val) -> toLocalDateTime((Date) val));
    }

    private static LocalDateTime getLastSuccessfullyStoppedDate(Document doc)
            throws NotesException {
        return DominoUtil.getItemValue(doc, "stopped", (val) -> toLocalDateTime((Date) val));
    }

    private static Date toDate(LocalDateTime value) {
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }

    private static LocalDateTime toLocalDateTime(Date value) {
        return LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault());
    }

}
