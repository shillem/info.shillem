package info.shillem.domino.util;

import java.util.List;

import lotus.domino.NotesException;
import lotus.domino.View;
import lotus.domino.ViewEntry;

public class ViewManager {

    private final DominoSilo silo;
    private final ViewPath viewPath;
    private final List<String> viewColumnNames;
    private final String notesUrlPrefix;

    public ViewManager(ViewPath viewPath, DominoSilo silo) throws NotesException {
        this.silo = silo;
        this.viewPath = viewPath;
        this.viewColumnNames = silo.getViewColumnNames(viewPath);

        notesUrlPrefix = String.format("notes://%s/%s/0",
                silo.getDatabasePath().getServerNameAsUrl(),
                silo.getDatabase().getReplicaID());
    }

    public String getDatabaseUrl(ViewEntry entry) throws NotesException {
        return notesUrlPrefix + "/" + entry.getUniversalID();
    }

    public View getView(ViewAccessPolicy policy) throws NotesException {
        return silo.getView(viewPath, policy);
    }

    public int indexOfColumn(String columnName) {
        return viewColumnNames.indexOf(columnName);
    }

}
