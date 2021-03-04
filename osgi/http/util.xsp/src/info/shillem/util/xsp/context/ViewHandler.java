package info.shillem.util.xsp.context;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.faces.FacesException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;

import com.ibm.xsp.application.ViewHandlerExImpl;
import com.ibm.xsp.component.UIViewRootEx;

import info.shillem.util.CastUtil;
import info.shillem.util.xsp.annotation.ManagedBean;
import info.shillem.util.xsp.annotation.ManagedProperty;
import info.shillem.util.xsp.bean.PageBean;

public abstract class ViewHandler extends ViewHandlerExImpl {

    public ViewHandler(javax.faces.application.ViewHandler delegate) {
        super(delegate);
    }

    @Override
    public UIViewRoot createView(FacesContext facesContext, String page) {
        Class<? extends PageBean> beanClass = getPageBeanClass(page.substring(1));

        if (beanClass == null) {
            return super.createView(facesContext, page);
        }

        PageBean bean;

        try {
            bean = beanClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new FacesException(e);
        }

        Map<Field, String> deferred = new LinkedHashMap<>();

        for (Field field : bean.getClass().getDeclaredFields()) {
            ManagedProperty annotation = field.getAnnotation(ManagedProperty.class);

            if (annotation == null) {
                continue;
            }

            String expression = annotation.value();

            if (annotation.post()) {
                deferred.put(field, expression);

                continue;
            }

            setManagedProperty(facesContext, bean, field, expression);
        }

        bean.init(facesContext);

        String beanName = beanClass.getAnnotation(ManagedBean.class).name();

        Map<String, Object> requestScope = XPageScope.REQUEST.getValues(facesContext);
        requestScope.put(beanName, bean);
        UIViewRootEx view = (UIViewRootEx) super.createView(facesContext, page);
        CastUtil.toAnyMap(view.getViewMap()).put(beanName, bean);
        requestScope.remove(beanName);

        deferred.forEach(
                (field, expression) -> setManagedProperty(facesContext, bean, field, expression));

        if (view.getBeforeRenderResponse() == null) {
            view.setBeforeRenderResponse(facesContext.getApplication().createMethodBinding(
                    "#{".concat(beanName).concat(".beforeRenderResponse}"),
                    new Class[] { PhaseEvent.class }));
        }

        if (view.getAfterRenderResponse() == null) {
            view.setAfterRenderResponse(facesContext.getApplication().createMethodBinding(
                    "#{".concat(beanName).concat(".afterRenderResponse}"),
                    new Class[] { PhaseEvent.class }));
        }

        return view;
    }

    private Class<? extends PageBean> getPageBeanClass(String page) {
        for (Class<? extends PageBean> cls : getPageBeanClasses()) {
            ManagedBean annotation = cls.getAnnotation(ManagedBean.class);

            if (annotation == null) {
                continue;
            }

            String identifier = annotation.page();

            if (identifier.isEmpty()) {
                identifier = annotation.name();
            }

            if (identifier.equals(page)) {
                return cls;
            }
        }

        return null;
    }

    protected abstract Set<Class<? extends PageBean>> getPageBeanClasses();

    private void setManagedProperty(
            FacesContext facesContext,
            PageBean bean,
            Field field,
            String expression) {
        try {
            field.setAccessible(true);
            field.set(bean, facesContext.getApplication()
                    .createValueBinding(expression)
                    .getValue(facesContext));
        } catch (Exception e) {
            throw new FacesException(e);
        }
    }

}
