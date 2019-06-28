package info.shillem.synchronizer.util;

import info.shillem.synchronizer.dto.Record;

public class ProcessorBuilder {
	
	public Processor<? extends Record> build(ProcessorHelper helper) {
	    return new ProcessorSqlToDomino<Record>(helper, () -> new Record());
	}
	
}
