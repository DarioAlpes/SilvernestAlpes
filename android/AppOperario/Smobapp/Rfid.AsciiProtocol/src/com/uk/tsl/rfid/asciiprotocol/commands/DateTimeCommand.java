package com.uk.tsl.rfid.asciiprotocol.commands;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.uk.tsl.rfid.asciiprotocol.Constants;
import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 A command to obtain or set both date and time of the reader's real-time clock 
*/
public class DateTimeCommand extends AsciiSelfResponderCommandBase
{
	/** 
	 Holds the response to the first command issued to the reader (the date or the time command
	*/
	private java.util.ArrayList<String> partialResponse;

	/** 
	 Initializes a new instance of the DateTimeCommand class
	*/
	public DateTimeCommand()
	{
		super(".da");
	}

	/** 
	 Gets or sets the date and time to write to or read from the reader. 
	 Set to null to read the date and time from the reader
	 Set to a value to update the date and time
	*/
	private java.util.Date privateValue;
	public final java.util.Date getValue()
	{
		return privateValue;
	}
	public final void setValue(java.util.Date value)
	{
		privateValue = value;
	}

	/** 
	 Returns a new instance of the command class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance to read the date and time
	*/
	public static DateTimeCommand synchronousCommand()
	{
		DateTimeCommand command;
		command = new DateTimeCommand();
		command.setSynchronousCommandResponder(command);
		return command;
	}

	/** 
	 Returns a new instance of the command class that will execute synchronously (as its own responder)
	 
	 @param value The date time to set the reader to
	 @return A new synchronous command instance
	*/
	public static DateTimeCommand synchronousCommand(java.util.Date value)
	{
		DateTimeCommand command;
		command = new DateTimeCommand();
		command.setSynchronousCommandResponder(command);
		command.setValue(value);
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
		// Construct the TWO commands required to set both date and time

		// start by appending a command with .da to set the date
		super.buildCommandLine(line);

		if (this.getValue() != null)
		{
			// [dateFormat setDateFormat:@"yyMMdd"];
			// dateCL = [dateCL stringByAppendingFormat:@"-s%@", [dateFormat stringFromDate:self.date]];
			DateFormat formatter = new SimpleDateFormat("yyMMdd", Constants.COMMAND_LOCALE);
			line.append(formatter.format(this.getValue()));
		}

		// dateCL = [dateCL stringByAppendingFormat:@"\r\n.tm%@ ", TSL_LibraryCommandId];
		line.append("\r\n");

		// set to append a time command
		this.setCommandName(".tm");
		super.buildCommandLine(line);

		// restore to a date command to receive it
		this.setCommandName(".da");

		if (this.getValue() != null)
		{
			// [dateFormat setDateFormat:@"HHmmss"];
			// dateCL = [dateCL stringByAppendingFormat:@"-s%@", [dateFormat stringFromDate:self.date]];
			DateFormat formatter = new SimpleDateFormat("HHmmss", Constants.COMMAND_LOCALE);
			line.append(formatter.format(this.getValue()));
		}

		// dateCL = [dateCL stringByAppendingString:@"\r\n"];
		line.append("\r\n");
	}

	// Holders for the partial response received
	private String mtimeValue;
	private String mDateValue;

	/** 
	 The is called when the responder received a line with an OK: or an ER: header. i.e. the command is complete
	 
	 @param async True if the command finished asynchronously
	*/
	@Override
	protected void responseDidFinish(boolean async)
	{
		if (".da".equals(this.getCommandName()))
		{
			// First part of the command has completed
			if (this.isSuccessful())
			{
				// Preserve response so far
				this.partialResponse = new java.util.ArrayList<String>();
				this.partialResponse.addAll(this.getResponse());

				// Set responder to handle  second
				this.setCommandName(".tm");
			}
			else
			{
				// Command failed so abort operation
				// Allow the synchronous command to complete
				super.responseDidFinish(async);
			}
		}
		else
		{
			this.setCommandName(".da");

			// Combine the responses
			this.partialResponse.addAll(this.getResponse());
			this.setResponse(this.partialResponse);

			// Combine the results into a single Date
			DateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Constants.COMMAND_LOCALE);
			String value = mDateValue + " " + mtimeValue;
			try {
				setValue(parser.parse(value));
			} catch (ParseException e) {
				setValue(null);
				this.setIsSuccessful(false);
			}


			// Allow the synchronous command to complete        
			super.responseDidFinish(async);
		}
	}

	@Override
	protected void responseDidStart()
	{
		if (".da".equals(this.getCommandName()))
		{
			super.responseDidStart();

			mDateValue = null;
			mtimeValue = null;
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
				mDateValue = value;
			}
			if ("TM".equals(header))
			{
				mtimeValue = value;
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