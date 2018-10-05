package info.shillem.dto;

import java.io.Serializable;
import java.util.List;

class ValueHolder implements Serializable {

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

    private Object value;
    private Object updateValue;
    private Object transactionValue;
    private State state;

    private ValueHolder(Object value, State state) {
        this.value = value;
        this.state = state;
    }

    void commit() {
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

    Object getValue() {
        switch (state) {
        case TRANSACTION:
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

    boolean isUpdated() {
        return state != State.SAVE;
    }

    private boolean isValueChanged(Object before, Object after, Class<?> type) {
        if (after == before) {
            return false;
        }

        if (after == null) {
            return true;
        }

        testValue(after, type);

        return type.isArray()
                ? !(((List<?>) after).equals((List<?>) before))
                : !after.equals(before);
    }

    void rollback() {
        switch (state) {
        case TRANSACTION:
            transactionValue = null;
            
            break;
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

    void setAsUpdated() {
        updateValue = getValue();
        state = State.UPDATE;
    }

    void transactValue(Object value, Class<?> type) {
        if (isValueChanged(getValue(), value, type)) {
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

    void updateValue(Object value, Class<?> type) {
        if (isValueChanged(getValue(), value, type)) {
            updateValue = value;
            state = State.UPDATE;
        }
    }

    static ValueHolder newSavedValue(Object value, Class<?> type) {
        testValue(value, type);

        return new ValueHolder(value, State.SAVE);
    }

    static ValueHolder newTransactionValue(Object value, Class<?> type) {
        testValue(value, type);

        return new ValueHolder(value, State.TRANSACTION);
    }

    static ValueHolder newValue(Object value, Class<?> type) {
        testValue(value, type);

        return new ValueHolder(value, State.NEW);
    }

    private static void testValue(Object value, Class<?> type) {
        if (value == null) {
            return;
        }

        if (type.isArray()) {
            if (!(value instanceof List)) {
                throw illegalValue(value, type);
            }

            ((List<?>) value).stream()
                    .filter(o -> o.getClass() != type.getComponentType())
                    .findAny()
                    .ifPresent(o -> {
                        throw illegalValue(value, type);
                    });
        } else if (value.getClass() != type) {
            throw illegalValue(value, type);
        }
    }

    private static IllegalArgumentException illegalValue(Object value, Class<?> type) {
        if (type.isArray()) {
            return new IllegalArgumentException(
                    String.format("Value type should be List<%s> and not %s",
                            type.getComponentType().getName(), value.getClass()));
        } else {
            return new IllegalArgumentException(
                    String.format("Value type should be %s and not %s",
                            type.getName(), value.getClass().getName()));
        }
    }

}
