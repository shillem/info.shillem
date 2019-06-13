package info.shillem.util;

import java.io.Serializable;
import java.util.Objects;

public class ConnectionParams implements Serializable {

    public static class Builder {
        
        private String hostname;
        private String username;
        private String password;
        
        public ConnectionParams build() {
            return new ConnectionParams(this);
        }
        
        public void withCredentials(String username, String password) {
            this.username = Objects.requireNonNull(username, "Username cannot be null");
            this.password = Objects.requireNonNull(password, "Password cannot be null");
        }
        
        public void withHostname(String hostname) {
            this.hostname = Objects.requireNonNull(hostname, "Host name cannot be null");
        }
        
    }
    
    private static final long serialVersionUID = 1L;
    
    private final String hostname;
    private final String username;
    private final String password;

    protected ConnectionParams(Builder builder) {
        hostname = builder.hostname;
        username = builder.username;
        password = builder.password;
    }
    
    public String getHostname() {
        return hostname;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getUsername() {
        return username;
    }
    
    public boolean hasCredentials() {
        return StringUtil.isNotEmpty(username) && StringUtil.isNotEmpty(password);
    }
    
}
