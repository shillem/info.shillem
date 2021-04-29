package info.shillem.synchronizer.util;

import info.shillem.synchronizer.dto.Record;

public interface ProcessorBuilder {
	
	public Processor<? extends Record> build(ProcessorHelper helper);
	
}
