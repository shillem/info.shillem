package info.shillem.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FieldProperties implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final FieldProperties BIG_DECIMAL = new FieldProperties(BigDecimal.class);
	public static final FieldProperties BIG_DECIMAL_LIST = new FieldProperties(BigDecimal[].class);

	public static final FieldProperties BOOLEAN = new FieldProperties(Boolean.class);
	public static final FieldProperties BOOLEAN_LIST = new FieldProperties(Boolean[].class);

	public static final FieldProperties DATE = new FieldProperties(Date.class);
	public static final FieldProperties DATE_LIST = new FieldProperties(Date[].class);

	public static final FieldProperties DOUBLE = new FieldProperties(Double.class);
	public static final FieldProperties DOUBLE_LIST = new FieldProperties(Double[].class);

	public static final FieldProperties INTEGER = new FieldProperties(Integer.class);
	public static final FieldProperties INTEGER_LIST = new FieldProperties(Integer[].class);

	public static final FieldProperties STRING = new FieldProperties(String.class);
	public static final FieldProperties STRING_LIST = new FieldProperties(String[].class);

	public static FieldProperties newInstance(Class<? extends Serializable> type) {
		if (type == BigDecimal.class)
			return BIG_DECIMAL;
		if (type == BigDecimal[].class)
			return BIG_DECIMAL_LIST;

		if (type == Boolean.class)
			return BOOLEAN;
		if (type == Boolean[].class)
			return BOOLEAN_LIST;

		if (type == Date.class)
			return DATE;
		if (type == Date[].class)
			return DATE_LIST;

		if (type == Double.class)
			return DOUBLE;
		if (type == Double[].class)
			return DOUBLE_LIST;

		if (type == Integer.class)
			return INTEGER;
		if (type == Integer[].class)
			return INTEGER_LIST;

		if (type == String.class)
			return STRING;
		if (type == String.class)
			return STRING_LIST;

		return new FieldProperties(type);
	}

	public static <T extends Serializable> List<T> newList(Class<T> cls) {
		return new ArrayList<>();
	}

	private final Class<? extends Serializable> type;

	private FieldProperties(Class<? extends Serializable> type) {
		this.type = type;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends Serializable> getType() {
		return isList() ? (Class<? extends Serializable>) type.getComponentType() : type;
	}

	public boolean isList() {
		return type.isArray();
	}
	
}
