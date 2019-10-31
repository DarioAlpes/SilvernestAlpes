package com.uk.tsl.rfid.asciiprotocol.commands;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.uk.tsl.rfid.asciiprotocol.Constants;
import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 A command to obtain or set the time of the reader's real-time clock 
*/
public class TimeCommand extends AsciiSelfResponderCommandBase
{
	/** 
	 Initializes a new instance of the TimeCommand class
	*/
	public TimeCommand()
	{
		super(".tm");
	}

	/** 
	 Gets or sets the time value to write to or read from the reader.
	 Set to null (Nothing in Visual Basic) to read the time from the reader.
	 When not null the reader time will be set to the time part of this value
	*/
	private java.util.Date privateTime;
	public final java.util.Date getTime()
	{
		return privateTime;
	}
	public final void setTime(java.util.Date value)
	{
		privateTime = value;
	}

	/** 
	 Returns a new instance of the command class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance
	*/
	public static TimeCommand synchronousCommand()
	{
		TimeCommand command;

		command = new TimeCommand();
		command.setSynchronousCommandResponder(command);
		return command;
	}

	/** 
	 Returns a new instance of the command class that will execute synchronously (as its own responder)
	 
	 @param time The time to write
	 @return A new synchronous command instance
	*/
	public static TimeCommand synchronousCommand(java.util.Date time)
	{
		TimeCommand command;

		command = new TimeCommand();
		command.setSynchronousCommandResponder(command);
		command.setTime(time);
		return command;
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

		if (this.getTime() != null)
		{
			DateFormat formatter = new SimpleDateFormat("HHmmss", Constants.COMMAND_LOCALE);
			line.append("-s" + formatter.format(this.getTime()));
		}
	}

	/** 
	 Each correctly terminated line from the device is passed to this method for processing
	 
	 @param fullLine The line to be processed
	 @param header The response line header excluding the colon e.g. 'CS' for a command started response
	 @param value The response line following the colon e.g. '.iv'
	 @param moreAvailable When true indicates there are additional lines to be processed (and will also be passed to this method)
	 @return Return true if this line should NOT be passed to any other responder.
	 @throws Exception 
	 
	*/
	@Override
	protected boolean processReceivedLine(String fullLine, String header, String value, boolean moreAvailable) throws Exception
	{
		// Allow super class to detect the command start, messages, parameters and command end
		boolean superDidProcess;

		superDidProcess = super.processReceivedLine(fullLine, header, value, moreAvailable);

		if (superDidProcess)
		{
			// This line has been handled (no more processing needed)
			return true;
		}
		else
		{
			if ("TM".equals(header))
			{
				DateFormat parser = new SimpleDateFormat("HH:mm:ss", Constants.COMMAND_LOCALE);
				this.setTime(parser.parse(value.trim()));
			}
			else
			{
				// Not recognised so allow others to see it
				return false;
			}

			// This line has been handled (no more processing needed)
			this.appendToResponse(fullLine);
			return true;
		}
	}
}