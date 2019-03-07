package info.shillem.domino.factory;

import info.shillem.domino.util.DominoSilo;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.ViewEntry;

public interface DominoFactory {

    void addDominoSilo(DominoSilo silo);
    
    boolean containsDominoSilo(String name);

    DominoSilo getDominoSilo(String name);
    
	Session getSession();
	
	void recycle();
	
	Document setDefaults(Document doc) throws NotesException;
	
	ViewEntry setDefaults(ViewEntry entry) throws NotesException;

}
