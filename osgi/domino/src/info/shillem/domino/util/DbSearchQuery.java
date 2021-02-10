package info.shillem.domino.util;

import java.util.Date;
import java.util.function.Function;

import info.shillem.dao.Query;
import info.shillem.dto.BaseField;
import info.shillem.util.ComparisonOperator;
import info.shillem.util.LogicalOperator;

public class DbSearchQuery<E extends Enum<E> & BaseField> extends AbstractSearchQuery<E> {

    public DbSearchQuery(Query<E> query) {
        super(query);
    }

    @Override
    protected String outputOperator(LogicalOperator value) {
        switch (value) {
        case AND:
            return "&";
        case OR:
            return "|";
        default:
            throw new UnsupportedOperationException(value.name());
        }
    }

    @Override
    protected String outputStringValue(Object value) {
        if (value == null) {
            return "\"\"";
        }

        if (value instanceof String) {
            return "\"" + value + "\"";
        }

        if (value instanceof Date) {
            return "[" + SHORT_DATE_FORMAT.format((Date) value) + "]";
        }

        return value.toString();
    }

    @Override
    protected String outputValue(E field, ComparisonOperator operator, Object value) {
        if (operator == ComparisonOperator.LIKE) {
            return String.format("@Contains(%s;%s)",
                    outputField(field),
                    outputStringValue(value));
        }

        return String.format("%s %s %s",
                outputField(field),
                outputOperator(operator),
                outputStringValue(value));
    }

    public DbSearchQuery<E> withNamer(Function<E, String> namer) {
        setNamer(namer);

        return this;
    }

}
