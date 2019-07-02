package info.shillem.domino.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import info.shillem.util.CastUtil;
import info.shillem.util.Unthrow;
import lotus.domino.Base;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.MIMEEntity;
import lotus.domino.MIMEHeader;
import lotus.domino.NotesError;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.ViewEntry;
import lotus.domino.ViewNavigator;

public enum DominoUtil {
    ;

    private static final Pattern MIME_FILENAME = Pattern.compile(
            "filename=['\"]*([^'\"]+)['\"]*", Pattern.CASE_INSENSITIVE);

    private static final Vector<String> MIME_FILTERED_HEADERS = new Vector<>();

    static {
        MIME_FILTERED_HEADERS.add("Content-Type");
        MIME_FILTERED_HEADERS.add("Content-Disposition");
    }

    public static <T> T getEntryValue(
            List<String> vwColumns, ViewEntry entry, String itemName, Function<Object, T> converter)
            throws NotesException {
        Objects.requireNonNull(vwColumns, "View columns cannot be null");
        Objects.requireNonNull(entry, "Entry cannot be null");
        Objects.requireNonNull(itemName, "Item name cannot be null");
        Objects.requireNonNull(converter, "Converter cannot be null");

        if (!vwColumns.contains(itemName)) {
            return converter.apply(null);
        }

        List<?> columnValues = entry.getColumnValues();
        Object result = columnValues.get(vwColumns.indexOf(itemName));

        if (result instanceof List) {
            List<?> l = (List<?>) result;

            return converter.apply(l.isEmpty() ? null : l.get(0));
        }

        return converter.apply(result);
    }

    public static <T> List<T> getEntryValues(
            List<String> vwColumns, ViewEntry entry, String itemName, Function<Object, T> converter)
            throws NotesException {
        Objects.requireNonNull(vwColumns, "View columns cannot be null");
        Objects.requireNonNull(entry, "Entry cannot be null");
        Objects.requireNonNull(itemName, "Item name cannot be null");
        Objects.requireNonNull(converter, "Converter cannot be null");

        if (!vwColumns.contains(itemName)) {
            return Collections.emptyList();
        }

        List<?> columnValues = entry.getColumnValues();
        Object result = columnValues.get(vwColumns.indexOf(itemName));

        if (result instanceof List) {
            return ((List<?>) result)
                    .stream()
                    .map(converter)
                    .collect(Collectors.toList());
        }

        return Arrays.asList(converter.apply(result));
    }

    public static Boolean getItemBoolean(Document doc, String itemName)
            throws NotesException {
        return getItemValue(doc, itemName, (val) -> Boolean.valueOf((String) val));
    }

    public static List<Boolean> getItemBooleans(Document doc, String itemName)
            throws NotesException {
        return getItemValues(doc, itemName, (val) -> Boolean.valueOf((String) val));
    }

    public static Date getItemDate(Document doc, String itemName)
            throws NotesException {
        return getItemValue(doc, itemName, Date.class::cast);
    }

    public static List<Date> getItemDates(Document doc, String itemName)
            throws NotesException {
        return getItemValues(doc, itemName, Date.class::cast);
    }

    public static BigDecimal getItemDecimal(Document doc, String itemName)
            throws NotesException {
        return getItemValue(doc, itemName, (val) -> new BigDecimal(((Number) val).toString()));
    }

    public static List<BigDecimal> getItemDecimals(Document doc, String itemName)
            throws NotesException {
        return getItemValues(doc, itemName, (val) -> new BigDecimal(((Number) val).toString()));
    }

    public static Double getItemDouble(Document doc, String itemName)
            throws NotesException {
        return getItemValue(doc, itemName, (val) -> ((Number) val).doubleValue());
    }

    public static List<Double> getItemDoubles(Document doc, String itemName)
            throws NotesException {
        return getItemValues(doc, itemName, (val) -> ((Number) val).doubleValue());
    }

    public static Integer getItemInteger(Document doc, String itemName)
            throws NotesException {
        return getItemValue(doc, itemName, (val) -> ((Number) val).intValue());
    }

    public static List<Integer> getItemIntegers(Document doc, String itemName)
            throws NotesException {
        return getItemValues(doc, itemName, (val) -> ((Number) val).intValue());
    }

    public static String getItemString(Document doc, String itemName)
            throws NotesException {
        return getItemValue(doc, itemName, String.class::cast);
    }

    public static List<String> getItemStrings(Document doc, String itemName)
            throws NotesException {
        return getItemValues(doc, itemName, String.class::cast);
    }

