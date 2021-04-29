package info.shillem.util;

import java.util.ArrayList;
import java.util.List;

public class ProgressMonitor {

    private final List<Exception> exceptions;
    private final List<String> messages;
    
    private boolean canceled;

    public ProgressMonitor() {
        exceptions = new ArrayList<>();
        messages = new ArrayList<>();
    }

    public List<Exception> getExceptions() {
        return exceptions;
    }

    public List<String> getMessages() {
        return messages;
    }

    public boolean isRequestCanceled() {
        return canceled;
    }

    public void logException(Exception e) {
        if (e != null) {
            exceptions.add(e);
        }
    }

    public void logMessage(String message) {
        if (message != null) {
            messages.add(message);
        }
    }

    public void setRequestCanceled() {
        canceled = true;
    }

}
