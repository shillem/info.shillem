package info.shillem.domino.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import info.shillem.util.CastUtil;
import info.shillem.util.MimeUtil;
import info.shillem.util.StringUtil;
import info.shillem.util.Unthrow;
import lotus.domino.Base;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.MIMEEntity;
import lotus.domino.MIMEHeader;
import lotus.domino.NotesException;
import lotus.domino.RichTextItem;
import lotus.domino.Session;

public class DominoUtil {

    private static final Vector<String> MIME_FILTERED_HEADERS = new Vector<>();

    static {
        MIME_FILTERED_HEADERS.add("Content-Type");
        MIME_FILTERED_HEADERS.add("Content-Disposition");
    }

    private DominoUtil() {
        throw new UnsupportedOperationException();
    }

    public static Boolean getItemBoolean(Document doc, String itemName) throws NotesException {
        return getItemValue(doc, itemName, (val) -> Boolean.valueOf((String) val));
    }

    public static List<Boolean> getItemBooleans(
            Document doc,
            String itemName)
            throws NotesException {
        return getItemValues(doc, itemName, (val) -> Boolean.valueOf((String) val));
    }

    public static Date getItemDate(Document doc, String itemName) throws NotesException {
        return getItemValue(doc, itemName, (val) -> Unthrow.on(() -> {
            if (val instanceof DateTime) {
                try {
                    return ((DateTime) val).toJavaDate();
                } finally {
                    recycle((DateTime) val);
                }
            }

            return (Date) val;
        }));
    }

    public static List<Date> getItemDates(Document doc, String itemName) throws NotesException {
        return getItemValues(doc, itemName, (val) -> Unthrow.on(() -> {
            if (val instanceof DateTime) {
                try {
                    return ((DateTime) val).toJavaDate();
                } finally {
                    recycle((DateTime) val);
                }
            }

            return (Date) val;
        }));
    }

    public static BigDecimal getItemDecimal(Document doc, String itemName) throws NotesException {
        return getItemValue(doc, itemName, (val) -> new BigDecimal(((Number) val).toString()));
    }

    public static List<BigDecimal> getItemDecimals(
            Document doc,
            String itemName)
            throws NotesException {
        return getItemValues(doc, itemName, (val) -> new BigDecimal(((Number) val).toString()));
    }

    public static Double getItemDouble(Document doc, String itemName) throws NotesException {
        return getItemValue(doc, itemName, (val) -> ((Number) val).doubleValue());
    }

    public static List<Double> getItemDoubles(Document doc, String itemName) throws NotesException {
        return getItemValues(doc, itemName, (val) -> ((Number) val).doubleValue());
    }

    public static Integer getItemInteger(Document doc, String itemName) throws NotesException {
        return getItemValue(doc, itemName, (val) -> ((Number) val).intValue());
    }

    public static List<Integer> getItemIntegers(
            Document doc,
            String itemName)
            throws NotesException {
        return getItemValues(doc, itemName, (val) -> ((Number) val).intValue());
    }

    public static RichTextItem getItemRichText(
            Document doc,
            String itemName)
            throws NotesException {
        Item item = doc.getFirstItem(itemName);

        if (item == null) {
            return null;
        }

        return (RichTextItem) item;
    }

    public static String getItemString(Document doc, String itemName) throws NotesException {
        return getItemValue(doc, itemName, String.class::cast);
    }

    public static List<String> getItemStrings(
            Document doc,
            String itemName)
            throws NotesException {
        return getItemValues(doc, itemName, String.class::cast);
    }

    public static Object getItemValue(Document doc, String itemName) throws NotesException {
        return getItemValue(doc, itemName, Object.class::cast);
    }

    public static <T> T getItemValue(
            Document doc,
            String itemName,
            Function<Object, T> converter)
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
            }

            List<?> values = item.getValues();

            if (values == null || values.isEmpty()) {
                return null;
            }

            return converter.apply(values.get(0));
        } finally {
            DominoUtil.recycle(item);
        }
    }

    public static List<Object> getItemValues(
            Document doc,
            String itemName)
            throws NotesException {
        return getItemValues(doc, itemName, Object.class::cast);
    }

    public static <T> List<T> getItemValues(
            Document doc,
            String itemName,
            Function<Object, T> converter)
            throws NotesException {
        return (List<T>) getItemValues(doc, itemName, converter, ArrayList::new);
    }

    public static <T> Collection<T> getItemValues(
            Document doc,
            String itemName,
            Function<Object, T> converter,
            Supplier<Collection<T>> supplier)
            throws NotesException {
        Objects.requireNonNull(doc, "Document cannot be null");
        Objects.requireNonNull(itemName, "Item name cannot be null");
        Objects.requireNonNull(converter, "Converter cannot be null");
        Objects.requireNonNull(supplier, "Supplier cannot be null");

        Item item = null;

        try {
            item = doc.getFirstItem(itemName);

            if (item == null) {
                return supplier.get();
            }

            if (item.getType() == Item.RICHTEXT) {
                Collection<T> values = supplier.get();

                values.add(converter.apply(item.getText()));

                return values;
            }

            List<?> values = item.getValues();

            if (values == null) {
                return supplier.get();
            }

            return values.stream()
                    .map(converter)
                    .collect(Collectors.toCollection(supplier));
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

    public static List<String> getLockHolders(Document doc) throws NotesException {
        Vector<String> values = CastUtil.toAnyVector(doc.getLockHolders());

        return values.stream().filter(StringUtil::isNotEmpty).collect(Collectors.toList());
    }

    public static List<MIMEEntity> getMimeEntitiesByContentType(
            MIMEEntity entity,
            MimeContentType contentType)
            throws NotesException {
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

    public static MIMEEntity getMimeEntity(
            Document doc,
            String itemName,
            boolean createOnFail)
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

    public static Optional<String> getMimeEntityAttachmentFilename(
            MIMEEntity entity)
            throws NotesException {
        Objects.requireNonNull(entity, "Entity cannot be null");

        return getMimeEntityHeaderValAndParams(
                entity, h -> Unthrow.on(() -> h.contains("attachment")))
                        .map(s -> MimeUtil.getHeaderProperty("filename", s));
    }

    public static Optional<String> getMimeEntityHeaderValAndParams(
            MIMEEntity entity,
            Predicate<String> matcher)
            throws NotesException {
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
        if (objs != null) {
            objs.stream()
                    .filter(o -> o instanceof Base)
                    .map(o -> (Base) o)
                    .forEach(DominoUtil::recycle);

        }
    }

    public static void setAuthorValue(
            Document doc,
            String itemName,
            Object value)
            throws NotesException {
        Item item = null;

        try {
            item = doc.replaceItemValue(itemName, value);
            item.setAuthors(true);
        } finally {
            recycle(item);
        }
    }

    public static void setDate(
            Session session,
            Document doc,
            String itemName,
            Date value)
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

    public static void setNameValue(
            Document doc,
            String itemName,
            Object value)
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

    public static void setReaderValue(
            Document doc,
            String itemName,
            Object value)
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