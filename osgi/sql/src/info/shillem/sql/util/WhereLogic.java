package info.shillem.sql.util;

import info.shillem.util.LogicalOperator;

public class WhereLogic extends AWhere {

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
    protected void link(SelectQuery select) {
        // Unlinked
    }

    @Override
    public String output() {
        return operator.name();
    }

}
