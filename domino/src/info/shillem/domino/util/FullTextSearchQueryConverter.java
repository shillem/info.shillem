package info.shillem.domino.util;

import java.util.Date;
import java.util.function.Function;

import info.shillem.dao.SearchQuery;
import info.shillem.dao.SearchQuery.Value;
import info.shillem.dto.BaseField;
import info.shillem.util.LogicalOperator;

public class FullTextSearchQueryConverter<E extends Enum<E> & BaseField>
        extends DbSearchQueryConverter<E> {

    public FullTextSearchQueryConverter(SearchQuery<E> query) {
        super(query);
    }

    public FullTextSearchQueryConverter(SearchQuery<E> query, Function<E, String> namer) {
        super(query, namer);
    }

    @Override
    protected String formatField(E field) {
        return "[" + namer.apply(field) + "]";
    }

    @Override
    protected String formatLogicalOperator(LogicalOperator operator) {
        return " " + operator.name() + " ";
    }

    @Override
    protected String formatValue(Object value) {
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
    protected void formatValue(StringBuilder builder, Value<E> value) {
        switch (value.getOperator()) {
        case EQUAL:
            if (value.getValue() == null) {
                builder.append("NOT " + formatField(value.getField()) + " IS PRESENT");
            } else {
                builder.append(formatField(value.getField())
                        + formatComparisonOperator(value.getOperator())
                        + formatValue(value.getValue()));
            }

            break;
        case LIKE: {
            String stringValue = (String) value.getValue();

            builder.append(formatField(value.getField())
                    + formatComparisonOperator(value.getOperator())
                    + formatValue(stringValue.isEmpty()
                            ? stringValue
                            : stringValue.contains("*")
                                    ? stringValue
                                    : "*" + stringValue + "*"));

            break;
        }
        case NOT_EQUAL:
            if (value.getValue() == null) {
                builder.append(formatField(value.getField()) + " IS PRESENT");
            } else {
                builder.append("NOT " + formatField(value.getField())
                        + formatComparisonOperator(value.getOperator().getOpposite())
                        + formatValue(value.getValue()));
            }

            break;
        case NOT_IN:
            builder.append("NOT " + formatField(value.getField())
                    + formatComparisonOperator(value.getOperator().getOpposite())
                    + formatValue(value.getValue()));

            break;
        default:
            builder.append(formatField(value.getField())
                    + formatComparisonOperator(value.getOperator())
                    + formatValue(value.getValue()));
        }
    }

}
