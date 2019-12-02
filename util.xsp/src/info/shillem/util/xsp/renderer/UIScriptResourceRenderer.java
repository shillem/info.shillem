package info.shillem.util.xsp.renderer;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.ibm.xsp.component.UIViewRootEx;
import com.ibm.xsp.renderkit.html_basic.AttrsUtil;
import com.ibm.xsp.renderkit.html_basic.HtmlRendererUtil;
import com.ibm.xsp.renderkit.html_basic.ScriptResourceRenderer;
import com.ibm.xsp.resource.Resource;
import com.ibm.xsp.resource.ScriptResource;
import com.ibm.xsp.util.JSUtil;

import info.shillem.util.StringUtil;

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
                    if (lowerCaseType.contains("noamd")) {
                        return JAVASCRIPT_ASYNC_NOAMD;
                    } else {
                        return JAVASCRIPT_ASYNC;
                    }
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

        String resCharset = scriptResource.getCharset();
        String resSrc = scriptResource.getSrc();
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
                    + resSrc
                    + '|' + Type.JAVASCRIPT.getType() + '|'
                    + resCharset
                    + tail;

            UIViewRootEx root = (UIViewRootEx) facesContext.getViewRoot();

            if (root.hasEncodeProperty(identifier)) {
                return;
            }

            root.putEncodeProperty(identifier, Boolean.TRUE);
        }

        ResponseWriter writer = facesContext.getResponseWriter();

        writer.startElement("script", component);
        writer.writeAttribute("type", Type.JAVASCRIPT.getType(), "type");

        if (!StringUtil.isEmpty(resCharset)) {
            writer.writeAttribute("charset", resCharset, "charset");
        }

        if (!StringUtil.isEmpty(resSrc)) {
            writer.writeURIAttribute("src", HtmlRendererUtil.getImageURL(facesContext, resSrc),
                    "src");
        }

        for (Map.Entry<String, String> attr : resAttrs.entrySet()) {
            writer.writeAttribute(attr.getKey(), attr.getValue(), null);
        }

        AttrsUtil.encodeAttrs(facesContext, writer, scriptResource);

        String resContents = scriptResource.getContents();

        if (StringUtil.isEmpty(resSrc) && resContents != null) {
            writer.writeText(resContents, null);
        }

        writer.endElement("script");
        JSUtil.writeln(writer);
    }

}
