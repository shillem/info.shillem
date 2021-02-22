package info.shillem.util.xsp.renderer;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.ibm.xsp.renderkit.html_extended.HtmlTagRenderer;

import info.shillem.util.xsp.component.UITag;

public class UITagRenderer extends HtmlTagRenderer {

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        if (!component.isRendered()) {
            return;
        }
        
        UITag tag = (UITag) component;

        if (tag.isDisableOutputTag()) {
            return;
        }
        
        ResponseWriter writer = context.getResponseWriter();

        RenderUtil.startElement(writer, tag.getName(), component);
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
        
        UITag tag = (UITag) component;

        if (tag.isDisableOutputTag()) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();

        RenderUtil.endElement(writer, tag.getName());
        RenderUtil.writeNewLine(writer);
    }

}