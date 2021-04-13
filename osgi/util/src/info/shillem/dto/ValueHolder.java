package info.shillem.dto;

import java.io.Serializable;
import java.util.Collection;

public class ValueHolder implements Serializable {

    private enum State {
        NEW,
        SAVE,
        UPDATE,
        TRANSACTION,
        TRANSACTION_FROM_NEW,
        TRANSACTION_FROM_SAVE,
        TRANSACTION_FROM_UPDATE
    }

    private static final long serialVersionUID = 1L;

    private final ValueType type;

    private Object value;
    private Object updateValue;
    private Object transactionValue;
    private State state;

    private ValueHolder(ValueType type, Object value, State state) {
        this.type = type;
        this.value = value;
        this.state = state;
    }

    public void commit() {
        switch (state) {
        case TRANSACTION:
        case TRANSACTION_FROM_NEW:
        case TRANSACTION_FROM_SAVE:
        case TRANSACTION_FROM_UPDATE:
            value = transactionValue;
            updateValue = null;
            transactionValue = null;

            break;
        case UPDATE:
            value = updateValue;
            updateValue = null;
            transactionValue = null;

            break;
        default:
            // Do nothing
        }

        state = State.SAVE;
    }

    public Object getValue() {
        switch (state) {
        case TRANSACTION_FROM_NEW:
        case TRANSACTION_FROM_SAVE:
        case TRANSACTION_FROM_UPDATE:
            return transactionValue;
        case UPDATE:
            return updateValue;
        default:
            return value;
        }
    }

    public boolean isUpdated() {
        return state != State.SAVE;
    }

    private boolean isValueChanged(Object before, Object after) {
        if (after == before) {
            return false;
        }

        if (after == null) {
            return true;
        }

        testValue(after, type);

        return type.isCollection()
                ? !(((Collection<?>) after).equals((Collection<?>) before))
                : !after.equals(before);
    }

    public void rollback() {
        switch (state) {
        case TRANSACTION_FROM_NEW:
            transactionValue = null;
            state = State.NEW;

            break;
        case TRANSACTION_FROM_SAVE:
            transactionValue = null;
            state = State.SAVE;

            break;
        case TRANSACTION_FROM_UPDATE:
            transactionValue = null;
            state = State.UPDATE;

            break;
        default:
            // Do nothing
        }
    }

    public void setAsUpdated() {
        updateValue = getValue();
        state = State.UPDATE;
    }

    public void transactValue(Object value) {
        if (isValueChanged(getValue(), value)) {
            transactionValue = value;

            switch (state) {
            case NEW:
                this.state = State.TRANSACTION_FROM_NEW;
                break;
            case SAVE:
                this.state = State.TRANSACTION_FROM_SAVE;
                break;
            case UPDATE:
                this.state = State.TRANSACTION_FROM_UPDATE;
                break;
            default:
                // Do nothing
            }
        }
    }

    public void updateValue(Object value) {
        if (isValueChanged(getValue(), value)) {
            updateValue = value;
            state = State.UPDATE;
        }
    }

    public static ValueHolder newSavedValue(Object value, ValueType type) {
        return newValueHolder(type, value, State.SAVE);
    }

    public static ValueHolder newTransactionValue(Object value, ValueType type) {
        return newValueHolder(type, value, State.TRANSACTION);
    }

    public static ValueHolder newValue(Object value, ValueType type) {
        return newValueHolder(type, value, State.NEW);
    }

    private static ValueHolder newValueHolder(ValueType type, Object value, State state) {
        testValue(value, type);

        return new ValueHolder(type, value, state);
    }

    private static void testValue(Object value, ValueType type) {
        if (value == null) {
            return;
        }

        if (type.isCollection()) {
            if (!(value instanceof Collection)) {
                throw new IllegalArgumentException(
                        String.format("Value type is %s and not %s",
                                type.toString(),
                                value.getClass().getName()));
            }

            ((Collection<?>) value).stream()
                    .filter(o -> o.getClass() != type.getValueClass())
                    .findAny()
                    .ifPresent(o -> {
                        throw new IllegalArgumentException(
                                String.format("Value type is List<%s> and not List<%s>",
                                        type.toString(),
                                        o.getClass().getName()));
                    });
        } else if (value.getClass() != type.getValueClass()) {
            throw new IllegalArgumentException(
                    String.format("Value type is %s and not %s",
                            type.toString(),
                            value.getClass().getName()));
        }
    }

}
