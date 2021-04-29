package info.shillem.domino.util;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import info.shillem.domino.util.DominoLoop.OptionsDocument;
import info.shillem.util.Unthrow;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;

public class DominoI18n {

    private DominoI18n() {
        throw new UnsupportedOperationException();
    }

    public static String getLocaleItemName(String itemName, Locale locale) {
        return locale == null || locale.getLanguage().isEmpty()
                ? itemName
                : itemName + "_" + locale.getLanguage();
    }

    public static Map<String, String> getValues(
            Document doc,
            String valueItemName,
            Locale locale)
            throws NotesException {
        return getValues(doc, valueItemName, getLocaleItemName(valueItemName, locale));
    }

    public static Map<String, String> getValues(
            Document doc,
            String valueItemName,
            String labelItemName)
            throws NotesException {
        return getValues(doc, valueItemName, labelItemName, null);
    }

    public static Map<String, String> getValues(
            Document doc,
            String valueItemName,
            String labelItemName,
            String prefixItemName)
            throws NotesException {
        if (!doc.hasItem(valueItemName)) {
            throw new IllegalStateException(valueItemName.concat(" value item does not exist"));
        }
        
        Map<String, String> map = new LinkedHashMap<>();

        if (!doc.hasItem(labelItemName)) {
            return map;
        }

        Vector<?> values = doc.getItemValue(valueItemName);
        Vector<?> labels = doc.getItemValue(labelItemName);

        String prefix = prefixItemName != null
                ? doc.getItemValueString(prefixItemName) + "_"
                : "";

        for (int i = 0; i < values.size(); i++) {
            String value = (String) values.get(i);

            map.put(prefix + value,
                    values.size() == labels.size() ? (String) labels.get(i) : value);
        }

        return map;
    }

    public static Map<String, String> getValues(
            View vw,
            String valueItemName,
            Locale locale)
            throws NotesException {
        return getValues(vw, valueItemName, getLocaleItemName(valueItemName, locale));
    }

    public static Map<String, String> getValues(
            View vw,
            String valueItemName,
            String labelItemName)
            throws NotesException {
        return getValues(vw, valueItemName, labelItemName, null);
    }

    public static Map<String, String> getValues(
            View vw,
            String valueItemName,
            String labelItemName,
            String prefixItemName)
            throws NotesException {
        Map<String, String> values = new LinkedHashMap<>();
        
        OptionsDocument<Object> options = new DominoLoop.OptionsDocument<>();
        
        options.setReader((doc) -> Unthrow.on(() -> {
            values.putAll(getValues(doc, valueItemName, labelItemName, prefixItemName));
        }));

        DominoLoop.read(vw, options);

        return values;
    }

}
