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
import com.ibm.xsp.util.JSUtil;
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
    public void encodeResource(FacesContext facesContext, UIComponent component, Resource resource)
            throws IOException {
        DojoModuleResource dojoResource = (DojoModuleResource) resource;

        String name = dojoResource.getName();
        String condition = dojoResource.getCondition();

        UIViewRootEx root = (UIViewRootEx) facesContext.getViewRoot();
        String property = "resource_" + DojoModuleResource.class.getName() + name + condition;

        if (root.hasEncodeProperty(property)) {
            return;
        }

        root.putEncodeProperty(property, Boolean.TRUE);

        DojoModulePathResource dojoModulePathResource = DojoModulePathLoader
                .lookupExtensionModulePath(name);
        if ((dojoModulePathResource != null) && (dojoModulePathResource.isRendered())) {
            dojoModulePathResource.encodeObject(facesContext, component);
        }

        ResponseWriter writer = facesContext.getResponseWriter();

        writer.startElement("script", component);
        writer.writeAttribute("type", "text/javascript", null);

        StringBuilder builder = new StringBuilder();
        DojoLoader dojoLoader = getDojoLoader((ApplicationEx) facesContext.getApplication());

        if (StringUtil.isEmpty(condition)) {
            dojoLoader.print(builder, name);
        } else {
            dojoLoader.printIf(builder, name);
        }

        writer.writeText(builder.toString(), null);
        writer.endElement("script");
        JSUtil.writeln(writer);
    }

    private DojoLoader getDojoLoader(ApplicationEx application) {
        String djConfig = application.getProperty("xsp.client.script.dojo.djConfig", null);

        if (djConfig != null && ASYNC_PATTERN.matcher(djConfig).find()) {
            return DojoLoader.ASYNC;
        }

        return DojoLoader.SYNC;
    }

}
