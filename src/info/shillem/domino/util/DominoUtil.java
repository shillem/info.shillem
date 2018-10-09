package info.shillem.domino.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;

import lotus.domino.Base;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.MIMEEntity;
import lotus.domino.MIMEHeader;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.ViewEntry;

public enum DominoUtil {
    ;

    private static final Vector<String> MIME_FILTERED_HEADERS = new Vector<>();

    static {
        MIME_FILTERED_HEADERS.add("Content-Type");
        MIME_FILTERED_HEADERS.add("Content-Disposition");
    }

    public static <T> T getItemValue(Document doc, String itemName, Function<Object, T> converter)
            throws NotesException {
        Objects.requireNonNull(doc, "Document cannot be null");
        Objects.requireNonNull(itemName, "Item name cannot be null");
        Objects.requireNonNull(converter, "Converter cannot be null");

        Item item = null;

        try {
            item = doc.getFirstItem(itemName);

            if (item == null) {
                return null;
            }

            if (item.getType() == Item.RICHTEXT) {
                return converter.apply(item.getText());
            } else {
                List<?> values = item.getValues();

                if (values == null) {
                    return null;
                }

                return converter.apply(values.get(0));
            }
        } finally {
            DominoUtil.recycle(item);
        }
    }

    public static <T> List<T> getItemValues(
            Document doc, String itemName, Function<Object, T> converter)
            throws NotesException {
        Objects.requireNonNull(doc, "Document cannot be null");
        Objects.requireNonNull(itemName, "Item name cannot be null");
        Objects.requireNonNull(converter, "Converter cannot be null");

        Item item = null;

        try {
            item = doc.getFirstItem(itemName);

            if (item == null) {
                return Collections.emptyList();
            }

            if (item.getType() == Item.RICHTEXT) {
                List<T> values = new ArrayList<>();

                values.add(converter.apply(item.getText()));

                return values;
            }

            List<?> values = item.getValues();

            if (values == null) {
                return Collections.emptyList();
            }

            return values.stream()
                    .map(converter)
                    .collect(Collectors.toList());
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

    public static Map<MimeContentType, List<MIMEEntity>> getMimeEntities(MIMEEntity entity,
            MimeContentType... contentTypes) throws NotesException {
        if (entity == null || contentTypes == null) {
            return Collections.emptyMap();
        }

        Map<MimeContentType, List<MIMEEntity>> mimeEntities = new HashMap<>();
        MIMEEntity nextEntity = null;

        try {
            nextEntity = entity.getNextEntity();

            while (nextEntity != null) {
                String[] entityFilteredHeaders =
                        nextEntity.getSomeHeaders(MIME_FILTERED_HEADERS, true).split("\\n");

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

    public static MIMEEntity getMimeEntity(Document doc, String itemName, boolean createOnFail)
            throws NotesException {
        Objects.requireNonNull(doc, "Document cannot be null");
        Objects.requireNonNull(itemName, "Item name cannot be null");

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

    public static String getMimeEntityFilename(MIMEEntity entity) throws NotesException {
        Objects.requireNonNull(entity, "Entity cannot be null");

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
            recycle(headers);
        }
    }

    public static String getMimeEntityHeaderValAndParams(MIMEEntity entity, String name)
            throws NotesException {
        Objects.requireNonNull(entity, "Entity cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");

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
            recycle(headers);
        }
    }

    public static boolean hasEncouragedOptions(Document doc) throws NotesException {
        return doc.isPreferJavaDates();
    }

    public static boolean hasEncouragedOptions(ViewEntry entry) throws NotesException {
        return entry.isPreferJavaDates();
    }

    public static void recycle(Base base) {
        if (base != null) {
            try {
                base.recycle();
            } catch (NotesException e) {
                // Do nothing
            }
        }
    }

    public static void recycle(Base b1, Base b2) {
        recycle(b1);
        recycle(b2);
    }

    public static void recycle(Base b1, Base b2, Base b3) {
        recycle(b1);
        recycle(b2);
        recycle(b3);
    }

    public static void recycle(Base b1, Base b2, Base b3, Base... bases) {
        recycle(b1, b2, b3);

        if (bases != null) {
            Arrays.stream(bases).forEach(DominoUtil::recycle);
        }
    }

    public static void recycle(Collection<? extends Object> objs) {
        objs.stream()
                .filter(o -> o instanceof Base)
                .map(o -> (Base) o)
                .forEach(DominoUtil::recycle);
    }

    public static void setDate(Session session, Document doc, String itemName, Date value)
            throws NotesException {
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

    public static void setEncouragedOptions(Document doc) throws NotesException {
        doc.setPreferJavaDates(true);
    }

    public static void setEncouragedOptions(ViewEntry entry) throws NotesException {
        entry.setPreferJavaDates(true);
    }

}