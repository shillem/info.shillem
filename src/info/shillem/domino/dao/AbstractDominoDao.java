package info.shillem.domino.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import info.shillem.dao.Query;
import info.shillem.dao.lang.DaoException;
import info.shillem.dao.lang.DaoRecordException;
import info.shillem.dao.lang.DaoResolutionException;
import info.shillem.domino.util.DominoFactory;
import info.shillem.domino.util.DominoI18n;
import info.shillem.domino.util.DominoSilo;
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

public abstract class AbstractDominoDao<T extends BaseDto<E>, E extends Enum<E> & BaseField> {

    private static class GsonLoader {
        private static final Gson INSTANCE = new GsonBuilder().create();
    }

    protected final DominoFactory factory;

    protected AbstractDominoDao(DominoFactory factory) {
        this.factory = factory;
    }

    protected void checkTimestampAlignment(T wrapper, Document doc)
            throws DaoException, NotesException {
        Date wrapperDate = wrapper.getLastModified();
        Date docDate = DominoUtil.getLastModified(doc);

        if (wrapperDate != null
                && wrapperDate.getTime() / 1000 != docDate.getTime() / 1000) {
            throw DaoRecordException.asDirty(wrapper.getId(), wrapperDate, docDate);
        }
    }

    protected Document createDocument(Database database) throws NotesException {
        Document doc = database.createDocument();

        DominoUtil.setEncouragedOptions(doc);

        return doc;
    }

    protected String getDocumentItemName(E field) {
        return field.toString();
    }

    private AttachmentMap getPullAttachmentMap(Document doc, E field) throws NotesException {
        String itemName = getDocumentItemName(field);
        MIMEEntity mimeEntity = null;

        try {
            mimeEntity = DominoUtil.getMimeEntity(doc, itemName, false);

            if (mimeEntity == null) {
                return null;
            }

            List<MIMEEntity> attachments = DominoUtil
                    .getMimeEntitiesByContentType(mimeEntity, MimeContentType.ATTACHMENT);

            if (attachments.isEmpty()) {
                return null;
            }

            try {
                AttachmentMap am = new AttachmentMap();

                for (MIMEEntity attachment : attachments) {
                    DominoUtil.getMimeEntityAttachmentFilename(attachment)
                            .ifPresent(am::put);
                }

                return am;
            } finally {
                DominoUtil.recycle(attachments);
            }
        } finally {
            DominoUtil.recycle(mimeEntity);
        }
    }

    private Object getPullJsonValue(Document doc, E field, Class<?> type)
            throws NotesException {
        String itemName = getDocumentItemName(field);
        MIMEEntity mimeEntity = null;

        try {
            mimeEntity = DominoUtil.getMimeEntity(doc, itemName, false);

            if (mimeEntity != null) {
                TypeToken<?> t = field.getProperties().isList()
                        ? TypeToken.getParameterized(ArrayList.class, type)
                        : TypeToken.get(type);

                return GsonLoader.INSTANCE.fromJson(mimeEntity.getContentAsText(), t.getType());
            }

            return null;
        } finally {
            DominoUtil.recycle(mimeEntity);

            doc.closeMIMEEntities(false, itemName);
        }
    }

