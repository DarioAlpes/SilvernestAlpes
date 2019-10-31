package com.uk.tsl.rfid.asciiprotocol.commands;

import com.uk.tsl.rfid.asciiprotocol.responders.IAsciiCommandResponder;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 Defines an ASCII command that can be performed on any device supporting the TSL ASCII 2.0 Protocol
 
 
 A TSLAsciiCommand can be executed using any object that implements the TSLAsciiCommandExecuting protocol.
 The command can be executed either asynchronously or synchronously (by setting synchronousCommandResponder prior to execution).
 Synchronous commands prevent the issue of subsequent commands until the command’s response has been received.
 
*/
public interface IAsciiCommand
{
	/** 
	 Gets the Ascii command identifier e.g. ‘.vr’ or ‘.da’
	*/
	String getCommandName();

	/** 
	 Gets or sets the maximum time (in seconds) to wait for this command to complete when invoked synchronously
	*/
	double getMaxSynchronousWaitTime();
	void setMaxSynchronousWaitTime(double value);

	/** 
	 Gets or sets the IAsciiCommandResponder that will cause this command to execute synchronously
	*/
	IAsciiCommandResponder getSynchronousCommandResponder();
	void setSynchronousCommandResponder(IAsciiCommandResponder value);

	/** 
	 Returns the Ascii command line (including terminators) to be sent to the device to execute the command
	 
	 @return 
	 The ASCII command line to execute the command
	 
	*/
	String getCommandLine();
}