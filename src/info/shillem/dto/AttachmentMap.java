package info.shillem.dto;

import java.io.File;
import java.util.Hashtable;

public class AttachmentMap extends Hashtable<String, AttachmentFile> {

    private static final long serialVersionUID = 1L;
    
    public void put(String fileName) {
        put(fileName, new AttachmentFile(fileName));
    }

    public void put(String fileName, File uploadedFile) {
        put(fileName, new AttachmentFile(fileName, uploadedFile));
    }
    
    public AttachmentFile getFirst() {
        return values().iterator().next();
    }

}
