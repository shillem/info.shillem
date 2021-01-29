package info.shillem.domino.util;

import lotus.domino.NotesException;
import lotus.domino.View;
import lotus.domino.ViewEntry;

public class ViewManager {

    private final DominoSilo silo;
    private final ViewPath viewPath;
    private final String notesUrlPrefix;

    public ViewManager(ViewPath viewPath, DominoSilo silo) throws NotesException {
        this.silo = silo;
        this.viewPath = viewPath;

        notesUrlPrefix = String.format("notes://%s/%s/0",
                silo.getDatabasePath().getServerNameAsUrl(),
                silo.getDatabase().getReplicaID());
    }

    public String getDatabaseUrl(ViewEntry entry) throws NotesException {
        return notesUrlPrefix.concat("/").concat(entry.getUniversalID());
    }

    public View getView(ViewAccessPolicy policy) throws NotesException {
        return silo.getView(viewPath, policy);
    }

    public int indexOfColumn(String columnName) throws NotesException {
        return silo.getViewColumnNames(viewPath).indexOf(columnName);
    }

}
