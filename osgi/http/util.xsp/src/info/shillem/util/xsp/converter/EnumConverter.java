package info.shillem.util.xsp.converter;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import info.shillem.util.StringUtil;

public class EnumConverter implements Converter {

    public static final String CONVERTER_ID = "info.shillem.xsp.EnumConverter";

    private Object className;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            return StringUtil.enumFromString(getEnumClass(context, component), value);
        } catch (Exception e) {
            FacesMessage message = new FacesMessage(e.getMessage());
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(message);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return value.toString();
    }

    public Object getClassName() {
        return className;
    }

    protected Class<?> getEnumClass(
            FacesContext context,
            UIComponent component)
            throws ClassNotFoundException {
        if (className != null) {
            if (className instanceof Class) {
                return (Class<?>) className;
            }

            if (className instanceof String) {
                return Class.forName((String) className);
            }
        }

        return component.getValueBinding("value").getType(context);
    }

    public void restoreState(FacesContext context, Object state) {
        Object[] values = (Object[]) state;

        className = values[0];
    }

    public Object saveState(FacesContext context) {
        Object[] values = new Object[1];

        values[0] = className;

        return values;
    }

    public void setClassName(Object name) {
        this.className = name;
    }

}
