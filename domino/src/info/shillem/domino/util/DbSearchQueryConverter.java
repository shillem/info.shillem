package info.shillem.domino.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;

import info.shillem.dao.SearchQuery;
import info.shillem.dao.SearchQuery.Group;
import info.shillem.dao.SearchQuery.Logical;
import info.shillem.dao.SearchQuery.Value;
import info.shillem.dao.SearchQuery.Values;
import info.shillem.dao.SearchQueryConverter;
import info.shillem.dto.BaseField;
import info.shillem.util.ComparisonOperator;
import info.shillem.util.LogicalOperator;

public class DbSearchQueryConverter<E extends Enum<E> & BaseField>
        extends SearchQueryConverter<E> {

    protected static final DateFormat SHORT_DATE_FORMAT =
            SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);

    public DbSearchQueryConverter(SearchQuery<E> query) {
        super(query);
    }

    public DbSearchQueryConverter(SearchQuery<E> query, Function<E, String> namer) {
        super(query, namer);
    }

    @Override
    protected String formatComparisonOperator(ComparisonOperator operator) {
        switch (operator) {
        case LOWER:
            return " < ";
        case LOWER_EQUAL:
            return " <= ";
        case EQUAL:
        case IN:
        case LIKE:
            return " = ";
        case NOT_EQUAL:
        case NOT_IN:
            return " != ";
        case GREATER_EQUAL:
            return " >= ";
        case GREATER:
            return " > ";
        default:
            throw new UnsupportedOperationException(operator.name());
        }
    }

    @Override
    protected void formatGroup(StringBuilder builder, Group group) {
        if (group.isEmpty()) {
            return;
        }

        builder.append("(");

        group.getPieces().forEach((piece) -> formatPiece(builder, piece));

        builder.append(")");
    }

    @Override
    protected void formatLogical(StringBuilder builder, Logical logical) {
        builder.append(formatLogicalOperator(logical.getOperator()));
    }

    @Override
    protected String formatLogicalOperator(LogicalOperator operator) {
        switch (operator) {
        case AND:
            return " & ";
        case OR:
            return " | ";
        default:
            throw new UnsupportedOperationException(operator.name());
        }
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
            return "[" + SHORT_DATE_FORMAT.format((Date) value) + "]";
        }

        return value.toString();
    }

    @Override
    protected void formatValue(StringBuilder builder, Value<E> value) {
        switch (value.getOperator()) {
        case LIKE:
            builder.append(String.format("@Contains(%s;%s)",
                    formatField(value.getField()),
                    formatValue(value.getValue())));

            break;
        default:
            builder.append(formatField(value.getField())
                    + formatComparisonOperator(value.getOperator())
                    + formatValue(value.getValue()));
        }

    }

    @Override
    protected void formatValues(StringBuilder builder, Values<E> values) {
        String itemName = formatField(values.getField());
        String operator = formatComparisonOperator(values.getOperator());

        builder.append("(");

        builder.append(values.getValues().stream()
                .map((val) -> itemName + operator + formatValue(val))
                .collect(Collectors.joining(formatLogicalOperator(LogicalOperator.OR))));

        builder.append(")");
    }

}
