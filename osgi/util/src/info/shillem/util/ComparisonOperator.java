package info.shillem.util;

public enum ComparisonOperator {
    LOWER,
    LOWER_EQUAL,
    EQUAL,
    GREATER_EQUAL,
    GREATER,
    IN,
    NOT_EQUAL,
    NOT_IN,
    LIKE;

    public ComparisonOperator getOpposite() {
        switch (this) {
        case LOWER:
            return GREATER;
        case LOWER_EQUAL:
            return GREATER_EQUAL;
        case EQUAL:
            return NOT_EQUAL;
        case IN:
            return NOT_IN;
        case NOT_IN:
            return IN;
        case NOT_EQUAL:
            return EQUAL;
        case GREATER_EQUAL:
            return LOWER_EQUAL;
        case GREATER:
            return LOWER;
        default:
            throw new UnsupportedOperationException(name());
        }
    }

}
