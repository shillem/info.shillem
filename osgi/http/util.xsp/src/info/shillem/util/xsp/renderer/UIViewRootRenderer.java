package info.shillem.util.xsp.renderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import com.ibm.xsp.complex.Attr;
import com.ibm.xsp.component.UIViewRootEx;
import com.ibm.xsp.context.FacesContextEx;
import com.ibm.xsp.context.FacesContextExImpl;
import com.ibm.xsp.render.ResourceRenderer;
import com.ibm.xsp.renderkit.html_basic.ViewRootRendererEx2;
import com.ibm.xsp.resource.Resource;
import com.ibm.xsp.resource.ScriptResource;
import com.ibm.xsp.util.FacesUtil;
import com.ibm.xsp.webapp.XspHttpServletResponse;

import info.shillem.util.Unthrow;
import info.shillem.util.xsp.XspLoader;
import info.shillem.util.xsp.context.XPageUtil;

public class UIViewRootRenderer extends ViewRootRendererEx2 {

    private static final ScriptResource SCRIPT_XSP;
    private static final ScriptResource SCRIPT_JQUERY_DEFINE_AMD;

    static {
        SCRIPT_JQUERY_DEFINE_AMD = new ScriptResource();
        SCRIPT_JQUERY_DEFINE_AMD.setContents("define.amd.jQuery = true;");
        SCRIPT_JQUERY_DEFINE_AMD.setClientSide(true);

        SCRIPT_XSP = new ScriptResource(
                "/.ibmxspres/.extlib/".concat(XspLoader.NAMESPACE).concat("/js/xsp.js"), true);
    }

    @Override
    protected void addCommonResources(
            FacesContext context,
            UIViewRootEx root,
            List<Resource> resources)
            throws IOException {
        super.addCommonResources(context, root, resources);

        if ("none".equalsIgnoreCase(
                XPageUtil.getProperty(
                        context, "xsp.client.script.libraries"))
                && XPageUtil.isPropertyEnabled(context,
                        "xsp.client.script.".concat(XspLoader.NAMESPACE))) {
            resources.add(SCRIPT_XSP);
        }

        if (XPageUtil.isPropertyEnabled(context, "xsp.client.script.jQuery.defineAmd")) {
            resources.add(SCRIPT_JQUERY_DEFINE_AMD);
        }
    }

    @Override
    protected void addPageResources(
            FacesContext context,
            UIViewRootEx root,
            List<Resource> resources)
            throws IOException {
        List<Resource> pageResources = root.getResources();

        if (pageResources == null) {
            return;
        }

        pageResources.sort((res1, res2) -> {
            boolean res1Low = isScriptResourceLowPriority(context, res1);
            boolean res2Low = isScriptResourceLowPriority(context, res2);

            if (res1Low && !res2Low) return -1;
            if (!res1Low && res2Low) return 1;
            return 0;
        });

        resources.addAll(pageResources);
    }

    @Override
    protected List<Resource> buildResourceList(
            FacesContext context,
            UIViewRootEx root,
            boolean commonResources,
            boolean customizerResources,
            boolean pageResources,
            boolean encodeResources)
            throws IOException {
        List<Resource> resources = new ArrayList<>();

        if (pageResources) {
            addPageResources(context, root, resources);
        }

        if (commonResources) {
            addCommonResources(context, root, resources);
        }

        if (customizerResources) {
            addCustomizerResources(context, root, resources);
        }

        if (encodeResources) {
            addEncodeResources(context, root, resources);
        }

        return resources;
    }

    @Override
    protected void encodeEndPage(
            FacesContext context,
            ResponseWriter writer,
            UIViewRootEx root)
            throws IOException {
        XspHttpServletResponse response = null;

        if (context instanceof FacesContextEx) {
            response = ((FacesContextExImpl) context).getXspResponse();
        }

        if (response == null) {
            List<Resource> resources =
                    buildResourceList(context, root, false, false, false, true);
            encodeResourcesList(context, root, writer, resources);
        }

        encodeHtmlEnd(context, root, writer);

        if (response != null) {
            response.switchToHeader(writer);
            encodeHtmlHead(context, root, writer);
            response.switchToBody(writer);
        }
    }

