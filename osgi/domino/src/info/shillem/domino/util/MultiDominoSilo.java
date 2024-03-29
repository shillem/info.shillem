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

    private Consumer<Entry<DbIdentifier, Database>> databaseConsumer;
    private Session session;
    private Map<String, SingleDominoSilo> silos;
    private DbPath templatePath;

    public MultiDominoSilo(DbIdentifier identifier, ServerFolder serverFolder) {
        this.identifier = Objects.requireNonNull(identifier, "Identifier cannot be null");
        this.serverFolder = serverFolder;
        this.silos = new HashMap<>();
    }

    public DominoSilo get(String key) {
        return silos.computeIfAbsent(key, (k) -> {
            SingleDominoSilo silo = new SingleDominoSilo(
                    identifier,
                    new DbPath(
                            serverFolder.getServerName(),
                            serverFolder.getFolderPath().concat(key)));
            silo.setSession(session);
            silo.setTemplateCreation(templatePath, databaseConsumer);

            return silo;
        });
    }

    @Override
    public Database getDatabase() throws NotesException {
        throw new UnsupportedOperationException("Use get(...).getDatabase()");
    }

    @Override
    public DbPath getDbPath() {
        throw new UnsupportedOperationException("Use get(...).getDbPath()");
    }

    @Override
    public DbIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public View getView(VwPath vwPath, VwAccessPolicy accessPolicy) throws NotesException {
        throw new UnsupportedOperationException("Use get(...).getView(...)");
    }

    @Override
    public List<String> getViewColumnNames(VwPath vwPath) throws NotesException {
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
    public void setSession(Session value) {
        session = value;
    }

    @Override
    public void setTemplateCreation(
            DbPath templatePath,
            Consumer<Entry<DbIdentifier, Database>> databaseConsumer) {
        this.templatePath = templatePath;
        this.databaseConsumer = databaseConsumer;
    }

}
