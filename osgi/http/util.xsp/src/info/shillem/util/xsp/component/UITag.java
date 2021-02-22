package info.shillem.util.xsp.component;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import com.ibm.xsp.complex.Attr;
import com.ibm.xsp.component.FacesAttrsObject;
import com.ibm.xsp.stylekit.ThemeControl;
import com.ibm.xsp.util.StateHolderUtil;

import info.shillem.util.StringUtil;

public class UITag extends UIComponentBase implements FacesAttrsObject, ThemeControl {

    public static final String COMPONENT_FAMILY = "info.shillem.xsp.Tag";
    public static final String COMPONENT_TYPE = "info.shillem.xsp.Tag";
    public static final String RENDERER_TYPE = "info.shillem.xsp.Tag";

    private List<Attr> attrs;
    private boolean disableOutputTag;
    private boolean disableOutputTag_set;
    private String name;

    public UITag() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public void addAttr(Attr attr) {
        if (attrs == null) {
            attrs = new ArrayList<>();
        }

        attrs.add(attr);
    }

    @Override
    public List<Attr> getAttrs() {
        return attrs;
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    public String getName() {
        return name != null ? name : "span";
    }

    @Override
    public String getStyleKitFamily() {
        return "Tag".concat(StringUtil.firstCharToUpperCase(getName()));
    }

    public boolean isDisableOutputTag() {
        if (disableOutputTag_set) {
            return disableOutputTag;
        }

        ValueBinding binding = getValueBinding("disableOutputTag");

        if (binding == null) {
            return disableOutputTag;
        }

        Object value = binding.getValue(getFacesContext());

        return value != null ? (Boolean) value : false;
    }

    public void restoreState(FacesContext facesContext, Object value) {
        Object[] values = (Object[]) value;

        super.restoreState(facesContext, values[0]);
        name = (String) values[1];
        disableOutputTag = ((Boolean) values[2]).booleanValue();
        attrs = StateHolderUtil.restoreList(facesContext, this, values[3]);
    }

    public Object saveState(FacesContext facesContext) {
        Object[] values = new Object[] {
                super.saveState(facesContext),
                name,
                Boolean.valueOf(disableOutputTag),
                StateHolderUtil.saveList(facesContext, attrs) };

        return values;
    }

    public void setAttrs(List<Attr> values) {
        attrs = values;
    }

    public void setDisableOutputTag(boolean value) {
        disableOutputTag = value;
    }

    public void setName(String name) {
        this.name = name;
    }

}
