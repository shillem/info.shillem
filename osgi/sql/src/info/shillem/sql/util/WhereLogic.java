package info.shillem.sql.util;

import info.shillem.util.LogicalOperator;

public class WhereLogic implements IWhere {

    public static final WhereLogic AND = new WhereLogic(LogicalOperator.AND);
    public static final WhereLogic OR = new WhereLogic(LogicalOperator.OR);

    private final LogicalOperator operator;

    private WhereLogic(LogicalOperator operator) {
        this.operator = operator;
    }

    public LogicalOperator getOperator() {
        return operator;
    }

    @Override
    public String output(Schema schema) {
        return operator.name();
    }

}
