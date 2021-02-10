package info.shillem.util.xsp.helper.component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.model.SelectItem;

public class FilterableSelectItems extends StaticSelectItems {

    private static final long serialVersionUID = 1L;

    private List<SelectItem> filtered;

    public FilterableSelectItems(Object value) {
        setDefaultValue(value);
    }

    public void filter(Collection<? extends Object> values) {
        Object defaultValue = getDefaultValue();

        filtered = super.getValues().stream()
                .filter(s -> s.getValue().equals(defaultValue) || values.contains(s.getValue()))
                .collect(Collectors.toList());
    }

    public List<SelectItem> getValues() {
        return filtered != null ? filtered : super.getValues();
    }

}
