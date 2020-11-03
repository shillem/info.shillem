package info.shillem.dto;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AttachmentFiles implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<AttachmentFile> values;

    public AttachmentFiles() {
        values = new ArrayList<>();
    }

    public void add(String name) {
        Objects.requireNonNull(name, "Name cannot be null");

        for (AttachmentFile value : values) {
            if (name.equalsIgnoreCase(value.getName())) {
                return;
            }
        }

        values.add(new AttachmentFile(name));
    }

    public void add(String name, File file) {
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(file, "File cannot be null");

        for (AttachmentFile value : values) {
            if (name.equalsIgnoreCase(value.getName())) {
                value.setFile(file);

                return;
            }
        }

        values.add(new AttachmentFile(name, file));
    }

    public AttachmentFile get(String name) {
        for (AttachmentFile value : values) {
            if (name.equalsIgnoreCase(value.getName())) {
                return value;
            }
        }

        return null;
    }

    public List<AttachmentFile> getAll() {
        return new ArrayList<>(values);
    }

    public AttachmentFile getFirst() {
        if (values.isEmpty()) {
            return null;
        }

        return values.get(0);
    }
    
    public void remove(AttachmentFile value) {
        values.remove(value);
    }

}
