package com.uk.tsl.rfid.asciiprotocol.commands;


public class SwitchSinglePressUserActionCommand extends
		SwitchPressUserActionCommand {

	public SwitchSinglePressUserActionCommand() {
		super(".sp", "SP");

	}


	/** 
	 Returns a new instance of the SwitchSinglePressUserActionCommand class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance
	*/
	public static SwitchSinglePressUserActionCommand synchronousCommand()
	{
		SwitchSinglePressUserActionCommand command;
		command = new SwitchSinglePressUserActionCommand();
		command.setSynchronousCommandResponder(command);
		return command;
	}

}
