package info.shillem.domino.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.shillem.domino.util.DatabaseIdentifier;
import info.shillem.domino.util.DatabasePath;
import info.shillem.domino.util.DominoFactory;
import info.shillem.domino.util.DominoUtil;
import info.shillem.domino.util.ViewAccessPolicy;
import info.shillem.domino.util.ViewIdentifier;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

public class DominoFactoryImpl implements DominoFactory {

	private Session session;

	private Map<String, DatabasePath> databasePaths;
	private Map<String, Database> databaseHandles;
	private Map<String, View> viewHandles;
	private Map<String, List<String>> viewColumnNames;

	public DominoFactoryImpl(Session session) {
		if (session == null) {
			throw new NullPointerException("Session cannot be null");
		}

		this.session = session;
		this.databasePaths = new HashMap<>();
		this.databaseHandles = new HashMap<>();
		this.viewHandles = new HashMap<>();
	}

	public DominoFactoryImpl addDatabasePath(DatabaseIdentifier identifier, DatabasePath databasePath) {
		databasePaths.put(identifier.getName(), databasePath);
		
		return this;
	}

	@Override
	public final Database getDatabase(DatabaseIdentifier ddi) throws NotesException {
		if (ddi == null) {
			throw new NullPointerException("Database Identifier cannot be null");
		}

		Database db = databaseHandles.get(ddi.getName());

		if (db == null) {
			DatabasePath ddp = databasePaths.get(ddi.getName());

			if (ddp == null) {
				throw new IllegalArgumentException(ddi + " is undefined");
			}

			db = getSession().getDatabase(ddp.getServerName(), ddp.getFilePath());

			databaseHandles.put(ddi.getName(), db);
		}

		if (db == null) {
			throw new NullPointerException("Unable to open " + ddi);
		}

		return db;
	}

	@Override
	public final Document getDocumentById(DatabaseIdentifier dd, String id) throws NotesException {
		if (id == null || id.isEmpty()) {
			throw new NullPointerException("Invalid Id for " + dd);
		}

		if (id.length() == 32) {
			return getDatabase(dd).getDocumentByUNID(id);
		} else {
			return getDatabase(dd).getDocumentByID(id);
		}
	}

	@Override
	public Session getSession() {
		return session;
	}

	@Override
	public final View getView(ViewIdentifier dvi, ViewAccessPolicy policy) throws NotesException {
		if (viewHandles == null) {
			viewHandles = new HashMap<>();
		}

		View vw = viewHandles.get(dvi.getName());

		if (vw == null) {
			vw = getDatabase(dvi.getDatabaseIdentifier()).getView(dvi.getName());

			if (vw == null) {
				throw new NullPointerException(String.format("Unable to access view %s on %s",
						dvi.getName(),
						dvi.getDatabaseIdentifier()));
			}

			vw.setAutoUpdate(false);

			viewHandles.put(dvi.getName(), vw);
		}

		if (policy == ViewAccessPolicy.REFRESH) {
			vw.refresh();
		}

		return vw;
	}

	@Override
	public List<String> getViewColumnNames(ViewIdentifier dvi) throws NotesException {
		if (viewColumnNames == null) {
			viewColumnNames = new HashMap<String, List<String>>();
		}

		List<String> columnNames = viewColumnNames.get(dvi.getName());

		if (columnNames == null) {
			columnNames = new ArrayList<>();

			View vw = getView(dvi, ViewAccessPolicy.CACHE);

			for (Object columnName : vw.getColumnNames()) {
				columnNames.add(String.valueOf(columnName));
			}

			viewColumnNames.put(dvi.getName(), columnNames);
		}

		return columnNames;
	}

	@Override
	public void recycle() {
		DominoUtil.recycle(viewHandles.values().toArray(new View[0]));
		DominoUtil.recycle(databaseHandles.values().toArray(new Database[0]));

		viewHandles.clear();
		databaseHandles.clear();
	}

}
