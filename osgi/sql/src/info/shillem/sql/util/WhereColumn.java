package info.shillem.sql.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import info.shillem.util.ComparisonOperator;

public class WhereColumn extends AWhere {

    private final String name;
    private final ComparisonOperator operator;
    private final Object value;

    private List<String> nfuns;
    private List<String> vfuns;

    public WhereColumn(String name, ComparisonOperator operator, Object value) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.operator = Objects.requireNonNull(operator, "Operator cannot be null");
        this.value = value;
    }

    public WhereColumn addNameFunction(String value) {
        if (nfuns == null) {
            nfuns = new ArrayList<>();
        }

        nfuns.add(validateFunction(value));

        return this;
    }

    public WhereColumn addValueFunction(String value) {
        if (vfuns == null) {
            vfuns = new ArrayList<>();
        }

        vfuns.add(validateFunction(value));

        return this;
    }

    @Override
    public String output() {
        StringBuilder identifier = new StringBuilder(outputName(name))
                .append(" ")
                .append(outputOperator())
                .append(" ");

        if (operator == ComparisonOperator.IN
                || operator == ComparisonOperator.NOT_IN) {
            return identifier
                    .append("(")
                    .append(outputValues())
                    .append(")")
                    .toString();
        }

        if (value instanceof Collection) {
            Collection<?> values = (Collection<?>) value;

            return "("
                    .concat(values.stream()
                            .map((val) -> identifier.toString().concat(outputValue(val)))
                            .collect(Collectors.joining(" AND ")))
                    .concat(")");
        }

        return identifier.append(outputValue(value)).toString();
    }

    private String outputName(String name) {
        String n = findSchemaColumn(name)
                .map(this::outputSchemaColumn)
                .orElse(name);

        if (nfuns == null) {
            return n;
        }

        for (String nfun : nfuns) {
            n = String.format(nfun, n);
        }

        return n;
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
        Function<String, String> formatter = vfuns != null
                ? (v) -> {
                    for (String vfun : vfuns) {
                        v = String.format(vfun, v);
                    }

                    return v;
                }
                : Function.identity();

        if (value == null) {
            return formatter.apply("NULL");
        }

        if (value instanceof String) {
            String val = ((String) value).replaceAll("'", "''");

            if (operator != ComparisonOperator.LIKE) {
                return formatter.apply("'".concat(val).concat("'"));
            }

            val = val.replace('*', '%');

            if (!val.contains("%")) {
                val = "%".concat(val).concat("%");
            }

            return formatter.apply("'".concat(val).concat("'"));
        }

        if (value instanceof Date) {
            return formatter.apply(
                    "'".concat(SelectQuery.SHORT_DATE_FORMAT.format((Date) value)).concat("'"));
        }

        if (value instanceof SelectColumn) {
            return outputName(((SelectColumn) value).getName());
        }

        return formatter.apply(value.toString());
    }

    private String outputValues() {
        if (value == null) {
            return "NULL";
        }

        if (value instanceof Collection) {
            Collection<?> values = (Collection<?>) value;

            return values.stream()
                    .map((v) -> outputValue(v))
                    .collect(Collectors.joining(","));
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

}
