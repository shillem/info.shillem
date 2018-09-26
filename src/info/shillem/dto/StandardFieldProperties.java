package info.shillem.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public enum StandardFieldProperties {
    ATTACHMENT_MAP(AttachmentMap.class),
    BIG_DECIMAL(BigDecimal.class),
    BIG_DECIMAL_LIST(BigDecimal[].class),
    BOOLEAN(Boolean.class),
    BOOLEAN_LIST(Boolean[].class),
    DATE(Date.class),
    DATE_LIST(Date[].class),
    DOUBLE(Double.class),
    DOUBLE_LIST(Double[].class),
    I18N_STRING(I18nString.class),
    I18N_STRING_LIST(I18nStringList.class),
    INTEGER(Integer.class),
    INTEGER_LIST(Integer[].class),
    STRING(String.class),
    STRING_LIST(String[].class);

    private final FieldProperties instance;

    private StandardFieldProperties(Class<? extends Serializable> cls) {
        this.instance = new FieldProperties(cls);
    }

    public FieldProperties get() {
        return instance;
    }

    public static FieldProperties getInstance(Class<? extends Serializable> type) {
        for (StandardFieldProperties standard : values()) {
            if (type == standard.get().getFullType()) {
                return standard.get();
            }
        }

        throw new IllegalArgumentException(type.getName());
    }

}
