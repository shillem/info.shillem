package info.shillem.util.xsp.helper.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

public abstract class SelectItems implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<SelectItem> items;
    private Object defaultValue;

    public Object getDefaultValue() {
        return defaultValue;
    }

    public List<SelectItem> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }

        return items;
    }

    protected void setItems(List<SelectItem> items) {
        this.items = items;
    }

    public void sort(Comparator<SelectItem> comparator) {
        getItems().sort(comparator);
    }

    public void validate(FacesContext context, UIComponent component, Object value) {

    }

    public SelectItem valueOf(Object value) {
        return getItems().stream()
                .filter(i -> Objects.equals(value, i.getValue()))
                .findFirst()
                .orElse(null);
    }

}
