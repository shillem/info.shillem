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
            FacesContext context,
            Severity severity,
            String messageId,
            Object... params) {
        addFacesMessage(context, null, severity, messageId, params);
    }

    public static void addFacesMessage(
            FacesContext context,
            String componentId,
            Severity severity,
            String messageId,
            Object... params) {
        FacesMessage msg = MessageFactory.getMessage(context, messageId, params);

        msg.setSeverity(severity);

        addMessage(context, componentId, msg);
    }

    public static void addFlashMessage(FacesContext context, FacesMessage message) {
        CastUtil.toAnyList((List<?>) XPageScope.FLASH
                .getValues(context)
                .computeIfAbsent(FLASH_MESSAGES_KEY, (k) -> new ArrayList<FacesMessage>()))
                .add(message);
    }

    public static void addMessage(FacesContext context, FacesMessage message) {
        addMessage(context, null, message);
    }

    public static void addMessage(
            FacesContext context,
            String componentId,
            FacesMessage message) {
        context.addMessage(componentId, message);
    }

    public static void addRequestListener(
            FacesContext context,
            FacesContextListener listener) {
        ((FacesContextEx) context).addRequestListener(listener);
    }

    public static void applySuccessRefreshId(FacesContext context) {
        applySuccessRefreshId(context,
                (String) XPageScope.REQUEST.getValue(
                        context,
                        RequestParameter.SUCCESS_REFRESH_ID.getName()));
    }

    public static void applySuccessRefreshId(FacesContext context, ActionEvent event) {
        applySuccessRefreshId(context,
                ComponentUtil.getHandlerParam(
                        event.getComponent(),
                        RequestParameter.SUCCESS_REFRESH_ID.getName()));
    }

    public static void applySuccessRefreshId(FacesContext context, String refreshId) {
        if (refreshId != null) {
            ((FacesContextEx) context).setPartialRefreshId(refreshId);
        }
    }

    public static void bindBeforeRenderResponseMethod(FacesContext context, String el) {
        getViewRootEx2(context).setBeforeRenderResponse(
                context.getApplication().createMethodBinding(
                        el, new Class<?>[] { PhaseEvent.class }));
    }

    public static List<Locale> getApplicationLocales(FacesContext context) {
        Iterator<?> iterator = context.getApplication().getSupportedLocales();

        return StreamUtil
                .stream(iterator)
                .map(Locale.class::cast)
                .collect(Collectors.toList());
    }

    public static String getBaseUrl(FacesContext context) {
        return getBaseUrl(getHttpServletRequest(context));
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

    public static List<FacesMessage> getFlashMessages(FacesContext context) {
        return Collections.unmodifiableList(CastUtil.toAnyList((List<?>) XPageScope.FLASH
                .getValues(context)
                .getOrDefault(FLASH_MESSAGES_KEY, Collections.emptyList())));
    }

    public static HttpServletRequest getHttpServletRequest(FacesContext context) {
        return (HttpServletRequest) context.getExternalContext().getRequest();
    }

    public static HttpServletResponse getHttpServletResponse(FacesContext context) {
        return (HttpServletResponse) context.getExternalContext().getResponse();
    }

    public static String getPageName(FacesContext context) {
        return ((UIViewRootEx) context.getViewRoot()).getPageName();
    }

    public static String getProperty(FacesContext context, String property) {
        return ((FacesContextEx) context).getProperty(property);
    }

    public static Locale getViewLocale(FacesContext context) {
        return context.getViewRoot().getLocale();
    }

    public static UIViewRootEx2 getViewRootEx2(FacesContext context) {
        return (UIViewRootEx2) context.getViewRoot();
    }

    public static boolean isPropertyEnabled(FacesContext context, String property) {
        return Boolean.valueOf(getProperty(context, property));
    }

    public static boolean isRenderingPhase(FacesContext context) {
        return getViewRootEx2(context).isRenderingPhase();
    }

    public static void postScript(FacesContext context, String script) {
        getViewRootEx2(context).postScript(script);
    }

    public static Object resolveVariable(FacesContext context, String name) {
        return context
                .getApplication()
                .getVariableResolver()
                .resolveVariable(context, name);
    }

    public static Object resolveVariable(String name) {
        return resolveVariable(FacesContext.getCurrentInstance(), name);
    }

    public static void setResponseStatusCode(FacesContext context, int statusCode) {
        HttpServletResponse response = (HttpServletResponse) context
                .getExternalContext()
                .getResponse();

        response.setStatus(statusCode);
    }

}
