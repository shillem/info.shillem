package info.shillem.synchronizer.dots;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.dots.task.AbstractServerTask;
import com.ibm.dots.task.DotsException;
import com.ibm.dots.task.RunWhen;

import info.shillem.domino.factory.DominoFactory;
import info.shillem.domino.factory.LocalDominoFactory;
import info.shillem.domino.util.DatabasePath;
import info.shillem.domino.util.DominoStream;
import info.shillem.domino.util.DominoUtil;
import info.shillem.domino.util.SingleDominoSilo;
import info.shillem.synchronizer.dots.Program.RunMode;
import info.shillem.synchronizer.dto.Record;
import info.shillem.synchronizer.lang.ProcessorException;
import info.shillem.synchronizer.util.Processor;
import info.shillem.synchronizer.util.ProcessorBuilder;
import info.shillem.synchronizer.util.ProcessorHelper;
import info.shillem.synchronizer.util.ProcessorHelper.Mode;
import info.shillem.synchronizer.util.SqlFactory;
import info.shillem.util.Unthrow;
import info.shillem.util.dots.Commands;
import info.shillem.util.dots.DefaultProgressMonitor;
import info.shillem.util.dots.Preferences;
import info.shillem.util.dots.Preferences.Property;
import info.shillem.util.dots.Preferences.PropertyPolicy;
import info.shillem.util.dots.TaskManager.TaskRun;
import info.shillem.util.dots.lang.PreferencesException;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewNavigator;

public class SynchronizerTask extends AbstractServerTask {

    private static enum Command {
        EXEC(Option.builder("e")
                .longOpt("exec")
                .hasArg(true)
                .desc("execute program/s <uuid>...")
                .build()),
        HELP(Option.builder("h")
                .longOpt("print help")
                .build()),
        PRINT(Option.builder("p")
                .longOpt("print-config")
                .desc("print configuration")
                .build()),
        PRINT_SERVICES(Option.builder()
                .longOpt("print-services")
                .desc("print services")
                .build()),
        RELOAD(Option.builder()
                .longOpt("reload-config")
                .desc("reload configuration")
                .build()),
        VERBOSE(Option.builder()
                .longOpt("verbose")
                .desc("used with execution it outputs a more verbose log")
                .build());

        private Option option;

        Command(Option option) {
            this.option = option;
        }

        public Option getOption() {
            return option;
        }

    }

    private static enum PreferenceProperty {
        ENABLED {
            @Override
            Property newInstance() {
                return new Property(name(), Boolean.class, PropertyPolicy.ON_DISK, false);
            }
        },
        SYNCHRONIZER {
            @Override
            Property newInstance() {
                return new Property(name(), String.class, PropertyPolicy.ON_DISK_MANDATORY);
            }
        };

        abstract Property newInstance();
    }

    private static Commands COMMANDS;
    private static Preferences PREFERENCES;
    private static Map<String, Program> PROGRAMS;

    @Override
    public void dispose() throws NotesException {

    }

    private Map<String, Program> getPrograms() {
        synchronized (SynchronizerTask.class) {
            if (PROGRAMS == null) {
                DatabasePath path = new DatabasePath(getTaskPreferences()
                        .getProperty(PreferenceProperty.SYNCHRONIZER.name())
                        .getValue(String.class));

                Database db = null;
                View vwIds = null;
                View vwPrograms = null;
                ViewNavigator vwNav = null;
                Map<String, Document> connectionDocs = new HashMap<>();

                try {
                    db = getSession().getDatabase(path.getServerName(), path.getFilePath());
                    vwIds = db.getView("($Uuid)");
                    vwPrograms = db.getView("($Program)");
                    vwPrograms.setAutoUpdate(false);
                    vwNav = vwPrograms.createViewNavFromCategory(getSession().getServerName());

                    try (Stream<ViewEntry> stream = DominoStream.stream(vwNav)) {
                        View vwIdsFinal = vwIds;

                        PROGRAMS = stream
                                .map((entry) -> Unthrow.on(() -> {
                                    Document programDoc = entry.getDocument();

                                    String connectionName = Program.Builder
                                            .getConnectionName(programDoc);

                                    Document connectionDoc = connectionDocs.computeIfAbsent(
                                            "SqlConnection:" + connectionName,
                                            (key) -> Unthrow.on(
                                                    () -> vwIdsFinal.getDocumentByKey(key, true)));

                                    if (connectionDoc == null) {
                                        logMessage(String.format(
                                                "Cannot load connection document %s for program %s",
                                                connectionName,
                                                Program.Builder.getTitle(programDoc)));

                                        return null;
                                    }

                                    return new Program.Builder(programDoc, connectionDoc).build();
                                }))
                                .filter(Objects::nonNull)
                                .collect(Collectors.toMap(Program::getId, Function.identity()));
                    }
                } catch (NotesException e) {
                    throw new DotsException(e);
                } finally {
                    DominoUtil.recycle(connectionDocs.values());
                    DominoUtil.recycle(vwNav, vwPrograms, vwIds, db);
                }
            }
        }

        return PROGRAMS;
    }

