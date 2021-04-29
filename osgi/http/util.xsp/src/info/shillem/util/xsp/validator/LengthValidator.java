package info.shillem.util.xsp.validator;

import java.util.Map;
import java.util.Optional;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import info.shillem.util.xsp.component.ComponentUtil;

public class LengthValidator extends javax.faces.validator.LengthValidator {

    @Override
    public void validate(
            FacesContext context,
            UIComponent component,
            Object value)
            throws ValidatorException {
        Map<String, String> attrs = ComponentUtil.getFacesAttrs(component);

        Optional
                .ofNullable(attrs.get("data-max-length"))
                .map(Integer::valueOf)
                .ifPresent(this::setMaximum);

        Optional
                .ofNullable(attrs.get("data-min-length"))
                .map(Integer::valueOf)
                .ifPresent(this::setMinimum);

        super.validate(context, component, value);
    }

}
