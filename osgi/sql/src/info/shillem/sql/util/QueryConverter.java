package info.shillem.sql.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import info.shillem.util.ComparisonOperator;
import info.shillem.util.LogicalOperator;

public class QueryConverter {

    private static final DateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private QueryConverter() {
        
    }
    
    public static String formatComparisonOperator(ComparisonOperator operator) {
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
    
    public static String formatLogicalOperator(LogicalOperator operator) {
        return " " + operator + " ";
    }
    
    public static String formatValue(Object value) {
        if (value instanceof String) {
            return "'" + ((String) value).replaceAll("'", "''") + "'";
        }

        if (value instanceof Date) {
            return SHORT_DATE_FORMAT.format((Date) value);
        }

        return value.toString();
    }
    
}
