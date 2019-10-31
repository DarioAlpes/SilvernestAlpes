package com.uk.tsl.rfid.asciiprotocol.commands;

import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;

/**
 * A command to write commands to the device's autorun file
 * 
 * This command does not use the library marker and command index because they
 * would be interpreted as part of the command to be added to the autorun file.
 * So, by default, this command will capture non-library responses to allow
 * synchronous commands to execute correctly.
 *  
 */
public class WriteAutorunFileCommand extends AsciiSelfResponderCommandBase {

	/**
	 * @return the current autorun command line
	 */
	public String getValue()
	{
		return privateValue;
	}
	private String privateValue;
	/**
	 * @param value the new autorun command line as a string
	 */
	public void setValue(String value)
	{
		privateValue = value;
	}
	
	/**
	 * @return the current autorun command line as an AsciiCommand
	 */
	public IAsciiCommand getAutorunCommand()
	{
		return privateAutorunCommand;
	}
	private IAsciiCommand privateAutorunCommand;
	/**
	 * @param value the new autorun command line as an AsciiCommand
	 */
	public void setAutorunCommand(IAsciiCommand command)
	{
		privateAutorunCommand = command;
	}


	public WriteAutorunFileCommand() {
		super(".wa");

		setCaptureNonLibraryResponses(true);

		privateValue = null;
		privateAutorunCommand = null;
	}

	
	/** 
	 Returns a new instance of the WriteAutorunFileCommand class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance
	*/
	public static WriteAutorunFileCommand synchronousCommand()
	{
		WriteAutorunFileCommand command;
		command = new WriteAutorunFileCommand();
		command.setSynchronousCommandResponder(command);
		return command;
	}

	
	/** 
	 Builds the command line to send to the reader to execute the command
	 
	*/
	@Override
	public String getCommandLine()
	{
		// Replace the default command line build to prevent the library command marker being added to the autorun file
		String line = getCommandName();
		String autorunCommandLine = "";

		if( getAutorunCommand() != null) {
			autorunCommandLine = getAutorunCommand().getCommandLine();
		} else if( getValue() != null) {
			autorunCommandLine = getValue();
		}
		line += " " + autorunCommandLine;
		
		return line;
	}

	
	
}
