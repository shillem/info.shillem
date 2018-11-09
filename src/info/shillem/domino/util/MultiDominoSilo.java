package info.shillem.domino.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

public class MultiDominoSilo implements DominoSilo {

    private final String name;
    private final ServerFolder serverFolder;

    private Session session;
    private DatabasePath templatePath;
    private Consumer<Entry<String, Database>> databaseConsumer;

    private Map<String, SingleDominoSilo> silos;

    public MultiDominoSilo(String name, ServerFolder serverFolder) {
        this.name = name;
        this.serverFolder = serverFolder;
        this.silos = new HashMap<>();
    }

    public DominoSilo get(String key) {
        SingleDominoSilo silo = silos.get(key);

        if (silo == null) {
            silo = new SingleDominoSilo(key, new DatabasePath(
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
    public Document getDocumentById(String id) throws NotesException {
        throw new UnsupportedOperationException("Use get(...).getDocumentById(...)");
    }

    @Override
    public String getName() {
        return name;
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
    public void recycle() {
        silos.values().forEach(DominoSilo::recycle);
        silos.clear();
    }

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public void setTemplateCreation(DatabasePath templatePath,
            Consumer<Map.Entry<String, Database>> databaseConsumer) {
        this.templatePath = templatePath;
        this.databaseConsumer = databaseConsumer;
    }

}
