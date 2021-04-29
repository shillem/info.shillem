package info.shillem.util;

import java.util.Random;

public class UUIDGenerator {

    private final String prefix;
    
    public UUIDGenerator(String prefix) {
        if (StringUtil.isEmpty(prefix)) {
            throw new UnsupportedOperationException("Prefix must be a non-zero character string");
        }
        
        this.prefix = prefix;
    }
    
    public String generate() {
        StringBuilder builder = new StringBuilder(prefix);
        
        builder.append("-");
        
        Random rand = new Random();
        
        for (int i = 0; i < 3; i++) {
            builder.append(rand.nextInt(10));
        }
        
        builder.append("-");
        
        return builder.append(System.currentTimeMillis()).toString();
    }
    
}
