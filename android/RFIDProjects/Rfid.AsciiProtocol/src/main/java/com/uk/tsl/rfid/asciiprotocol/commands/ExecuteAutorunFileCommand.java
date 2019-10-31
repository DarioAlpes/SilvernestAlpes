package com.uk.tsl.rfid.asciiprotocol.commands;

import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;

public class ExecuteAutorunFileCommand extends AsciiSelfResponderCommandBase {

	public ExecuteAutorunFileCommand() {
		super(".ea");
	}

	/** 
	 Returns a new instance of the ExecuteAutorunFileCommand class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance
	*/
	public static ExecuteAutorunFileCommand synchronousCommand()
	{
		ExecuteAutorunFileCommand command;
		command = new ExecuteAutorunFileCommand();
		command.setSynchronousCommandResponder(command);
		return command;
	}

	

}
