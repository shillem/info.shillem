package info.shillem.util.xsp.renderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

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
import info.shillem.util.xsp.context.XPageHelper;

public class UIViewRootRenderer extends ViewRootRendererEx2 {

    private static final ScriptResource JQUERY_DEFINE_AMD;

    static {
        JQUERY_DEFINE_AMD = new ScriptResource();
        JQUERY_DEFINE_AMD.setContents("define.amd.jQuery = true;");
        JQUERY_DEFINE_AMD.setClientSide(true);
    }

    @Override
    protected void addCommonResources(FacesContext facesContext, UIViewRootEx root,
            List<Resource> resources) throws IOException {
        super.addCommonResources(facesContext, root, resources);

        if (XPageHelper.isPropertyEnabled(facesContext, "xsp.client.script.jQuery.defineAmd")) {
            resources.add(JQUERY_DEFINE_AMD);
        }
    }

    @Override
    protected void addPageResources(FacesContext facesContext, UIViewRootEx root,
            List<Resource> resources)
            throws IOException {
        List<Resource> pageResources = root.getResources();

        if (pageResources == null) {
            return;
        }

        pageResources.sort((res1, res2) -> {
            boolean res1Low = isScriptResourceLowPriority(facesContext, res1);
            boolean res2Low = isScriptResourceLowPriority(facesContext, res2);

            if (res1Low && !res2Low) return -1;
            if (!res1Low && res2Low) return 1;
            return 0;
        });

        resources.addAll(pageResources);
    }

    @Override
    protected List<Resource> buildResourceList(FacesContext facesContext, UIViewRootEx root,
            boolean commonResources,
            boolean customizerResources, boolean pageResources, boolean encodeResources)
            throws IOException {
        List<Resource> resources = new ArrayList<>();

        if (pageResources) {
            addPageResources(facesContext, root, resources);
        }

        if (commonResources) {
            addCommonResources(facesContext, root, resources);
        }

        if (customizerResources) {
            addCustomizerResources(facesContext, root, resources);
        }

        if (encodeResources) {
            addEncodeResources(facesContext, root, resources);
        }

        return resources;
    }

    @Override
    protected void encodeEndPage(FacesContext facesContext, ResponseWriter writer,
            UIViewRootEx root)
            throws IOException {
        XspHttpServletResponse response = null;

        if (facesContext instanceof FacesContextEx) {
            response = ((FacesContextExImpl) facesContext).getXspResponse();
        }

        if (response == null) {
            List<Resource> resources =
                    buildResourceList(facesContext, root, false, false, false, true);
            encodeResourcesList(facesContext, root, writer, resources);
        }

        encodeHtmlEnd(facesContext, root, writer);

        if (response != null) {
            response.switchToHeader(writer);
            encodeHtmlHead(facesContext, root, writer);
            response.switchToBody(writer);
        }
    }

    private void encodeHtmlEnd(FacesContext facesContext, UIViewRootEx root, ResponseWriter writer)
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
                    ResourceRenderer renderer = getScriptResourceRenderer(facesContext, res);

                    if (!(renderer instanceof UIScriptResourceRenderer)) {
                        return;
                    }

                    UIScriptResourceRenderer uisr = (UIScriptResourceRenderer) renderer;

                    switch (UIScriptResourceRenderer.Type.parse(res.getType())) {
                    case JAVASCRIPT_ASYNC:
                        uisr.encodeTypedResource(facesContext, root, res);
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
                () -> renderer.encodeTypedResource(facesContext, root, script)));

        RenderUtil.startElement(writer, "script", root);
        RenderUtil.writeUnescapedText(writer, "'function' == typeof define"
                + " && define._amd"
                + " && (define.amd = define._amd, delete define._amd);");
        RenderUtil.endElement(writer, "script");
        RenderUtil.writeNewLine(writer);

        encodeHtmlEnd(root, writer);
    }

    private ResourceRenderer getScriptResourceRenderer(
            FacesContext facesContext, ScriptResource scriptResource) {
        Renderer renderer = FacesUtil.getRenderer(
                facesContext, scriptResource.getFamily(), scriptResource.getRendererType());

        return (ResourceRenderer) FacesUtil.getRendererAs(renderer, ResourceRenderer.class);
    }

    private boolean isScriptResourceLowPriority(FacesContext facesContext, Resource resource) {
        if (!(resource instanceof ScriptResource)) {
            return false;
        }

        ScriptResource scriptResource = (ScriptResource) resource;
        Renderer scriptResourceRenderer = getScriptResourceRenderer(facesContext, scriptResource);

        if (!(scriptResourceRenderer instanceof UIScriptResourceRenderer)) {
            return false;
        }

        return UIScriptResourceRenderer.Type
                .parse(scriptResource.getType()) == UIScriptResourceRenderer.Type.JAVASCRIPT;
    }

}
