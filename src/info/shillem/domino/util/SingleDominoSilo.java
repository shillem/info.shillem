package info.shillem.domino.util;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

public class SingleDominoSilo implements DominoSilo {

    private final String name;
    private final DatabasePath databasePath;

    private Session session;
    private DatabasePath templatePath;
    private Consumer<Entry<String, Database>> databaseConsumer;

    private Database databaseHandle;
    private Map<String, View> viewHandles;
    private Map<String, List<String>> viewColumnNames;

    public SingleDominoSilo(String name, DatabasePath databasePath) {
        this.name = name;
        this.databasePath = databasePath;
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
                databaseConsumer.accept(new SimpleEntry<String, Database>(name, newDatabase));
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
    public Document getDocumentById(String id) throws NotesException {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Invalid id");
        }

        if (id.length() == 32) {
            return getDatabase().getDocumentByUNID(id);
        } else {
            return getDatabase().getDocumentByID(id);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public View getView(ViewPath viewPath, ViewAccessPolicy accessPolicy) throws NotesException {
        Objects.requireNonNull(viewPath, "View path cannot be null");
        Objects.requireNonNull(accessPolicy, "View access policy cannot be null");

        if (viewHandles == null) {
            viewHandles = new HashMap<>();
        }

        View vw = viewHandles.get(viewPath.getKey());

        if (vw == null) {
            vw = getDatabase().getView(viewPath.getName());

            if (vw == null) {
                throw new NullPointerException(
                        String.format("Unable to access view %s on %s", viewPath, databasePath));
            }

            vw.setAutoUpdate(false);

            viewHandles.put(viewPath.getKey(), vw);
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

        List<String> columnNames = viewColumnNames.get(viewPath.getKey());

        if (columnNames == null) {
            columnNames = new ArrayList<>();

            View vw = getView(viewPath, ViewAccessPolicy.CACHE);

            for (Object columnName : vw.getColumnNames()) {
                columnNames.add(String.valueOf(columnName));
            }

            viewColumnNames.put(viewPath.getKey(), columnNames);
        }

        return columnNames;
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
            DatabasePath templatePath, Consumer<Map.Entry<String, Database>> databaseConsumer) {
        this.templatePath = templatePath;
        this.databaseConsumer = databaseConsumer;
    }

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

}
