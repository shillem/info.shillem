package info.shillem.synchronizer.util;

import info.shillem.synchronizer.dots.Program.Nature;
import info.shillem.synchronizer.dto.Record;
import info.shillem.synchronizer.lang.ProcessorException;

public interface Processor<T extends Record> {
    
	/**
	 * Execute the job
	 * 
	 * @param the
	 *            process manager object that holds everything needed for the
	 *            process to function
	 * 
	 * @return true if the execution completed, false if canceled
	 */
	boolean execute() throws ProcessorException;
	
	boolean isNature(Nature nature);
	
	T newRecord();

}
