package info.shillem.dto;

import java.io.File;
import java.io.Serializable;

public class AttachmentFile implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private boolean remove;
	private File uploadedFile;

	public AttachmentFile(String fileName) {
		this(fileName, null);
	}

	public AttachmentFile(String fileName, File uploadedFile) {
		this.name = fileName;
		this.uploadedFile = uploadedFile;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public File getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(File uploadFile) {
		this.uploadedFile = uploadFile;
	}

	public boolean isRemove() {
		return remove;
	}

	public void setRemove(boolean remove) {
		this.remove = remove;
	}
	
	public void toggleRemove() {
		remove = !remove;
	}

}
