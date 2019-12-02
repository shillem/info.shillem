package info.shillem.util.xsp.converter;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

public class NumberConverter implements Converter, StateHolder {
    
    public static final String CONVERTER_ID = "info.shillem.xsp.NumberConverter";

    private static final Class<?>[] GET_INSTANCE_PARAM_TYPES = { String.class };

    private static Class<?> currencyClass;

    static {
        try {
            currencyClass = Class.forName("java.util.Currency");
        } catch (Exception localException) {

        }
    }

    private String currencyCode;

    private String currencySymbol;

    private boolean groupingUsed = true;

    private boolean integerOnly;

    private int maxFractionDigits;

    private boolean maxFractionDigitsSpecified;

    private int maxIntegerDigits;

    private boolean maxIntegerDigitsSpecified;

    private int minFractionDigits;

    private boolean minFractionDigitsSpecified;

    private int minIntegerDigits;

    private boolean minIntegerDigitsSpecified;

    private Locale locale;

    private String pattern;

    private String type = "number";

    private Integer nearest;

    private boolean transientFlag;

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public boolean isGroupingUsed() {
        return groupingUsed;
    }

    public void setGroupingUsed(boolean groupingUsed) {
        this.groupingUsed = groupingUsed;
    }

    public boolean isIntegerOnly() {
        return integerOnly;
    }

    public void setIntegerOnly(boolean integerOnly) {
        this.integerOnly = integerOnly;
    }

    public int getMaxFractionDigits() {
        return maxFractionDigits;
    }

    public void setMaxFractionDigits(int maxFractionDigits) {
        this.maxFractionDigits = maxFractionDigits;
    }

    public boolean isMaxFractionDigitsSpecified() {
        return maxFractionDigitsSpecified;
    }

    public void setMaxFractionDigitsSpecified(boolean maxFractionDigitsSpecified) {
        this.maxFractionDigitsSpecified = maxFractionDigitsSpecified;
    }

    public int getMaxIntegerDigits() {
        return maxIntegerDigits;
    }

    public void setMaxIntegerDigits(int maxIntegerDigits) {
        this.maxIntegerDigits = maxIntegerDigits;
    }

    public boolean isMaxIntegerDigitsSpecified() {
        return maxIntegerDigitsSpecified;
    }

    public void setMaxIntegerDigitsSpecified(boolean maxIntegerDigitsSpecified) {
        this.maxIntegerDigitsSpecified = maxIntegerDigitsSpecified;
    }

    public int getMinFractionDigits() {
        return minFractionDigits;
    }

    public void setMinFractionDigits(int minFractionDigits) {
        this.minFractionDigits = minFractionDigits;
    }

    public boolean isMinFractionDigitsSpecified() {
        return minFractionDigitsSpecified;
    }

    public void setMinFractionDigitsSpecified(boolean minFractionDigitsSpecified) {
        this.minFractionDigitsSpecified = minFractionDigitsSpecified;
    }

    public int getMinIntegerDigits() {
        return minIntegerDigits;
    }

    public void setMinIntegerDigits(int minIntegerDigits) {
        this.minIntegerDigits = minIntegerDigits;
    }

    public boolean isMinIntegerDigitsSpecified() {
        return minIntegerDigitsSpecified;
    }

    public void setMinIntegerDigitsSpecified(boolean minIntegerDigitsSpecified) {
        this.minIntegerDigitsSpecified = minIntegerDigitsSpecified;
    }

    public Locale getLocale() {
        if (locale == null) {
            locale = getLocale(FacesContext.getCurrentInstance());
        }

        return locale;
    }

    private Locale getLocale(FacesContext context) {
        if (locale == null) {
            locale = context.getViewRoot().getLocale();
        }

        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getNearest() {
        return nearest;
    }

    public void setNearest(Integer nearest) {
        this.nearest = nearest;
    }

    public boolean isTransient() {
        return transientFlag;
    }

    public void setTransient(boolean transientFlag) {
        this.transientFlag = transientFlag;
    }

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (context == null || component == null) {
            throw new NullPointerException();
        }

        if (value == null) {
            return null;
        }

        value = value.trim();

        if (value.isEmpty()) {
            return null;
        }

        try {
            Locale locale = getLocale(context);

            NumberFormat parser = getNumberFormat(locale);

            if ((pattern != null && !pattern.equals("")) || "currency".equals(type)) {
                configureCurrency(parser);
            }

            parser.setParseIntegerOnly(isIntegerOnly());

            Number num = parser.parse(value);

            if ("bigDecimal".equals(type) || "currency".equals(type)) {
                BigDecimal bd = new BigDecimal(num.toString());

                if (nearest != null && bd.intValue() > nearest) {
                    bd = bd.setScale(-(nearest.toString().length()), RoundingMode.HALF_UP);
                }

                return bd;
            } else {
                double d = num.doubleValue();

                if (nearest != null && d > nearest) {
                    num = Double.valueOf(Math.round(d / nearest) * nearest);
                }

                if ("double".equals(type)) {
                    return num.doubleValue();
                }

                if ("integer".equals(type)) {
                    return num.intValue();
                }
            }

            return num;
        } catch (Exception e) {
            throw new ConverterException(e);
        }
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (context == null || component == null) {
            throw new NullPointerException();
        }

        if (value == null) {
            return "";
        }

        if (value instanceof String) {
            return (String) value;
        }

        try {
            Locale locale = getLocale(context);
            NumberFormat formatter = getNumberFormat(locale);

            if ((pattern != null && !pattern.equals("")) || ("currency".equals(type))) {
                configureCurrency(formatter);
            }

            configureFormatter(formatter);

            return formatter.format(value);
        } catch (Exception e) {
            throw new ConverterException(e);
        }
    }

