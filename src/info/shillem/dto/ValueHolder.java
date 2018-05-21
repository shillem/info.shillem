package info.shillem.dto;

import java.io.Serializable;

class ValueHolder implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Object value;
	private ValueOperation operation;

	ValueHolder(Object value, ValueOperation operation) {
		this.value = value;
		this.operation = operation;
	}

	ValueOperation getOperation() {
		return operation;
	}

	Object getValue() {
		return value;
	}

	void updateValue(Object value, ValueOperation operation) {
		this.value = value;

		if (!(operation == ValueOperation.TRANSACTION_VALUE && this.operation == ValueOperation.UPDATE_VALUE)) {
			this.operation = operation;
		}
	}
}
