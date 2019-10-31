package com.uk.tsl.rfid.asciiprotocol.responders;


import android.util.Log;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------


/** 
 A simple responder that inserts every line it sees, preceded by '>', into the standard log file 
*/
public class LoggerResponder implements IAsciiCommandResponder
{
	/** 
	 Gets a value indicating whether the response is complete (i.e. received OK: or ER:)
	*/
	@Override
	public final boolean isResponseFinished()
	{
		return false;
	}

	/** 
	 Clears the values from the last response (does nothing)
	*/
	public final void clearLastResponse()
	{
		// does nothing
	}

	/** 
	 Captures the line to the log
	 
	 @param fullLine The fullLine to log
	 @param moreLinesAvailable True if more lines are going to be passed to this method
	 @return False to allow other IAsciiCommandResponders to process the responses also
	*/
	public final boolean processReceivedLine(String fullLine, boolean moreLinesAvailable)
	{
		Log.i("LoggerResponder", ">" + fullLine);
		return false;
	}

}