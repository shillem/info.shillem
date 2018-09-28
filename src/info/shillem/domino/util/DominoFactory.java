package info.shillem.domino.util;

import lotus.domino.NotesException;
import lotus.domino.Session;

public interface DominoFactory {

    void addDominoSilo(DominoSilo silo);
    
    boolean containsDominoSilo(String name);

    DominoSilo getDominoSilo(String name);
    
	Session getSession() throws NotesException;
	
	void recycle();

}
