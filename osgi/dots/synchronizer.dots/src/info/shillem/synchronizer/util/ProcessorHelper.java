package info.shillem.synchronizer.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import info.shillem.domino.factory.DominoFactory;
import info.shillem.sql.factory.SqlFactory;
import info.shillem.synchronizer.dots.Program;
import info.shillem.util.ProgressMonitor;

public class ProcessorHelper {

    public static class Builder {

        private final Program program;
        private final ProgressMonitor progressMonitor;

        private Set<Mode> modes;
        private DominoFactory dominoFactory;
        private SqlFactory sqlFactory;

        public Builder(Program program, ProgressMonitor progressMonitor) {
            this.program = Objects.requireNonNull(
                    program, "Program cannot be null");

            this.progressMonitor = Objects.requireNonNull(
                    progressMonitor, "Progress monitor cannot be null");

            this.modes = new HashSet<>();
        }

        public ProcessorHelper build() {
            return new ProcessorHelper(this);
        }

        public Builder setDominoFactory(DominoFactory factory) {
            dominoFactory = Objects.requireNonNull(factory, "Domino Factory cannot be null");

            return this;
        }

        public Builder setMode(Mode mode, boolean flag) {
            if (flag) {
                modes.add(mode);
            } else {
                modes.remove(mode);
            }

            return this;
        }

        public Builder setSqlFactory(SqlFactory factory) {
            sqlFactory = Objects.requireNonNull(factory, "Domino Factory cannot be null");

            return this;
        }

    }

    public enum Mode {
        TEST, VERBOSE
    }

    private final Program program;
    private final ProgressMonitor progressMonitor;
    private final Set<Mode> modes;
    private final DominoFactory dominoFactory;
    private final SqlFactory sqlFactory;

    private final ProcessorTracker tracker;
    private final StringBuilder log;

    private ProcessorHelper(Builder builder) {
        program = builder.program;
        progressMonitor = builder.progressMonitor;
        modes = builder.modes;
        dominoFactory = builder.dominoFactory;
        sqlFactory = builder.sqlFactory;

        tracker = new ProcessorTracker();
        log = new StringBuilder();
    }

    public DominoFactory getDominoFactory() {
        return dominoFactory;
    }

    public Optional<Field> getFieldDeletion() {
        return Optional
                .ofNullable(program.getFieldDeletion())
                .map((pair) -> pair.getTo());
    }

    public Field getFieldKey() {
        return program.getFieldKey().getTo();
    }

    public List<FieldPair> getFieldPairs() {
        return program.getFieldPairs();
    }

    public Map<String, FieldPair> getFieldTemporary() {
        return program.getFieldTemporary();
    }

    public String getId() {
        return program.getId();
    }

    public String getLog() {
        return log.toString();
    }

    public Set<Mode> getModes() {
        return new HashSet<>(modes);
    }

    public String getQueryReferenceTable() {
        return program.getQueryReferenceTable();
    }

    public String getQueryStatement() {
        return program.getQueryStatement();
    }

    public Integer getQueryTimeout() {
        return program.getQueryTimeout();
    }

    public RecordPolicy getRecordPolicy() {
        return program.getProcessorRecordPolicy();
    }

    public SqlFactory getSqlFactory() {
        return sqlFactory;
    }

    public ProcessorTracker getTracker() {
        return tracker;
    }

    public Optional<String> getVariable(String name) {
        return program.getProcessorVariable(name);
    }

    public String getViewName() {
        return program.getViewName();
    }

    public boolean isExecutionCanceled() {
        return progressMonitor.isRequestCanceled();
    }

    public boolean isMode(Mode m) {
        return modes.contains(m);
    }

    public void logException(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        e.printStackTrace(pw);
        
        log.append(sw.toString());
    }

    public void logMessage(String message) {
        log.append("\n" + Instant.now().atZone(ZoneOffset.systemDefault()) + " " + message);
    }

    public void logVerboseMessage(String message) {
        if (isMode(Mode.VERBOSE) && tracker.getTouched() < 250) {
            logMessage(message);
        }
    }
    
    public void logVerboseMessage(Supplier<String> message) {
        if (isMode(Mode.VERBOSE) && tracker.getTouched() < 250) {
            logMessage(message.get());
        }
    }

    public final void recycle() {
        try {
            getDominoFactory().recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            getSqlFactory().recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setVariable(String name, String value) {
        program.setProcessorVariable(name, value);
    }

}
