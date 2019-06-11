package info.shillem.dao;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import info.shillem.dto.BaseField;

public class SearchQuery<E extends Enum<E> & BaseField> extends Query<E> {

    public static class Builder<E extends Enum<E> & BaseField> extends QueryBuilder<E, Builder<E>> {

        private final Deque<Piece> deque = new ArrayDeque<Piece>();
        private int maxCount;

        private Builder<E> addLogicalOperator(LogicalOperator operator) {
            Piece piece = deque.peekLast();

            if (piece instanceof Condition
                    || piece instanceof Wrapper) {
                deque.add(operator);

                return this;
            }

            throw new IllegalStateException(
                    "Logical operator not allowed when preceded by " + piece);
        }

        public Builder<E> and() {
            return addLogicalOperator(LogicalOperator.AND);
        }

        public SearchQuery<E> build() {
            if (deque.isEmpty()) {
                throw new IllegalStateException("Search query cannot be empty");
            }

            return new SearchQuery<>(this);
        }

        public Builder<E> condition(Condition<E> condition) {
            Piece piece = deque.peekLast();

            if (Objects.isNull(piece) || piece instanceof LogicalOperator) {
                deque.add(condition);

                return this;
            }

            throw new IllegalStateException("Condition not allowed when preceded by " + piece);
        }

        public int getMaxCount() {
            return maxCount;
        }

        public Builder<E> or() {
            return addLogicalOperator(LogicalOperator.OR);
        }

        public Builder<E> setMaxCount(int maxCount) {
            this.maxCount = maxCount;

            return this;
        }
        
        public Builder<E> wrap() {
            Piece piece = deque.peekLast();

            if (piece instanceof Condition) {
                deque.add(Wrapper.INSTANCE);

                return this;
            }

            throw new IllegalStateException("Wrapper not allowed when preceded by " + piece);
        }

    }

    public enum ComparisonOperator {
        LESS_THAN("<"),
        LESS_THAN_OR_EQUAL("<="),
        EQUAL("="),
        NOT_EQUAL("!="),
        GREATER_THAN_OR_EQUAL(">="),
        GREATER_THAN(">");

        private final String sign;

        private ComparisonOperator(String sign) {
            this.sign = sign;
        }

        public String getSign() {
            return sign;
        }

        public ComparisonOperator non() {
            switch (this) {
            case LESS_THAN:
                return GREATER_THAN;
            case LESS_THAN_OR_EQUAL:
                return GREATER_THAN_OR_EQUAL;
            case EQUAL:
                return NOT_EQUAL;
            case NOT_EQUAL:
                return EQUAL;
            case GREATER_THAN_OR_EQUAL:
                return LESS_THAN_OR_EQUAL;
            case GREATER_THAN:
                return LESS_THAN;
            }

            throw new UnsupportedOperationException(this.name());
        }
    }

    public static abstract class Condition<E extends Enum<E> & BaseField> implements Piece {

        private final E field;
        private final ComparisonOperator operator;

        private Condition(E field) {
            this(field, ComparisonOperator.EQUAL);
        }

        private Condition(E field, ComparisonOperator operator) {
            this.field = Objects.requireNonNull(field, "Field cannot be null");
            this.operator = Objects.requireNonNull(operator, "Operator cannot be null");
        }

        public E getField() {
            return field;
        }

        public ComparisonOperator getOperator() {
            return operator;
        }

    }

    public static class FieldValue<E extends Enum<E> & BaseField> extends Condition<E> {

        private final Object value;

        public FieldValue(E field, Object value) {
            this(field, value, ComparisonOperator.EQUAL);
        }

        public FieldValue(E field, Object value, ComparisonOperator operator) {
            super(field, operator);

            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "{" + getField() + " " + getOperator().getSign() + " " + value + "}";
        }

    }

    public static class FieldValues<E extends Enum<E> & BaseField> extends Condition<E> {

        private final Collection<Object> values;

        public FieldValues(E field, Collection<Object> values) {
            this(field, values, ComparisonOperator.EQUAL);
        }

        public FieldValues(E field, Collection<Object> values, ComparisonOperator operator) {
            super(field, operator);

            this.values = values;
        }

        public Collection<Object> getValues() {
            return values;
        }

        @Override
        public String toString() {
            return "{" + getField() + " " + getOperator().getSign() + " " + values + "}";
        }

    }

    public enum LogicalOperator implements Piece {
        AND, OR;
    }

    public interface Piece {

    }

    public enum Wrapper implements Piece {
        INSTANCE {
            @Override
            public String toString() {
                return "WRAP";
            }
        };
    }

    private final List<Piece> pieces;
    private final int maxCount;

    private SearchQuery(Builder<E> builder) {
        super(builder);

        pieces = new ArrayList<>(builder.deque);
        maxCount = builder.getMaxCount();
    }
    
    public int getMaxCount() {
        return maxCount;
    }
    
    public List<Piece> getPieces() {
        return pieces;
    }

    @Override
    public String toString() {
        return pieces.toString();
    }

}
