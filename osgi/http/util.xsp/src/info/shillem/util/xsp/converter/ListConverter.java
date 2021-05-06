package info.shillem.util.xsp.converter;

import java.io.Serializable;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class ListConverter implements Converter, Serializable, StateHolder {

    private static final long serialVersionUID = 1L;

    private String delimiter;
    private boolean trans;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return Stream.of(value.split(Pattern.quote(delimiter))).collect(Collectors.toList());
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<String> values = (Collection<String>) value;

            return values.stream().collect(Collectors.joining(delimiter));
        }

        return String.valueOf(value);
    }

    public String getDelimiter() {
        return delimiter;
    }

    @Override
    public boolean isTransient() {
        return trans;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        Object[] values = (Object[]) state;

        delimiter = (String) values[0];
    }

    @Override
    public Object saveState(FacesContext context) {
        return new Object[] { delimiter };
    }

    public void setDelimiter(String value) {
        delimiter = value;
    }

    @Override
    public void setTransient(boolean value) {
        trans = value;
    }

}
