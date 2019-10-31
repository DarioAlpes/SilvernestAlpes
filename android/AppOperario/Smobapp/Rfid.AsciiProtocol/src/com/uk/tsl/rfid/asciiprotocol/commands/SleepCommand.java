package com.uk.tsl.rfid.asciiprotocol.commands;

import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 A command to send the reader to sleep as soon as it has responded to this command
 Note: this will disconnect the reader from the terminal
*/
public class SleepCommand extends AsciiSelfResponderCommandBase
{
	/** 
	 Initializes a new instance of the SleepCommand class
	*/
	public SleepCommand()
	{
		super(".sl");
	}

	/** 
	 Returns a new instance of the SleepCommand class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance
	*/
	public static SleepCommand synchronousCommand()
	{
		SleepCommand command;

		command = new SleepCommand();
		command.setSynchronousCommandResponder(command);
		return command;
	}
}