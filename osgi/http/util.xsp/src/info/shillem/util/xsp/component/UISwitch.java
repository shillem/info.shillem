package info.shillem.util.xsp.component;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.component.ContextCallback;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import com.ibm.xsp.stylekit.ThemeControl;
import com.ibm.xsp.util.FacesUtil;

public class UISwitch extends UIComponentBase implements NamingContainer, ThemeControl {

    public static final String COMPONENT_FAMILY = "info.shillem.xsp.Switch";
    public static final String COMPONENT_TYPE = "info.shillem.xsp.Switch";
    public static final String RENDERER_TYPE = "info.shillem.xsp.Switch";

    private Object defaultValue;
    private Object value;

    public UISwitch() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {

    }

    public void encodeChildren(FacesContext facesContext) throws IOException {
        if (!isRendered()) {
            return;
        }

        UIComponent component = selectFacet();

        if (component == null) {
            return;
        }

        FacesUtil.renderComponent(facesContext, component);
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {

    }

    public Object getDefaultValue() {
        if (defaultValue != null) {
            return defaultValue;
        }

        ValueBinding binding = getValueBinding("defaultValue");

        return binding != null ? binding.getValue(getFacesContext()) : null;
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public String getStyleKitFamily() {
        return "Container.Switch";
    }

    public Object getValue() {
        if (value != null) {
            return value;
        }

        ValueBinding binding = getValueBinding("value");

        return binding != null ? binding.getValue(getFacesContext()) : null;
    }

    public boolean invokeOnComponent(
            FacesContext facesContext,
            String clientId,
            ContextCallback callback) throws FacesException {
        if (clientId.equals(getClientId(facesContext))) {
            try {
                callback.invokeContextCallback(facesContext, this);

                return true;
            } catch (Exception e) {
                throw new FacesException(e);
            }
        }

        UIComponent component = selectFacet();

        if (component == null) {
            return false;
        }

        return component.invokeOnComponent(facesContext, clientId, callback);
    }

    public void processDecodes(FacesContext facesContext) {
        if (!isRendered()) {
            return;
        }

        UIComponent component = selectFacet();

        if (component == null) {
            return;
        }

        component.processDecodes(facesContext);

        decode(facesContext);
    }

    public void processUpdates(FacesContext facesContext) {
        if (!isRendered()) {
            return;
        }

        UIComponent component = selectFacet();

        if (component == null) {
            return;
        }

        component.processUpdates(facesContext);
    }

    public void processValidators(FacesContext facesContext) {
        if (!isRendered()) {
            return;
        }

        UIComponent component = selectFacet();

        if (component == null) {
            return;
        }

        component.processValidators(facesContext);
    }

    public void restoreState(FacesContext facesContext, Object state) {
        Object[] values = (Object[]) state;

        super.restoreState(facesContext, values[0]);

        defaultValue = values[1];
        value = values[2];
    }

    public Object saveState(FacesContext facesContext) {
        return new Object[] {
                super.saveState(facesContext),
                defaultValue,
                value };
    }

    protected UIComponent selectFacet() {
        UIComponent component = selectFacet(getValue());

        if (component == null) {
            component = selectFacet(getDefaultValue());
        }

        return component;
    }

    protected UIComponent selectFacet(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String && ((String) value).isEmpty()) {
            return null;
        }

        return getFacet(value.toString());
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean visitTree(VisitContext visitContext, VisitCallback visitCallback) {
        if (!isVisitable(visitContext)) {
            return false;
        }

        VisitResult visitResult = visitContext.invokeVisitCallback(this, visitCallback);

        if (visitResult == VisitResult.COMPLETE) {
            return true;
        }

        if (visitResult == VisitResult.ACCEPT
                && !visitContext.getSubtreeIdsToVisit(this).isEmpty()) {
            UIComponent component = selectFacet();

            if (component != null && component.visitTree(visitContext, visitCallback)) {
                return true;
            }
        }

        return false;
    }

}
