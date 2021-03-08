package info.shillem.util.xsp.helper.component;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

public class SiStatic extends SelectItems {

    private static final long serialVersionUID = 1L;

    public void addItem(SelectItem item) {
        getItems().add(item);
    }

    public void addValue(Object value) {
        getItems().add(new SelectItem(value));
    }

    public void addValue(Object value, String label) {
        getItems().add(new SelectItem(value, label));
    }

    public void setItems(List<SelectItem> values) {
        super.setItems(new ArrayList<>(values));
    }

}
