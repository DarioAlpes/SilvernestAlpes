package com.uk.tsl.rfid.asciiprotocol.commands;


public class SwitchDoublePressUserActionCommand extends
		SwitchPressUserActionCommand {

	public SwitchDoublePressUserActionCommand() {
		super(".dp", "DP");
	}

	/** 
	 Returns a new instance of the SwitchDoublePressUserActionCommand class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance
	*/
	public static SwitchDoublePressUserActionCommand synchronousCommand()
	{
		SwitchDoublePressUserActionCommand command;
		command = new SwitchDoublePressUserActionCommand();
		command.setSynchronousCommandResponder(command);
		return command;
	}

}
