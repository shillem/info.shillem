package info.shillem.util.xsp.context;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.faces.FacesException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;

import com.ibm.xsp.application.ViewHandlerExImpl;
import com.ibm.xsp.component.UIViewRootEx2;

import info.shillem.util.CastUtil;
import info.shillem.util.xsp.annotation.ManagedMethod;
import info.shillem.util.xsp.annotation.ManagedPage;
import info.shillem.util.xsp.annotation.ManagedProperty;
import info.shillem.util.xsp.annotation.MethodPhase;
import info.shillem.util.xsp.annotation.PropertyPhase;

public abstract class ViewHandler extends ViewHandlerExImpl {

    public ViewHandler(javax.faces.application.ViewHandler delegate) {
        super(delegate);
    }

    @Override
    public UIViewRoot createView(FacesContext context, String viewId) {
        Class<?> beanClass = getViewBeanClass(viewId);

        if (beanClass == null) {
            return super.createView(context, viewId);
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

        Map<PropertyPhase, List<Field>> fields = getAnnotatedFields(bean);
        Map<MethodPhase, List<Method>> methods = getAnnotatedMethods(bean);

        setAnnotatedFields(context, bean, fields.get(PropertyPhase.BEFORE_VIEW_CREATION));
        invokeAnnotatedMethods(context, bean, methods.get(MethodPhase.BEFORE_VIEW_CREATION));

        UIViewRootEx2 root = (UIViewRootEx2) super.createView(context, viewId);

        setAnnotatedFields(context, bean, fields.get(PropertyPhase.AFTER_VIEW_CREATION));
        invokeAnnotatedMethods(context, bean, methods.get(MethodPhase.AFTER_VIEW_CREATION));

        CastUtil.toAnyMap(root.getViewMap()).put(beanName, bean);
        requestScope.remove(beanName);

        List<Method> renderMethods;

        renderMethods = methods.get(MethodPhase.BEFORE_RENDER_RESPONSE);

        if (renderMethods != null && root.getBeforeRenderResponse() == null) {
            root.setBeforeRenderResponse(context.getApplication().createMethodBinding(
                    String.format("#{%s.%s}", beanName, renderMethods.get(0).getName()),
                    new Class[] { PhaseEvent.class }));
        }
        
        renderMethods = methods.get(MethodPhase.AFTER_RENDER_RESPONSE);
        
        if (renderMethods != null && root.getAfterRenderResponse() == null) {
            root.setAfterRenderResponse(context.getApplication().createMethodBinding(
                    String.format("#{%s.%s}", beanName, renderMethods.get(0).getName()),
                    new Class[] { PhaseEvent.class }));
        }

        return root;
    }

    private Map<PropertyPhase, List<Field>> getAnnotatedFields(Object bean) {
        return Stream.of(bean.getClass().getDeclaredFields())
                .filter((f) -> f.getAnnotation(ManagedProperty.class) != null)
                .collect(Collectors.groupingBy(
                        (f) -> f.getAnnotation(ManagedProperty.class).phase()));
    }

    private Map<MethodPhase, List<Method>> getAnnotatedMethods(Object bean) {
        return Stream.of(bean.getClass().getMethods())
                .filter((m) -> m.getAnnotation(ManagedMethod.class) != null)
                .collect(Collectors.groupingBy(
                        (m) -> m.getAnnotation(ManagedMethod.class).value()));
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

    private void invokeAnnotatedMethod(FacesContext context, Object bean, Method method) {
        try {
            if (method.getParameterCount() == 0) {
                method.invoke(bean);
            } else {
                method.invoke(bean, new Object[] { context });
            }
        } catch (Exception e) {
            throw new FacesException(e);
        }
    }
    
    private void invokeAnnotatedMethods(FacesContext context, Object bean, List<Method> methods) {
        if (methods == null) {
            return;
        }
        
        for (Method method : methods) {
            invokeAnnotatedMethod(context, bean, method);
        }
    }

    private void setAnnotatedField(FacesContext context, Object bean, Field field) {
        try {
            field.setAccessible(true);
            field.set(bean, context.getApplication()
                    .createValueBinding(field.getAnnotation(ManagedProperty.class).value())
                    .getValue(context));
            field.setAccessible(false);
        } catch (Exception e) {
            throw new FacesException(e);
        }
    }
    
    private void setAnnotatedFields(FacesContext context, Object bean, List<Field> fields) {
        if (fields == null) {
            return;
        }
        
        for (Field field : fields) {
            setAnnotatedField(context, bean, field);
        }
    }

}
