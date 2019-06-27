package info.shillem.synchronizer.util;

public class ProcessorTracker {

    private int created;
    private int deleted;
	private int modified;
	private int skipped;
	private int unmodified;

	public void addCreated() {
		created++;
	}

	public void addDeleted() {
		deleted++;
	}

	public void addModified() {
		modified++;
	}

	public void addModified(boolean isNewNote) {
		if (isNewNote) {
			addCreated();
		} else {
			addModified();
		}
	}

	public void addSkipped() {
		skipped++;
	}

	public void addUnmodified() {
		unmodified++;
	}

	public int getCreated() {
		return created;
	}

	public int getDeleted() {
		return deleted;
	}

	public int getModified() {
		return modified;
	}

	public int getProcessed() {
		return skipped + created + modified + unmodified + deleted;
	}

	public int getSkipped() {
		return skipped;
	}

	public int getTouched() {
		return created + modified;
	}

	public int getUnmodified() {
		return unmodified;
	}

	@Override
	public String toString() {
		return String.format(
		        "skipped=%s created=%s modified=%s unmodified=%s deleted=%s",
		        skipped, created, modified, unmodified, deleted);
	}

}