    public static Object getItemValue(Document doc, String itemName)
            throws NotesException {
        return getItemValue(doc, itemName, Object.class::cast);
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

                if (values == null || values.isEmpty()) {
                    return null;
                }

                return converter.apply(values.get(0));
            }
        } finally {
            DominoUtil.recycle(item);
        }
    }

    public static List<Object> getItemValues(Document doc, String itemName)
            throws NotesException {
        return getItemValues(doc, itemName, Object.class::cast);
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
        Objects.requireNonNull(doc, "Document cannot be null");

        DateTime d = null;

        try {
            d = doc.getLastModified();

            return d.toJavaDate();
        } finally {
            DominoUtil.recycle(d);
        }
    }

    public static List<MIMEEntity> getMimeEntitiesByContentType(MIMEEntity entity,
            MimeContentType contentType) throws NotesException {
        Objects.requireNonNull(entity, "Entity cannot be null");
        Objects.requireNonNull(contentType, "Content type cannot be null");

        List<MIMEEntity> subentities = new ArrayList<>();
        MIMEEntity nextEntity = null;

        try {
            nextEntity = entity.getNextEntity();

            while (nextEntity != null) {
                String[] entityFilteredHeaders = nextEntity
                        .getSomeHeaders(MIME_FILTERED_HEADERS, true)
                        .split("\\n");

                if (contentType.matches(entityFilteredHeaders)) {
                    subentities.add(nextEntity);
                }

                nextEntity = nextEntity.getNextEntity();
            }
        } finally {
            DominoUtil.recycle(nextEntity);
        }

        return subentities;
    }

    public static MIMEEntity getMimeEntity(Document doc, String itemName, boolean createOnFail)
            throws NotesException {
        Objects.requireNonNull(doc, "Document cannot be null");
        Objects.requireNonNull(itemName, "Item name cannot be null");

        MIMEEntity mimeEntity = doc.getMIMEEntity(itemName);

        if (mimeEntity == null) {
            if (createOnFail) {
                if (doc.hasItem(itemName)) {
                    doc.removeItem(itemName);
                }

                mimeEntity = doc.createMIMEEntity(itemName);
            }
        }

        return mimeEntity;
    }

    public static Optional<String> getMimeEntityAttachmentFilename(MIMEEntity entity)
            throws NotesException {
        Objects.requireNonNull(entity, "Entity cannot be null");

        return getMimeEntityHeaderValAndParams(
                entity, h -> Unthrow.on(() -> h.contains("attachment")))
                        .map(s -> {
                            Matcher m = MIME_FILENAME.matcher(s);
                            m.find();
                            return m.group(1);
                        });
    }

    public static Optional<String> getMimeEntityHeaderValAndParams(
            MIMEEntity entity, Predicate<String> matcher) throws NotesException {
        Objects.requireNonNull(entity, "Entity cannot be null");
        Objects.requireNonNull(matcher, "Matcher cannot be null");

        Vector<MIMEHeader> headers = CastUtil.toAnyVector(entity.getHeaderObjects());

        try {
            return headers
                    .stream()
                    .map((h) -> Unthrow.on(() -> h.getHeaderValAndParams(false, true)))
                    .filter(matcher)
                    .findFirst();
        } finally {
            recycle(headers);
        }
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

    public static void setAuthorValue(Document doc, String itemName, Object value)
            throws NotesException {
        Item item = null;

        try {
            item = doc.replaceItemValue(itemName, value);
            item.setAuthors(true);
        } finally {
            recycle(item);
        }
    }

    public static void setDate(Session session, Document doc, String itemName, Date value)
            throws NotesException {
        Objects.requireNonNull(doc, "Document cannot be null");
        Objects.requireNonNull(itemName, "Item name cannot be null");

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

    public static void setGuidance(ViewNavigator nav, int maxEntries, int readMode)
            throws NotesException {
        try {
            nav.setCacheGuidance(maxEntries, readMode);
        } catch (NotesException e) {
            if (e.id != NotesError.NOTES_ERR_NOT_IMPLEMENTED) {
                throw e;
            }
        }
    }

    public static void setNameValue(Document doc, String itemName, Object value)
            throws NotesException {
        Objects.requireNonNull(doc, "Document cannot be null");
        Objects.requireNonNull(itemName, "Item name cannot be null");

        Item item = null;

        try {
            item = doc.replaceItemValue(itemName, value);
            item.setNames(true);
        } finally {
            recycle(item);
        }
    }

    public static void setReaderValue(Document doc, String itemName, Object value)
            throws NotesException {
        Objects.requireNonNull(doc, "Document cannot be null");
        Objects.requireNonNull(itemName, "Item name cannot be null");

        Item item = null;

        try {
            item = doc.replaceItemValue(itemName, value);
            item.setReaders(true);
        } finally {
            recycle(item);
        }
    }

}