    private Commands getTaskCommands() {
        synchronized (SynchronizerTask.class) {
            if (COMMANDS == null) {
                COMMANDS = new Commands(SynchronizerActivator.PLUGIN_ID, Stream
                        .of(Command.values())
                        .collect(Collectors.toMap(Command::name, (c) -> c.getOption())));
            }
        }

        return COMMANDS;
    }

    private Preferences getTaskPreferences() {
        synchronized (SynchronizerTask.class) {
            if (PREFERENCES == null) {
                PREFERENCES = new Preferences(SynchronizerActivator.PLUGIN_ID, Stream
                        .of(PreferenceProperty.values())
                        .map(PreferenceProperty::newInstance)
                        .collect(Collectors.toSet()));

                PREFERENCES.readPropertiesOnDisk();
            }
        }

        return PREFERENCES;
    }

    private DominoFactory newDominoFactory(Program program) {
        try {
            DominoFactory factory = new LocalDominoFactory.Builder(getSession())
                    .addOption(LocalDominoFactory.Option.DO_NOT_CONVERT_MIME)
                    .addOption(LocalDominoFactory.Option.PREFER_JAVA_DATES)
                    .addOption(LocalDominoFactory.Option.TRACK_MILLISEC_IN_JAVA_DATES)
                    .build();

            factory.addDominoSilo(
                    new SingleDominoSilo(program.getId(), program.getDatabasePath()));

            return factory;
        } catch (NotesException e) {
            throw new RuntimeException(e);
        }
    }

    private SqlFactory newSqlFactory(Program program) throws ClassNotFoundException {
        return new SqlFactory(
                SynchronizerActivator.getSqlDriver(program.getConnectionDriverClassName()),
                program.getConnectionUrl());
    }

    @Override
    public void run(RunWhen runWhen, String[] args, IProgressMonitor progressMonitor)
            throws NotesException {
        TaskRun taskRun = new TaskRun(this, runWhen, args, progressMonitor);

        if (taskRun.isInteractive()) {
            runInteractive(taskRun);
        } else {
            runScheduled(taskRun);
        }
    }

    private void runInteractive(TaskRun taskRun) {
        Commands taskCommands = getTaskCommands();
        Commands.Result taskCommandResults = null;
        Command cmd = null;

        try {
            taskCommandResults = taskCommands.resolve(taskRun.getArgs());
            cmd = Command.valueOf(taskCommandResults.getTriggeredCommandName());
        } catch (UnrecognizedOptionException e) {
            cmd = Command.HELP;
        } catch (ParseException e) {
            logMessage(e.getMessage());

            return;
        } catch (IllegalArgumentException e) {
            throw new DotsException(e);
        }

        switch (cmd) {
        case EXEC: {
            boolean verbose = taskCommandResults.getCommandLine().hasOption("verbose");

            taskCommandResults
                    .getTriggeredCommandValues()
                    .forEach((id) -> {
                        Program program = getPrograms().get(id);

                        if (program == null) {
                            logMessage(String.format("Program id %s does not exist", id));
                        } else {
                            runProgram(program, taskRun, verbose);
                        }
                    });

            return;
        }
        case HELP:
            logMessage(taskCommands.getHelp());

            return;
        case PRINT: {
            StringBuilder builder = new StringBuilder();
            builder.append(getTaskPreferences().getHelp());

            String templateHeader = "\n---%-50s\t%-10s\t%-10s\t%-10s";
            String templateBody = templateHeader.replace("---", "   ");

            builder.append("\nPrograms");
            builder.append(String.format(templateHeader,
                    "Title",
                    "Interval",
                    "Run Mode",
                    "Status"));
            getPrograms().values().forEach((p) -> builder.append(
                    String.format(templateBody,
                            Optional
                                    .of(p.toString())
                                    .map((s) -> s.length() > 50
                                            ? "..." + s.subSequence(s.length() - 47, s.length())
                                            : s)
                                    .get(),
                            String.valueOf(p.getIntervalInMinutes()) + " min.",
                            p.getRunMode().name(),
                            p.getStatus().name())));

            logMessage(builder.toString());

            return;
        }
        case PRINT_SERVICES: {
            StringBuilder builder = new StringBuilder();

            String templateHeader = "\n---%-50s";
            String templateBody = templateHeader.replace("---", "   ");

            builder.append(String.format(templateHeader, "(1) SQL Drivers"));
            SynchronizerActivator.getSqlDriverClassNames()
                    .forEach((name) -> builder.append(String.format(templateBody, name)));

            builder.append(String.format(templateHeader, "(2) Processes"));
            SynchronizerActivator.getProcessorClassNames()
                    .forEach((name) -> builder.append(String.format(templateBody, name)));

            logMessage(builder.toString());

            return;
        }
        case RELOAD:
            try {
                Program firstRunningProgram = getPrograms()
                        .values()
                        .stream()
                        .filter(Program::isRunning)
                        .findFirst()
                        .orElse(null);

                if (firstRunningProgram != null) {
                    logMessage("Cannot reload the configuration while programs are running."
                            + " Try again later");

                    return;
                }

                PROGRAMS = null;

                getTaskPreferences().readPropertiesOnDisk();

                logMessage("The configuration was reloaded");
            } catch (PreferencesException e) {
                logMessage(e.getMessage());
            }

            return;
        default:
            logMessage(String.format(
                    "Command %s cannot be invoked as main command. See help for more information",
                    cmd));
        }
    }

