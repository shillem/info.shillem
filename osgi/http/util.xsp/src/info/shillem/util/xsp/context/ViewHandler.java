package info.shillem.util.xsp.context;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.faces.FacesException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;

import com.ibm.xsp.application.ViewHandlerExImpl;
import com.ibm.xsp.component.UIViewRootEx;

import info.shillem.util.CastUtil;
import info.shillem.util.xsp.annotation.ManagedExecution;
import info.shillem.util.xsp.annotation.ManagedPage;
import info.shillem.util.xsp.annotation.ManagedProperty;

public abstract class ViewHandler extends ViewHandlerExImpl {

    public ViewHandler(javax.faces.application.ViewHandler delegate) {
        super(delegate);
    }

    private void bindFields(FacesContext context, Object bean) {
        for (Field field : bean.getClass().getDeclaredFields()) {
            ManagedProperty annotation = field.getAnnotation(ManagedProperty.class);

            if (annotation == null) {
                continue;
            }

            String expression = annotation.value();

            try {
                field.setAccessible(true);
                field.set(bean, context.getApplication()
                        .createValueBinding(expression)
                        .getValue(context));
            } catch (Exception e) {
                throw new FacesException(e);
            }
        }
    }

    private void bindMethods(
            FacesContext context,
            UIViewRootEx root,
            Object bean,
            String beanName) {
        for (Method method : bean.getClass().getMethods()) {
            ManagedExecution execution = method.getAnnotation(ManagedExecution.class);

            if (execution == null) {
                continue;
            }

            switch (execution.value()) {
            case POST_CONSTRUCT: {
                try {
                    if (method.getParameterCount() == 0) {
                        method.invoke(bean);
                    } else {
                        method.invoke(bean, new Object[] { context });
                    }
                } catch (Exception e) {
                    throw new FacesException(e);
                }

                break;
            }
            case BEFORE_RENDER_RESPONSE:
                if (root.getBeforeRenderResponse() == null) {
                    root.setBeforeRenderResponse(context.getApplication().createMethodBinding(
                            String.format("#{%s.%s}", beanName, method.getName()),
                            new Class[] { PhaseEvent.class }));
                }
                break;
            case AFTER_RENDER_RESPONSE:
                if (root.getAfterRenderResponse() == null) {
                    root.setAfterRenderResponse(context.getApplication().createMethodBinding(
                            String.format("#{%s.%s}", beanName, method.getName()),
                            new Class[] { PhaseEvent.class }));
                }

                break;
            }
        }
    }

    @Override
    protected UIViewRoot doCreateView(
            FacesContext context,
            String viewId,
            Locale locale,
            String renderKit) {
        Class<?> beanClass = getViewBeanClass(viewId);

        if (beanClass == null) {
            return super.doCreateView(context, viewId, locale, renderKit);
        }

        Object bean;

        try {
            bean = beanClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new FacesException(e);
        }

        String beanName = beanClass.getAnnotation(ManagedPage.class).beanName();

        Map<String, Object> requestScope = XPageScope.REQUEST.getValues(context);
        requestScope.put(beanName, bean);
        UIViewRoot root = super.doCreateView(context, viewId, locale, renderKit);
        CastUtil.toAnyMap(root.getViewMap()).put(beanName, bean);
        requestScope.remove(beanName);

        bindFields(context, bean);
        bindMethods(context, (UIViewRootEx) root, bean, beanName);

        return root;
    }

    private Class<?> getViewBeanClass(String viewId) {
        for (Class<?> cls : getViewBeanClasses()) {
            ManagedPage annotation = cls.getAnnotation(ManagedPage.class);

            if (annotation == null) {
                continue;
            }

            if (annotation.viewId().equals(viewId)) {
                return cls;
            }
        }

        return null;
    }

    protected abstract Set<Class<?>> getViewBeanClasses();

}
