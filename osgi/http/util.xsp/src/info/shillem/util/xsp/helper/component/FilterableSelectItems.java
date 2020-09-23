package info.shillem.util.xsp.helper.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.model.SelectItem;

public class FilterableSelectItems extends StaticSelectItems {

    private static final long serialVersionUID = 1L;

    private List<SelectItem> filteredValues;

    public FilterableSelectItems() {

    }

    public FilterableSelectItems(Object value) {
        setDefaultValue(value);
    }

    public void filter(Collection<? extends Object> values) {
        if (filteredValues == null) {
            filteredValues = new ArrayList<>();
        } else {
            filteredValues.clear();
        }

        Object defaultValue = getDefaultValue();

        super.getValues().stream()
                .filter(sel -> (defaultValue != null && defaultValue.equals(sel.getValue()))
                        || values.contains(sel.getValue()))
                .forEach(filteredValues::add);
    }

    public List<SelectItem> getFilteredValues() {
        return filteredValues == null ? getValues() : filteredValues;
    }

}
