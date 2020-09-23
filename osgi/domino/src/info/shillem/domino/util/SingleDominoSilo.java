package info.shillem.domino.util;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Vector;
import java.util.function.Consumer;

import info.shillem.util.CastUtil;
import info.shillem.util.Unthrow;
import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

public class SingleDominoSilo implements DominoSilo {

    private final DbIdentifier identifier;
    private final DatabasePath databasePath;

    private Session session;
    private DatabasePath templatePath;
    private Consumer<Entry<DbIdentifier, Database>> databaseConsumer;

    private Database databaseHandle;
    private boolean documentLockingEnabled;

    private Map<String, View> viewHandles;
    private Map<String, List<String>> viewColumnNames;

    public SingleDominoSilo(DbIdentifier identifier, DatabasePath databasePath) {
        this.identifier = Objects.requireNonNull(identifier, "Identifier cannot be null");
        this.databasePath = Objects.requireNonNull(databasePath, "Database path cannot be null");
    }

    @Override
    public Database getDatabase() throws NotesException {
        if (databaseHandle == null) {
            databaseHandle = session.getDatabase(
                    databasePath.getServerName(), databasePath.getFilePath(), false);

            if (databaseHandle == null && templatePath != null) {
                databaseHandle = createTemplate();
            }

            if (databaseHandle == null) {
                throw new NullPointerException("Unable to open database " + databasePath);
            }

            documentLockingEnabled = databaseHandle.isDocumentLockingEnabled();
        }

        return databaseHandle;
    }

    private synchronized Database createTemplate() throws NotesException {
        Database templateHandle = null;

        try {
            templateHandle = session
                    .getDatabase(templatePath.getServerName(), templatePath.getFilePath());

            if (templateHandle == null) {
                throw new NullPointerException("Unable to open template " + templatePath);
            }

            Database newDatabase = templateHandle.createFromTemplate(
                    databasePath.getServerName(), databasePath.getFilePath(), true);

            if (databaseConsumer != null) {
                databaseConsumer.accept(
                        new SimpleEntry<DbIdentifier, Database>(identifier, newDatabase));
            }

            return newDatabase;
        } finally {
            DominoUtil.recycle(templateHandle);
        }
    }

    @Override
    public DatabasePath getDatabasePath() {
        return databasePath;
    }
    
    @Override
    public DbIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public View getView(ViewPath viewPath, ViewAccessPolicy accessPolicy) throws NotesException {
        Objects.requireNonNull(viewPath, "View path cannot be null");
        Objects.requireNonNull(accessPolicy, "View access policy cannot be null");

        if (viewHandles == null) {
            viewHandles = new HashMap<>();
        }

        View vw = viewHandles.get(viewPath.getName());

        if (vw == null) {
            vw = getDatabase().getView(viewPath.getName());

            if (vw == null) {
                throw new NullPointerException(
                        String.format("Unable to access view %s on %s", viewPath, databasePath));
            }

            vw.setAutoUpdate(false);

            viewHandles.put(viewPath.getName(), vw);
        }

        if (accessPolicy == ViewAccessPolicy.REFRESH) {
            vw.refresh();
        }

        return vw;
    }

    @Override
    public List<String> getViewColumnNames(ViewPath viewPath) throws NotesException {
        Objects.requireNonNull(viewPath, "View path cannot be null");

        if (viewColumnNames == null) {
            viewColumnNames = new HashMap<>();
        }

        return viewColumnNames.computeIfAbsent(viewPath.getName(),
                key -> Unthrow.on(() -> {
                    Vector<String> columnNames = CastUtil.toAnyVector(
                            getView(viewPath, ViewAccessPolicy.CACHE).getColumnNames());
                
                    return new ArrayList<>(columnNames);
                }));
    }

    @Override
    public boolean isDocumentLockingEnabled() {
        return documentLockingEnabled;
    }

    @Override
    public void recycle() {
        if (viewColumnNames != null) {
            viewColumnNames.clear();
        }

        if (viewHandles != null) {
            DominoUtil.recycle(viewHandles.values());
            viewHandles.clear();

        }

        DominoUtil.recycle(databaseHandle);

        databaseHandle = null;
    }

    @Override
    public void setTemplateCreation(
            DatabasePath templatePath, Consumer<Entry<DbIdentifier, Database>> databaseConsumer) {
        this.templatePath = templatePath;
        this.databaseConsumer = databaseConsumer;
    }

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

}
