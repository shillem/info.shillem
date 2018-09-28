package info.shillem.domino.util;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

public interface DominoSilo {

    Database getDatabase() throws NotesException;

    Document getDocumentById(String id) throws NotesException;

    String getName();

    View getView(ViewPath viewPath, ViewAccessPolicy accessPolicy) throws NotesException;

    List<String> getViewColumnNames(ViewPath viewPath) throws NotesException;

    void recycle();

    void setSession(Session session);

    void setTemplateCreation(
            DatabasePath templatePath, Consumer<Map.Entry<String, Database>> databaseConsumer);

}