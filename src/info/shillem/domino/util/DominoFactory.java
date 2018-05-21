package info.shillem.domino.util;

import java.util.List;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

public interface DominoFactory {
	
	void addDatabasePath(DatabaseIdentifier identifier, DatabasePath path);

	Database getDatabase(DatabaseIdentifier ddi) throws NotesException;

	Document getDocumentById(DatabaseIdentifier dd, String id) throws NotesException;

	Session getSession() throws NotesException;

	View getView(ViewIdentifier dvi, ViewAccessPolicy policy) throws NotesException;

	List<String> getViewColumnNames(ViewIdentifier dvi) throws NotesException;
	
	void recycle();

}
