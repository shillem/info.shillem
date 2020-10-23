package info.shillem.dto;

import java.io.File;
import java.io.Serializable;

public class AttachmentFile implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private boolean remove;
    private File file;

    public AttachmentFile(String name) {
        this.name = name;
    }

    public AttachmentFile(String name, File file) {
        this.name = name;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public boolean isRemove() {
        return remove;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
    }

    public void toggleRemove() {
        remove = !remove;
    }

    public void unlink() {
        this.file = null;
    }

}
