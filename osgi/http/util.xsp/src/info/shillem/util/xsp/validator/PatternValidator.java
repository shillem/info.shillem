package info.shillem.util.xsp.validator;

import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

public class PatternValidator implements StateHolder, Validator {
    
    public static final String VALIDATOR_ID = "info.shillem.xsp.PatternValidator";

    private String message;
    private Pattern pattern;
    private boolean transientValue;

    public PatternValidator(Pattern pattern, String message) {
        this.pattern = pattern;
        this.message = message;
    }
    
    public PatternValidator(String regex, String message) {
        this(Pattern.compile(regex), message);
    }

    @Override
    public boolean isTransient() {
        return transientValue;
    }

    @Override
    public void restoreState(FacesContext facesContext, Object state) {
        Object[] values = (Object[]) state;

        this.message = (String) values[0];
        this.pattern = (Pattern) values[1];
    }

    @Override
    public Object saveState(FacesContext facesContext) {
        return new Object[] { this.message, this.pattern };
    }

    public void setRegex(String regex) {
        setPattern(Pattern.compile(regex));
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public void setTransient(boolean transientValue) {
        this.transientValue = transientValue;
    }

    @Override
    public void validate(FacesContext facesContext, UIComponent component, Object value)
            throws ValidatorException {
        if (!(value instanceof String)) {
            return;
        }

        if (!pattern.matcher((String) value).matches()) {
            throw new ValidatorException(new FacesMessage(message));
        }
    }

}
