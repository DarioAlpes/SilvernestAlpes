package com.uk.tsl.rfid.asciiprotocol.commands;


/**
 * A command to programmatically press the device switch
 */
public class SwitchSinglePressCommand extends SwitchPressCommand
{

	public SwitchSinglePressCommand() {
		super(".ps");
	}


	/**
	 * @return a synchronous command
	 */
	public static SwitchSinglePressCommand synchronousCommand()
	{
		SwitchSinglePressCommand command;
		command = new SwitchSinglePressCommand();
		command.setSynchronousCommandResponder(command);
		return command;
	}

	/**
	 * @param duration the duration of the single press
	 * @return a synchronous command
	 */
	public static SwitchSinglePressCommand synchronousCommand(int duration)
	{
		SwitchSinglePressCommand command;
		command = new SwitchSinglePressCommand();
		command.setSynchronousCommandResponder(command);
		command.setDuration(duration);
		return command;
	}

}