    @Override
    protected void encodeHtmlAttributes(
            FacesContext context,
            UIViewRootEx root,
            ResponseWriter writer,
            boolean xhtml) throws IOException {
        RenderUtil.writeAttribute(writer, "dir", root.getDir());
        RenderUtil.writeAttribute(writer, "lang", root.getLocale().getLanguage());

        if (xhtml) {
            RenderUtil.writeAttribute(writer, "xml:lang", root.getLocale().getLanguage());
        }

        if (root.getAttrs() != null) {
            for (Attr attr : root.getAttrs()) {
                if (!attr.getName().startsWith("data")) {
                    RenderUtil.writeAttribute(writer, attr.getName(), attr.getValue());
                }
            }
        }
    }

    @Override
    protected void encodeHtmlBodyStart(
            FacesContext context,
            UIViewRootEx root,
            ResponseWriter writer) throws IOException {
        RenderUtil.startElement(writer, "body", (UIComponent) root);

        RenderUtil.writeAttribute(writer, "class", root.getStyleClass());
        RenderUtil.writeAttribute(writer, "style", root.getStyle());

        if (root.getAttrs() != null) {
            for (Attr attr : root.getAttrs()) {
                if (attr.getName().startsWith("data")) {
                    RenderUtil.writeAttribute(writer, attr.getName(), attr.getValue());
                }
            }
        }

        RenderUtil.writeNewLine(writer);
    }

    private void encodeHtmlEnd(FacesContext context, UIViewRootEx root, ResponseWriter writer)
            throws IOException {
        List<Resource> pageResources = root.getResources();

        if (pageResources == null) {
            encodeHtmlEnd(root, writer);

            return;
        }

        Map<ScriptResource, UIScriptResourceRenderer> resNoAmd = new LinkedHashMap<>();

        pageResources.stream()
                .filter(Resource::isRendered)
                .filter((res) -> res instanceof ScriptResource)
                .map(ScriptResource.class::cast)
                .forEach((res) -> Unthrow.on(() -> {
                    ResourceRenderer renderer = getScriptResourceRenderer(context, res);

                    if (!(renderer instanceof UIScriptResourceRenderer)) {
                        return;
                    }

                    UIScriptResourceRenderer uisr = (UIScriptResourceRenderer) renderer;

                    switch (UIScriptResourceRenderer.Type.parse(res.getType())) {
                    case JAVASCRIPT_ASYNC:
                        uisr.encodeTypedResource(context, root, res);
                        break;
                    case JAVASCRIPT_ASYNC_NOAMD:
                        resNoAmd.put(res, uisr);
                        break;
                    default:
                        // Do nothing
                    }
                }));

        if (resNoAmd.isEmpty()) {
            encodeHtmlEnd(root, writer);

            return;
        }

        RenderUtil.startElement(writer, "script", root);
        RenderUtil.writeUnescapedText(writer, "'function' == typeof define"
                + " && define.amd"
                + " && 'dojotoolkit.org' == define.amd.vendor"
                + " && (define._amd = define.amd, delete define.amd);");
        RenderUtil.endElement(writer, "script");
        RenderUtil.writeNewLine(writer);

        resNoAmd.forEach((script, renderer) -> Unthrow.on(
                () -> renderer.encodeTypedResource(context, root, script)));

        RenderUtil.startElement(writer, "script", root);
        RenderUtil.writeUnescapedText(writer, "'function' == typeof define"
                + " && define._amd"
                + " && (define.amd = define._amd, delete define._amd);");
        RenderUtil.endElement(writer, "script");
        RenderUtil.writeNewLine(writer);

        encodeHtmlEnd(root, writer);
    }

    private ResourceRenderer getScriptResourceRenderer(
            FacesContext context,
            ScriptResource scriptResource) {
        Renderer renderer = FacesUtil.getRenderer(
                context, scriptResource.getFamily(), scriptResource.getRendererType());

        return (ResourceRenderer) FacesUtil.getRendererAs(renderer, ResourceRenderer.class);
    }

    private boolean isScriptResourceLowPriority(FacesContext context, Resource resource) {
        if (!(resource instanceof ScriptResource)) {
            return false;
        }

        ScriptResource scriptResource = (ScriptResource) resource;
        Renderer scriptResourceRenderer = getScriptResourceRenderer(context, scriptResource);

        if (!(scriptResourceRenderer instanceof UIScriptResourceRenderer)) {
            return false;
        }

        return UIScriptResourceRenderer.Type
                .parse(scriptResource.getType()) == UIScriptResourceRenderer.Type.JAVASCRIPT;
    }

}
