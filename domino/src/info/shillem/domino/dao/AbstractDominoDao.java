package info.shillem.domino.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import info.shillem.dao.Query;
import info.shillem.dao.SearchQuery;
import info.shillem.dao.UrlQuery;
import info.shillem.dao.lang.DaoException;
import info.shillem.dao.lang.DaoQueryException;
import info.shillem.dao.lang.DaoRecordException;
import info.shillem.domino.factory.DominoFactory;
import info.shillem.domino.util.DeletionType;
import info.shillem.domino.util.DominoSilo;
import info.shillem.domino.util.DominoStream;
import info.shillem.domino.util.DominoUtil;
import info.shillem.domino.util.FullTextSearchQueryConverter;
import info.shillem.domino.util.MimeContentType;
import info.shillem.domino.util.ViewManager;
import info.shillem.domino.util.ViewMatch;
import info.shillem.dto.AttachmentFile;
import info.shillem.dto.AttachmentMap;
import info.shillem.dto.BaseDto;
import info.shillem.dto.BaseDto.SchemaFilter;
import info.shillem.dto.BaseField;
import info.shillem.dto.JsonValue;
import info.shillem.util.CastUtil;
import info.shillem.util.CollectionUtil;
import info.shillem.util.IOUtil;
import info.shillem.util.StringUtil;
import info.shillem.util.Unthrow;
import lotus.domino.Base;
import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.DbDirectory;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.MIMEEntity;
import lotus.domino.MIMEHeader;
import lotus.domino.NotesError;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.Stream;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;

public abstract class AbstractDominoDao<T extends BaseDto<E>, E extends Enum<E> & BaseField> {

    private static class GsonLoader {
        private static final Gson INSTANCE = new GsonBuilder().create();
    }

    private static final Pattern DOC_NOTES_URL_PATTERN = Pattern.compile("^notes:\\/\\/"
            + "(?<host>\\w+)(?>@\\w+)*\\/"
            + "(?<db>(?>_{2})?(?>(?<replicaId>\\w{32}|\\w{16})|[\\w\\/]+)(?>\\.nsf)?)(?>\\/(?>0|\\w{32}))?\\/"
            + "(?<documentId>\\w+)(?>\\?OpenDocument)?$",
            Pattern.CASE_INSENSITIVE);

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

    protected void create(T wrapper, DominoSilo silo) throws DaoException, NotesException {
        create(wrapper, silo, null);
    }

    protected void create(T wrapper, DominoSilo silo, String formName)
            throws DaoException, NotesException {
        if (!wrapper.isNew()) {
            throw new IllegalArgumentException(
                    "Creation cannot be performed on an existing record");
        }

        Document doc = null;

        try {
            doc = createDocument(silo, formName);

            pushWrapper(wrapper, doc);

            doc.save();

            wrapper.setId(doc.getUniversalID());
            wrapper.commit(DominoUtil.getLastModified(doc));
        } finally {
            DominoUtil.recycle(doc);
        }
    }

    protected Document createDocument(DominoSilo silo) throws NotesException {
        return createDocument(silo, null);
    }

    protected Document createDocument(DominoSilo silo, String formName) throws NotesException {
        Document doc = factory.setDefaults(silo.getDatabase().createDocument());

        if (formName != null) {
            Item itm = doc.replaceItemValue("Form", formName);

            DominoUtil.recycle(itm);
        }

        return doc;
    }

    protected boolean deleteDocument(DominoSilo silo, Document doc, DeletionType deletion)
            throws NotesException {
        if (silo.isDocumentLockingEnabled()) {
            if (!doc.lock()) {
                throw new RuntimeException("Unable to acquire lock on note " + doc.getNoteID());
            }
        }

        return doc.removePermanently(deletion.isHard());
    }

