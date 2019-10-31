package com.uk.tsl.rfid.asciiprotocol.commands;

import com.uk.tsl.rfid.asciiprotocol.Constants;
import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;

public abstract class SwitchPressUserActionCommand extends
		AsciiSelfResponderCommandBase {

	/**
	 * @return the current single press user action
	 */
	public String getValue()
	{
		return privateValue;
	}
	private String privateValue;
	/**
	 * @param value the new single press user action as a string
	 */
	public void setValue(String value)
	{
		privateValue = value;
	}
	
	/**
	 * @return the current single press user action
	 */
	public IAsciiCommand getActionCommand()
	{
		return privateActionCommand;
	}
	private IAsciiCommand privateActionCommand;
	/**
	 * @param command the new single press user action as a command
	 */
	public void setActionCommand(IAsciiCommand command)
	{
		privateActionCommand = command;
	}


	/**
	 * @param commandName the ASCII 2 command name - including '.'
	 * @param header the header response to capture e.g. 'SP' or 'DP'
	 */
	public SwitchPressUserActionCommand(String commandName, String header) {
		super(commandName);

		privateValue = null;
		privateActionCommand = null;
		mHeader = header;
	}

	private String mHeader;

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

		if (getActionCommand() != null)
		{
			line.append(String.format(Constants.COMMAND_LOCALE, "-s%s", getActionCommand().getCommandLine()));
		}

		if (getValue() != null)
		{
			line.append(String.format(Constants.COMMAND_LOCALE, "-s%s", getValue()));
		}
	}

	
	/** 
	 Each correctly terminated line from the device is passed to this method for processing
	 
	 @param fullLine The line to be processed
	 @param header The response line header excluding the colon e.g. 'CS' for a command started response
	 @param value The response line following the colon e.g. '.iv'
	 @param moreAvailable When true indicates there are additional lines to be processed (and will also be passed to this method)
	 @return 
	 Return true if this line should NOT be passed to any other responder.
	 * @throws Exception 
	 
	*/
	@Override
	protected boolean processReceivedLine(String fullLine, String header, String value, boolean moreAvailable) throws Exception
	{
		if (super.processReceivedLine(fullLine, header, value, moreAvailable))
		{
			return true;
		}
		else if (this.getResponseStarted())
		{
			if (mHeader.equals(header))
			{
				setValue(value);
			}
			else
			{
				return false;
			}

			this.appendToResponse(fullLine);
			return true;
		}

		return false;
	}

}
