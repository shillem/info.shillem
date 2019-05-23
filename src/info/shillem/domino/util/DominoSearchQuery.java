package info.shillem.domino.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;

import info.shillem.dao.SearchQuery;
import info.shillem.dao.SearchQuery.ComparisonOperator;
import info.shillem.dao.SearchQuery.FieldValue;
import info.shillem.dao.SearchQuery.FieldValues;
import info.shillem.dao.SearchQuery.LogicalOperator;
import info.shillem.dao.SearchQuery.Piece;
import info.shillem.dao.SearchQuery.Wrapper;
import info.shillem.dto.BaseField;

public class DominoSearchQuery<E extends Enum<E> & BaseField> {

    protected static final DateFormat SHORT_DATE_FORMAT =
            SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);

    private final String syntax;

    @SuppressWarnings("unchecked")
    public DominoSearchQuery(SearchQuery<E> query, Function<E, String> itemNamer) {
        StringBuilder builder = new StringBuilder();

        for (Piece piece : query.getPieces()) {
            if (piece instanceof FieldValue) {
                builder.append(formatFieldValue((FieldValue<E>) piece, itemNamer));
            } else if (piece instanceof FieldValues) {
                builder.append(formatFieldValues((FieldValues<E>) piece, itemNamer));
            } else if (piece instanceof LogicalOperator) {
                builder.append(formatLogicalOperator((LogicalOperator) piece));
            } else if (piece instanceof Wrapper) {
                builder.insert(0, "(").append(")");
            }
        }

        syntax = builder.toString();
    }

    protected String formatComparisonOperator(ComparisonOperator operator) {
        return " " + operator.getSign() + " ";
    }

    protected String formatFieldValue(FieldValue<E> condition, Function<E, String> itemNamer) {
        return formatFieldValue(
                condition.getField(), condition.getOperator(), condition.getValue(), itemNamer);
    }

    protected String formatFieldValue(
            E field, ComparisonOperator operator, Object value, Function<E, String> itemNamer) {
        return formatItemName(field, itemNamer)
                + formatComparisonOperator(operator)
                + formatValue(value);
    }

    protected String formatFieldValues(FieldValues<E> condition, Function<E, String> itemNamer) {
        String logicalOperator = formatLogicalOperator(LogicalOperator.OR);

        String result = condition.getValues()
                .stream()
                .map(value -> formatFieldValue(
                        condition.getField(), condition.getOperator(), value, itemNamer))
                .collect(Collectors.joining(logicalOperator));

        if (condition.getValues().size() > 1) {
            result = "(" + result + ")";
        }

        return result;
    }

    protected String formatItemName(E field, Function<E, String> itemNamer) {
        return itemNamer.apply(field);
    }

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

    protected String formatValue(Object value) {
        if (value instanceof String) {
            return "\"" + value + "\"";
        }

        if (value instanceof Date) {
            return "[" + SHORT_DATE_FORMAT.format((Date) value) + "]";
        }

        return value.toString();
    }

    @Override
    public String toString() {
        return syntax;
    }

}
