package com.uk.tsl.rfid.asciiprotocol.responders;

import com.uk.tsl.rfid.asciiprotocol.commands.IAsciiCommand;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 Base class for commands that receive their own reponses
*/
public abstract class AsciiSelfResponderCommandBase extends AsciiCommandLibraryResponderBase implements IAsciiCommand
{
	/** 
	 Used to generate the sequential command id
	*/
	private static int commandId = 0;

	/** 
	 Initializes a new instance of the AsciiSelfResponderCommandBase class
	 
	 @param commandName The command name e.g. ".iv" for Inventory
	*/
	protected AsciiSelfResponderCommandBase(String commandName)
	{
		super(commandName);
		this.setMaxSynchronousWaitTime(3.0);
	}

	/** 
	 Gets or sets the maximum time to wait for this command to complete when invoked synchronously
	*/
	private double privateMaxSynchronousWaitTime;
	public final double getMaxSynchronousWaitTime()
	{
		return privateMaxSynchronousWaitTime;
	}
	public final void setMaxSynchronousWaitTime(double value)
	{
		privateMaxSynchronousWaitTime = value;
	}

	/** 
	 Gets or sets the IAsciiCommandResponder to make this command execute synchronously
	*/
	private IAsciiCommandResponder privateSynchronousCommandResponder;
	public final IAsciiCommandResponder getSynchronousCommandResponder()
	{
		return privateSynchronousCommandResponder;
	}
	public final void setSynchronousCommandResponder(IAsciiCommandResponder value)
	{
		privateSynchronousCommandResponder = value;
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

		line.append( String.format("%06d", commandId++));
	}
}