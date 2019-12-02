package info.shillem.util.xsp.helper.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

public class FreeSelectItems extends StaticSelectItems {

    private static final long serialVersionUID = 1L;

    @Override
    public void validate(FacesContext facesContext, UIComponent component, Object value)
            throws ValidatorException {
        List<SelectItem> selectItems = getValues();

        if (value == null) {
            selectItems.clear();

            return;
        }

        List<Object> values = new ArrayList<>();

        if (value instanceof List) {
            values.addAll((List<?>) value);
        } else {
            values.add(value);
        }

        for (Iterator<SelectItem> iter = selectItems.iterator(); iter.hasNext();) {
            SelectItem selectItem = iter.next();

            if (values.contains(selectItem.getValue())) {
                values.remove(selectItem.getValue());
            } else {
                iter.remove();
            }
        }
        
        values.forEach(this::addValue);
    }

}
