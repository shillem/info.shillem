package info.shillem.domino.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import info.shillem.dto.BaseField;
import info.shillem.dto.I18nString;
import info.shillem.dto.I18nStringList;
import info.shillem.dto.I18nValue;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;

public enum DominoI18n {
	;

	public static I18nValue getValue(Locale locale, Document doc, BaseField field)
			throws NotesException {
		String valueItemName = field.toString();
		String labelItemName = locale != null ? valueItemName + "_" + locale.getLanguage()
				: valueItemName;

		Class<?> type = field.getProperties().getType();

		if (type == I18nString.class) {
			String value = doc.getItemValueString(valueItemName);
			String label = doc.getItemValueString(labelItemName);

			I18nString string = new I18nString();
			string.set(value, label.isEmpty() ? value : label);

			return string;
		} else if (type == I18nStringList.class) {
			List<?> values = doc.getItemValue(valueItemName);
			List<?> labels = doc.getItemValue(labelItemName);

			I18nStringList stringList = new I18nStringList();

			for (int i = 0; i < values.size(); i++) {
				String value = (String) values.get(i);

				stringList.add(value, labels.isEmpty() ? value : (String) labels.get(i));
			}

			return stringList;
		}

		throw new IllegalArgumentException(type + " is invalid class for internationalization");
	}

	public static Map<BaseField, I18nValue> getValues(Locale locale, View vw, BaseField... fields)
			throws NotesException {
		Document doc = null;

		try {
			Map<BaseField, I18nValue> resources = new HashMap<>();

			doc = vw.getFirstDocument();

			while (doc != null) {
				doc.setPreferJavaDates(true);

				for (BaseField field : fields) {
					I18nStringList stringList = (I18nStringList) resources.get(field);

					if (stringList == null) {
						stringList = new I18nStringList();

						resources.put(field, stringList);
					}

					stringList.add(getValue(locale, doc, field));
				}

				Document nextDocument = vw.getNextDocument(doc);
				DominoUtil.recycle(doc);
				doc = nextDocument;
			}

			return resources;
		} finally {
			DominoUtil.recycle(doc);
		}
	}

	public static Map<BaseField, I18nValue> getValues(Locale locale, View vw, String key,
			BaseField... fields) throws NotesException {
		Document doc = null;

		try {
			doc = vw.getDocumentByKey(key, true);

			if (doc == null) {
				return Collections.emptyMap();
			}

			doc.setPreferJavaDates(true);

			Map<BaseField, I18nValue> resources = new HashMap<>();

			for (BaseField field : fields) {
				resources.put(field, getValue(locale, doc, field));
			}

			return resources;
		} finally {
			DominoUtil.recycle(doc);
		}
	}

}
