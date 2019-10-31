package com.uk.tsl.rfid.asciiprotocol.commands;

import java.util.Locale;

import com.uk.tsl.rfid.asciiprotocol.Constants;
import com.uk.tsl.rfid.asciiprotocol.enumerations.SwitchAction;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.rfid.asciiprotocol.parameters.CommandParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.ICommandParameters;
import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;


/** 
 A command to set the action of the reader's switch
 The TSL_SwitchAction enum is used to specify values for the singlePressAction and doublePressAction parameters
 When issuing a command to the reader the values are interpreted as follows:
   TSL_SwitchAction_NOT_SPECIFIED - The value has not been specified and the parameter will not be sent
   TSL_SwitchAction_off - _'do nothing' has been specified and is sent as 'off'_
   TSL_SwitchAction_read - _'.rd' command has been specified and is sent as 'rd'_
   TSL_SwitchAction_write - _'.wr' command has been specified and is sent as 'wr'_
   TSL_SwitchAction_inventory - _'.iv' command has been specified and is sent as 'inv'_
   TSL_SwitchAction_barcode - _'.bc' command has been specified and is sent as 'bar'_
   TSL_SwitchAction_user - _a 'user-defined' command has been specified and is sent as 'usr'_
 When a command includes the '-p' option the singlePressAction and doublePressAction parameters will be set to the reader's current values 
*/
public class SwitchActionCommand extends AsciiSelfResponderCommandBase implements ICommandParameters
{
	/** 
	 Initializes a new instance of the SwitchActionCommand class
	*/
	public SwitchActionCommand()
	{
		super(".sa");

		CommandParameters.setDefaultParametersFor(this);
		
		privateAsynchronousReportingEnabled = TriState.NOT_SPECIFIED;
		privateSinglePressAction = SwitchAction.NO_CHANGE;
		privateDoublePressAction = SwitchAction.NO_CHANGE;
		privateSinglePressRepeatDelay = -1;
		privateDoublePressRepeatDelay = -1;
	}

	///#region ICommandParameters Members

	/** 
	 Gets a value indicating whether the implementing command uses the  ReadParameters property
	*/
	public final boolean implementsReadParameters()
	{
		return true;
	}

	/** 
	 Gets a value indicating whether the implementing command uses the  ResetParameters property
	*/
	public final boolean implementsResetParameters()
	{
		return true;
	}

	/** 
	 Gets a value indicating whether the implementing command uses the  TakeNoAction property
	*/
	public final boolean implementsTakeNoAction()
	{
		return false;
	}

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

	///#endregion

	/**
	 Gets or sets a value indicating whether asynchronous switch status reports should be reported.
	 When set to NotSpecified the asynchronous reporting state is unchanged.
	 If readParameters is specified then after execution this property will reflect the current state 
	 */
	private TriState privateAsynchronousReportingEnabled;
	public final TriState getAsynchronousReportingEnabled()
	{
		return privateAsynchronousReportingEnabled;
	}
	public final void setAsynchronousReportingEnabled(TriState value)
	{
		privateAsynchronousReportingEnabled = value;
	}

	/**
	 Gets or sets a value indicating whether haptic feedback is used.
	 When set to NotSpecified the state is unchanged.
	 If readParameters is specified then after execution this property will reflect the current state
	 */
	private TriState privateHapticFeedbackEnabled;
	public final TriState getHapticFeedbackEnabled()
	{
		return privateHapticFeedbackEnabled;
	}
	public final void setHapticFeedbackEnabled(TriState value)
	{
		privateHapticFeedbackEnabled = value;
	}

	/**
	 Gets or sets the action to perform for a single press of trigger
	*/
	private SwitchAction privateSinglePressAction = SwitchAction.getValues()[0];
	public final SwitchAction getSinglePressAction()
	{
		return privateSinglePressAction;
	}
	public final void setSinglePressAction(SwitchAction value)
	{
		privateSinglePressAction = value;
	}

	/** 
	 Gets or sets the action to perform for a double press of trigger
	*/
	private SwitchAction privateDoublePressAction = SwitchAction.getValues()[0];
	public final SwitchAction getDoublePressAction()
	{
		return privateDoublePressAction;
	}
	public final void setDoublePressAction(SwitchAction value)
	{
		privateDoublePressAction = value;
	}

	/**
	 * Gets or Sets the repeat delay for the trigger single press
	 */
	private int privateSinglePressRepeatDelay;
	public final int getSinglePressRepeatDelay() {
		return privateSinglePressRepeatDelay;
	}

