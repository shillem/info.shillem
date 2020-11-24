package info.shillem.dto;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Objects;

public class AttachmentFile implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;

    private File file;
    private InputStream inputStream;
    private boolean remove;
    private Long size;

    public AttachmentFile(String name) {
        this(name, null);
    }

    public AttachmentFile(String name, File file) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public InputStream getInputStream() {
        return inputStream;
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

    public void setInputStream(InputStream value) {
        inputStream = value;
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
