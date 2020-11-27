package info.shillem.sql.util;

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

public class SqlSearchQueryConverter<E extends Enum<E> & BaseField>
        extends SearchQueryConverter<E> {

	public SqlSearchQueryConverter(SearchQuery<E> query) {
		super(query);
	}

	public SqlSearchQueryConverter(SearchQuery<E> query, Function<E, String> namer) {
		super(query, namer);
	}

	@Override
	protected String formatComparisonOperator(ComparisonOperator operator) {
		return QueryConverter.formatComparisonOperator(operator);
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
		return QueryConverter.formatLogicalOperator(operator);
	}

	@Override
	protected String formatValue(Object value) {
		return QueryConverter.formatValue(value);
	}

	@Override
	protected void formatValue(StringBuilder builder, Value<E> value) {
		ComparisonOperator operator = value.getOperator();

		if (operator == ComparisonOperator.LIKE) {
			builder.append(String.format("LOWER(%s)", namer.apply(value.getField())));

			builder.append(formatComparisonOperator(operator));

			String stringValue = (String) value.getValue();

			builder.append(String.format("LOWER(%s)",
			        formatValue(stringValue.contains("*")
			                ? stringValue.replaceAll("\\*", "%")
			                : "%" + stringValue + "%")));
		} else {
			builder.append(namer.apply(value.getField())
			        + formatComparisonOperator(operator)
			        + formatValue(value.getValue()));
		}
	}

	@Override
	protected void formatValues(StringBuilder builder, Values<E> values) {
		String columnName = namer.apply(values.getField());
		String operator = formatComparisonOperator(values.getOperator());

		builder.append(columnName);

		builder.append(operator);

		builder.append("(");

		builder.append(values.getValues()
		        .stream()
		        .map(this::formatValue)
		        .collect(Collectors.joining(",")));

		builder.append(")");
	}

}
