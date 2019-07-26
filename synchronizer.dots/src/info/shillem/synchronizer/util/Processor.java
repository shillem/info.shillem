package info.shillem.synchronizer.util;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.function.Supplier;

import info.shillem.domino.util.DominoSilo;
import info.shillem.domino.util.ViewAccessPolicy;
import info.shillem.domino.util.ViewPath;
import info.shillem.synchronizer.dots.Program.Nature;
import info.shillem.synchronizer.dto.Record;
import info.shillem.synchronizer.lang.ProcessorException;
import lotus.domino.NotesException;
import lotus.domino.View;

public abstract class Processor<T extends Record> {

    protected final ProcessorHelper helper;
    private final Supplier<T> recordSupplier;
    private final ViewPath viewPath;

    public Processor(ProcessorHelper helper, Supplier<T> recordSupplier) {
        this.helper = Objects.requireNonNull(
                helper, "Processor helper cannot be null");

        this.recordSupplier = Objects.requireNonNull(
                recordSupplier, "Record supplier helper cannot be null");

        this.viewPath = new ViewPath() {
            @Override
            public String getName() {
                return helper.getViewName();
            }
        };
    }

    public abstract boolean execute() throws ProcessorException;

    protected final DominoSilo getDominoSilo() {
        return helper.getDominoFactory().getDominoSilo(helper.getId());
    }

    protected Field getKeyField() {
        return helper.getFieldKey();
    }

    protected Object getKeyValue(T record) {
        return record.getValue(getKeyField().getName());
    }

    protected final View getView() {
        try {
            return getDominoSilo().getView(viewPath, ViewAccessPolicy.REFRESH);
        } catch (NotesException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract boolean isNature(Nature nature);

    public T newRecord() {
        return recordSupplier.get();
    }

    protected Object transformValue(Object value, Field.Type destinationType) {
        if (value == null) {
            return value;
        }

        if (value instanceof Date) {
            if (Field.Type.STRING == destinationType) {
                return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(((Date) value).toInstant());
            }

            return value;
        }

        if (value instanceof Number) {
            Number numberValue = (Number) value;

            if (Field.Type.BOOLEAN == destinationType) {
                switch (numberValue.intValue()) {
                case 0:
                    return false;
                case 1:
                    return true;
                default:
                    return value;
                }
            }

            if (Field.Type.DECIMAL == destinationType) {
                return new BigDecimal(numberValue.toString());
            }

            if (Field.Type.DOUBLE == destinationType) {
                return numberValue.doubleValue();
            }

            if (Field.Type.INTEGER == destinationType) {
                return numberValue.intValue();
            }

            if (Field.Type.STRING == destinationType) {
                return numberValue.toString();
            }

            return value;
        }

        if (value instanceof String) {
            String stringValue = (String) value;
            
            if (stringValue.isEmpty()) {
                return null;
            }

            if (Field.Type.BOOLEAN == destinationType) {
                return Boolean.valueOf(stringValue);
            }

            if (Field.Type.DATE == destinationType) {
                return DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(stringValue);
            }

            if (Field.Type.DECIMAL == destinationType) {
                return new BigDecimal(stringValue);
            }

            if (Field.Type.DOUBLE == destinationType) {
                return Double.valueOf(stringValue);
            }

            if (Field.Type.INTEGER == destinationType) {
                return Integer.valueOf(stringValue);
            }

            return value;
        }

        return value;
    }

}
