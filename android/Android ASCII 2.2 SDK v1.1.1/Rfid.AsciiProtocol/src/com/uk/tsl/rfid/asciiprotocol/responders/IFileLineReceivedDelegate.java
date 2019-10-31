package com.uk.tsl.rfid.asciiprotocol.responders;

/**
 * The delegate interface to handle device file information received as CRLF delimited lines
 */
public interface IFileLineReceivedDelegate {
	/**
	 * Commands will invoke this method for each line received from a file
	 * 
	 * Commands will also invoke this method with line = null and moreAvailable = false
	 * upon command completion
	 * 
	 * @param line - the line received from the device (without line end delimiters) or null
	 * @param moreAvailable - true if there are more lines coming immediately
	 */
	void fileLineReceived(String line, boolean moreAvailable);
}
