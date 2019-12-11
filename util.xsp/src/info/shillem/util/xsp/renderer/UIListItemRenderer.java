package info.shillem.util.xsp.renderer;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.ibm.xsp.renderkit.html_extended.HtmlTagRenderer;

public class UIListItemRenderer extends HtmlTagRenderer {

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        if (!component.isRendered()) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();

        RenderUtil.startElement(writer, "li", component);
        writeId(writer, context, component);
        encodeHtmlAttributes(writer, component);
        RenderUtil.writeAttribute(writer, "role", component);
        RenderUtil.writeFacesAttrs(context, writer, component);
        RenderUtil.writeNewLine(writer);
    }

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        if (!component.isRendered()) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();

        RenderUtil.endElement(writer, "li");
        RenderUtil.writeNewLine(writer);
    }

}