package info.shillem.util.xsp.converter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

public class NumberConverter implements Converter, Serializable, StateHolder {

    private static final long serialVersionUID = 1L;

    public static final String CONVERTER_ID = "info.shillem.xsp.NumberConverter";

    private String currencyCode;
    private String currencySymbol;
    private boolean groupingUsed;
    private boolean integerOnly;
    private Integer maxFractionDigits;
    private Integer maxIntegerDigits;
    private Integer minFractionDigits;
    private Integer minIntegerDigits;
    private Integer nearestInteger;
    private Locale locale;
    private String pattern;
    private String type;
    private boolean transientFlag;

    public NumberConverter() {
        groupingUsed = true;
        type = "number";
    }

    private void configureCurrency(NumberFormat formatter) throws UnsupportedOperationException {
        if (currencyCode != null) {
            formatter.setCurrency(Currency.getInstance(currencyCode));
        } else if (currencySymbol != null) {
            DecimalFormat df = (DecimalFormat) formatter;
            DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
            dfs.setCurrencySymbol(currencySymbol);
            df.setDecimalFormatSymbols(dfs);
        }
    }

    private void configureFormatter(NumberFormat formatter) {
        formatter.setGroupingUsed(groupingUsed);

        if (maxIntegerDigits != null) {
            formatter.setMaximumIntegerDigits(maxIntegerDigits);
        }

        if (minIntegerDigits != null) {
            formatter.setMinimumIntegerDigits(minIntegerDigits);
        }

        if (maxFractionDigits != null) {
            formatter.setMaximumFractionDigits(maxFractionDigits);
        }

        if (minFractionDigits != null) {
            formatter.setMinimumFractionDigits(minFractionDigits);
        }
    }

    @Override
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

            if ((pattern != null && !pattern.isEmpty()) || "currency".equals(type)) {
                configureCurrency(parser);
            }

            parser.setParseIntegerOnly(isIntegerOnly());

            Number num = parser.parse(value);

            if ("bigDecimal".equals(type) || "currency".equals(type)) {
                BigDecimal bd = new BigDecimal(num.toString());

                if (nearestInteger != null && bd.intValue() > nearestInteger) {
                    bd = bd.setScale(-(nearestInteger.toString().length()), RoundingMode.HALF_UP);
                }

                return bd;
            }

            if (nearestInteger != null && num.doubleValue() > nearestInteger) {
                num = Double
                        .valueOf(Math.round(num.doubleValue() / nearestInteger) * nearestInteger);
            }

            if ("double".equals(type)) {
                return num.doubleValue();
            }

            if ("integer".equals(type)) {
                return num.intValue();
            }

            return num;
        } catch (Exception e) {
            throw new ConverterException(e);
        }
    }

    @Override
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
            Number num = (Number) value;

            if ((pattern != null && !pattern.isEmpty()) || ("currency".equals(type))) {
                configureCurrency(formatter);
            }

            configureFormatter(formatter);

            if (nearestInteger != null && num.doubleValue() > nearestInteger) {
                num = Double
                        .valueOf(Math.round(num.doubleValue() / nearestInteger) * nearestInteger);
            }

            return formatter.format(num);
        } catch (Exception e) {
            throw new ConverterException(e);
        }
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
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

    public Integer getMaxFractionDigits() {
        return maxFractionDigits;
    }

    public Integer getMaxIntegerDigits() {
        return maxIntegerDigits;
    }

    public Integer getMinFractionDigits() {
        return minFractionDigits;
    }

    public Integer getMinIntegerDigits() {
        return minIntegerDigits;
    }

    public Integer getNearestInteger() {
        return nearestInteger;
    }

    private NumberFormat getNumberFormat(Locale locale) {
        if (pattern == null && type == null) {
            throw new IllegalStateException("Either pattern or type must be configured");
        }

        if (pattern != null) {
            return new DecimalFormat(pattern, new DecimalFormatSymbols(locale));
        }

        if ("currency".equals(type)) {
            return NumberFormat.getCurrencyInstance(locale);
        }

        if ("percent".equals(type)) {
            return NumberFormat.getPercentInstance(locale);
        }

        if (type.matches("bigDecimal|integer|double|number")) {
            return NumberFormat.getNumberInstance(locale);
        }

        throw new ConverterException(new IllegalArgumentException(type));
    }

    public String getPattern() {
        return pattern;
    }

    public String getType() {
        return type;
    }

    public boolean isGroupingUsed() {
        return groupingUsed;
    }

    public boolean isIntegerOnly() {
        return integerOnly;
    }

    @Override
    public boolean isTransient() {
        return transientFlag;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        Object[] values = (Object[]) state;

        currencyCode = (String) values[0];
        currencySymbol = (String) values[1];
        groupingUsed = ((Boolean) values[2]).booleanValue();
        integerOnly = ((Boolean) values[3]).booleanValue();
        maxFractionDigits = (Integer) values[4];
        maxIntegerDigits = (Integer) values[5];
        minFractionDigits = (Integer) values[6];
        minIntegerDigits = (Integer) values[7];
        nearestInteger = (Integer) values[8];
        locale = (Locale) values[9];
        pattern = (String) values[10];
        type = (String) values[11];
    }

    @Override
    public Object saveState(FacesContext context) {
        Object[] values = new Object[12];

        values[0] = currencyCode;
        values[1] = currencySymbol;
        values[2] = groupingUsed;
        values[3] = integerOnly;
        values[4] = maxFractionDigits;
        values[5] = maxIntegerDigits;
        values[6] = minFractionDigits;
        values[7] = minIntegerDigits;
        values[8] = nearestInteger;
        values[9] = locale;
        values[10] = pattern;
        values[11] = type;

        return values;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public void setGroupingUsed(boolean groupingUsed) {
        this.groupingUsed = groupingUsed;
    }

    public void setIntegerOnly(boolean integerOnly) {
        this.integerOnly = integerOnly;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setMaxFractionDigits(Integer maxFractionDigits) {
        this.maxFractionDigits = maxFractionDigits;
    }

    public void setMaxIntegerDigits(Integer maxIntegerDigits) {
        this.maxIntegerDigits = maxIntegerDigits;
    }

    public void setMinFractionDigits(Integer minFractionDigits) {
        this.minFractionDigits = minFractionDigits;
    }

    public void setMinIntegerDigits(Integer minIntegerDigits) {
        this.minIntegerDigits = minIntegerDigits;
    }

    public void setNearestInteger(Integer nearestInteger) {
        this.nearestInteger = nearestInteger;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public void setTransient(boolean transientFlag) {
        this.transientFlag = transientFlag;
    }

    public void setType(String type) {
        this.type = type;
    }

}
