package info.shillem.util.xsp.helper.component;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

public class DynamicSelectItems extends SelectItems {

    private static final long serialVersionUID = 1L;

    public void setItem(SelectItem value) {
        List<SelectItem> items = getItems();
        
        items.clear();
        items.add(value);
    }

    public void setItemLabel(String value) {
        if (value == null) {
            return;
        }

        List<SelectItem> values = getItems();

        if (values.isEmpty()) {
            return;
        }

        values.get(0).setLabel(value);
    }
    
    public void setValue(Object value) {
        setItem(new SelectItem(value));
    }

    public void setValue(Object value, String label) {
        setItem(new SelectItem(value, label));
    }

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) {
        SelectItem item = getItems().stream()
                .filter((i) -> i.getValue().equals(value))
                .findFirst()
                .orElse(null);

        if (item != null) {
            return;
        }

        setItem(new SelectItem(value));
    }

}
