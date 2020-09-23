package info.shillem.util.xsp.component;

import java.io.IOException;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;

import com.ibm.xsp.util.JSUtil;

public class UINewLine extends UIComponentBase {

    public UINewLine() {
        setRendererType(null);
    }

    @Override
    public String getFamily() {
        return "info.shillem.xsp.Space";
    }
    
    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        // Do nothing
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }
        
        JSUtil.writeln(context.getResponseWriter());
    }

}
