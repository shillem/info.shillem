package info.shillem.sql.util;

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

public class SqlSearchQueryConverter<E extends Enum<E> & BaseField>
        extends SearchQueryConverter<E> {

	protected static final DateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	public SqlSearchQueryConverter(SearchQuery<E> query) {
		super(query);
	}

	public SqlSearchQueryConverter(SearchQuery<E> query, Function<E, String> namer) {
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
			return " = ";
		case IN:
			return " IN ";
		case LIKE:
			return " LIKE ";
		case NOT_EQUAL:
			return " != ";
		case NOT_IN:
			return " NOT IN ";
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
		return " " + operator + " ";
	}

	@Override
	protected String formatValue(Object value) {
		if (value instanceof String) {
			return "'" + ((String) value).replaceAll("'", "''") + "'";
		}

		if (value instanceof Date) {
			return SHORT_DATE_FORMAT.format((Date) value);
		}

		return value.toString();
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
