package info.shillem.domino.util;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

public interface DominoSilo {

    Database getDatabase() throws NotesException;

    DatabasePath getDatabasePath();

    String getName();

    View getView(ViewPath viewPath, ViewAccessPolicy accessPolicy) throws NotesException;

    List<String> getViewColumnNames(ViewPath viewPath) throws NotesException;

    boolean isDocumentLockingEnabled();

    void recycle();

    void setSession(Session session);

    void setTemplateCreation(
            DatabasePath templatePath, Consumer<Map.Entry<String, Database>> databaseConsumer);

}