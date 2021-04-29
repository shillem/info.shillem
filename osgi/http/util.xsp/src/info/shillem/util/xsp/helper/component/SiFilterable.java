package info.shillem.util.xsp.helper.component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.model.SelectItem;

public class SiFilterable extends SiStatic {

    private static final long serialVersionUID = 1L;

    private List<SelectItem> filtered;

    public void filter(Collection<? extends Object> values) {
        Object defaultValue = getDefaultValue();

        filtered = super.getItems().stream()
                .filter(s -> s.getValue().equals(defaultValue) || values.contains(s.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SelectItem> getItems() {
        return filtered != null ? filtered : super.getItems();
    }

}
