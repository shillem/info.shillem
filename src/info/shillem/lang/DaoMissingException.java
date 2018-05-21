package info.shillem.lang;

import info.shillem.dto.BaseField;

public class DaoMissingException extends DaoException {

	private static final long serialVersionUID = 1L;

	private final BaseField field;
	private final Object value;

	public DaoMissingException(BaseField field, Object value) {
		super(String.format(
				"The resource with field %s and value %s is missing", field.toString(), value));

		this.field = field;
		this.value = value;
	}

	public BaseField getField() {
		return field;
	}

	public Object getValue() {
		return value;
	}

}