    private Optional<Document> getDocumentById(Database database, String id)
            throws NotesException {
        Objects.requireNonNull(id, "Id cannot be null");

        Document doc = id.length() == 32
                ? database.getDocumentByUNID(id)
                : database.getDocumentByID(id);

        if (doc == null) {
            return Optional.empty();
        }

        return Optional.of(factory.setDefaults(doc));
    }

    protected Optional<Document> getDocumentById(DominoSilo silo, String id)
            throws NotesException {
        return getDocumentById(silo.getDatabase(), id);
    }

    protected Optional<Document> getDocumentByKey(View view, Object key, ViewMatch match)
            throws NotesException {
        Objects.requireNonNull(key, "Key cannot be null");

        Document doc = view.getDocumentByKey(key, match.isExact());

        if (doc == null) {
            return Optional.empty();
        }

        return Optional.of(factory.setDefaults(doc));
    }

    protected Optional<Document> getDocumentByKeys(View view, Collection<?> keys, ViewMatch match)
            throws NotesException {
        Objects.requireNonNull(keys, "Key(s) cannot be null");

        if (keys.isEmpty()) {
            throw new IllegalArgumentException("Key(s) cannot be empty");
        }

        Document doc = view.getDocumentByKey(CollectionUtil.asVector(keys), match.isExact());

        if (doc == null) {
            return Optional.empty();
        }

        return Optional.of(factory.setDefaults(doc));
    }

    protected String getDocumentItemName(E field) {
        return field.name();
    }

    protected ViewEntryCollection getViewEntriesByKey(
            View view, Object key, ViewMatch match) throws NotesException {
        Objects.requireNonNull(key, "Key cannot be null");

        return view.getAllEntriesByKey(key, match.isExact());
    }

    protected ViewEntryCollection getViewEntriesByKeys(
            View view, Collection<?> keys, ViewMatch match) throws NotesException {
        Objects.requireNonNull(keys, "Key(s) cannot be null");

        if (keys.isEmpty()) {
            throw new IllegalArgumentException("Key(s) cannot be empty");
        }

        return view.getAllEntriesByKey(CollectionUtil.asVector(keys), match.isExact());
    }

    protected Optional<ViewEntry> getViewEntryByKey(
            View view, Object key, ViewMatch match) throws NotesException {
        Objects.requireNonNull(key, "Key cannot be null");

        ViewEntry entry = view.getEntryByKey(key, match.isExact());

        if (entry == null) {
            return Optional.empty();
        }

        return Optional.of(factory.setDefaults(entry));
    }

    protected Optional<ViewEntry> getViewEntryByKeys(
            View view, Collection<?> keys, ViewMatch match) throws NotesException {
        Objects.requireNonNull(keys, "Key(s) cannot be null");

        if (keys.isEmpty()) {
            throw new IllegalArgumentException("Key(s) cannot be empty");
        }

        ViewEntry entry = view.getEntryByKey(CollectionUtil.asVector(keys), match.isExact());

        if (entry == null) {
            return Optional.empty();
        }

        return Optional.of(factory.setDefaults(entry));
    }

    protected void pullDocument(Document doc, T wrapper, Query<E> query) throws NotesException {
        for (E field : query.getSchema()) {
            pullItem(field, doc, wrapper, query.getLocale());
        }

        if (!doc.isNewNote()) {
            wrapper.setId(doc.getUniversalID());
            wrapper.setLastModified(DominoUtil.getLastModified(doc));
        }

        if (query.isFetchDatabaseUrl()) {
            wrapper.setDatabaseUrl(doc.getNotesURL());
        }
    }