    private Object getPullValue(Object value, Class<?> type) {
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

    private Object getPushValue(Object value, Class<? extends Serializable> type)
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

    protected void pullDocument(Document doc, T wrapper, Query<E> query)
            throws NotesException {
        if (!DominoUtil.hasEncouragedOptions(doc)) {
            throw new IllegalArgumentException("Document is not treated with encouraged options");
        }

        for (E field : query.getSchema()) {
            pullItem(doc, wrapper, field, query.getLocale());
        }

        if (!doc.isNewNote()) {
            wrapper.setId(doc.getUniversalID());
            wrapper.setLastModified(DominoUtil.getLastModified(doc));
        }

        if (query.isFetchDatabaseUrl()) {
            wrapper.setDatabaseUrl(doc.getNotesURL());
        }
    }

    protected void pullEntry(ViewEntry entry, T wrapper, Query<E> query, List<String> columns)
            throws NotesException {
        if (!DominoUtil.hasEncouragedOptions(entry)) {
            throw new IllegalArgumentException("Entry is not treated with encouraged options");
        }

        for (E field : query.getSchema()) {
            String fieldName = getDocumentItemName(field);

            if (columns.contains(fieldName)) {
                List<?> columnValues = entry.getColumnValues();

                wrapper.presetValue(field, getPullValue(
                        columnValues.get(columns.indexOf(fieldName)),
                        field.getProperties().getType()));
            } else {
                throw new IllegalArgumentException(
                        "Unable to retrieve value for schema field " + field);
            }
        }

        wrapper.setId(entry.getUniversalID());

        if (query.isFetchDatabaseUrl()) {
            throw new UnsupportedOperationException(
                    "Database URL cannot be fetched from a view entry");
        }
    }

    protected void pullItem(Document doc, T wrapper, E field, Locale locale)
            throws NotesException {
        Class<? extends Serializable> type = field.getProperties().getType();

        if (type == AttachmentMap.class) {
            wrapper.presetValue(field, getPullAttachmentMap(doc, field));
        } else if (I18nValue.class.isAssignableFrom(type)) {
            wrapper.presetValue(field, DominoI18n.getValue(locale, doc, field));
        } else if (JsonValue.class.isAssignableFrom(type)) {
            wrapper.presetValue(field, getPullJsonValue(doc, field, type));
        } else if (field.getProperties().isList()) {
            wrapper.presetValue(field,
                    DominoUtil.getItemValues(
                            doc, getDocumentItemName(field), (value) -> getPullValue(value, type)));
        } else {
            wrapper.presetValue(field,
                    DominoUtil.getItemValue(
                            doc, getDocumentItemName(field), (value) -> getPullValue(value, type)));
        }
    }

    private void pushAttachmentMap(AttachmentMap am, Document doc, E field)
            throws NotesException {
        String itemName = getDocumentItemName(field);
        MIMEEntity mimeEntity = null;

        try {
            mimeEntity = DominoUtil.getMimeEntity(doc, itemName, true);

            List<MIMEEntity> attachments = DominoUtil
                    .getMimeEntitiesByContentType(mimeEntity, MimeContentType.ATTACHMENT);

            Map<String, MIMEEntity> attachmentsByName = new HashMap<>();

            for (MIMEEntity attachment : attachments) {
                DominoUtil.getMimeEntityAttachmentFilename(attachment)
                        .ifPresent(name -> attachmentsByName.put(name, attachment));
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

            // Adding files
            for (AttachmentFile af : am.values()) {
                File uploadedFile = af.getUploadedFile();

                if (uploadedFile == null) {
                    continue;
                }

                InputStream is = null;
                Stream stm = null;

                try {
                    is = new FileInputStream(uploadedFile);
                    stm = factory.getSession().createStream();
                    stm.setContents(is);

                    MIMEEntity child = attachmentsByName.get(af.getName());

                    if (child == null) {
                        child = mimeEntity.createChildEntity();
                        MIMEHeader header = child.createHeader("Content-Disposition");
                        header.setHeaderValAndParams(
                                "attachment; filename=\"" + af.getName() + "\"");
                    }

                    child.setContentFromBytes(stm,
                            "application/octet-stream",
                            MIMEEntity.ENC_IDENTITY_BINARY);

                    stm.close();
                } catch (FileNotFoundException e) {
                    throw new IllegalArgumentException(String.format(
                            "The file with path %s was not found", uploadedFile.getAbsolutePath()));
                } finally {
                    DominoUtil.recycle(stm);
                    IOUtil.close(is);
                }

                af.setUploadedFile(null);
            }

            // Clear entity if there are no attachments
            if (mimeEntity.getNextEntity() == null) {
                mimeEntity.remove();
            }
        } finally {

            if (doc.hasItem(itemName)) {
                doc.closeMIMEEntities(true, itemName);
            }

            DominoUtil.recycle(mimeEntity);
        }
    }

    private void pushJsonable(Object jsonable, Document doc, E field)
            throws NotesException {
        String itemName = getDocumentItemName(field);

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

    protected void pushWrapper(T wrapper, Document doc) throws NotesException {
        for (E field : wrapper.getFields()) {
            if (wrapper.isValueUpdated(field)) {
                pushWrapperValue(wrapper, doc, field);
            }
        }
    }

    protected void pushWrapperValue(T wrapper, Document doc, E field) throws NotesException {
        Class<? extends Serializable> type = field.getProperties().getType();
        Object value = wrapper.getValue(field);
        String itemName = getDocumentItemName(field);

        if (value == null) {
            doc.replaceItemValue(itemName, "");
        } else if (type == AttachmentMap.class) {
            pushAttachmentMap((AttachmentMap) value, doc, field);
        } else if (JsonValue.class.isAssignableFrom(type)) {
            pushJsonable(value, doc, field);
        } else if (field.getProperties().isList()) {
            Vector<Object> values = new Vector<>();

            try {
                for (Object o : (List<?>) value) {
                    values.add(getPushValue(o, type));
                }

                doc.replaceItemValue(itemName, values);
            } finally {
                factory.getSession().recycle(values);
            }
        } else {
            Object transformedValue = getPushValue(value, type);

            try {
                doc.replaceItemValue(itemName, transformedValue);
            } finally {
                if (transformedValue instanceof Base) {
                    DominoUtil.recycle((Base) transformedValue);
                }
            }
        }
    }

    protected Document resolveDocument(String notesUrl) throws DaoException, NotesException {
        Objects.requireNonNull(notesUrl, "Notes URL cannot be null");

        try {
            Base base = factory.getSession().resolve(notesUrl);

            if (!(base instanceof Document)) {
                throw new RuntimeException();
            }

            Document doc = (Document) base;

            DominoUtil.setEncouragedOptions(doc);

            return doc;
        } catch (Exception e) {
            throw new DaoResolutionException(notesUrl);
        }
    }

    protected void update(List<T> wrappers, DominoSilo silo) throws DaoException, NotesException {
        Document doc = null;

        for (T wrapper : wrappers) {
            if (wrapper.isNew()) {
                throw new IllegalStateException("Update cannot be performed on a new object");
            }

            try {
                doc = silo.getDocumentById(wrapper.getId());

                checkTimestampAlignment(wrapper, doc);

                pushWrapper(wrapper, doc);

                doc.save();

                wrapper.commit(DominoUtil.getLastModified(doc));
            } finally {
                DominoUtil.recycle(doc);
            }
        }
    }

}
