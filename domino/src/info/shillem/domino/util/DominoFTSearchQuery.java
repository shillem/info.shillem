package info.shillem.domino.util;

import java.util.Date;
import java.util.function.Function;

import info.shillem.dao.SearchQuery;
import info.shillem.dao.SearchQuery.ComparisonOperator;
import info.shillem.dao.SearchQuery.LogicalOperator;
import info.shillem.dto.BaseField;

public class DominoFTSearchQuery<E extends Enum<E> & BaseField> extends DominoSearchQuery<E> {

    public DominoFTSearchQuery(SearchQuery<E> query, Function<E, String> itemNamer) {
        super(query, itemNamer);
    }

    @Override
    protected String formatFieldValue(E field, ComparisonOperator operator, Object value,
            Function<E, String> itemNamer) {
        String itemName = formatItemName(field, itemNamer);
        String itemValue = formatValue(value);

        if (operator == ComparisonOperator.NOT_EQUAL) {
            return "NOT " + itemName
                    + formatComparisonOperator(ComparisonOperator.EQUAL) + itemValue;
        }

        return itemName + formatComparisonOperator(operator) + itemValue;
    }
    
    @Override
    protected String formatItemName(E field, Function<E, String> itemNamer) {
        return "[" + super.formatItemName(field, itemNamer) + "]";
    }

    @Override
    protected String formatLogicalOperator(LogicalOperator operator) {
        return " " + operator.name() + " ";
    }

    @Override
    protected String formatValue(Object value) {
        if (value instanceof String) {
            return "\"" + value + "\"";
        }

        if (value instanceof Date) {
            return SHORT_DATE_FORMAT.format((Date) value);
        }

        return value.toString();
    }

}
