package com.uk.tsl.rfid.asciiprotocol.commands;


/**
 * A command to programmatically press the device switch
 */
public class SwitchDoublePressCommand extends SwitchPressCommand
{

	public SwitchDoublePressCommand() {
		super(".pd");
	}


	/**
	 * @return a synchronous command
	 */
	public static SwitchDoublePressCommand synchronousCommand()
	{
		SwitchDoublePressCommand command;
		command = new SwitchDoublePressCommand();
		command.setSynchronousCommandResponder(command);
		return command;
	}

	/**
	 * @param duration the duration of the single press
	 * @return a synchronous command
	 */
	public static SwitchDoublePressCommand synchronousCommand(int duration)
	{
		SwitchDoublePressCommand command;
		command = new SwitchDoublePressCommand();
		command.setSynchronousCommandResponder(command);
		command.setDuration(duration);
		return command;
	}

}
