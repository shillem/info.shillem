package info.shillem.domino.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import lotus.domino.Base;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.MIMEEntity;
import lotus.domino.MIMEHeader;
import lotus.domino.NotesException;
import lotus.domino.Session;

public enum DominoUtil {
	;

	private static final Vector<String> MIME_FILTERED_HEADERS = new Vector<>();

	static {
		MIME_FILTERED_HEADERS.add("Content-Type");
		MIME_FILTERED_HEADERS.add("Content-Disposition");
	}

	public static <T> T getItemValue(Document doc, String itemName, Class<T> type) throws NotesException {
		Item item = null;

		try {
			item = doc.getFirstItem(itemName);

			if (item == null) {
				return null;
			}

			if (item.getType() == Item.RICHTEXT) {
				return type.cast(item.getText());
			} else {
				@SuppressWarnings("unchecked")
				List<T> values = (List<T>) item.getValues();

				return values != null ? values.get(0) : null;
			}
		} finally {
			DominoUtil.recycle(item);
		}
	}

	public static <T> List<T> getItemValues(Document doc, String itemName, Class<T> type) throws NotesException {
		Item item = null;

		try {
			item = doc.getFirstItem(itemName);

			if (item == null) {
				return Collections.<T>emptyList();
			}

			if (item.getType() == Item.RICHTEXT) {
				List<T> values = new ArrayList<>();

				values.add(type.cast(item.getText()));

				return values;
			} else {
				@SuppressWarnings("unchecked")
				List<T> values = (List<T>) item.getValues();

				return values != null ? new ArrayList<>(values) : Collections.<T>emptyList();
			}
		} finally {
			DominoUtil.recycle(item);
		}
	}

	public static Date getLastModified(Document doc) throws NotesException {
		DateTime d = null;

		try {
			d = doc.getLastModified();

			return d.toJavaDate();
		} finally {
			DominoUtil.recycle(d);
		}
	}

	public static MIMEEntity getMimeEntity(Document doc, String itemName, boolean createOnFail)
			throws NotesException {
		if (itemName == null) {
			throw new NullPointerException("Invalid MIME entity item name");
		}

		MIMEEntity mimeEntity = doc.getMIMEEntity(itemName);

		if (mimeEntity == null) {
			if (doc.hasItem(itemName)) {
				doc.removeItem(itemName);
			}

			if (createOnFail) {
				mimeEntity = doc.createMIMEEntity(itemName);
			}
		}

		return mimeEntity;
	}

	public static String getMimeEntityHeaderValAndParams(MIMEEntity entity, String name) throws NotesException {
		if (name == null) {
			return null;
		}

		Vector<?> headers = entity.getHeaderObjects();

		try {
			for (Object header : headers) {
				MIMEHeader h = (MIMEHeader) header;

				if (h.getHeaderName().equals(name)) {
					return h.getHeaderValAndParams();
				}
			}

			return null;
		} finally {
			DominoUtil.recycle(headers.toArray(new Base[0]));
		}
	}

	public static String getMimeEntityFilename(MIMEEntity entity) throws NotesException {
		Vector<?> headers = entity.getHeaderObjects();

		try {
			for (Object header : headers) {
				String fileName = ((MIMEHeader) header).getParamVal("filename");

				if (!fileName.isEmpty()) {
					return fileName.replaceAll("'|\"", "");
				}
			}

			return null;
		} finally {
			DominoUtil.recycle(headers.toArray(new Base[0]));
		}
	}

	public static Map<MimeContentType, List<MIMEEntity>> getMimeEntities(MIMEEntity entity,
			MimeContentType... contentTypes) throws NotesException {
		if (entity == null || contentTypes == null) {
			return Collections.<MimeContentType, List<MIMEEntity>>emptyMap();
		}

		Map<MimeContentType, List<MIMEEntity>> mimeEntities = new HashMap<MimeContentType, List<MIMEEntity>>();
		MIMEEntity nextEntity = null;

		try {
			nextEntity = entity.getNextEntity();

			while (nextEntity != null) {
				String[] entityFilteredHeaders = nextEntity.getSomeHeaders(MIME_FILTERED_HEADERS, true).split("\\n");

				for (MimeContentType contentType : contentTypes) {
					if (contentType.matches(entityFilteredHeaders)) {
						List<MIMEEntity> filteredEntities = mimeEntities.get(contentType);

						if (filteredEntities == null) {
							filteredEntities = new ArrayList<>();
							mimeEntities.put(contentType, filteredEntities);
						}

						filteredEntities.add(nextEntity);
					}
				}

				// No recycle because mimeEntity is passed in
				// and remains under the control of the caller
				nextEntity = nextEntity.getNextEntity();
			}
		} finally {
			DominoUtil.recycle(nextEntity);
		}

		return mimeEntities;
	}

	public static void recycle(Base... bases) {
		for (Base base : bases) {
			if (base != null) {
				try {
					base.recycle();
				} catch (Exception e) {
					// Do nothing
				}
			}
		}
	}

	public static void setDate(Session session, Document doc, String itemName, Date value) throws NotesException {
		DateTime dateTime = null;

		try {
			if (value != null) {
				dateTime = session.createDateTime(value);
			}

			doc.replaceItemValue(itemName, dateTime);
		} finally {
			recycle(dateTime);
		}
	}

}