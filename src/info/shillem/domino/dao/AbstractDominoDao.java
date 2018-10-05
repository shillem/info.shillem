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
import java.util.Objects;
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

import info.shillem.dao.lang.DaoException;
import info.shillem.dao.lang.DaoResolutionException;
import info.shillem.domino.util.DominoFactory;
import info.shillem.domino.util.DominoI18n;
import info.shillem.domino.util.DominoUtil;
import info.shillem.domino.util.MimeContentType;
import info.shillem.dto.AttachmentFile;
import info.shillem.dto.AttachmentMap;
import info.shillem.dto.BaseDto;
import info.shillem.dto.BaseField;
import info.shillem.dto.I18nValue;
import info.shillem.dto.JsonValue;
import info.shillem.util.IOUtil;
import info.shillem.util.StringUtil;
import lotus.domino.Base;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.MIMEEntity;
import lotus.domino.MIMEHeader;
import lotus.domino.NotesException;
import lotus.domino.Stream;
import lotus.domino.ViewEntry;

public abstract class AbstractDominoDao<T extends BaseDto> {

    private static class GsonLoader {

        private static final Gson INSTANCE;
        private static final Pattern DATE_PATTERN;

        static {
            DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}");

            GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    .registerTypeAdapter(Map.class, new JsonDeserializer<Map<String, Object>>() {

                        @Override
                        public Map<String, Object> deserialize(JsonElement jsonElement, Type type,
                                JsonDeserializationContext context) throws JsonParseException {
                            Map<String, Object> map = new TreeMap<>();

                            for (Entry<String, JsonElement> entry : jsonElement.getAsJsonObject()
                                    .entrySet()) {
                                JsonElement value = entry.getValue();

                                if (value.isJsonPrimitive()
                                        && value.getAsJsonPrimitive().isString()
                                        && DATE_PATTERN.matcher(value.getAsString()).find()) {
                                    map.put(entry.getKey(), context.deserialize(value, Date.class));
                                } else {
                                    map.put(entry.getKey(),
                                            context.deserialize(value, Object.class));
                                }
                            }

                            return map;
                        }
                    });

