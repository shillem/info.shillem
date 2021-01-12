package info.shillem.sql.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import info.shillem.util.ComparisonOperator;

public class WhereColumn implements IWhere {

    private static final DateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final String name;
    private final ComparisonOperator operator;
    private final Object value;

    private String nfun;
    private String vfun;

    public WhereColumn(String name, ComparisonOperator operator, Object value) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.operator = Objects.requireNonNull(operator, "Operator cannot be null");
        this.value = value;
    }

    @Override
    public String output(Schema schema) {
        if (operator == ComparisonOperator.IN
                || operator == ComparisonOperator.NOT_IN) {
            return new StringBuilder(outputColumn(schema))
                    .append(" ")
                    .append(outputOperator())
                    .append(" ")
                    .append("(")
                    .append(outputValues())
                    .append(")")
                    .toString();
        }

        String left = outputColumn(schema)
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

    private String outputColumn(Schema schema) {
        String n = SelectQuery.getColumner(schema).apply(name);

        return nfun != null ? String.format(nfun, n) : n;
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
        Function<String, String> formatter = vfun != null
                ? (v) -> String.format(vfun, v)
                : Function.identity();

        if (value == null) {
            return formatter.apply("NULL");
        }

        if (value instanceof String) {
            String val = ((String) value).replaceAll("'", "''");

            if (operator != ComparisonOperator.LIKE) {
                return formatter.apply("'".concat(val).concat("'"));
            }

            val.replace('*', '%');

            return formatter.apply("'"
                    .concat(val.contains("%") ? val : "%".concat(val).concat("%"))
                    .concat("'"));
        }

        if (value instanceof Date) {
            return formatter.apply(SHORT_DATE_FORMAT.format((Date) value));
        }

        return formatter.apply(value.toString());
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

    private String validateFunction(String function) {
        if (function != null && !function.contains("%s")) {
            throw new IllegalArgumentException(
                    "Function must contain '%s' token for replacement");
        }

        return function;
    }

    public WhereColumn withNameFunction(String function) {
        this.nfun = validateFunction(function);

        return this;
    }

    public WhereColumn withValueFunction(String function) {
        this.vfun = validateFunction(function);

        return this;
    }

}
