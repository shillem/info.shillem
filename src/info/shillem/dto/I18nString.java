package info.shillem.dto;

import java.io.Serializable;

public class I18nString implements I18nValue, Serializable {

    private static final long serialVersionUID = 1L;

    private String value;
    private String label;

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public void set(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value + "=" + label;
    }

}
