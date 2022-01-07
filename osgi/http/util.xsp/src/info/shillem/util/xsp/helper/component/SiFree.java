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

    public void addImmovableValue(Object value) {
        addItem(new ImmovableSelectItem(value));
    }

    @Override
    public void validate(
            FacesContext context,
            UIComponent component,
            Object value)
            throws ValidatorException {
        List<Object> values = new ArrayList<>();

        if (value instanceof List) {
            values.addAll((List<?>) value);
        } else {
            values.add(value);
        }

        List<SelectItem> safeItems = new ArrayList<>();

        values.forEach((v) -> {
            SelectItem si = valueOf(v);

            if (si != null) {
                safeItems.add(si);
                return;
            }

            si = new SelectItem(v);

            addItem(si);
            safeItems.add(si);
        });

        List<SelectItem> items = getItems();

        for (Iterator<SelectItem> iter = items.iterator(); iter.hasNext();) {
            SelectItem item = iter.next();

            if (!(item instanceof ImmovableSelectItem || safeItems.contains(item))) {
                iter.remove();
            }
        }
    }

}