	public final void setSinglePressRepeatDelay(int singlePressRepeatDelay) {
		
		if( singlePressRepeatDelay < minimumRepeatDelay() || singlePressRepeatDelay > maximumRepeatDelay() )
		{
			throw new IllegalArgumentException(String.format( Locale.US,
					"Single Press repeat delay (%d) is not in the range of %d to %d milliseconds",
					singlePressRepeatDelay,
					minimumRepeatDelay(),
					maximumRepeatDelay()
					));
		}
		privateSinglePressRepeatDelay = singlePressRepeatDelay;
	}

	/**
	 * Gets or Sets the repeat delay for the trigger double press
	 */
	private int privateDoublePressRepeatDelay;
	public final int getDoublePressRepeatDelay() {
		return privateDoublePressRepeatDelay;
	}

	public final void setDoublePressRepeatDelay(int doublePressRepeatDelay) {
		
		if( doublePressRepeatDelay < minimumRepeatDelay() || doublePressRepeatDelay > maximumRepeatDelay() )
		{
			throw new IllegalArgumentException(String.format( Locale.US,
					"Double Press repeat delay (%d) is not in the range of %d to %d milliseconds",
					doublePressRepeatDelay,
					minimumRepeatDelay(),
					maximumRepeatDelay()
					));
		}
		privateDoublePressRepeatDelay = doublePressRepeatDelay;
	}

	/**
	 * @return The minimum valid trigger press repeat delay
	 */
	public static int minimumRepeatDelay() {
		return 1;
	}
	
	/**
	 * @return The maximum valid trigger press repeat delay
	 */
	public static int maximumRepeatDelay() {
		return 999;
	}
	
	/** 
	 Returns a new instance of the SwitchActionCommand class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance
	*/
	public static SwitchActionCommand synchronousCommand()
	{
		SwitchActionCommand command;
		command = new SwitchActionCommand();
		command.setSynchronousCommandResponder(command);
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
		CommandParameters.appendToCommandLine(this, line);

		if (this.getAsynchronousReportingEnabled() != TriState.NOT_SPECIFIED)
		{
			line.append(String.format(Constants.COMMAND_LOCALE, "-a%s", this.getAsynchronousReportingEnabled().getArgument()));
		}

		if (this.getSinglePressAction() != SwitchAction.NO_CHANGE)
		{
			line.append(String.format(Constants.COMMAND_LOCALE, "-s%s", this.getSinglePressAction().getArgument()));
		}

		if (this.getDoublePressAction() != SwitchAction.NO_CHANGE)
		{
			line.append(String.format(Constants.COMMAND_LOCALE, "-d%s", this.getDoublePressAction().getArgument()));
		}

		if( getSinglePressRepeatDelay() > 0 )
		{
			line.append(String.format(Constants.COMMAND_LOCALE, "-rs%d", getSinglePressRepeatDelay()));
		}

		if( getDoublePressRepeatDelay() > 0 )
		{
			line.append(String.format(Constants.COMMAND_LOCALE, "-rd%d", getDoublePressRepeatDelay()));
		}

		if (this.getHapticFeedbackEnabled() != TriState.NOT_SPECIFIED)
		{
			line.append(String.format(Constants.COMMAND_LOCALE, "-h%s", this.getHapticFeedbackEnabled().getArgument()));
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
			if (parameter.length() > 1 )
			{
				if (parameter.startsWith("a"))
				{
					setAsynchronousReportingEnabled(TriState.Parse(parameter.substring(1)));
				}
				else if (parameter.startsWith("s"))
				{
					setSinglePressAction(SwitchAction.Parse(parameter.substring(1)));
				}
				else if (parameter.startsWith("d"))
				{
					setDoublePressAction(SwitchAction.Parse(parameter.substring(1)));
				}
				else if (parameter.startsWith("h"))
				{
					setHapticFeedbackEnabled(TriState.Parse(parameter.substring(1)));
				}
				else if (parameter.length() > 2 && parameter.startsWith("r") )
				{
					if (parameter.startsWith("s", 1))
					{
						setSinglePressRepeatDelay(Integer.parseInt(String.format(Constants.COMMAND_LOCALE, parameter.substring(2).trim())));
					}
					else if (parameter.startsWith("d", 1))
					{
						setDoublePressRepeatDelay(Integer.parseInt(String.format(Constants.COMMAND_LOCALE, parameter.substring(2).trim())));
					}
					else
					{
						return super.responseDidReceiveParameter(parameter);
					}
				}
				else
				{
					return super.responseDidReceiveParameter(parameter);
				}
			}
			else
			{
				return super.responseDidReceiveParameter(parameter);
			}
		}

		return true;
	}


}