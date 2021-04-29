package info.shillem.dto;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

public class AttachedFile implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;

    private File file;
    private boolean remove;
    private Long size;

    public AttachedFile(String name) {
        this(name, null);
    }

    public AttachedFile(String name, File file) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public Long getSize() {
        return size;
    }

    public boolean isRemove() {
        return remove;
    }

    public void setFile(File value) {
        file = value;
    }

    public void setRemove(Boolean value) {
        remove = value;
    }

    public void setSize(Long value) {
        size = value;
    }

    public void toggleRemove() {
        remove = !remove;
    }

    public void unlink() {
        file = null;
    }

}
