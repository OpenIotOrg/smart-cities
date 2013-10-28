package edu.kit.cm.ivu.smartmeetings.ui.util;

/**
 * These are callbacks that get called when a scan operation was requested and
 * succeeded.
 * 
 * @author michael
 * 
 */
public interface IScanCallback {
	/**
	 * Called when the scan was successful and we read a String from the Tag.
	 * 
	 * @param data
	 *            The String we read.
	 */
	void scanSuccessful(String data);
}
