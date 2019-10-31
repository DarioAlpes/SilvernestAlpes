package com.uk.tsl.rfid.asciiprotocol.commands;

import java.util.Locale;

import com.uk.tsl.rfid.asciiprotocol.Constants;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.rfid.asciiprotocol.parameters.CommandParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.ICommandParameters;
import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;
import com.uk.tsl.utils.StringHelper;

//-----------------------------------------------------------------------
//Copyright (c) 2014 Technology Solutions UK Ltd. All rights reserved. 
//
//Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------


public class SleepTimeoutCommand extends AsciiSelfResponderCommandBase implements ICommandParameters
{
	/** 
	 Initializes a new instance of the SleepCommand class
	*/
	public SleepTimeoutCommand()
	{
		super(".st");
		CommandParameters.setDefaultParametersFor(this);
	}

	/** 
	 Returns a new instance of the SleepCommand class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance
	*/
	public static SleepTimeoutCommand synchronousCommand()
	{
		SleepTimeoutCommand command;

		command = new SleepTimeoutCommand();
		command.setSynchronousCommandResponder(command);
		return command;
	}

	/**
	 * Returns a new instance of the SleepCommand class that will execute synchronously (as its own responder)
	 * with the given duration set
	 * @param duration the sleep timeout duration to set
	 * @return A new synchronous command instance
	 */
	public static SleepTimeoutCommand synchronousCommand(int duration)
	{
		SleepTimeoutCommand command;

		command = new SleepTimeoutCommand();
		command.setSynchronousCommandResponder(command);
		command.setDuration(duration);
		return command;
	}
	
	///#region ICommandParameters Members

	/** 
	 Gets a value indicating whether the implementing command uses the  ReadParameters property
	*/
	public final boolean implementsReadParameters() { return true; }

	/** 
	 Gets a value indicating whether the implementing command uses the  ResetParameters property
	*/
	public final boolean implementsResetParameters() { return true; }

	/** 
	 Gets a value indicating whether the implementing command uses the  TakeNoAction property
	*/
	public final boolean implementsTakeNoAction() {	return false; }

	/** 
	 Gets or sets a value indicating whether the command should include a list of supported parameters and their current values
	*/
	private TriState privateReadParameters;
	public final TriState getReadParameters()
	{
		return privateReadParameters;
	}
	public final void setReadParameters(TriState value)
	{
		privateReadParameters = value;
	}

	/** 
	 Gets or sets a value indicating whether the command should reset all its parameters to default values
	*/
	private TriState privateResetParameters;
	public final TriState getResetParameters()
	{
		return privateResetParameters;
	}
	public final void setResetParameters(TriState value)
	{
		privateResetParameters = value;
	}

	/** 
	 Gets or sets a value indicating whether the command primary action should not be performed
	 (e.g. InventoryCommand will not perform the inventory action)
	 All other actions, such as setting parameters in the reader are performed
	*/
	private TriState privateTakeNoAction;
	public final TriState getTakeNoAction()
	{
		return privateTakeNoAction;
	}
	public final void setTakeNoAction(TriState value)
	{
		privateTakeNoAction = value;
	}

	///#region SleepTimeout specific parameters

	/** 
	 Gets or sets the reader sleep timeout duration in seconds
	 Valid values are between getMinimumDuration() and getMaximumDuration()
	*/
	private int mDuration;
	public final int getDuration()
	{
		return mDuration;
	}

	public final void setDuration(int duration)
	{
		if ((duration < getMinimumDuration()) || (duration > getMaximumDuration()))
		{
			throw new IllegalArgumentException(String.format( Locale.US, "Duration (%d) is not in the range of %d to %d seconds",
					duration,
					getMinimumDuration(),
					getMaximumDuration()
					));
		}

		mDuration = duration;
	}

	/**
	 * @return The minimum valid duration for the SleepTimeoutCommand
	 */
	public static final int getMinimumDuration()
	{
		return 15;
	}

	/**
	 * @return The maximum valid duration for the SleepTimeoutCommand
	 */
	public static final int getMaximumDuration()
	{
		return 999;
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

		CommandParameters.appendToCommandLine(this, line);

		if (this.getDuration() >= getMinimumDuration() && this.getDuration() <= getMaximumDuration())
		{
			line.append(String.format(Constants.COMMAND_LOCALE, "-t%d", this.getDuration()));
		}
	}

	/** 
	 This method is called for each parameter in the parameters (PR:) list. 
	 Returns true if the parameter was handled or false otherwise.
	 Derived classes can override this method to extract individual parameters from the PR: line
	 
	 @param parameter
	 A single parameter extracted from the PR: response, excluding the '-' and trimmed of leading and trailing whitespace
	 
	 @return Return true if the parameter was handled
	*/
	@Override
	protected boolean responseDidReceiveParameter(String parameter)
	{
		if (!CommandParameters.parseParameterFor(this, parameter))
		{
			// Check for specific command parameters
			if (!StringHelper.isNullOrEmpty(parameter))
			{
				switch (parameter.charAt(0))
				{
				case 't':
				{
					this.setDuration(Integer.parseInt(String.format(Constants.COMMAND_LOCALE, parameter.substring(1).trim())));
					return true;
				}

				default:
					return super.responseDidReceiveParameter(parameter);
				}
			}
		}

		return true;
	}


}
