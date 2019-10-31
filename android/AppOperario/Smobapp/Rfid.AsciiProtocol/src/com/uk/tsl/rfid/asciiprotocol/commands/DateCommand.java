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
 A command to obtain or set the date of the reader's real time clock
*/
public class DateCommand extends AsciiSelfResponderCommandBase
{
	/** 
	 Initializes a new instance of the DateCommand class
	*/
	public DateCommand()
	{
		super(".da");
	}

	/** 
	 Gets or sets the date to read or write (time is ignored)
	*/
	private java.util.Date privateDate;
	public final java.util.Date getDate()
	{
		return privateDate;
	}
	public final void setDate(java.util.Date value)
	{
		privateDate = value;
	}

	/** 
	 Returns a new instance of the command class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance
	*/
	public static DateCommand synchronousCommand()
	{
		DateCommand command;

		command = new DateCommand();
		command.setSynchronousCommandResponder(command);

		return command;
	}

	/** 
	 Returns a new instance of the command class that will execute synchronously (as its own responder)
	 
	 @param value The date to write
	 @return A new synchronous command instance
	*/
	public static DateCommand synchronousCommand(java.util.Date value)
	{
		DateCommand command;

		command = new DateCommand();
		command.setSynchronousCommandResponder(command);
		command.setDate(value);

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

		if (this.getDate() != null)
		{
			// [dateFormat setDateFormat:@"yyMMdd"];
			// [timeFormat setDateFormat:@"HH:mm:ss"];
			// line = [line stringByAppendingFormat:@"-s%@", [dateFormat stringFromDate:self.date]];
			DateFormat formatter = new SimpleDateFormat("yyMMdd", Constants.COMMAND_LOCALE);
			line.append("-s" + formatter.format(this.getDate()));
		}
	}

	/** 
	 Each correctly terminated line from the device is passed to this method for processing
	 
	 @param fullLine The line to be processed
	 @param header The response line header excluding the colon e.g. 'CS' for a command started response
	 @param value The response line following the colon e.g. '.iv'
	 @param moreAvailable When true indicates there are additional lines to be processed (and will also be passed to this method)
	 @return true if this line should NOT be passed to any other responder.
	 * @throws Exception 
	 
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
			if ("DA".equals(header))
			{
				DateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Constants.COMMAND_LOCALE);
				this.setDate(parser.parse(value.trim()));
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