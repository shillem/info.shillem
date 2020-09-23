package info.shillem.dao;

import java.util.Objects;
import java.util.function.Function;

import info.shillem.dao.SearchQuery.Group;
import info.shillem.dao.SearchQuery.Logical;
import info.shillem.dao.SearchQuery.Value;
import info.shillem.dao.SearchQuery.Values;
import info.shillem.dto.BaseField;
import info.shillem.util.ComparisonOperator;
import info.shillem.util.LogicalOperator;

public abstract class SearchQueryConverter<E extends Enum<E> & BaseField> {

    protected final SearchQuery<E> query;
    protected final Function<E, String> namer;

    public SearchQueryConverter(SearchQuery<E> query) {
        this(query, (field) -> field.name());
    }

    public SearchQueryConverter(SearchQuery<E> query, Function<E, String> namer) {
        this.query = Objects.requireNonNull(query, "Query cannot be null");
        this.namer = Objects.requireNonNull(namer, "Namer cannot be null");
    }

    protected abstract String formatComparisonOperator(ComparisonOperator operator);

    protected String formatField(E field) {
        return namer.apply(field);
    }

    protected abstract void formatGroup(StringBuilder builder, Group group);

    protected abstract void formatLogical(StringBuilder builder, Logical logical);

    protected abstract String formatLogicalOperator(LogicalOperator operator);

    @SuppressWarnings("unchecked")
    protected void formatPiece(StringBuilder builder, SearchQuery.Piece piece) {
        if (piece instanceof Group) {
            formatGroup(builder, (Group) piece);
        } else if (piece instanceof Logical) {
            formatLogical(builder, (Logical) piece);
        } else if (piece instanceof Value) {
            formatValue(builder, (Value<E>) piece);
        } else if (piece instanceof Values) {
            formatValues(builder, (Values<E>) piece);
        }
    }

    protected abstract String formatValue(Object value);

    protected abstract void formatValue(StringBuilder builder, Value<E> value);

    protected abstract void formatValues(StringBuilder builder, Values<E> values);

    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();

        query.getPieces().forEach((piece) -> formatPiece(builder, piece));

        return builder.toString();
    }

}
