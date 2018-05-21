package info.shillem.dto;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class AttachmentMap implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Map<String, AttachmentFile> attachments;

	public AttachmentMap() {
		attachments = new Hashtable<String, AttachmentFile>();
	}

	public AttachmentMap(int size) {
		attachments = new Hashtable<String, AttachmentFile>(size);
	}

	public void add(String fileName) {
		attachments.put(fileName, new AttachmentFile(fileName));
	}

	public void add(String fileName, File uploadedFile) {
		attachments.put(fileName, new AttachmentFile(fileName, uploadedFile));
	}

	public void remove(String fileName) {
		attachments.remove(fileName);
	}

	public AttachmentFile getFile(String fileName) {
		return attachments.get(fileName);
	}

	public List<AttachmentFile> getFiles() {
		return new ArrayList<>(attachments.values());
	}

	public boolean isEmpty() {
		return attachments.isEmpty();
	}

	public void clear() {
		attachments.clear();
	}

}
