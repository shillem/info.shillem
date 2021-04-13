package info.shillem.domino.dao;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import info.shillem.dao.Query;
import info.shillem.dao.lang.DaoException;
import info.shillem.dao.lang.DaoRecordException;
import info.shillem.domino.factory.DominoFactory;
import info.shillem.domino.util.DeletionType;
import info.shillem.domino.util.DominoSilo;
import info.shillem.domino.util.DominoUtil;
import info.shillem.domino.util.MimeContentType;
import info.shillem.domino.util.VwMatch;
import info.shillem.domino.util.VwWalker;
import info.shillem.dto.AttachedFile;
import info.shillem.dto.AttachedFiles;
import info.shillem.dto.BaseDto;
import info.shillem.dto.BaseDto.SchemaFilter;
import info.shillem.dto.BaseField;
import info.shillem.dto.ValueType;
import info.shillem.dto.JsonValue;
import info.shillem.util.CastUtil;
import info.shillem.util.CollectionUtil;
import info.shillem.util.IOUtil;
import info.shillem.util.JsonHandler;
import info.shillem.util.StringUtil;
import info.shillem.util.Unthrow;
import lotus.domino.Base;
import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.DbDirectory;
import lotus.domino.Document;
import lotus.domino.MIMEEntity;
import lotus.domino.MIMEHeader;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;

public abstract class AbstractDominoDao<T extends BaseDto<E>, E extends Enum<E> & BaseField> {

    private static final Pattern DOC_NOTES_URL_PATTERN = Pattern.compile("^notes:\\/\\/"
            + "(?<host>\\w+)(?>@\\w+)*\\/"
            + "(?<db>(?>_{2})?(?>(?<replicaId>\\w{32}|\\w{16})|[\\w\\/]+)(?>\\.nsf)?)(?>\\/(?>0|\\w{32}))?\\/"
            + "(?<documentId>\\w+)(?>\\?OpenDocument)?$",
            Pattern.CASE_INSENSITIVE);

