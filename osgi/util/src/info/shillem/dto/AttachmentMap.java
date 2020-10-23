package info.shillem.dto;

import java.io.File;
import java.util.Hashtable;

public class AttachmentMap extends Hashtable<String, AttachmentFile> {

    private static final long serialVersionUID = 1L;

    public AttachmentFile getFirst() {
        if (isEmpty()) {
            return null;
        }

        return values().iterator().next();
    }

    public void put(String fileName) {
        put(fileName, new AttachmentFile(fileName));
    }

    public void put(String fileName, File file) {
        put(fileName, new AttachmentFile(fileName, file));
    }

}
