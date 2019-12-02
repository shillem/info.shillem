package info.shillem.util.xsp.converter;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.el.ValueBinding;

public class EnumConverter implements Converter {
    
    public static final String CONVERTER_ID = "info.shillem.xsp.EnumConverter";

    private String classForName;

    @SuppressWarnings("unchecked")
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            return Enum.valueOf(getEnumClass(context, component), value);
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

    public String getClassForName() {
        return classForName;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Class<? extends Enum> getEnumClass(FacesContext context, UIComponent component)
            throws ClassNotFoundException {
        ValueBinding binding = component.getValueBinding("value");

        return (classForName != null && classForName.length() > 0)
                ? (Class<? extends Enum>) Class.forName(classForName)
                : (Class<? extends Enum>) binding.getType(context);
    }

    public void restoreState(FacesContext context, Object state) {
        Object[] values = (Object[]) state;

        classForName = ((String) values[0]);
    }

    public Object saveState(FacesContext context) {
        Object[] values = new Object[1];

        values[0] = classForName;

        return values;
    }

    public void setClassForName(String classForName) {
        this.classForName = classForName;
    }

}
