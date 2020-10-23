package info.shillem.util;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum MimeUtil {
    ;
    
    public static String getHeaderProperty(String name, String value) {
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(value, "Value cannot be null");
        
        Pattern pattern = Pattern.compile(
                name + "=['\"]*([^'\";]+)['\"]*;*",
                Pattern.CASE_INSENSITIVE);
        
        Matcher m = pattern.matcher(value);
        
        m.find();
        
        return m.group(1);
    }
    
}
