package info.shillem.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberUtil {

    private static final BigDecimal BIG_DECIMAL_HUNDRED = new BigDecimal(100);

    private NumberUtil() {
        throw new UnsupportedOperationException();
    }

    public static BigDecimal percentageOf(BigDecimal value, BigDecimal percentage) {
        BigDecimal fraction = percentage.divide(BIG_DECIMAL_HUNDRED);
        fraction = value.multiply(fraction);
        fraction = fraction.setScale(-1, RoundingMode.HALF_UP);

        return fraction;
    }

    public static BigDecimal subtractPercentageOf(BigDecimal value, BigDecimal percentage) {
        return value.subtract(percentageOf(value, percentage));
    }

}
