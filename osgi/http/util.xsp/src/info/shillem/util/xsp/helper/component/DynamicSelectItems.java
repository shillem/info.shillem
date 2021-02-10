package info.shillem.util.xsp.helper.component;

import java.util.Collection;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

public class DynamicSelectItems extends StaticSelectItems {

    private static final long serialVersionUID = 1L;

    @Override
    public void addItem(SelectItem item) {
        throw new UnsupportedOperationException("Use setItems(...)");
    }

    @Override
    public void addValue(Object value) {
        throw new UnsupportedOperationException("Use setItems(...)");
    }

    @Override
    public void addValue(Object value, String label) {
        throw new UnsupportedOperationException("Use setItems(...)");
    }

    @Override
    public void addValues(Collection<? extends Object> values) {
        throw new UnsupportedOperationException("Use setItems(...)");
    }

    @Override
    public void validate(FacesContext facesContext, UIComponent component, Object value) {
        SelectItem item = getValues().stream()
                .filter((i) -> i.getValue().equals(value))
                .findFirst()
                .orElse(null);
        
        if (item != null) {
            return;
        }
        
        getValues().clear();
        
        super.addValue(value);
    }

}
