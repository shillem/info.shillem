package info.shillem.util.xsp.renderer;

import java.io.IOException;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.ibm.xsp.complex.Attr;
import com.ibm.xsp.component.FacesAttrsObject;
import com.ibm.xsp.renderkit.html_basic.AttrsUtil;
import com.ibm.xsp.renderkit.html_extended.HtmlTagRenderer;
import com.ibm.xsp.util.JSUtil;

public class UIListItemRenderer extends HtmlTagRenderer {

    private static final String TAG_NAME = "li";

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        if (!component.isRendered()) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();

        writer.startElement(TAG_NAME, component);
        writeId(writer, context, component);
        encodeHtmlAttributes(writer, component);
        writeAttribute(writer, component, "role");

        if (component instanceof FacesAttrsObject) {
            FacesAttrsObject fao = (FacesAttrsObject) component;
            List<Attr> attrs = fao.getAttrs();

            if (attrs != null) {
                for (Attr a : attrs) {
                    AttrsUtil.encodeAttr(context, writer, a);
                }
            }
        }

        JSUtil.writeln(writer);
    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        if (!component.isRendered()) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();

        writer.endElement(TAG_NAME);
        
        JSUtil.writeln(writer);
    }
    
}