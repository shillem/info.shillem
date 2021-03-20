package info.shillem.dto;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AttachedFiles implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<AttachedFile> values;

    public AttachedFiles() {
        values = new ArrayList<>();
    }

    public void add(String name) {
        Objects.requireNonNull(name, "Name cannot be null");

        for (AttachedFile value : values) {
            if (name.equalsIgnoreCase(value.getName())) {
                return;
            }
        }

        values.add(new AttachedFile(name));
    }

    public void add(String name, File file) {
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(file, "File cannot be null");

        for (AttachedFile value : values) {
            if (name.equalsIgnoreCase(value.getName())) {
                value.setFile(file);

                return;
            }
        }

        values.add(new AttachedFile(name, file));
    }

    public AttachedFile get(String name) {
        for (AttachedFile value : values) {
            if (name.equalsIgnoreCase(value.getName())) {
                return value;
            }
        }

        return null;
    }

    public List<AttachedFile> getAll() {
        return values;
    }

    public AttachedFile getFirst() {
        if (values.isEmpty()) {
            return null;
        }

        return values.get(0);
    }

    public List<AttachedFile> getPending() {
        return getAll().stream()
                .filter((f) -> f.getFile() != null)
                .collect(Collectors.toList());
    }

    public void remove(AttachedFile value) {
        values.remove(value);
    }

}
