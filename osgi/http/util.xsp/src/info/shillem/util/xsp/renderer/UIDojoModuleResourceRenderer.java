package info.shillem.util.xsp.renderer;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.component.UIViewRootEx;
import com.ibm.xsp.renderkit.html_basic.DojoModuleResourceRenderer;
import com.ibm.xsp.resource.DojoModulePathLoader;
import com.ibm.xsp.resource.DojoModulePathResource;
import com.ibm.xsp.resource.DojoModuleResource;
import com.ibm.xsp.resource.Resource;

import info.shillem.util.StringUtil;

public class UIDojoModuleResourceRenderer extends DojoModuleResourceRenderer {

    private enum DojoLoader {
        ASYNC("require([", "])") {
            @Override
            String parseModuleName(String moduleName) {
                return moduleName.replace('.', '/');
            }
        },
        SYNC("dojo.require(", ")") {
            @Override
            String parseModuleName(String moduleName) {
                return moduleName;
            }
        };

        static char quote = '"';

        private String begin;
        private String end;

        DojoLoader(String begin, String end) {
            this.begin = begin;
            this.end = end;
        }

        abstract String parseModuleName(String moduleName);

        void print(StringBuilder builder, String moduleName) {
            builder
                    .append(begin)
                    .append(quote)
                    .append(parseModuleName(moduleName))
                    .append(quote)
                    .append(end);
        }

        void printIf(StringBuilder builder, String moduleName) {
            // TODO there's no sync version?
            builder
                    .append(SYNC.begin)
                    .append(quote)
                    .append(parseModuleName(moduleName))
                    .append(quote)
                    .append(SYNC.end);
        }

    }

    private static final Pattern ASYNC_PATTERN =
            Pattern.compile("async:\\s*true", Pattern.CASE_INSENSITIVE);

    @Override
    public void encodeResource(FacesContext context, UIComponent component, Resource resource)
            throws IOException {
        DojoModuleResource dojoResource = (DojoModuleResource) resource;

        String name = dojoResource.getName();
        String condition = dojoResource.getCondition();

        UIViewRootEx root = (UIViewRootEx) context.getViewRoot();
        String property = "resource_" + DojoModuleResource.class.getName() + name + condition;

        if (root.hasEncodeProperty(property)) {
            return;
        }

        root.putEncodeProperty(property, Boolean.TRUE);

        DojoModulePathResource dojoModulePathResource = DojoModulePathLoader
                .lookupExtensionModulePath(name);
        if ((dojoModulePathResource != null) && (dojoModulePathResource.isRendered())) {
            dojoModulePathResource.encodeObject(context, component);
        }

        ResponseWriter writer = context.getResponseWriter();

        RenderUtil.startElement(writer, "script", component);
        RenderUtil.writeAttribute(writer, "type", "text/javascript");

        StringBuilder builder = new StringBuilder();
        DojoLoader dojoLoader = getDojoLoader((ApplicationEx) context.getApplication());

        if (StringUtil.isEmpty(condition)) {
            dojoLoader.print(builder, name);
        } else {
            dojoLoader.printIf(builder, name);
        }

        RenderUtil.writeUnescapedText(writer, builder);
        RenderUtil.endElement(writer, "script");
        RenderUtil.writeNewLine(writer);
    }

    private DojoLoader getDojoLoader(ApplicationEx application) {
        String djConfig = application.getProperty("xsp.client.script.dojo.djConfig", null);

        if (djConfig != null && ASYNC_PATTERN.matcher(djConfig).find()) {
            return DojoLoader.ASYNC;
        }

        return DojoLoader.SYNC;
    }

}
