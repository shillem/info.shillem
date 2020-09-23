package info.shillem.domino.factory;

import info.shillem.domino.util.DbIdentifier;
import info.shillem.domino.util.DominoSilo;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.ViewEntry;

public interface DominoFactory {
    
    void addSilo(DominoSilo silo);

    boolean containsSilo(DbIdentifier identifier);

    Session getSession();

    DominoSilo getSilo(DbIdentifier identifier);

    boolean isRemote();

    DominoFactory newInstance(Session session) throws NotesException;

    void recycle();

    Document setDefaults(Document doc) throws NotesException;
    
    ViewEntry setDefaults(ViewEntry entry) throws NotesException;

}
