package info.shillem.domino.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

public class MultiDominoSilo implements DominoSilo {

    private final DbIdentifier identifier;
    private final ServerFolder serverFolder;

    private Session session;
    private DatabasePath templatePath;
    private Consumer<Entry<DbIdentifier, Database>> databaseConsumer;

    private Map<String, SingleDominoSilo> silos;

    public MultiDominoSilo(DbIdentifier identifier, ServerFolder serverFolder) {
        this.identifier = Objects.requireNonNull(identifier, "Identifier cannot be null");
        this.serverFolder = serverFolder;
        this.silos = new HashMap<>();
    }

    public DominoSilo get(String key) {
        SingleDominoSilo silo = silos.get(key);

        if (silo == null) {
            silo = new SingleDominoSilo(identifier, new DatabasePath(
                    serverFolder.getServerName(), serverFolder.getFolderPath() + key));
            silo.setSession(session);
            silo.setTemplateCreation(templatePath, databaseConsumer);

            silos.put(key, silo);
        }

        return silo;
    }

    @Override
    public Database getDatabase() throws NotesException {
        throw new UnsupportedOperationException("Use get(...).getDatabase()");
    }

    @Override
    public DatabasePath getDatabasePath() {
        throw new UnsupportedOperationException("Use get(...).getDatabasePath()");
    }

    @Override
    public DbIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public View getView(ViewPath viewPath, ViewAccessPolicy accessPolicy) throws NotesException {
        throw new UnsupportedOperationException("Use get(...).getView(...)");
    }

    @Override
    public List<String> getViewColumnNames(ViewPath viewPath) throws NotesException {
        throw new UnsupportedOperationException("Use get(...).getViewColumnNames(...)");
    }

    @Override
    public boolean isDocumentLockingEnabled() {
        throw new UnsupportedOperationException("Use get(...).isDocumentLockingEnabled()");
    }

    @Override
    public void recycle() {
        silos.values().forEach(DominoSilo::recycle);
        silos.clear();
    }

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public void setTemplateCreation(
            DatabasePath templatePath,
            Consumer<Entry<DbIdentifier, Database>> databaseConsumer) {
        this.templatePath = templatePath;
        this.databaseConsumer = databaseConsumer;
    }

}
