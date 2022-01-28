package info.shillem.util.xsp.helper.component;

import java.io.Serializable;

import javax.faces.model.SelectItem;

public class ImmovableSelectItem extends SelectItem implements Serializable {

    public ImmovableSelectItem(Object value) {
        super(value);
    }

    private static final long serialVersionUID = 1L;

}
