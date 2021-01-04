package info.shillem.domino.util;

import java.util.Date;
import java.util.function.Function;

import info.shillem.dao.FilterQuery;
import info.shillem.dao.SearchQuery;
import info.shillem.dto.BaseField;
import info.shillem.util.ComparisonOperator;
import info.shillem.util.LogicalOperator;

public class FullTextSearchQuery<E extends Enum<E> & BaseField> extends AbstractSearchQuery<E> {

    public FullTextSearchQuery(FilterQuery<E> query) {
        super(query);
    }

    public FullTextSearchQuery(SearchQuery<E> query) {
        super(query);
    }

    protected String outputField(E field) {
        return "[".concat(super.outputField(field)).concat("]");
    }

    @Override
    protected String outputOperator(LogicalOperator value) {
        return value.name();
    }

    protected String outputStringValue(Object value) {
        if (value == null) {
            return "\"\"";
        }

        if (value instanceof String) {
            return "\"" + value + "\"";
        }

        if (value instanceof Date) {
            return SHORT_DATE_FORMAT.format((Date) value);
        }

        return value.toString();
    }

    @Override
    protected String outputValue(E field, ComparisonOperator operator, Object value) {
        switch (operator) {
        case LIKE: {
            String stringValue;

            if (value != null && value instanceof String) {
                String s = (String) value;

                stringValue = outputStringValue(s.contains("*") ? s : "*".concat(s).concat("*"));
            } else {
                stringValue = outputStringValue(value);
            }

            return String.format("%s %s %s",
                    outputField(field),
                    outputOperator(operator),
                    stringValue);
        }
        case NOT_EQUAL:
        case NOT_IN:
            if (value == null && operator == ComparisonOperator.NOT_EQUAL) {
                return outputField(field).concat(" IS PRESENT");
            }

            return String.format("NOT %s %s %s",
                    outputField(field),
                    outputOperator(operator.getOpposite()),
                    outputStringValue(value));
        default:
            if (value == null && operator == ComparisonOperator.EQUAL) {
                return "NOT ".concat(outputField(field)).concat(" IS PRESENT");
            }

            return String.format("%s %s %s",
                    outputField(field),
                    outputOperator(operator),
                    outputStringValue(value));
        }
    }

    public FullTextSearchQuery<E> withNamer(Function<E, String> namer) {
        setNamer(namer);

        return this;
    }

}
