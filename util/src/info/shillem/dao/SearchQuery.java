package info.shillem.dao;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Objects;
import java.util.Set;

import info.shillem.dto.BaseField;
import info.shillem.util.ComparisonOperator;
import info.shillem.util.LogicalOperator;

public class SearchQuery<E extends Enum<E> & BaseField> extends PageQuery<E> {

    public static class Group implements Piece {

        private final Deque<Piece> deque = new ArrayDeque<>();

        public Group add(Logical logicalValue, Piece newPiece) {
            Piece lastPiece = deque.peekLast();

            if (lastPiece != null && !(lastPiece instanceof Logical)) {
                deque.add(logicalValue);
            }

            deque.add(Objects.requireNonNull(newPiece, "Piece cannot be null"));

            return this;
        }

        public Group and(Piece piece) {
            return add(Logical.AND, piece);
        }
        
        public Collection<Piece> getPieces() {
            return deque;
        }

        public boolean isEmpty() {
            return deque.isEmpty();
        }

        public Group or(Piece piece) {
            return add(Logical.OR, piece);
        }
        
        @Override
        public String toString() {
            return deque.toString();
        }

    }

    public static class Logical implements Piece {

        public static final Logical AND = new Logical(LogicalOperator.AND);
        public static final Logical OR = new Logical(LogicalOperator.OR);

        private final LogicalOperator operator;

        private Logical(LogicalOperator operator) {
            this.operator = operator;
        }
        
        public LogicalOperator getOperator() {
            return operator;
        }

        @Override
        public String toString() {
            return operator.toString();
        }

    }

    public interface Piece {
        
    }

    public static class Value<E extends Enum<E> & BaseField> implements Piece {

        private final E field;
        private final ComparisonOperator operator;
        private final Object value;

        public Value(E field, Object value) {
            this(field, value, ComparisonOperator.EQUAL);
        }

        public Value(E field, Object value, ComparisonOperator operator) {
            this.field = Objects.requireNonNull(field, "Field cannot be null");
            this.operator = Objects.requireNonNull(operator, "Operator cannot be null");
            this.value = value;
        }
        
        public E getField() {
            return field;
        }
        
        public ComparisonOperator getOperator() {
            return operator;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "{" + getField() + " " + getOperator() + " " + value + "}";
        }

    }

    public static class Values<E extends Enum<E> & BaseField> implements Piece {

        private final E field;
        private final ComparisonOperator operator;
        private final Set<Object> values;

        public Values(E field, Set<Object> values) {
            this(field, values, ComparisonOperator.IN);
        }

        public Values(E field, Set<Object> values, ComparisonOperator operator) {
            this.field = Objects.requireNonNull(field, "Field cannot be null");
            this.operator = Objects.requireNonNull(operator, "Operator cannot be null");
            
            Objects.requireNonNull(operator, "Values cannot be null");
            
            if (values.isEmpty()) {
                throw new IllegalArgumentException("Values cannot be empty");
            }
            
            this.values = values;
        }
        
        public E getField() {
            return field;
        }
        
        public ComparisonOperator getOperator() {
            return operator;
        }

        public Set<Object> getValues() {
            return values;
        }

        @Override
        public String toString() {
            return "{" + getField() + " " + getOperator() + " " + values + "}";
        }

    }

    private final Group group;

    SearchQuery(SearchQueryBuilder<E> builder) {
        super(builder.base, builder.page);

        group = builder.group;
    }

    public Collection<Piece> getPieces() {
        return group.getPieces();
    }

}
