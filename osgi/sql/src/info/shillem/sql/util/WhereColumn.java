package info.shillem.sql.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import info.shillem.util.ComparisonOperator;

public class WhereColumn implements IWhere {

    private static final DateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

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
    public String output(Schema schema) {
        if (operator == ComparisonOperator.IN
                || operator == ComparisonOperator.NOT_IN) {
            return new StringBuilder(outputColumn(schema))
                    .append(" ")
                    .append(outputOperator())
                    .append(" ")
                    .append("(")
                    .append(outputValues(schema))
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
                            .map((val) -> left.concat(outputValue(val, schema)))
                            .collect(Collectors.joining(" AND ")))
                    .concat(")");
        }

        return left.concat(outputValue(value, schema));
    }

    private String outputColumn(Schema schema) {
        return outputColumn(name, schema);
    }

    private String outputColumn(String name, Schema schema) {
        String n = SelectQuery.getColumner(schema).apply(name);

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

    private String outputValue(Object value, Schema schema) {
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
            return formatter.apply("'".concat(SHORT_DATE_FORMAT.format((Date) value)).concat("'"));
        }

        if (value instanceof SelectQuery.Column) {
            return outputColumn(((SelectQuery.Column) value).getName(), schema);
        }

        return formatter.apply(value.toString());
    }

    private String outputValues(Schema schema) {
        if (value == null) {
            return "NULL";
        }

        if (value instanceof Collection) {
            Collection<?> values = (Collection<?>) value;

            return values.stream()
                    .map((v) -> outputValue(v, schema))
                    .collect(Collectors.joining(","));
        }

        return outputValue(value, schema);
    }

    private String validateFunction(String function) {
        if (function != null && !function.contains("%s")) {
            throw new IllegalArgumentException(
                    "Function must contain '%s' token for replacement");
        }

        return function;
    }

}
