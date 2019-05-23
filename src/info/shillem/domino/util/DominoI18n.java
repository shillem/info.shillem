package info.shillem.domino.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Stream;

import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;

public enum DominoI18n {
    ;

    public static String getLocaleItemName(String itemName, Locale locale) {
        return locale == null || locale.getLanguage().isEmpty()
                ? itemName
                : itemName + "_" + locale.getLanguage();
    }

    public static Map<String, String> getValues(
            Document doc, String valueItemName, Locale locale) {
        return getValues(doc, valueItemName, getLocaleItemName(valueItemName, locale));
    }

    public static Map<String, String> getValues(
            Document doc, String valueItemName, String labelItemName) {
        return getValues(doc, valueItemName, labelItemName, null);
    }

    public static Map<String, String> getValues(
            Document doc, String valueItemName, String labelItemName, String prefixItemName) {
        try {
            if (!doc.hasItem(valueItemName) || !doc.hasItem(labelItemName)) {
                return Collections.emptyMap();
            }

            Vector<?> values = doc.getItemValue(valueItemName);
            Vector<?> labels = doc.getItemValue(labelItemName);

            Map<String, String> map = new LinkedHashMap<>();
            String prefix = prefixItemName != null
                    ? doc.getItemValueString(prefixItemName) + "_"
                    : "";

            for (int i = 0; i < values.size(); i++) {
                String value = (String) values.get(i);

                map.put(prefix + value,
                        values.size() == labels.size() ? (String) labels.get(i) : value);
            }

            return map;
        } catch (NotesException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String> getValues(View vw, String valueItemName, Locale locale) {
        return getValues(vw, valueItemName, getLocaleItemName(valueItemName, locale));
    }

    public static Map<String, String> getValues(
            View vw, String valueItemName, String labelItemName) {
        return getValues(vw, valueItemName, labelItemName, null);
    }

    public static Map<String, String> getValues(
            View vw, String valueItemName, String labelItemName, String prefixItemName) {
        try (Stream<Document> stream = DominoStream.stream(vw)) {
            return stream
                    .map(doc -> getValues(doc, valueItemName, labelItemName, prefixItemName))
                    .collect(LinkedHashMap::new, Map::putAll, Map::putAll);
        }
    }

}
