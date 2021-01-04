package info.shillem.domino.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import info.shillem.dao.FilterQuery;
import info.shillem.dao.Query;
import info.shillem.dao.SearchQuery;
import info.shillem.dao.SearchQuery.Group;
import info.shillem.dao.SearchQuery.Logical;
import info.shillem.dao.SearchQuery.Value;
import info.shillem.dao.SearchQuery.Values;
import info.shillem.dto.BaseField;
import info.shillem.util.ComparisonOperator;
import info.shillem.util.LogicalOperator;

abstract class AbstractSearchQuery<E extends Enum<E> & BaseField> {

    protected static final DateFormat SHORT_DATE_FORMAT =
            SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);

    private final Query<E> query;

    private Function<E, String> namer;

    public AbstractSearchQuery(FilterQuery<E> query) {
        this.query = Objects.requireNonNull(query, "Db query cannot be null");
    }

    public AbstractSearchQuery(SearchQuery<E> query) {
        this.query = Objects.requireNonNull(query, "Db query cannot be null");
    }

    public String output() {
        if (query instanceof FilterQuery) {
            return outputFilterQuery((FilterQuery<E>) query);
        }

        return outputSearchQuery((SearchQuery<E>) query);
    }

    protected String outputField(E field) {
        return namer != null ? namer.apply(field) : field.name();
    }

    protected String outputFilterQuery(FilterQuery<E> query) {
        return query.getFilters().entrySet().stream()
                .map((e) -> String.format("%s %s %s",
                        outputField(e.getKey()),
                        outputOperator(ComparisonOperator.EQUAL),
                        outputStringValue(e.getValue())))
                .collect(Collectors.joining(
                        " ".concat(outputOperator(LogicalOperator.AND)).concat(" ")));
    }

    protected String outputGroup(Group group) {
        if (group.isEmpty()) {
            return "";
        }

        return new StringBuilder()
                .append("(")
                .append(group.getPieces().stream()
                        .map((piece) -> outputPiece(piece))
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
    protected String outputPiece(SearchQuery.Piece piece) {
        if (piece instanceof Group) {
            return outputGroup((Group) piece);
        }

        if (piece instanceof Logical) {
            return outputOperator(((Logical) piece).getOperator());
        }

        if (piece instanceof Value) {
            return outputValue((Value<E>) piece);
        }

        if (piece instanceof Values) {
            return outputValues((Values<E>) piece);
        }

        throw new UnsupportedOperationException(piece.getClass().getName()
                .concat(" joining is not implemented"));
    }

    protected String outputSearchQuery(SearchQuery<E> query) {
        return query.getPieces().stream()
                .map(this::outputPiece)
                .collect(Collectors.joining(" "));
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
