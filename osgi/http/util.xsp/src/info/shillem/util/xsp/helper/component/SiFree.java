package info.shillem.util.xsp.helper.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

public class SiFree extends SiStatic {

    private static final long serialVersionUID = 1L;

    @Override
    public void validate(
            FacesContext context,
            UIComponent component,
            Object value)
            throws ValidatorException {
        List<SelectItem> selectItems = getItems();

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
