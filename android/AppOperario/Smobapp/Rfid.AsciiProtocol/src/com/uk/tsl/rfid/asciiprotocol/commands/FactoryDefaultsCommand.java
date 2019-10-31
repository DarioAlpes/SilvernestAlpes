package com.uk.tsl.rfid.asciiprotocol.commands;

import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 A command to reset the reader to its default configuration 
*/
public class FactoryDefaultsCommand extends AsciiSelfResponderCommandBase
{
	/** 
	 Initializes a new instance of the FactoryDefaultsCommand class
	*/
	public FactoryDefaultsCommand()
	{
		super(".fd");
	}

	/** 
	 Returns a new instance of the FactoryDefaultsCommand class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance
	*/
	public static FactoryDefaultsCommand synchronousCommand()
	{
		FactoryDefaultsCommand command;

		command = new FactoryDefaultsCommand();
		command.setSynchronousCommandResponder(command);
		return command;
	}
}