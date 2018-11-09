package info.shillem.domino.util;

import lotus.domino.Session;

public interface DominoFactory {

    void addDominoSilo(DominoSilo silo);
    
    boolean containsDominoSilo(String name);

    DominoSilo getDominoSilo(String name);
    
	Session getSession();
	
	void recycle();

}
