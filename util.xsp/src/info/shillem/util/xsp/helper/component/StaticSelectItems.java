package info.shillem.util.xsp.helper.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

public class StaticSelectItems implements SelectItems, Serializable {

    private static final long serialVersionUID = 1L;

    private List<SelectItem> values;
    private Object defaultValue;

    @Override
    public void addItem(SelectItem item) {
        getValues().add(item);
    }

    @Override
    public void addValue(Object value) {
        getValues().add(new SelectItem(value));
    }

    @Override
    public void addValue(Object value, String label) {
        getValues().add(new SelectItem(value, label));
    }

    @Override
    public void addValue(SelectItem item) {
        getValues().add(new SelectItem(item.getValue(), item.getLabel()));
    }

    @Override
    public void addValues(Collection<? extends Object> values) {
        values.forEach(this::addValue);
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public List<SelectItem> getValues() {
        if (values == null) {
            values = new ArrayList<>();
        }

        return values;
    }

    @Override
    public void setDefaultValue(Object value) {
        defaultValue = value;
    }

    @Override
    public void setItems(List<SelectItem> selectItems) {
        values = selectItems;
    }

    @Override
    public void sortValues(Comparator<SelectItem> comparator) {
        getValues().sort(comparator);
    }

    @Override
    public void validate(FacesContext facesContext, UIComponent component, Object value)
            throws ValidatorException {
        // Do nothing
    }

}