    protected static final JsonHandler JSON = new JsonHandler(new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(Include.NON_NULL)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE));

    protected final DominoFactory factory;

    protected AbstractDominoDao(DominoFactory factory) {
        this.factory = factory;
    }

    protected void checkTimestampAlignment(
            T wrapper,
            Document doc)
            throws DaoException, NotesException {
        Date wrapperDate = wrapper.getLastModified();
        Date docDate = DominoUtil.getLastModified(doc);

        if (wrapperDate != null && wrapperDate.getTime() / 1000 != docDate.getTime() / 1000) {
            throw DaoRecordException.asDirty(wrapper.getId(), wrapperDate, docDate);
        }
    }

    protected void create(T wrapper, DominoSilo silo) throws DaoException, NotesException {
        if (!wrapper.isNew()) {
            throw new IllegalArgumentException(
                    "Creation cannot be performed on an existing record");
        }

        Document doc = null;

        try {
            doc = createDocument(silo);

            pushWrapper(wrapper, doc);

            doc.save();

            wrapper.setId(doc.getUniversalID());
            wrapper.commit(DominoUtil.getLastModified(doc));
        } finally {
            DominoUtil.recycle(doc);
        }
    }

    protected Document createDocument(DominoSilo silo) throws NotesException {
        return factory.setDefaults(silo.getDatabase().createDocument());
    }

    protected boolean delete(DominoSilo silo, DeletionType deletionType, Query<E> query) {
        query.require(Query.Type.ID);

        Document doc = null;

        try {
            doc = getDocumentById(silo, query.getId())
                    .orElseThrow(() -> DaoRecordException.asMissing(query.getId()));

            return deleteDocument(silo, doc, deletionType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            DominoUtil.recycle(doc);
        }
    }

    protected boolean deleteDocument(
            DominoSilo silo,
            Document doc,
            DeletionType deletionType)
            throws NotesException {
        if (silo.isDocumentLockingEnabled()) {
            if (!doc.lock()) {
                throw new RuntimeException(
                        "Unable to acquire lock on note ".concat(doc.getNoteID()));
            }
        }

        return doc.removePermanently(deletionType.asBoolean());
    }

    private Optional<Document> getDocumentById(
            Database database,
            String id)
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

    protected Optional<Document> getDocumentById(
            DominoSilo silo,
            String id)
            throws NotesException {
        return getDocumentById(silo.getDatabase(), id);
    }

    protected Optional<Document> getDocumentByKey(
            View view,
            Object key,
            VwMatch match)
            throws NotesException {
        Objects.requireNonNull(key, "Key cannot be null");

        Document doc = view.getDocumentByKey(key, match.asBoolean());

        if (doc == null) {
            return Optional.empty();
        }

        return Optional.of(factory.setDefaults(doc));
    }

    protected Optional<Document> getDocumentByKeys(
            View view,
            Collection<?> keys,
            VwMatch match)
            throws NotesException {
        Objects.requireNonNull(keys, "Key(s) cannot be null");

        if (keys.isEmpty()) {
            throw new IllegalArgumentException("Key(s) cannot be empty");
        }

        Document doc = view.getDocumentByKey(CollectionUtil.asVector(keys), match.asBoolean());

        if (doc == null) {
            return Optional.empty();
        }

        return Optional.of(factory.setDefaults(doc));
    }

    protected String getDocumentItemName(E field) {
        return field.name();
    }

    protected ViewEntryCollection getViewEntriesByKey(
            View view,
            Object key,
            VwMatch match)
            throws NotesException {
        Objects.requireNonNull(key, "Key cannot be null");

        return view.getAllEntriesByKey(key, match.asBoolean());
    }

    protected ViewEntryCollection getViewEntriesByKeys(
            View view, Collection<?> keys,
            VwMatch match)
            throws NotesException {
        Objects.requireNonNull(keys, "Key(s) cannot be null");

        if (keys.isEmpty()) {
            throw new IllegalArgumentException("Key(s) cannot be empty");
        }

        return view.getAllEntriesByKey(CollectionUtil.asVector(keys), match.asBoolean());
    }

    protected Optional<ViewEntry> getViewEntryByKey(
            View view, Object key,
            VwMatch match)
            throws NotesException {
        Objects.requireNonNull(key, "Key cannot be null");

        ViewEntry entry = view.getEntryByKey(key, match.asBoolean());

        if (entry == null) {
            return Optional.empty();
        }

        return Optional.of(factory.setDefaults(entry));
    }

    protected Optional<ViewEntry> getViewEntryByKeys(
            View view,
            Collection<?> keys,
            VwMatch match)
            throws NotesException {
        Objects.requireNonNull(keys, "Key(s) cannot be null");

        if (keys.isEmpty()) {
            throw new IllegalArgumentException("Key(s) cannot be empty");
        }

        ViewEntry entry = view.getEntryByKey(CollectionUtil.asVector(keys), match.asBoolean());

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

        if (query.containsOption("FETCH_DATABASE_URL")) {
            wrapper.setDatabaseUrl(doc.getNotesURL());
        }
    }

    protected void pullItem(E field, Document doc, T wrapper, Locale locale) {
        Class<? extends Serializable> type = field.getValueType().getValueClass();

        try {
            if (AttachedFiles.class.isAssignableFrom(type)) {
                wrapper.presetValue(field, pullItemAttachedFiles(field, doc));
            } else if (JsonValue.class.isAssignableFrom(type)) {
                wrapper.presetValue(field, pullItemJsonValue(field, doc));
            } else if (field.getValueType().isCollection()) {
                wrapper.presetValue(field, DominoUtil.getItemValues(
                        doc,
                        getDocumentItemName(field),
                        (val) -> Unthrow.on(() -> pullValue(type, val)),
                        field.getValueType()::newCollection));
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

    private AttachedFiles pullItemAttachedFiles(E field, Document doc) throws NotesException {
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

            AttachedFiles files;

            try {
                files = (AttachedFiles) field.getValueType()
                        .getValueClass()
                        .getDeclaredConstructor()
                        .newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            try {
                for (MIMEEntity attachment : attachments) {
                    DominoUtil
                            .getMimeEntityAttachmentFilename(attachment)
                            .ifPresent(files::add);
                }

                return files;
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

            ValueType props = field.getValueType();
            JavaType type;

            if (props.isCollection()) {
                type = JSON
                        .getTypeFactory()
                        .constructCollectionType(props.getCollectionClass(), props.getValueClass());
            } else {
                type = JSON
                        .getTypeFactory()
                        .constructType(props.getValueClass());
            }

            return JSON.deserialize(mimeEntity.getContentAsText(), type);
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
            ViewEntry entry,
            T wrapper,
            Query<E> query,
            VwWalker<E> walker)
            throws NotesException {
        List<?> columnValues = entry.getColumnValues();

        for (E field : query.getSchema()) {
            String fieldName = getDocumentItemName(field);
            ValueType props = field.getValueType();
            int columnIndex = walker.indexOfColumn(fieldName);

            if (columnIndex < 0) {
                throw new IllegalStateException(
                        "Column for schema field ".concat(field.name()).concat(" does not exist"));
            }

            Object value = columnValues.get(columnIndex);
            Class<? extends Serializable> type = props.getValueClass();

            try {
                if (props.isCollection()) {
                    List<Object> values;

                    if (value instanceof List) {
                        values = CastUtil.toAnyList((List<?>) value);
                    } else if (!(value instanceof String && ((String) value).isEmpty())) {
                        values = Arrays.asList(value);
                    } else {
                        values = Collections.emptyList();
                    }
 
                    wrapper.presetValue(field, values.stream()
                            .map((val) -> Unthrow.on(() -> pullValue(type, val)))
                            .collect(Collectors.toCollection(props::newCollection)));
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

        int lastModifiedIndex = walker.indexOfColumn("$lastModified");

        if (lastModifiedIndex > -1) {
            wrapper.setLastModified(pullValue(
                    Date.class,
                    columnValues.get(lastModifiedIndex)));
        }

        if (query.containsOption("FETCH_DATABASE_URL")) {
            wrapper.setDatabaseUrl(walker.getDatabaseUrl(entry));
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
        Class<? extends Serializable> type = field.getValueType().getValueClass();

        if (AttachedFiles.class.isAssignableFrom(type)) {
            pushWrapperFieldAttachedFiles(field, wrapper, doc);

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
        } else if (field.getValueType().isCollection()) {
            Vector<Object> values = new Vector<>();

            try {
                for (Object o : (Collection<?>) value) {
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

    private void pushWrapperFieldAttachedFiles(
            E field,
            T wrapper,
            Document doc)
            throws NotesException {
        AttachedFiles files = wrapper.getValue(field, AttachedFiles.class);

        if (files == null) {
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
            List<AttachedFile> removals = files.getAll().stream()
                    .filter(AttachedFile::isRemove)
                    .collect(Collectors.toList());

            removals.forEach((file) -> Unthrow.on(() -> {
                if (attachmentsByName.containsKey(file.getName())) {
                    attachmentsByName.get(file.getName()).remove();
                }

                files.remove(file);
            }));

            // Adding files
            List<AttachedFile> additions = files.getAll().stream()
                    .filter((f) -> f.getFile() != null)
                    .collect(Collectors.toList());

            for (AttachedFile af : additions) {
                InputStream is;

                try {
                    is = new FileInputStream(af.getFile());
                } catch (FileNotFoundException e) {
                    throw new IllegalArgumentException(String.format(
                            "The file %s with path %s was not found",
                            af.getName(),
                            af.getFile().getAbsolutePath()));
                }

                lotus.domino.Stream stm = null;

                try {
                    stm = factory.getSession().createStream();
                    stm.setContents(is);

                    MIMEEntity child = attachmentsByName.get(af.getName());

                    if (child == null) {
                        child = mimeEntity.createChildEntity();
                        MIMEHeader header = child.createHeader("Content-Disposition");
                        header.setHeaderValAndParams(
                                "attachment; filename=\"".concat(af.getName()).concat("\""));
                    }

                    child.setContentFromBytes(stm,
                            "application/octet-stream",
                            MIMEEntity.ENC_IDENTITY_BINARY);

                    stm.close();
                } finally {
                    DominoUtil.recycle(stm);
                    IOUtil.close(is);
                }

                af.unlink();
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

        lotus.domino.Stream stm = null;
        MIMEEntity mimeEntity = null;

        try {
            stm = factory.getSession().createStream();
            stm.writeText(JSON.serialize(jsonValue));

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

    protected Optional<Document> resolveDocumentByUrl(String url) throws NotesException {
        Matcher matcher = DOC_NOTES_URL_PATTERN.matcher(url);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(url);
        }

        String host = matcher.group("host");
        String db = matcher.group("db");
        String replicaId = matcher.group("replicaId");
        String documentId = matcher.group("documentId");

        // TODO This handle should be better managed maybe?
        // It cannot be recycled because it could be a silo database used elsewhere
        Session session = factory.getSession();
        Database database = null;

        if (replicaId == null) {
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

    protected T wrapDocument(
            Document doc,
            Supplier<T> supplier,
            Query<E> query)
            throws NotesException {
        T wrapper = supplier.get();

        pullDocument(doc, wrapper, query);

        return wrapper;
    }

    protected RuntimeException wrappedPullColumnValueException(
            Exception e,
            E field,
            ViewEntry entry) {
        try {
            return new RuntimeException(String.format(
                    "Unable to pull column value %s from document %s",
                    field.name(),
                    entry.getNoteID()),
                    e);
        } catch (NotesException ne) {
            return new RuntimeException(String.format(
                    "Unable to pull column value %s",
                    field.name()),
                    e);
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
            ViewEntry entry,
            Supplier<T> supplier,
            Query<E> query,
            VwWalker<E> walker)
            throws NotesException {
        T wrapper = supplier.get();

        pullViewEntry(entry, wrapper, query, walker);

        return wrapper;
    }

}
