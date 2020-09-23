package info.shillem.dto;

public interface BaseField {

    FieldProperties getProperties();

    default boolean isMandatory() {
        return false;
    }
    
    String name();

}