    protected void pullItem(E field, Document doc, T wrapper, Locale locale) {
        Class<? extends Serializable> type = field.getProperties().getType();

        try {
            if (AttachmentMap.class.isAssignableFrom(type)) {
                wrapper.presetValue(field, pullItemAttachmentMap(field, doc));
            } else if (JsonValue.class.isAssignableFrom(type)) {
                wrapper.presetValue(field, pullItemJsonValue(field, doc));
            } else if (field.getProperties().isList()) {
                wrapper.presetValue(field, DominoUtil.getItemValues(
                        doc,
                        getDocumentItemName(field),
                        (value) -> Unthrow.on(() -> pullValue(type, value))));
            } else {
                wrapper.presetValue(field, DominoUtil.getItemValue(
                        doc,
                        getDocumentItemName(field),
                        (value) -> Unthrow.on(() -> pullValue(type, value))));
            }
        } catch (Exception e) {
            throw wrappedPullItemException(e, field, doc);
        }
    }

    private AttachmentMap pullItemAttachmentMap(E field, Document doc)
            throws IllegalAccessException, IllegalArgumentException, InstantiationException,
            InvocationTargetException, NoSuchMethodException, NotesException, SecurityException {
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
                AttachmentMap am = (AttachmentMap) field.getProperties()
                        .getType()
                        .getDeclaredConstructor()
                        .newInstance();

                for (MIMEEntity attachment : attachments) {
                    DominoUtil
                            .getMimeEntityAttachmentFilename(attachment)
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

    private Object pullItemJsonValue(E field, Document doc) throws NotesException {
        String itemName = getDocumentItemName(field);
        MIMEEntity mimeEntity = null;

        try {
            mimeEntity = DominoUtil.getMimeEntity(doc, itemName, false);

            if (mimeEntity == null) {
                return null;
            }

            Class<?> type = CastUtil.toAnyClass(field.getProperties().getType());

            TypeToken<?> t = field.getProperties().isList()
                    ? TypeToken.getParameterized(ArrayList.class, type)
                    : TypeToken.get(type);

            return GsonLoader.INSTANCE.fromJson(mimeEntity.getContentAsText(), t.getType());
        } finally {
            DominoUtil.recycle(mimeEntity);

            doc.closeMIMEEntities(false, itemName);
        }
    }

    private <V> V pullValue(Class<V> type, Object value) throws NotesException {
        if (type.isEnum()) {
            if (value instanceof String) {
                return type.cast(StringUtil.enumFromString(type, (String) value));
            }
        } else if (type == Boolean.class) {
            return type.cast(Boolean.valueOf((String) value));
        } else if (Number.class.isAssignableFrom(type)) {
            if (value == null) {
                return null;
            }

            Number num = (Number) value;

            if (type == Integer.class) {
                return type.cast(num.intValue());
            }

            if (type == Long.class) {
                return type.cast(num.longValue());
            }

            if (type == Double.class) {
                return type.cast(num.doubleValue());
            }

            if (type == BigDecimal.class) {
                return type.cast(new BigDecimal(num.toString()));
            }
        } else if (type == Date.class && value instanceof DateTime) {
            return type.cast(((DateTime) value).toJavaDate());
        }

        return type.cast(value);
    }

    protected void pullViewEntry(
            ViewEntry entry, T wrapper, Query<E> query, ViewManager manager)
            throws NotesException {
        List<?> columnValues = entry.getColumnValues();

        for (E field : query.getSchema()) {
            String fieldName = getDocumentItemName(field);
            int columnIndex = manager.indexOfColumn(fieldName);

            if (columnIndex < 0) {
                throw new IllegalStateException(
                        "Column for schema field " + field + " does not exist");
            }

            Object value = columnValues.get(columnIndex);
            Class<? extends Serializable> type = field.getProperties().getType();

            try {
                if (field.getProperties().isList()) {
                    List<?> values =
                            (value instanceof List) ? ((List<?>) value) : Arrays.asList(value);

                    wrapper.presetValue(field, values.stream()
                            .map((val) -> Unthrow.on(() -> pullValue(type, val)))
                            .collect(Collectors.toList()));
                } else {
                    Object val;

                    if (value instanceof List) {
                        List<?> values = (List<?>) value;

                        val = values.isEmpty() ? null : values.get(0);

                        if (val instanceof String && ((String) val).isEmpty()) {
                            val = null;
                        }
                    } else if (value instanceof String && ((String) value).isEmpty()) {
                        val = null;
                    } else {
                        val = value;
                    }

                    wrapper.presetValue(field, pullValue(type, val));
                }
            } catch (Exception e) {
                throw wrappedPullColumnValueException(e, field, entry);
            }
        }

        wrapper.setId(entry.getUniversalID());

        int lastModifiedIndex = manager.indexOfColumn("$lastModified");

        if (lastModifiedIndex > -1) {
            wrapper.setLastModified(pullValue(
                    Date.class,
                    columnValues.get(lastModifiedIndex)));
        }

        if (query.isFetchDatabaseUrl()) {
            wrapper.setDatabaseUrl(manager.getDatabaseUrl(entry));
        }
    }

    private Object pushValue(Object value) throws NotesException {
        if (value instanceof BigDecimal || value instanceof Long) {
            return ((Number) value).doubleValue();
        }

        if (value instanceof Boolean || value instanceof Enum) {
            return value.toString();
        }

        if (value instanceof Date) {
            return factory.getSession().createDateTime((Date) value);
        }

        return value;
    }

    protected void pushWrapper(T wrapper, Document doc) throws NotesException {
        for (E field : wrapper.getSchema(SchemaFilter.UPDATED)) {
            pushWrapperField(field, wrapper, doc);
        }
    }

    protected void pushWrapperField(E field, T wrapper, Document doc) throws NotesException {
        Class<? extends Serializable> type = field.getProperties().getType();

        if (AttachmentMap.class.isAssignableFrom(type)) {
            pushWrapperFieldAttachmentMap(field, wrapper, doc);

            return;
        }

        if (JsonValue.class.isAssignableFrom(type)) {
            pushWrapperFieldJsonValue(field, wrapper, doc);

            return;
        }

        Object value = wrapper.getValue(field);

        String itemName = getDocumentItemName(field);

        if (value == null) {
            doc.removeItem(itemName);
        } else if (field.getProperties().isList()) {
            Vector<Object> values = new Vector<>();

            try {
                for (Object o : (List<?>) value) {
                    values.add(pushValue(o));
                }

                doc.replaceItemValue(itemName, values);
            } finally {
                DominoUtil.recycle(values);
            }
        } else {
            Object transformedValue = pushValue(value);

            try {
                doc.replaceItemValue(itemName, transformedValue);
            } finally {
                if (transformedValue instanceof Base) {
                    DominoUtil.recycle((Base) transformedValue);
                }
            }
        }
    }

    private void pushWrapperFieldAttachmentMap(E field, T wrapper, Document doc)
            throws NotesException {
        AttachmentMap am = wrapper.getValue(field, AttachmentMap.class);

        if (am == null) {
            return;
        }

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

    private void pushWrapperFieldJsonValue(E field, T wrapper, Document doc)
            throws NotesException {
        String itemName = getDocumentItemName(field);

        Object jsonValue = wrapper.getValue(field);

        if (jsonValue == null) {
            doc.removeItem(itemName);

            return;
        }

        Stream stm = null;
        MIMEEntity mimeEntity = null;

        try {
            stm = factory.getSession().createStream();
            stm.writeText(GsonLoader.INSTANCE.toJson(jsonValue));

            mimeEntity = DominoUtil.getMimeEntity(doc, itemName, true);
            mimeEntity.setContentFromText(stm,
                    "application/json;charset=UTF-8",
                    MIMEEntity.ENC_NONE);

            stm.close();
        } finally {
            if (doc.hasItem(itemName)) {
                doc.closeMIMEEntities(true, itemName);
            }

            DominoUtil.recycle(stm, mimeEntity);
        }
    }

    protected Optional<Document> resolveDocument(UrlQuery<E> query)
            throws DaoException, NotesException {
        return resolveDocumentUrl(((UrlQuery<E>) query).getUrl());
    }

    protected Optional<Document> resolveDocumentUrl(String url)
            throws DaoException, NotesException {
        if (Objects.requireNonNull(url, "Url cannot be null").isEmpty()) {
            throw new IllegalArgumentException("Url cannot be empty");
        }

        Matcher matcher = DOC_NOTES_URL_PATTERN.matcher(url);

        if (!matcher.matches()) {
            throw DaoRecordException.asMissing(url);
        }

        String host = matcher.group("host");
        String db = matcher.group("db");
        String replicaId = matcher.group("replicaId");
        String documentId = matcher.group("documentId");

        // TODO This handle should be better managed
        // It cannot be recycled because it could be a silo database used elsewhere
        Session session = factory.getSession();
        Database database = null;

        if (Objects.isNull(replicaId)) {
            database = session.getDatabase(host, db);
        } else {
            DbDirectory dir = null;

            try {
                dir = session.getDbDirectory(host);

                database = dir.openDatabaseByReplicaID(replicaId);
            } catch (NotesException e) {
                throw new RuntimeException(e);
            } finally {
                DominoUtil.recycle(dir);
            }
        }

        return getDocumentById(database, documentId);
    }

    protected void update(T wrapper, DominoSilo silo) throws DaoException, NotesException {
        if (wrapper.isNew()) {
            throw new IllegalArgumentException("Update cannot be performed on a new record");
        }

        Document doc = null;

        try {
            doc = getDocumentById(silo, wrapper.getId())
                    .orElseThrow(() -> DaoRecordException.asMissing(wrapper.getId()));

            checkTimestampAlignment(wrapper, doc);

            pushWrapper(wrapper, doc);

            doc.save();

            wrapper.commit(DominoUtil.getLastModified(doc));
        } finally {
            DominoUtil.recycle(doc);
        }
    }

    protected T wrapDocument(Document doc, Supplier<T> supplier, Query<E> query)
            throws NotesException {
        T wrapper = supplier.get();

        pullDocument(doc, wrapper, query);

        return wrapper;
    }

    protected List<T> wrapFullTextSearch(View vw, Supplier<T> supplier, SearchQuery<E> query)
            throws DaoException, NotesException {
        String syntax = new FullTextSearchQueryConverter<E>(
                query, this::getDocumentItemName).toString();

        try {
            vw.FTSearchSorted(syntax, query.getMaxCount());

            try (java.util.stream.Stream<Document> stream = DominoStream.stream(vw)) {
                return stream
                        .map(doc -> Unthrow.on(() -> wrapDocument(doc, supplier, query)))
                        .collect(Collectors.toList());
            }
        } catch (NotesException e) {
            if (e.id == NotesError.NOTES_ERR_NOT_IMPLEMENTED || !e.text.contains("query")) {
                throw new RuntimeException(e);
            }

            throw DaoQueryException.asInvalid(syntax);
        }
    }

    protected RuntimeException wrappedPullColumnValueException(
            Exception e, E field, ViewEntry entry) {
        try {
            return new RuntimeException(String.format(
                    "Unable to pull column value %s from document %s",
                    field.name(),
                    entry.getNoteID()),
                    e);
        } catch (NotesException ne) {
            return new RuntimeException(
                    String.format("Unable to pull column value %s", field.name()), e);
        }
    }

    protected RuntimeException wrappedPullItemException(Exception e, E field, Document doc) {
        try {
            return new RuntimeException(String.format(
                    "Unable to pull item %s from document %s",
                    field.name(),
                    doc.getNoteID()),
                    e);
        } catch (NotesException ne) {
            return new RuntimeException(String.format("Unable to pull item %s", field.name()), e);
        }
    }

    protected T wrapViewEntry(
            ViewEntry entry, Supplier<T> supplier, Query<E> query, ViewManager manager)
            throws NotesException {
        T wrapper = supplier.get();

        pullViewEntry(entry, wrapper, query, manager);

        return wrapper;
    }

}
