package info.shillem.util.xsp.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;

import com.ibm.xsp.complex.Attr;
import com.ibm.xsp.complex.Parameter;
import com.ibm.xsp.component.FacesAttrsObject;
import com.ibm.xsp.component.xp.XspEventHandler;

import info.shillem.util.CastUtil;

public class ComponentUtil {

    public static abstract class Handler<T extends UIComponent> {

        public final T component;

        private Map<String, String> facesAttrs;

        public Handler(T component) {
            this.component = component;
        }

        public Map<String, Object> getAttributes() {
            return CastUtil.toAnyMap(component.getAttributes());
        }

        public Map<String, String> getFacesAttrs() {
            if (facesAttrs == null) {
                facesAttrs = ComponentUtil.getFacesAttrs(component);
            }

            return facesAttrs;
        }

        public boolean isAttributeTrue(String name) {
            return Optional.ofNullable((String) getAttributes().get(name))
                    .map(Boolean::valueOf)
                    .orElse(Boolean.FALSE);
        }

        public boolean isFacesAttrTrue(String name) {
            return Optional.ofNullable(getFacesAttrs().get(name))
                    .map(Boolean::valueOf)
                    .orElse(Boolean.FALSE);
        }

    }

    private ComponentUtil() {

    }

    public static String getFacesAttr(UIComponent component, String name) {
        return getFacesAttrList(component).stream()
                .filter(Attr::isRendered)
                .filter((attr) -> Objects.equals(name, attr.getName()))
                .map(Attr::getValue)
                .findFirst()
                .orElse(null);
    }

    private static List<Attr> getFacesAttrList(UIComponent component) {
        if (!(component instanceof FacesAttrsObject)) {
            return Collections.emptyList();
        }

        return Optional
                .ofNullable(((FacesAttrsObject) component).getAttrs())
                .orElse(Collections.emptyList());
    }

    public static Map<String, String> getFacesAttrs(UIComponent component) {
        return getFacesAttrs(component, (Predicate<Attr>) null);
    }

    public static Map<String, String> getFacesAttrs(
            UIComponent component, Predicate<Attr> predicate) {
        Stream<Attr> stream = getFacesAttrList(component).stream()
                .filter(Attr::isRendered);

        if (predicate != null) {
            stream.filter(predicate);
        }

        return stream
                .filter((attr) -> Objects.nonNull(attr.getValue()))
                .collect(Collectors.toMap(Attr::getName, Attr::getValue));
    }

    public static Map<String, String> getFacesAttrs(
            UIComponent component, String name, String... names) {
        List<String> filters = new ArrayList<>();

        filters.add(name);

        if (names != null) {
            filters.addAll(Arrays.asList(names));
        }

        return getFacesAttrs(component, (attr) -> filters.contains(attr.getName()));
    }

    public static String getHandlerParam(UIComponent component, String name) {
        return getHandlerParamList(component).stream()
                .filter((param) -> Objects.equals(name, param.getName()))
                .map(Parameter::getValue)
                .findFirst()
                .orElse(null);
    }

    private static List<Parameter> getHandlerParamList(UIComponent component) {
        if (!(component instanceof XspEventHandler)) {
            return Collections.emptyList();
        }

        return Optional
                .ofNullable(((XspEventHandler) component).getParameters())
                .orElse(Collections.emptyList());
    }

    public static void resetInput(UIComponent component) {
        if (component instanceof EditableValueHolder) {
            EditableValueHolder holder = (EditableValueHolder) component;

            holder.setSubmittedValue(null);
            holder.setValid(true);
        }

        if (component.getChildCount() == 0) {
            return;
        }

        List<UIComponent> children = CastUtil.toAnyList(component.getChildren());

        children.forEach(ComponentUtil::resetInput);
    }

}
