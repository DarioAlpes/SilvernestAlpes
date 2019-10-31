package com.uk.tsl.rfid.asciiprotocol.responders;

import com.uk.tsl.utils.StringHelper;


//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 Base class for library commands
 This is identical to the AsciiCommandResponderBase except, by default, it will only
 repond to commands containing  LibraryCommandId
*/
public abstract class AsciiCommandLibraryResponderBase extends AsciiCommandResponderBase
{
	/** 
	 The value inserted into the command to identify the command as a library command "LCMD"
	*/
	public static final String LibraryCommandId = "LCMD";

	/** 
	 Initializes a new instance of the AsciiCommandLibraryResponderBase class
	 
	 @param commandName The command name e.g. '.iv' for Inventory or string.Empty to respond to all commands
	*/
	protected AsciiCommandLibraryResponderBase(String commandName)
	{
		super(commandName);
		this.setCaptureNonLibraryResponses(false);
	}

	/** 
	 Gets or sets a value indicating whether to capture non-library responses
	*/
	private boolean privateCaptureNonLibraryResponses;
	public final boolean captureNonLibraryResponses()
	{
		return privateCaptureNonLibraryResponses;
	}
	public final void setCaptureNonLibraryResponses(boolean value)
	{
		privateCaptureNonLibraryResponses = value;
	}

	/** 
	 Each correctly terminated line from the device is passed to this method for processing
	 
	 @param fullLine The line to be processed
	 @param header The response line header excluding the colon e.g. 'CS' for a command started response
	 @param value The response line following the colon e.g. '.iv'
	 @param moreAvailable When true indicates there are additional lines to be processed (and will also be passed to this method)
	 @return 
	 Return true if this line should NOT be passed to any other responder.
	 
	*/
	@Override
	protected boolean processReceivedLine(String fullLine, String header, String value, boolean moreAvailable) throws Exception
	{
		// Check that the TSL_LibraryCommandId is present immediately after the command (unless allowing non library responses)
		if ( !this.getResponseStarted() && "CS".equals(header)
				&& (value.startsWith(this.getCommandName()) || StringHelper.isNullOrEmpty(this.getCommandName()))
				&&  !this.captureNonLibraryResponses() && value.substring(3).indexOf(LibraryCommandId) < 0)
		{
			// Don't handle this, allow others to see it

			return false;
		}

		return super.processReceivedLine(fullLine, header, value, moreAvailable);
	}

	/** 
	 Builds the command line to send to the reader to execute the command
	 
	 @param line The command line to append to
	 
	 When overriding this method call the base class to construct the command line as known to the base class and
	 then append the additional parameters to the end of the line
	 
	*/
	@Override
	protected void buildCommandLine(StringBuilder line)
	{
		super.buildCommandLine(line);

		line.append(LibraryCommandId);
		line.append(" ");
	}
}