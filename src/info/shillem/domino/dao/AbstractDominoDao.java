package info.shillem.domino.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import info.shillem.dao.Query;
import info.shillem.domino.util.DominoFactory;
import info.shillem.domino.util.DominoI18n;
import info.shillem.domino.util.DominoUtil;
import info.shillem.domino.util.MimeContentType;
import info.shillem.dto.AttachmentFile;
import info.shillem.dto.AttachmentMap;
import info.shillem.dto.BaseDto;
import info.shillem.dto.BaseField;
import info.shillem.dto.FieldProperties;
import info.shillem.dto.I18nValue;
import info.shillem.dto.JsonValue;
import info.shillem.dto.ValueOperation;
import info.shillem.util.Builder;
import info.shillem.util.IOUtil;
import info.shillem.util.StringUtil;
import lotus.domino.Base;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.MIMEEntity;
import lotus.domino.MIMEHeader;
import lotus.domino.NotesException;
import lotus.domino.Stream;
import lotus.domino.ViewEntry;

public abstract class AbstractDominoDao<T extends BaseDto> {

	protected final DominoFactory factory;
	protected final Builder<T> builder;

	protected AbstractDominoDao(DominoFactory factory, Builder<T> builder) {
		this.factory = factory;
		this.builder = builder;
	}

	protected String getDominoItemName(BaseField f) {
		return f.toString();
	}

	private Gson getGson() {
		final Pattern p = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}");

		GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
				.registerTypeAdapter(Map.class, new JsonDeserializer<Map<String, Object>>() {
					@Override
					public Map<String, Object> deserialize(JsonElement jsonElement, Type type,
							JsonDeserializationContext context) throws JsonParseException {
						Map<String, Object> map = new TreeMap<>();

						for (Entry<String, JsonElement> entry : jsonElement.getAsJsonObject()
								.entrySet()) {
							JsonElement value = entry.getValue();

							if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
									&& p.matcher(value.getAsString()).find()) {
								map.put(entry.getKey(), context.deserialize(value, Date.class));
							} else {
								map.put(entry.getKey(), context.deserialize(entry.getValue(),
										Object.class));
							}
						}

						return map;
					}
				});

		return gsonBuilder.create();
	}

	protected final Date getLastModified(Document doc) throws NotesException {
		DateTime d = null;

		try {
			d = doc.getLastModified();

			return d.toJavaDate();
		} finally {
			DominoUtil.recycle(d);
		}
	}

	protected final boolean lastModifiedDatesMatch(BaseDto dto, Document doc) throws NotesException {
		return dto.getLastModified() == null || dto.getLastModified().equals(getLastModified(doc));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void pull(Document doc, T wrapper, BaseField field, Locale locale)
			throws NotesException {
		Class<? extends Serializable> type = field.getProperties().getType();

		if (type == AttachmentMap.class) {
			wrapper.set(field, pullAttachmentMap(doc, field), ValueOperation.DEFAULT_VALUE);
		} else if (I18nValue.class.isAssignableFrom(type)) {
			wrapper.set(field, DominoI18n.getValue(locale, doc, field));
		} else if (JsonValue.class.isAssignableFrom(type)) {
			wrapper.set(field, pullJsonable(doc, field, field.getProperties().getType()),
					ValueOperation.DEFAULT_VALUE);
		} else {
			Item item = null;
			List values = null;

			try {
				item = doc.getFirstItem(getDominoItemName(field));

				if (item != null) {
					if (item.getType() == Item.RICHTEXT) {
						values = new ArrayList<>();
						values.add(item.getText());
					} else {
						values = item.getValues();
					}
				}
			} finally {
				DominoUtil.recycle(item);
			}

			if (field.getProperties().isList()) {
				List newValues = FieldProperties.newList(type);

				if (values != null) {
					for (Object value : values) {
						newValues.add(pullValue(value, type));
					}
				}

				wrapper.set(field, newValues, ValueOperation.DEFAULT_VALUE);
			} else {
				wrapper.set(field, pullValue(values != null ? values.get(0) : null, type),
						ValueOperation.DEFAULT_VALUE);
			}
		}
	}

	protected void pull(Document doc, T wrapper, Set<? extends BaseField> schema, Locale locale)
			throws NotesException {
		if (!doc.isPreferJavaDates()) {
			throw new UnsupportedOperationException("Java dates are not enabled");
		}

		for (BaseField field : schema) {
			pull(doc, wrapper, field, locale);
		}

		if (!doc.isNewNote()) {
			wrapper.setId(doc.getUniversalID());
			wrapper.setLastModified(getLastModified(doc));
		}
	}

	protected void pull(List<String> columns, ViewEntry entry, T wrapper,
			Set<? extends BaseField> schema) throws NotesException {
		if (!entry.isPreferJavaDates()) {
			throw new UnsupportedOperationException("Java dates are not enabled");
		}

		for (BaseField field : schema) {
			String fieldName = getDominoItemName(field);

			if (columns.contains(fieldName)) {
				List<?> columnValues = entry.getColumnValues();

				wrapper.set(field, pullValue(columnValues.get(columns.indexOf(fieldName)), field
						.getProperties().getType()), ValueOperation.DEFAULT_VALUE);
			} else {
				throw new IllegalArgumentException("Unable to retrieve value for schema field %s "
						+ field);
			}
		}

		wrapper.setId(entry.getUniversalID());
	}

	private AttachmentMap pullAttachmentMap(Document doc, BaseField field) throws NotesException {
		String itemName = getDominoItemName(field);
		MIMEEntity mimeEntity = null;

		try {
			mimeEntity = DominoUtil.getMimeEntity(doc, itemName, false);

			if (mimeEntity == null) {
				return null;
			}

			List<MIMEEntity> attachments = DominoUtil
					.getMimeEntities(mimeEntity, MimeContentType.ATTACHMENT)
					.get(MimeContentType.ATTACHMENT);

			if (attachments == null) {
				return null;
			}

			try {
				AttachmentMap am = new AttachmentMap(attachments.size());

				for (MIMEEntity attachment : attachments) {
					am.add(DominoUtil.getMimeEntityFilename(attachment));
				}

				return am;
			} finally {
				DominoUtil.recycle(attachments.toArray(new Base[attachments.size()]));
			}
		} finally {
			DominoUtil.recycle(mimeEntity);

			doc.closeMIMEEntities(false, itemName);
		}
	}

	private Object pullJsonable(Document doc, BaseField field, Class<?> type) throws NotesException {
		String itemName = getDominoItemName(field);
		MIMEEntity mimeEntity = null;

		try {
			mimeEntity = DominoUtil.getMimeEntity(doc, itemName, false);

			if (mimeEntity != null) {
				TypeToken<?> t = field.getProperties().isList() ? TypeToken.getParameterized(
						ArrayList.class, type) : TypeToken.get(type);

				return getGson().fromJson(mimeEntity.getContentAsText(), t.getType());
			}

			return null;
		} finally {
			DominoUtil.recycle(mimeEntity);

			doc.closeMIMEEntities(false, itemName);
		}
	}

	private Object pullValue(Object value, Class<?> type) {
		if (type.isEnum()) {
			if (value instanceof String) {
				return StringUtil.enumFromString(type, (String) value);
			}
		} else if (type == Boolean.class) {
			return Boolean.valueOf((String) value);
		} else if (Number.class.isAssignableFrom(type)) {
			if (value == null) {
				return null;
			}

			Number num = (Number) value;

			if (type == Integer.class) {
				return num.intValue();
			} else if (type == Long.class) {
				return num.longValue();
			} else if (type == Double.class) {
				return num.doubleValue();
			} else if (type == BigDecimal.class) {
				return new BigDecimal(num.toString());
			}
		}

		return value;
	}

	protected void push(T wrapper, Document doc) throws NotesException {
		for (BaseField field : wrapper.getChanges()) {
			push(wrapper, doc, field);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void push(T wrapper, Document doc, BaseField field) throws NotesException {
		Class<? extends Serializable> type = field.getProperties().getType();
		Object value = wrapper.get(field);
		String itemName = getDominoItemName(field);

		if (value == null) {
			doc.replaceItemValue(itemName, value);
		} else if (type == AttachmentMap.class) {
			pushAttachmentMap((AttachmentMap) value, doc, field);
		} else if (JsonValue.class.isAssignableFrom(type)) {
			pushJsonable(value, doc, field);
		} else if (field.getProperties().isList()) {
			Vector values = new Vector();

			try {
				for (Object o : (List) value) {
					values.add(pushValue(o, type));
				}

				doc.replaceItemValue(itemName, values);
			} finally {
				factory.getSession().recycle(values);
			}
		} else {
			Object transformedValue = pushValue(value, type);

			try {
				doc.replaceItemValue(itemName, transformedValue);
			} finally {
				if (transformedValue instanceof Base) {
					DominoUtil.recycle((Base) transformedValue);
				}
			}
		}
	}

	private void pushAttachmentMap(AttachmentMap am, Document doc, BaseField field)
			throws NotesException {
		String itemName = getDominoItemName(field);
		MIMEEntity mimeEntity = null;

		try {
			mimeEntity = DominoUtil.getMimeEntity(doc, itemName, true);

			List<MIMEEntity> attachments = DominoUtil
					.getMimeEntities(mimeEntity, MimeContentType.ATTACHMENT)
					.get(MimeContentType.ATTACHMENT);

			Map<String, MIMEEntity> attachmentsByName = new HashMap<>();

			if (attachments != null) {
				for (MIMEEntity attachment : attachments) {
					attachmentsByName.put(DominoUtil.getMimeEntityFilename(attachment), attachment);
				}

				// Removing files
				for (AttachmentFile af : am.getFiles()) {
					if (af.isRemove()) {
						MIMEEntity att = attachmentsByName.get(af.getName());

						if (att != null) {
							att.remove();
						}

						am.remove(af.getName());
					}
				}
			}

			// Header cleaning on empty entity
			if (mimeEntity.getNextEntity() == null) {
				for (Iterator<?> iter = mimeEntity.getHeaderObjects().iterator(); iter.hasNext();) {
					((MIMEHeader) iter.next()).remove();
				}
			}

			// Adding files
			for (AttachmentFile af : am.getFiles()) {
				File uploadedFile = af.getUploadedFile();

				if (uploadedFile == null) {
					continue;
				}

				InputStream is = null;

				try {
					is = new FileInputStream(uploadedFile);

					pushInputStream(is, af.getName(), attachmentsByName.get(af.getName()), mimeEntity);
				} catch (FileNotFoundException BaseField) {
					throw new IllegalArgumentException(String.format(
							"The file with path %s was not found", uploadedFile.getAbsolutePath()));
				} catch (IOException BaseField) {
					throw new IllegalArgumentException("Error handling file " + af.getName());
				} finally {
					IOUtil.close(is);
				}

				af.setUploadedFile(null);
			}
		} finally {
			DominoUtil.recycle(mimeEntity);

			doc.closeMIMEEntities(true, itemName);
		}
	}

	private void pushInputStream(InputStream is, String fileName, MIMEEntity child, MIMEEntity parent)
			throws NotesException, IOException {
		Stream stm = null;

		try {
			stm = factory.getSession().createStream();
			byte buffer[] = new byte[8];
			int read;

			do {
				read = is.read(buffer, 0, buffer.length);

				if (read > 0) {
					stm.write(buffer);
				}
			} while (read > -1);

			if (child == null) {
				child = parent.createChildEntity();
				MIMEHeader header = child.createHeader("Content-Disposition");
				header.setHeaderVal("attachment;filename=\"" + fileName + "\"");
			}

			child.setContentFromBytes(stm, "application/octet-stream", MIMEEntity.ENC_IDENTITY_BINARY);

			stm.close();
		} finally {
			DominoUtil.recycle(stm);
		}
	}

	private void pushJsonable(Object jsonable, Document doc, BaseField field) throws NotesException {
		String itemName = getDominoItemName(field);

		Stream stm = null;
		MIMEEntity mimeEntity = null;

		try {
			stm = factory.getSession().createStream();
			stm.writeText(getGson().toJson(jsonable));

			mimeEntity = DominoUtil.getMimeEntity(doc, itemName, true);
			mimeEntity.setContentFromText(stm, "application/json;charset=UTF-8", MIMEEntity.ENC_NONE);

			stm.close();
		} finally {
			DominoUtil.recycle(stm, mimeEntity);

			doc.closeMIMEEntities(true, itemName);
		}
	}

	private Object pushValue(Object value, Class<? extends Serializable> type)
			throws NotesException {
		if (value == null) {
			return null;
		}

		if (type.isEnum() || type == Boolean.class) {
			return value.toString();
		} else if (type == Date.class) {
			return factory.getSession().createDateTime((Date) value);
		} else if (Number.class.isAssignableFrom(type)) {
			Number num = (Number) value;

			if (type == Integer.class) {
				return num.intValue();
			} else if (type == Long.class) {
				return num.longValue();
			} else if (type == Double.class || type == BigDecimal.class) {
				return num.doubleValue();
			}
		}

		return value;
	}

	protected T read(Document doc, Query query) throws NotesException {
		T dto = builder.build();

		pull(doc, dto, query.getSchema(), query.getLocale());

		return dto;
	}

}
