package info.shillem.util.xsp.context;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.xsp.component.UIViewRootEx;
import com.ibm.xsp.component.UIViewRootEx2;
import com.ibm.xsp.context.FacesContextEx;
import com.ibm.xsp.event.FacesContextListener;
import com.sun.faces.util.MessageFactory;

import info.shillem.util.CastUtil;
import info.shillem.util.StreamUtil;
import info.shillem.util.xsp.component.ComponentUtil;

public class XPageUtil {

    public enum RequestParameter {
        SUCCESS_REFRESH_ID("$$xspsuccessrefreshid");

        private final String name;

        RequestParameter(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private static final String FLASH_MESSAGES_KEY = "messages";

    private XPageUtil() {
        throw new UnsupportedOperationException();
    }

    public static void addFacesMessage(
            FacesContext facesContext,
            Severity severity,
            String messageId,
            Object... params) {
        addFacesMessage(facesContext, null, severity, messageId, params);
    }

    public static void addFacesMessage(
            FacesContext facesContext,
            String componentId,
            Severity severity,
            String messageId,
            Object... params) {
        FacesMessage msg = MessageFactory.getMessage(facesContext, messageId, params);

        msg.setSeverity(severity);

        addMessage(facesContext, componentId, msg);
    }

    public static void addFlashMessage(FacesContext facesContext, FacesMessage message) {
        CastUtil.toAnyList((List<?>) XPageScope.FLASH
                .getValues(facesContext)
                .computeIfAbsent(FLASH_MESSAGES_KEY, (k) -> new ArrayList<FacesMessage>()))
                .add(message);
    }

    public static void addMessage(FacesContext facesContext, FacesMessage message) {
        addMessage(facesContext, null, message);
    }

    public static void addMessage(
            FacesContext facesContext,
            String componentId,
            FacesMessage message) {
        facesContext.addMessage(componentId, message);
    }

    public static void addRequestListener(
            FacesContext facesContext,
            FacesContextListener listener) {
        ((FacesContextEx) facesContext).addRequestListener(listener);
    }

    public static void applySuccessRefreshId(FacesContext facesContext) {
        applySuccessRefreshId(facesContext,
                (String) XPageScope.REQUEST.getValue(
                        facesContext,
                        RequestParameter.SUCCESS_REFRESH_ID.getName()));
    }

    public static void applySuccessRefreshId(FacesContext facesContext, ActionEvent event) {
        applySuccessRefreshId(facesContext,
                ComponentUtil.getHandlerParam(
                        event.getComponent(),
                        RequestParameter.SUCCESS_REFRESH_ID.getName()));
    }

    public static void applySuccessRefreshId(FacesContext facesContext, String refreshId) {
        ((FacesContextEx) facesContext).setPartialRefreshId(refreshId);
    }

    public static void bindBeforeRenderResponseMethod(FacesContext facesContext, String el) {
        getViewRootEx2(facesContext).setBeforeRenderResponse(
                facesContext.getApplication().createMethodBinding(
                        el, new Class<?>[] { PhaseEvent.class }));
    }

    public static List<Locale> getApplicationLocales(FacesContext facesContext) {
        Iterator<?> iterator = facesContext.getApplication().getSupportedLocales();

        return StreamUtil
                .stream(iterator)
                .map(Locale.class::cast)
                .collect(Collectors.toList());
    }

    public static String getBaseUrl(FacesContext facesContext) {
        return getBaseUrl(getHttpServletRequest(facesContext));
    }

    public static String getBaseUrl(HttpServletRequest request) {
        try {
            if (("http".equals(request.getScheme()) && request.getServerPort() == 80)
                    || ("https".equals(request.getScheme()) && request.getServerPort() == 443)) {
                return new URL(request.getScheme(), request.getServerName(), "").toString();
            }

            return new URL(
                    request.getScheme(),
                    request.getServerName(),
                    request.getServerPort(), "")
                            .toString();
        } catch (MalformedURLException e) {
            throw new FacesException(e);
        }
    }

    public static List<FacesMessage> getFlashMessages(FacesContext facesContext) {
        return Collections.unmodifiableList(CastUtil.toAnyList((List<?>) XPageScope.FLASH
                .getValues(facesContext)
                .getOrDefault(FLASH_MESSAGES_KEY, Collections.emptyList())));
    }

    public static HttpServletRequest getHttpServletRequest(FacesContext facesContext) {
        return (HttpServletRequest) facesContext.getExternalContext().getRequest();
    }

    public static HttpServletResponse getHttpServletResponse(FacesContext facesContext) {
        return (HttpServletResponse) facesContext.getExternalContext().getResponse();
    }

    public static String getPageName(FacesContext facesContext) {
        return ((UIViewRootEx) facesContext.getViewRoot()).getPageName();
    }

    public static String getProperty(FacesContext facesContext, String property) {
        return ((FacesContextEx) facesContext).getProperty(property);
    }

    public static UIViewRootEx2 getViewRootEx2(FacesContext facesContext) {
        return (UIViewRootEx2) facesContext.getViewRoot();
    }

    public static boolean isPropertyEnabled(FacesContext facesContext, String property) {
        return Boolean.valueOf(getProperty(facesContext, property));
    }

    public static boolean isRenderingPhase(FacesContext facesContext) {
        return getViewRootEx2(facesContext).isRenderingPhase();
    }

    public static void postScript(FacesContext facesContext, String script) {
        getViewRootEx2(facesContext).postScript(script);
    }

    public static Object resolveVariable(FacesContext facesContext, String name) {
        return facesContext
                .getApplication()
                .getVariableResolver()
                .resolveVariable(facesContext, name);
    }

    public static Object resolveVariable(String name) {
        return resolveVariable(FacesContext.getCurrentInstance(), name);
    }

    public static void setResponseErrorHeader(FacesContext facesContext, PhaseId phaseId) {
        HttpServletResponse response = (HttpServletResponse) facesContext
                .getExternalContext()
                .getResponse();

        response.setHeader("X-XspError", phaseId.toString());
    }

}
