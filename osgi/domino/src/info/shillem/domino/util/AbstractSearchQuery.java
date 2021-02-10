package info.shillem.domino.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import info.shillem.dao.Query;
import info.shillem.dao.Query.Group;
import info.shillem.dao.Query.Logical;
import info.shillem.dao.Query.Value;
import info.shillem.dao.Query.Values;
import info.shillem.dto.BaseField;
import info.shillem.util.ComparisonOperator;
import info.shillem.util.LogicalOperator;

abstract class AbstractSearchQuery<E extends Enum<E> & BaseField> {

    protected static final DateFormat SHORT_DATE_FORMAT =
            SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);

    private final Query<E> query;

    private Function<E, String> namer;

    public AbstractSearchQuery(Query<E> query) {
        this.query = Objects.requireNonNull(query, "Db query cannot be null");
    }

    public String output() {
        switch (query.getType()) {
        case FILTER:
            return query.getFilters().entrySet().stream()
                    .map((e) -> String.format("%s %s %s",
                            outputField(e.getKey()),
                            outputOperator(ComparisonOperator.EQUAL),
                            outputStringValue(e.getValue())))
                    .collect(Collectors.joining(
                            " ".concat(outputOperator(LogicalOperator.AND)).concat(" ")));
        case SEARCH:
            return query.getClauses().stream()
                    .map(this::outputClause)
                    .collect(Collectors.joining(" "));
        default:
            throw new UnsupportedOperationException(
                    query.getType().name().concat(" query is not supported"));
        }
    }

    protected String outputField(E field) {
        return namer != null ? namer.apply(field) : field.name();
    }

    protected String outputGroup(Group group) {
        if (group.isEmpty()) {
            return "";
        }

        return new StringBuilder()
                .append("(")
                .append(group.getClauses().stream()
                        .map((piece) -> outputClause(piece))
                        .collect(Collectors.joining(" ")))
                .append(")")
                .toString();
    }

    protected String outputOperator(ComparisonOperator value) {
        switch (value) {
        case LOWER:
            return "<";
        case LOWER_EQUAL:
            return "<=";
        case EQUAL:
        case IN:
        case LIKE:
            return "=";
        case NOT_EQUAL:
        case NOT_IN:
            return "!=";
        case GREATER_EQUAL:
            return ">=";
        case GREATER:
            return ">";
        default:
            throw new UnsupportedOperationException(value.name());
        }
    }

    protected abstract String outputOperator(LogicalOperator value);

    @SuppressWarnings("unchecked")
    protected String outputClause(Query.Clause clause) {
        if (clause instanceof Group) {
            return outputGroup((Group) clause);
        }

        if (clause instanceof Logical) {
            return outputOperator(((Logical) clause).getOperator());
        }

        if (clause instanceof Value) {
            return outputValue((Value<E>) clause);
        }

        if (clause instanceof Values) {
            return outputValues((Values<E>) clause);
        }

        throw new UnsupportedOperationException(clause.getClass().getName()
                .concat(" joining is not implemented"));
    }

    protected abstract String outputStringValue(Object value);

    protected abstract String outputValue(E field, ComparisonOperator operator, Object value);

    protected String outputValue(Value<E> value) {
        return outputValue(value.getField(), value.getOperator(), value.getValue());
    }

    protected String outputValues(Values<E> values) {
        return new StringBuilder()
                .append("(")
                .append(values.getValues().stream()
                        .map((val) -> outputValue(values.getField(), values.getOperator(), val))
                        .collect(Collectors.joining(
                                " ".concat(outputOperator(LogicalOperator.OR)).concat(" "))))
                .append(")")
                .toString();
    }

    protected void setNamer(Function<E, String> namer) {
        this.namer = namer;
    }

}
