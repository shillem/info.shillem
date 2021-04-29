package info.shillem.util.xsp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ManagedProperty {
    
    PropertyPhase phase() default PropertyPhase.BEFORE_VIEW_CREATION;
    
    String value();

}
