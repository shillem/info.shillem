package info.shillem.util.xsp.renderer;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.ibm.xsp.component.UIViewRootEx;
import com.ibm.xsp.renderkit.html_basic.HtmlRendererUtil;
import com.ibm.xsp.renderkit.html_basic.ScriptResourceRenderer;
import com.ibm.xsp.resource.Resource;
import com.ibm.xsp.resource.ScriptResource;

public class UIScriptResourceRenderer extends ScriptResourceRenderer {

    public enum Type {
        JAVASCRIPT, JAVASCRIPT_ASYNC, JAVASCRIPT_ASYNC_NOAMD;

        private static String DEFAULT_TYPE = "text/javascript";

        String getType() {
            return DEFAULT_TYPE;
        }

        static Type parse(String type) {
            if (type != null) {
                String lowerCaseType = type.toLowerCase();

                if (lowerCaseType.contains("async")) {
                    return lowerCaseType.contains("noamd")
                            ? JAVASCRIPT_ASYNC_NOAMD
                            : JAVASCRIPT_ASYNC;
                }
            }

            return JAVASCRIPT;
        }
    }

    @Override
    public void encodeResource(FacesContext facesContext, UIComponent component, Resource resource)
            throws IOException {
        ScriptResource scriptResource = (ScriptResource) resource;
        Type resType = Type.parse(scriptResource.getType());

        if (resType == Type.JAVASCRIPT) {
            super.encodeResource(facesContext, component, resource);
        } else {
            encodeTypedResource(facesContext, component, resource);
        }
    }

    public void encodeTypedResource(
            FacesContext facesContext, UIComponent component, Resource resource)
            throws IOException {
        ScriptResource scriptResource = (ScriptResource) resource;

        if (!scriptResource.isClientSide()) {
            return;
        }

        Map<String, String> resAttrs = scriptResource.getAttributes();

        if (scriptResource.getContents() == null) {
            String tail = "";

            if (!resAttrs.isEmpty()) {
                StringBuilder builder = new StringBuilder(124);

                for (Map.Entry<String, String> attr : resAttrs.entrySet()) {
                    builder.append(attr.getKey()).append('(').append(attr.getValue()).append(')');
                }

                tail = builder.toString();
            }

            String identifier = "resource_"
                    + ScriptResource.class.getName()
                    + scriptResource.getSrc()
                    + '|' + Type.JAVASCRIPT.getType() + '|'
                    + scriptResource.getCharset()
                    + tail;

            UIViewRootEx root = (UIViewRootEx) facesContext.getViewRoot();

            if (root.hasEncodeProperty(identifier)) {
                return;
            }

            root.putEncodeProperty(identifier, Boolean.TRUE);
        }

        ResponseWriter writer = facesContext.getResponseWriter();

        RenderUtil.startElement(writer, "script", component);
        RenderUtil.writeAttribute(writer, "type", Type.JAVASCRIPT.getType(), "type");
        RenderUtil.writeAttribute(writer, "charset", scriptResource.getCharset(), "charset");
        RenderUtil.writeURIAttribute(writer,
                "src", HtmlRendererUtil.getImageURL(facesContext, scriptResource.getSrc()), "src");

        for (Map.Entry<String, String> attr : resAttrs.entrySet()) {
            RenderUtil.writeAttribute(writer, attr.getKey(), attr.getValue(), attr.getKey());
        }

        RenderUtil.writeFacesAttrs(facesContext, writer, component);
        RenderUtil.writeUnescapedText(writer, scriptResource.getContents());
        RenderUtil.endElement(writer, "script");
        RenderUtil.writeNewLine(writer);
    }

}