            INSTANCE = gsonBuilder.create();
        }

    }

    protected final DominoFactory factory;

    protected AbstractDominoDao(DominoFactory factory) {
        this.factory = factory;
    }

    protected Document createDocument(Database database) throws NotesException {
        Document doc = database.createDocument();

        DominoUtil.setDefaultOptions(doc);

        return doc;
    }

    protected String getDominoItemName(BaseField f) {
        return f.toString();
    }

    protected final boolean lastModifiedDatesMatch(BaseDto dto, Document doc)
            throws NotesException {
        return dto.getLastModified() == null
                || dto.getLastModified().equals(DominoUtil.getLastModified(doc));
    }

    protected void pull(Document doc, T wrapper, BaseField field, Locale locale)
            throws NotesException {
        Class<? extends Serializable> type = field.getProperties().getType();

        if (type == AttachmentMap.class) {
            wrapper.preset(field, pullAttachmentMap(doc, field));
        } else if (I18nValue.class.isAssignableFrom(type)) {
            wrapper.preset(field, DominoI18n.getValue(locale, doc, field));
        } else if (JsonValue.class.isAssignableFrom(type)) {
            wrapper.preset(field, pullJsonable(doc, field, type));
        } else if (field.getProperties().isList()) {
            wrapper.preset(field,
                    DominoUtil.getItemValues(
                            doc, getDominoItemName(field), (value) -> pullValue(value, type)));
        } else {
            wrapper.preset(field,
                    DominoUtil.getItemValue(
                            doc, getDominoItemName(field), (value) -> pullValue(value, type)));
        }
    }

    protected void pull(Document doc, T wrapper, Set<? extends BaseField> schema, Locale locale)
            throws NotesException {
        if (!DominoUtil.hasDefaultOptions(doc)) {
            throw new IllegalArgumentException("Document is not treated with compatible options");
        }

        for (BaseField field : schema) {
            pull(doc, wrapper, field, locale);
        }

        if (!doc.isNewNote()) {
            wrapper.setId(doc.getUniversalID());
            wrapper.setLastModified(DominoUtil.getLastModified(doc));
        }
    }

    protected void pull(List<String> columns, ViewEntry entry, T wrapper,
            Set<? extends BaseField> schema) throws NotesException {
        if (!DominoUtil.hasDefaultOptions(entry)) {
            throw new IllegalArgumentException("Entry is not treated with compatible options");
        }

        for (BaseField field : schema) {
            String fieldName = getDominoItemName(field);

            if (columns.contains(fieldName)) {
                List<?> columnValues = entry.getColumnValues();

                wrapper.preset(field, pullValue(
                        columnValues.get(columns.indexOf(fieldName)),
                        field.getProperties().getType()));
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

            if (attachments.isEmpty()) {
                return null;
            }

            try {
                AttachmentMap am = new AttachmentMap();

                for (MIMEEntity attachment : attachments) {
                    am.put(DominoUtil.getMimeEntityFilename(attachment));
                }

                return am;
            } finally {
                DominoUtil.recycle(attachments);
            }
        } finally {
            DominoUtil.recycle(mimeEntity);

            doc.closeMIMEEntities(false, itemName);
        }
    }

    private Object pullJsonable(Document doc, BaseField field, Class<?> type)
            throws NotesException {
        String itemName = getDominoItemName(field);
        MIMEEntity mimeEntity = null;

        try {
            mimeEntity = DominoUtil.getMimeEntity(doc, itemName, false);

            if (mimeEntity != null) {
                TypeToken<?> t = field.getProperties().isList() ? TypeToken.getParameterized(
                        ArrayList.class, type) : TypeToken.get(type);

                return GsonLoader.INSTANCE.fromJson(mimeEntity.getContentAsText(), t.getType());
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
        for (BaseField field : wrapper.getFields()) {
            if (wrapper.isUpdated(field)) {
                push(wrapper, doc, field);
            }
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

            for (MIMEEntity attachment : attachments) {
                attachmentsByName.put(DominoUtil.getMimeEntityFilename(attachment), attachment);
            }

            // Removing files
            for (Iterator<Map.Entry<String, AttachmentFile>> iter = am.entrySet().iterator(); iter
                    .hasNext();) {
                Map.Entry<String, AttachmentFile> entry = iter.next();

                if (entry.getValue().isRemove()) {
                    if (attachmentsByName.containsKey(entry.getValue().getName())) {
                        attachmentsByName.get(entry.getValue().getName()).remove();
                    }

                    iter.remove();
                }
            }

            // Header cleaning on empty entity
            if (mimeEntity.getNextEntity() == null) {
                for (Iterator<?> iter = mimeEntity.getHeaderObjects().iterator(); iter.hasNext();) {
                    ((MIMEHeader) iter.next()).remove();
                }
            }

            // Adding files
            for (AttachmentFile af : am.values()) {
                File uploadedFile = af.getUploadedFile();

                if (uploadedFile == null) {
                    continue;
                }

                InputStream is = null;

                try {
                    is = new FileInputStream(uploadedFile);

                    pushInputStream(is,
                            af.getName(),
                            attachmentsByName.get(af.getName()),
                            mimeEntity);
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

    private void pushInputStream(InputStream is, String fileName,
            MIMEEntity child, MIMEEntity parent) throws NotesException, IOException {
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

            child.setContentFromBytes(stm,
                    "application/octet-stream",
                    MIMEEntity.ENC_IDENTITY_BINARY);

            stm.close();
        } finally {
            DominoUtil.recycle(stm);
        }
    }

    private void pushJsonable(Object jsonable, Document doc, BaseField field)
            throws NotesException {
        String itemName = getDominoItemName(field);

        Stream stm = null;
        MIMEEntity mimeEntity = null;

        try {
            stm = factory.getSession().createStream();
            stm.writeText(GsonLoader.INSTANCE.toJson(jsonable));

            mimeEntity = DominoUtil.getMimeEntity(doc, itemName, true);
            mimeEntity.setContentFromText(stm,
                    "application/json;charset=UTF-8",
                    MIMEEntity.ENC_NONE);

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

    protected Document resolveDocument(String notesUrl) throws DaoException, NotesException {
        Objects.requireNonNull(notesUrl, "Notes URL cannot be null");

        Base base = factory.getSession().resolve(notesUrl);

        if (base == null || !(base instanceof Document)) {
            throw new DaoResolutionException(notesUrl);
        }

        Document doc = (Document) base;

        DominoUtil.setDefaultOptions(doc);

        return doc;
    }

}
