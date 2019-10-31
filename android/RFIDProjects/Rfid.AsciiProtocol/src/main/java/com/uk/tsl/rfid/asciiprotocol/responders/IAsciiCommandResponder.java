package com.uk.tsl.rfid.asciiprotocol.responders;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 The interface for classes that handle responses from ASCII 2.0 commands
*/
public interface IAsciiCommandResponder
{
	/** 
	 Gets a value indicating whether the response is complete (i.e. received OK: or ER:)
	*/
	boolean isResponseFinished();

	/** 
	 Clears the values from the last response
	 
	 
	 Derived classes must call super class to ensure correct operation
	 
	*/
	void clearLastResponse();

	/** 
	 Each correctly terminated line from the device is passed to this method for processing
	 
	 @param fullLine The line to be processed
	 @param moreLinesAvailable When true indicates there are additional lines to be processed (and will also be passed to this method)
	 @return True if this line should not be passed to any other responder
	 @throws Exception 
	*/
	boolean processReceivedLine(String fullLine, boolean moreLinesAvailable) throws Exception;
}