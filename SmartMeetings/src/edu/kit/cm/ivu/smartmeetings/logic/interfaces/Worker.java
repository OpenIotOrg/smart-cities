package edu.kit.cm.ivu.smartmeetings.logic.interfaces;

/**
 * Interface defines methods to doWork of generic type and to handle the Result
 * 
 * @author Kirill
 * 
 * @param <I>
 *            Type of input
 * @param <O>
 *            Type of output
 */
public interface Worker<I, O> {
	O doWork(I... input);

	void handleResult(O result);
}
