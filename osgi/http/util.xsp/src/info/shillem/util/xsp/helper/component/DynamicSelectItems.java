package info.shillem.util.xsp.helper.component;

import java.util.List;
import java.util.Objects;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

public class DynamicSelectItems extends StaticSelectItems {

    private static final long serialVersionUID = 1L;

    private SelectItem dynamicValue;

    @Override
    public void setItems(List<SelectItem> selectItems) {
        super.setItems(selectItems);

        if (dynamicValue != null && isMemberOfValues(dynamicValue.getValue())) {
            removeDynamicValue();
        }
    }

    public SelectItem getDynamicValue() {
        return dynamicValue;
    }

    public void setDynamicValue(Object value) {
        applyDynamicValue(new SelectItem(value));
    }

    public void setDynamicValue(Object value, String label) {
        applyDynamicValue(new SelectItem(value, label));
    }

    protected void applyDynamicValue(SelectItem selectItem) {
        if (isDynamicItemValue(selectItem.getValue())) {
            return;
        }

        dynamicValue = isMemberOfValues(selectItem.getValue()) ? null : selectItem;
    }

    public void removeDynamicValue() {
        dynamicValue = null;
    }

    protected final boolean isMemberOfValues(Object value) {
        return getValues().stream()
                .map(SelectItem::getValue)
                .filter(val -> Objects.equals(val, value))
                .findFirst()
                .isPresent();
    }

    protected final boolean isDynamicItemValue(Object value) {
        return dynamicValue != null && dynamicValue.getValue().equals(value);
    }

    @Override
    public void validate(FacesContext facesContext, UIComponent component, Object value) {
        setDynamicValue(value);
    }

}
