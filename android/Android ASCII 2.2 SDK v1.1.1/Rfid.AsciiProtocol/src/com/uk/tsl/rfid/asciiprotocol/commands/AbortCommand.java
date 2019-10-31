package com.uk.tsl.rfid.asciiprotocol.commands;

import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 A command to terminate the current command and any pending commands. 
 It also stops any software switch presses that are in progress. 
*/
public class AbortCommand extends AsciiSelfResponderCommandBase implements IAsciiCommand
{
	/** 
	 Initializes a new instance of the AbortCommand class with the specified responder
	*/
	public AbortCommand()
	{
		super(".ab");
	}

	/** 
	 Returns a new instance of the command class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance
	*/
	public static AbortCommand synchronousCommand()
	{
		AbortCommand command;

		command = new AbortCommand();
		command.setSynchronousCommandResponder(command);
		return command;
	}
}