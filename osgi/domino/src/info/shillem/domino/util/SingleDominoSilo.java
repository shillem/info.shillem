package info.shillem.domino.util;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;

import info.shillem.util.CastUtil;
import info.shillem.util.Unthrow;
import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

public class SingleDominoSilo implements DominoSilo {

    private final DbIdentifier identifier;
    private final DbPath dbPath;

    private Consumer<Entry<DbIdentifier, Database>> dbConsumer;
    private Database dbHandle;
    private boolean documentLockingEnabled;
    private DbPath templatePath;
    private Session session;
    private Map<String, List<String>> vwColumnNames;
    private Map<String, View> vwHandles;

    public SingleDominoSilo(DbIdentifier identifier, DbPath dbPath) {
        this.identifier = Objects.requireNonNull(identifier, "Identifier cannot be null");
        this.dbPath = Objects.requireNonNull(dbPath, "Database path cannot be null");
    }

    private synchronized Database createTemplate() throws NotesException {
        Database templateHandle = null;

        try {
            templateHandle = session.getDatabase(
                    templatePath.getServerName(),
                    templatePath.getFilePath());

            if (templateHandle == null) {
                throw new NullPointerException("Unable to open template " + templatePath);
            }

            Database newDatabase = templateHandle.createFromTemplate(
                    dbPath.getServerName(),
                    dbPath.getFilePath(),
                    true);

            if (dbConsumer != null) {
                dbConsumer.accept(new SimpleEntry<>(identifier, newDatabase));
            }

            return newDatabase;
        } finally {
            DominoUtil.recycle(templateHandle);
        }
    }

    @Override
    public Database getDatabase() throws NotesException {
        if (dbHandle == null) {
            dbHandle = session.getDatabase(dbPath.getServerName(), dbPath.getFilePath(), false);

            if (dbHandle == null && templatePath != null) {
                dbHandle = createTemplate();
            }

            if (dbHandle == null) {
                throw new NullPointerException("Unable to open database " + dbPath);
            }

            documentLockingEnabled = dbHandle.isDocumentLockingEnabled();
        }

        return dbHandle;
    }

    @Override
    public DbPath getDbPath() {
        return dbPath;
    }

    @Override
    public DbIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public View getView(VwPath vwPath, VwAccessPolicy accessPolicy) throws NotesException {
        Objects.requireNonNull(vwPath, "View path cannot be null");
        Objects.requireNonNull(accessPolicy, "View access policy cannot be null");

        if (vwHandles == null) {
            vwHandles = new HashMap<>();
        }

        View vw = vwHandles.get(vwPath.getName());

        if (vw == null) {
            vw = getDatabase().getView(vwPath.getName());

            if (vw == null) {
                throw new NullPointerException(
                        String.format("Unable to access view %s on %s", vwPath, dbPath));
            }

            vw.setAutoUpdate(false);

            vwHandles.put(vwPath.getName(), vw);
        }

        if (accessPolicy == VwAccessPolicy.FRESH) {
            vw.refresh();
        }

        return vw;
    }

    @Override
    public List<String> getViewColumnNames(VwPath vwPath) throws NotesException {
        Objects.requireNonNull(vwPath, "View path cannot be null");

        if (vwColumnNames == null) {
            vwColumnNames = new HashMap<>();
        }

        return vwColumnNames.computeIfAbsent(
                vwPath.getName(),
                (key) -> Unthrow.on(
                        () -> new ArrayList<>(CastUtil.toAnyVector(
                                getView(vwPath, VwAccessPolicy.STALE).getColumnNames()))));
    }

    @Override
    public boolean isDocumentLockingEnabled() {
        return documentLockingEnabled;
    }

    @Override
    public void recycle() {
        if (vwColumnNames != null) {
            vwColumnNames.clear();
        }

        if (vwHandles != null) {
            DominoUtil.recycle(vwHandles.values());

            vwHandles.clear();

        }

        DominoUtil.recycle(dbHandle);

        dbHandle = null;
    }

    @Override
    public void setSession(Session value) {
        session = value;
    }

    @Override
    public void setTemplateCreation(
            DbPath templatePath,
            Consumer<Entry<DbIdentifier, Database>> databaseConsumer) {
        this.templatePath = templatePath;
        this.dbConsumer = databaseConsumer;
    }

}
