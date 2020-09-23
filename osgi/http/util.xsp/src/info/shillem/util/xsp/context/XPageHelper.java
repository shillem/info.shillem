package info.shillem.util.xsp.context;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.xsp.ajax.AjaxUtil;
import com.ibm.xsp.component.UIViewRootEx;
import com.ibm.xsp.component.UIViewRootEx2;
import com.ibm.xsp.context.FacesContextEx;
import com.ibm.xsp.util.FacesUtil;
import com.sun.faces.util.MessageFactory;

import info.shillem.util.CastUtil;
import info.shillem.util.xsp.component.ComponentUtil;

public enum XPageHelper {
    ;

    private static final String FLASH_MESSAGES_KEY = "messages";
    private static final String ON_SUCCESS_REFRESH_ID_PARAM = "onSuccessRefreshId";

    public static void addComponentFacesMessage(FacesContext facesContext, Severity severity,
            String componentId, String messageId, Object... params) {
        FacesMessage msg = MessageFactory.getMessage(facesContext, messageId, params);

        msg.setSeverity(severity);

        facesContext.addMessage(componentId, msg);
    }

    public static void addComponentMessage(FacesContext facesContext, Severity severity,
            UIComponent component, String summary, String detail) {
        FacesUtil.addMessage(facesContext, component, severity, summary, detail);
    }

    public static void addFacesMessage(
            FacesContext facesContext, Severity severity, String messageId, Object... params) {
        addComponentFacesMessage(facesContext, severity, null, messageId, params);
    }

    public static void addFlashMessage(
            FacesContext facesContext, Severity severity, String summary) {
        addFlashMessage(facesContext, severity, summary, "");
    }

    public static void addFlashMessage(
            FacesContext facesContext, Severity severity, String summary, String detail) {
        CastUtil.toAnyList((List<?>) XPageScope.FLASH
                .getValues(facesContext)
                .computeIfAbsent(FLASH_MESSAGES_KEY, (k) -> new ArrayList<FacesMessage>()))
                .add(new FacesMessage(severity, summary, detail));
    }

    public static void addMessage(FacesContext facesContext, Severity severity, String msg) {
        addMessage(facesContext, severity, msg, "");
    }

    public static void addMessage(
            FacesContext facesContext, Severity severity, String summary, String detail) {
        FacesUtil.addMessage(facesContext, severity, null, summary, detail);
    }

    public static void applyOnSuccessRefreshId(FacesContext facesContext) {
        if (!AjaxUtil.isAjaxPartialRefresh(facesContext)) {
            throw new UnsupportedOperationException();
        }

        String refreshId = getRequestParameterMap(facesContext).get(ON_SUCCESS_REFRESH_ID_PARAM);

        if (refreshId != null) {
            ((FacesContextEx) facesContext).setPartialRefreshId(refreshId);
        }
    }

    public static void applyOnSuccessRefreshId(FacesContext facesContext, ActionEvent event) {
        if (!AjaxUtil.isAjaxPartialRefresh(facesContext)) {
            throw new UnsupportedOperationException();
        }

        Optional
                .ofNullable(ComponentUtil.getHandlerParam(
                        event.getComponent(), ON_SUCCESS_REFRESH_ID_PARAM))
                .ifPresent((id) -> ((FacesContextEx) facesContext).setPartialRefreshId(id));
    }

    public static void bindBeforeRenderResponseMethod(FacesContext facesContext, String el) {
        getViewRootEx2(facesContext).setBeforeRenderResponse(
                facesContext.getApplication().createMethodBinding(
                        el, new Class<?>[] { PhaseEvent.class }));
    }

    public static List<Locale> getApplicationLocales(FacesContext facesContext) {
        Iterator<?> iterator = facesContext.getApplication().getSupportedLocales();

        return StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
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
                .getValues(facesContext, false)
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

    public static Map<String, String> getRequestParameterMap(FacesContext facesContext) {
        return CastUtil.toAnyMap(facesContext.getExternalContext().getRequestParameterMap());
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

    public static Object resolveVariable(FacesContext facesContext, String name) {
        return facesContext
                .getApplication()
                .getVariableResolver()
                .resolveVariable(facesContext, name);
    }

    public static Object resolveVariable(String name) {
        return resolveVariable(FacesContext.getCurrentInstance(), name);
    }

    public static void setRequestHeader(FacesContext facesContext, String key, String value) {
        Map<String, String> params = CastUtil.toAnyMap(facesContext
                .getExternalContext()
                .getRequestMap());

        params.put(key, value);
    }

    public static void setResponseErrorHeader(FacesContext facesContext, PhaseId phaseId) {
        HttpServletResponse response = (HttpServletResponse) facesContext
                .getExternalContext()
                .getResponse();

        response.setHeader("Application-Error", phaseId.toString());
    }

}
