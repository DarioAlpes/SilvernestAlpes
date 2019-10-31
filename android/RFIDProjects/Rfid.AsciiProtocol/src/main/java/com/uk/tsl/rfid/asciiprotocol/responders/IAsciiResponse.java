package com.uk.tsl.rfid.asciiprotocol.responders;

import java.util.List;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 A response to as IAsciiCommand
*/
public interface IAsciiResponse
{
	/** 
	 Gets the error code or an empty string if none
	*/
	String getErrorCode();

	/** 
	 Gets a value indicating whether the command executed successfully
	*/
	boolean isSuccessful();

	/** 
	 Gets the messages received from the last response
	*/
	List<String> getMessages();

	/** 
	 Gets the parameters received from the last response
	*/
	List<String> getParameters();

	/** 
	 Gets all the lines as received from the last response
	*/
	List<String> getResponse();
}