package info.shillem.sql.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

import info.shillem.util.ComparisonOperator;

public class WhereColumn implements IWhere {

    private static final DateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final String name;
    private final ComparisonOperator operator;
    private final Object value;

    public WhereColumn(String name, ComparisonOperator operator, Object value) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.operator = Objects.requireNonNull(operator, "Operator cannot be null");
        this.value = value;
    }

    @Override
    public String output(Schema schema) {
        if (operator == ComparisonOperator.IN
                || operator == ComparisonOperator.NOT_IN) {
            return new StringBuilder(SelectQuery.getColumner(schema).apply(name))
                    .append(" ")
                    .append(outputOperator())
                    .append(" ")
                    .append("(")
                    .append(outputValues())
                    .append(")")
                    .toString();
        }

        String left = SelectQuery.getColumner(schema).apply(name)
                .concat(" ")
                .concat(outputOperator())
                .concat(" ");

        if (value instanceof Collection) {
            Collection<?> values = (Collection<?>) value;

            return "("
                    .concat(values.stream()
                            .map((val) -> left.concat(outputValue(val)))
                            .collect(Collectors.joining(" AND ")))
                    .concat(")");
        }

        return left.concat(outputValue(value));
    }

    private String outputOperator() {
        switch (operator) {
        case LOWER:
            return "<";
        case LOWER_EQUAL:
            return "<=";
        case EQUAL:
            return "=";
        case IN:
            return "IN";
        case LIKE:
            return "LIKE";
        case NOT_EQUAL:
            return "!=";
        case NOT_IN:
            return "NOT IN";
        case GREATER_EQUAL:
            return ">=";
        case GREATER:
            return ">";
        default:
            throw new UnsupportedOperationException(operator.name());
        }
    }

    private String outputValue(Object value) {
        if (value == null) {
            return "NULL";
        }

        if (value instanceof String) {
            return "'" + ((String) value).replaceAll("'", "''") + "'";
        }

        if (value instanceof Date) {
            return SHORT_DATE_FORMAT.format((Date) value);
        }

        return value.toString();
    }

    private String outputValues() {
        if (value == null) {
            return "NULL";
        }

        if (value instanceof Collection) {
            Collection<?> values = (Collection<?>) value;

            return values.stream().map(this::outputValue).collect(Collectors.joining(","));
        }

        return outputValue(value);
    }

}
