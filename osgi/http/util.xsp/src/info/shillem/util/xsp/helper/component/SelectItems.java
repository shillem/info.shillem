package info.shillem.util.xsp.helper.component;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

public interface SelectItems {

    void addItem(SelectItem item);
    
    void addValue(Object value);
    
    void addValue(Object value, String label);
    
    void addValues(Collection<? extends Object> values);
    
    Object getDefaultValue();
        
    List<SelectItem> getValues();

    void setDefaultValue(Object value);
    
    void setItems(List<SelectItem> selectItems);
    
    void sortItems(Comparator<SelectItem> comparator);
    
    void validate(FacesContext facesContext, UIComponent component, Object value);
    
}