    private void runProgram(Program program, TaskRun taskRun, boolean verbose) {
        ProcessorHelper helper = null;
        boolean aborted = false;

        if (!program.setAsStarted(getSession())) {
            logMessage(String.format(
                    "Program %s can't run because it's already running", program));

            return;
        }

        try {
            Processor<? extends Record> processor;

            try {
                helper = new ProcessorHelper.Builder(
                        program, new DefaultProgressMonitor(taskRun.getProgressMonitor()))
                                .setMode(Mode.VERBOSE, verbose)
                                .setMode(Mode.TEST, program.isRunMode(RunMode.DRY_RUN))
                                .setDominoFactory(newDominoFactory(program))
                                .setSqlFactory(newSqlFactory(program))
                                .build();

                processor = Optional
                        .ofNullable(program.getProcessorBuilderClassName())
                        .map((name) -> Unthrow.on(() -> SynchronizerActivator
                                .getProcessorBuilder(name)))
                        .orElseGet(() -> {
                            switch (program.getNature()) {
                            case SQL_TO_DOMINO:
                                return new ProcessorBuilder();
                            default:
                                throw new UnsupportedOperationException(
                                        "Nature " + program.getNature() + " is not supported");
                            }
                        })
                        .build(helper);
            } catch (ClassNotFoundException e) {
                logMessage(e.getMessage());

                return;
            }

            helper.logMessage(String.format(
                    "Program %s started with modes %s", program, helper.getModes()));

            if (!processor.execute()) {
                String abortMessage = String.format(
                        "Program %s was aborted", program);

                if (program.isPrintSummary()) {
                    logMessage(abortMessage);
                }

                helper.logMessage(abortMessage);

                return;
            }

            String summary = String.format(
                    "Program %s ran with the following results: %s",
                    program,
                    helper.getTracker());

            if (taskRun.isInteractive() || program.isPrintSummary()) {
                logMessage(summary);
            }

            helper.logMessage(summary);
        } catch (ProcessorException e) {
            Optional
                    .ofNullable(helper)
                    .ifPresent((h) -> h.logException(e));

            logMessage(e.getMessage());
        } catch (Exception e) {
            Optional
                    .ofNullable(helper)
                    .ifPresent((h) -> h.logException(e));

            logException(e);
        } finally {
            if (helper == null) {
                program.setAsStopped(getSession(), "Program threw an exception", aborted);
            } else {
                helper.getDominoFactory().recycle();

                program.setAsStopped(getSession(), helper.getLog(), aborted);
            }
        }
    }

    private void runScheduled(TaskRun taskRun) {
        Boolean taskIsEnabled = getTaskPreferences()
                .getProperty(PreferenceProperty.ENABLED.name())
                .getValue(Boolean.class);

        if (!taskIsEnabled) {
            return;
        }

        for (Program program : getPrograms().values()) {
            if (taskRun.getProgressMonitor().isCanceled()) {
                return;
            }

            if (!program.isDue()) {
                continue;
            }

            runProgram(program, taskRun, false);
        }
    }

    @Override
    public void setSession(Session session) {
        try {
            session.setConvertMime(false);
            session.setTrackMillisecInJavaDates(true);
        } catch (NotesException e) {
            throw new DotsException(e);
        }

        super.setSession(session);
    }

}
