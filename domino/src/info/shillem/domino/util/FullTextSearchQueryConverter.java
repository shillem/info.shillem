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

    public FullTextSearchQueryConverter(SearchQuery<E> query, Function<E, String> itemNamer) {
        super(query, itemNamer);
    }

    @Override
    protected String formatLogicalOperator(LogicalOperator operator) {
        return " " + operator.name() + " ";
    }

    @Override
    protected String formatValue(Object value) {
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
        case NOT_EQUAL:
        case NOT_IN:
            builder.append("NOT " + namer.apply(value.getField())
                    + formatComparisonOperator(value.getOperator().getOpposite())
                    + formatValue(value.getValue()));

            break;
        default:
            super.formatValue(builder, value);
        }
    }

}
