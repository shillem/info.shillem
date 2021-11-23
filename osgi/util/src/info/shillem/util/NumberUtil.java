package info.shillem.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberUtil {

    private static final BigDecimal BIG_DECIMAL_HUNDRED = new BigDecimal(100);

    private NumberUtil() {
        throw new UnsupportedOperationException();
    }

    public static char getDecimalSeparator(Locale locale) {
        DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(locale);
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        
        return symbols.getDecimalSeparator();
    }

    public static BigDecimal percentageOf(BigDecimal value, BigDecimal percentage) {
        return value.multiply(percentage.divide(BIG_DECIMAL_HUNDRED));
    }
    
    public static BigDecimal subtractPercentageOf(BigDecimal value, BigDecimal percentage) {
        return value.subtract(percentageOf(value, percentage));
    }

}
