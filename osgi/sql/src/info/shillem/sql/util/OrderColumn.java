package info.shillem.sql.util;

import java.util.Objects;

import info.shillem.util.OrderOperator;

public class OrderColumn extends AOrder {

    private final String name;
    private final OrderOperator operator;

    public OrderColumn(String name, OrderOperator operator) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.operator = Objects.requireNonNull(operator, "Operator cannot be null");
    }

    @Override
    public String output() {
        return findSchemaColumn(name)
                .map(this::outputSchemaColumn)
                .orElse(name)
                .concat(" ")
                .concat(operator == OrderOperator.ASCENDING ? "ASC" : "DESC");
    }

}
