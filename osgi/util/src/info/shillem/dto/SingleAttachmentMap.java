package info.shillem.dto;

import java.util.Map;

public class SingleAttachmentMap extends AttachmentMap {

    private static final long serialVersionUID = 1L;

    @Override
    public synchronized AttachmentFile put(String key, AttachmentFile value) {
        if (isEmpty()) {
            return super.put(key, value);
        }

        AttachmentFile currentFile = getFirst();

        if (currentFile.getName().equals(value.getName())) {
            currentFile.setFile(value.getFile());

            return currentFile;
        }

        currentFile.setRemove(true);

        return super.put(key, value);
    }

    @Override
    public synchronized void putAll(Map<? extends String, ? extends AttachmentFile> t) {
        throw new UnsupportedOperationException();
    }

}
