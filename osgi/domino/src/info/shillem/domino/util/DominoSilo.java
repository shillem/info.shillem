package info.shillem.domino.util;

import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

public interface DominoSilo {

    Database getDatabase() throws NotesException;

    DbPath getDbPath();

    DbIdentifier getIdentifier();

    View getView(VwPath vwPath, VwAccessPolicy accessPolicy) throws NotesException;

    List<String> getViewColumnNames(VwPath vwPath) throws NotesException;

    boolean isDocumentLockingEnabled();

    void recycle();

    void setSession(Session session);

    void setTemplateCreation(
            DbPath templatePath, Consumer<Entry<DbIdentifier, Database>> databaseConsumer);

}