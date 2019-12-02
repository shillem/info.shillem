package info.shillem.util.xsp.component;

import com.ibm.xsp.component.UIComponentTag;

public class UIListItem extends UIComponentTag {

    public static final String COMPONENT_TYPE = "info.shillem.xsp.Li";
    public static final String RENDERER_TYPE = "info.shillem.xsp.Li";

    public UIListItem() {
        setRendererType(RENDERER_TYPE);
    }

    @Override
    public String getStyleKitFamily() {
        return "HtmlLi";
    }

}
