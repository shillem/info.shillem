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
import com.ibm.xsp.util.JSUtil;
import com.ibm.xsp.webapp.XspHttpServletResponse;

public class UIViewRootRenderer extends ViewRootRendererEx2 {

    protected static ScriptResource JQUERY_DEFINE_AMD;

    @Override
    protected void addDojoResources(FacesContext facesContext, UIViewRootEx root,
            List<Resource> resources) {
        super.addDojoResources(facesContext, root, resources);

        if (!isXspProperty(facesContext, "xsp.client.script.jQuery.defineAmd")) {
            return;
        }

        if (JQUERY_DEFINE_AMD == null) {
            JQUERY_DEFINE_AMD = new ScriptResource();
            JQUERY_DEFINE_AMD.setContents("define.amd.jQuery = true;");
            JQUERY_DEFINE_AMD.setClientSide(true);
        }

        resources.add(JQUERY_DEFINE_AMD);
    }

    @Override
    protected void addPageResources(FacesContext facesContext, UIViewRootEx root,
            List<Resource> resources)
            throws IOException {
        List<Resource> unprioritized = root.getResources();
        List<Resource> lowPriorityScript = null;

        if (unprioritized != null) {
            for (Resource resource : unprioritized) {
                if (resource instanceof ScriptResource) {
                    ScriptResource scriptResource = (ScriptResource) resource;
                    ResourceRenderer resourceRenderer =
                            getScriptResourceRenderer(facesContext, scriptResource);

                    if (resourceRenderer instanceof UIScriptResourceRenderer) {
                        if (UIScriptResourceRenderer.Type.parse(scriptResource
                                .getType()) == UIScriptResourceRenderer.Type.JAVASCRIPT) {
                            if (lowPriorityScript == null) {
                                lowPriorityScript = new ArrayList<>();
                            }

                            lowPriorityScript.add(resource);
                        }

                        continue;
                    }
                }

                resources.add(resource);
            }

            if (lowPriorityScript != null) {
                resources.addAll(lowPriorityScript);
            }
        }
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

        if (pageResources != null) {
            Map<ScriptResource, UIScriptResourceRenderer> resNoAmd = new LinkedHashMap<>(
                    pageResources.size());

            for (Resource resource : pageResources) {
                if (resource instanceof ScriptResource) {
                    ScriptResource scriptResource = (ScriptResource) resource;

                    if (scriptResource.isRendered()) {
                        ResourceRenderer resourceRenderer =
                                getScriptResourceRenderer(facesContext, scriptResource);

                        if (resourceRenderer instanceof UIScriptResourceRenderer) {
                            UIScriptResourceRenderer uisr =
                                    ((UIScriptResourceRenderer) resourceRenderer);

                            switch (UIScriptResourceRenderer.Type.parse(scriptResource.getType())) {
                            case JAVASCRIPT_ASYNC:
                                uisr.encodeTypedResource(facesContext, root, resource);
                                break;
                            case JAVASCRIPT_ASYNC_NOAMD:
                                resNoAmd.put(scriptResource, uisr);
                                break;
                            default:
                                // Do nothing
                            }
                        }
                    }
                }
            }

            if (!resNoAmd.isEmpty()) {
                writer.startElement("script", root);
                writer.writeText(
                        "'function' == typeof define && define.amd && 'dojotoolkit.org' == define.amd.vendor && (define._amd = define.amd, delete define.amd);",
                        null);
                writer.endElement("script");
                JSUtil.writeln(writer);

                for (Map.Entry<ScriptResource, UIScriptResourceRenderer> entry : resNoAmd
                        .entrySet()) {
                    entry.getValue().encodeTypedResource(facesContext, root, entry.getKey());
                }

                writer.startElement("script", root);
                writer.writeText(
                        "'function' == typeof define && define._amd && (define.amd = define._amd, delete define._amd);",
                        null);
                writer.endElement("script");
                JSUtil.writeln(writer);
            }
        }

        encodeHtmlEnd(root, writer);
    }

    private ResourceRenderer getScriptResourceRenderer(
            FacesContext facesContext, ScriptResource scriptResource) {
        Renderer renderer = FacesUtil.getRenderer(
                facesContext, scriptResource.getFamily(), scriptResource.getRendererType());

        return (ResourceRenderer) FacesUtil.getRendererAs(renderer, ResourceRenderer.class);
    }

    private boolean isXspProperty(FacesContext facesContext, String property) {
        return Boolean.valueOf(((FacesContextEx) facesContext).getProperty(property));
    }

}