    public Object saveState(FacesContext context) {
        Object[] values = new Object[16];
        values[0] = currencyCode;
        values[1] = currencySymbol;
        values[2] = (isGroupingUsed() ? Boolean.TRUE : Boolean.FALSE);
        values[3] = (isIntegerOnly() ? Boolean.TRUE : Boolean.FALSE);
        values[4] = new Integer(maxFractionDigits);
        values[5] = (maxFractionDigitsSpecified ? Boolean.TRUE : Boolean.FALSE);
        values[6] = new Integer(maxIntegerDigits);
        values[7] = (maxIntegerDigitsSpecified ? Boolean.TRUE : Boolean.FALSE);
        values[8] = new Integer(minFractionDigits);
        values[9] = (minFractionDigitsSpecified ? Boolean.TRUE : Boolean.FALSE);
        values[10] = new Integer(minIntegerDigits);
        values[11] = (minIntegerDigitsSpecified ? Boolean.TRUE : Boolean.FALSE);
        values[12] = locale;
        values[13] = pattern;
        values[14] = type;
        values[15] = nearest;
        return values;
    }

    public void restoreState(FacesContext context, Object state) {
        Object[] values = (Object[]) state;

        currencyCode = ((String) values[0]);
        currencySymbol = ((String) values[1]);
        groupingUsed = ((Boolean) values[2]).booleanValue();
        integerOnly = ((Boolean) values[3]).booleanValue();
        maxFractionDigits = ((Integer) values[4]).intValue();
        maxFractionDigitsSpecified = ((Boolean) values[5]).booleanValue();
        maxIntegerDigits = ((Integer) values[6]).intValue();
        maxIntegerDigitsSpecified = ((Boolean) values[7]).booleanValue();
        minFractionDigits = ((Integer) values[8]).intValue();
        minFractionDigitsSpecified = ((Boolean) values[9]).booleanValue();
        minIntegerDigits = ((Integer) values[10]).intValue();
        minIntegerDigitsSpecified = ((Boolean) values[11]).booleanValue();
        locale = ((Locale) values[12]);
        pattern = ((String) values[13]);
        type = ((String) values[14]);
        nearest = ((Integer) values[15]);
    }

    private NumberFormat getNumberFormat(Locale locale) {
        if (pattern == null && type == null) {
            throw new IllegalArgumentException("Either pattern or type must be specified.");
        }

        if (pattern != null) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
            return new DecimalFormat(pattern, symbols);
        }

        if ("currency".equals(type)) {
            return NumberFormat.getCurrencyInstance(locale);
        } else if ("percent".equals(type)) {
            return NumberFormat.getPercentInstance(locale);
        } else if (type.matches("integer|double|number")) {
            return NumberFormat.getNumberInstance(locale);
        }

        throw new ConverterException(new IllegalArgumentException(type));
    }

    private void configureCurrency(NumberFormat formatter) throws Exception {
        String code = null;
        String symbol = null;

        if (currencyCode == null && currencySymbol == null) {
            return;
        }

        if (currencyCode != null && currencySymbol != null) {
            if (currencyClass != null) {
                code = currencyCode;
            } else {
                symbol = currencySymbol;
            }
        } else if (currencyCode == null) {
            symbol = currencySymbol;
        } else if (currencyClass != null) {
            code = currencyCode;
        } else {
            symbol = currencyCode;
        }

        if (code != null) {
            Method method = currencyClass.getMethod("getInstance", GET_INSTANCE_PARAM_TYPES);
            Object currency = method.invoke(null, new Object[] { code });

            Class<?>[] paramTypes = new Class[] { currencyClass };
            Class<?> numberFormatClass = Class.forName("java.text.NumberFormat");
            method = numberFormatClass.getMethod("setCurrency", paramTypes);
            method.invoke(formatter, new Object[] { currency });
        } else {
            DecimalFormat df = (DecimalFormat) formatter;
            DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
            dfs.setCurrencySymbol(symbol);
            df.setDecimalFormatSymbols(dfs);
        }
    }

    private void configureFormatter(NumberFormat formatter) {
        formatter.setGroupingUsed(groupingUsed);

        if (maxIntegerDigitsSpecified) {
            formatter.setMaximumIntegerDigits(maxIntegerDigits);
        }

        if (minIntegerDigitsSpecified) {
            formatter.setMinimumIntegerDigits(minIntegerDigits);
        }

        if (maxFractionDigitsSpecified) {
            formatter.setMaximumFractionDigits(maxFractionDigits);
        }

        if (minFractionDigitsSpecified) {
            formatter.setMinimumFractionDigits(minFractionDigits);
        }
    }

}
