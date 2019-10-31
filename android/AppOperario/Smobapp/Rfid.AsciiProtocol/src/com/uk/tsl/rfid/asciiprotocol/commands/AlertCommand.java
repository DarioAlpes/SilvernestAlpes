package com.uk.tsl.rfid.asciiprotocol.commands;

import com.uk.tsl.rfid.asciiprotocol.Constants;
import com.uk.tsl.rfid.asciiprotocol.enumerations.AlertDuration;
import com.uk.tsl.rfid.asciiprotocol.enumerations.BuzzerTone;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.rfid.asciiprotocol.parameters.CommandParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.ICommandParameters;
import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;


/** 
 A command to configure and query the reader's alert options.

 Enable or disable the buzzer with the EnableBuzzer property.
 Enable or disable the vibrator with the EnableVibrator property.
 Set the duration of the alert with the Duration property.
 Set the buzzer tone with the Tone property.
 Query the current settings with the ReadParameters property.
 To suppress the alert action use the TakeNoAction property.
 To reset the parameters to default use the ResetParameters property.
*/
public class AlertCommand extends AsciiSelfResponderCommandBase implements ICommandParameters
{
	/** 
	 Initializes a new instance of the AlertCommand class
	*/
	public AlertCommand()
	{
		super(".al");
		CommandParameters.setDefaultParametersFor(this);

		setDuration(AlertDuration.NOT_SPECIFIED);
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
		return true;
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


	/** 
	 Gets or sets duration for which the buzzer and or vibration will activate
	*/
	private AlertDuration privateDuration = AlertDuration.getValues()[0];
	public final AlertDuration getDuration()
	{
		return privateDuration;
	}
	public final void setDuration(AlertDuration value)
	{
		privateDuration = value;
	}

	/** 
	 Gets or sets a value indicating whether the buzzer is enabled
	*/
	private TriState privateEnableBuzzer;
	public final TriState getEnableBuzzer()
	{
		return privateEnableBuzzer;
	}
	public final void setEnableBuzzer(TriState value)
	{
		privateEnableBuzzer = value;
	}

	/** 
	 Gets or sets a value indicating wheterh the vibrator is enabled
	*/
	private TriState privateEnableVibrator;
	public final TriState getEnableVibrator()
	{
		return privateEnableVibrator;
	}
	public final void setEnableVibrator(TriState value)
	{
		privateEnableVibrator = value;
	}

	/** 
	 Gets or sets the Buzzer tone. 
	*/
	private BuzzerTone privateTone = BuzzerTone.getValues()[0];
	public final BuzzerTone getTone()
	{
		return privateTone;
	}
	public final void setTone(BuzzerTone value)
	{
		privateTone = value;
	}

	/** 
	 Returns a new instance of the AlertCommand class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance
	*/
	public static AlertCommand synchronousCommand()
	{
		AlertCommand command;

		command = new AlertCommand();
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

		// line = [line stringByAppendingString: [TSLCommandParameters commandLine:self]];
		CommandParameters.appendToCommandLine(this, line);

		if (this.getEnableBuzzer() != TriState.NOT_SPECIFIED)
		{
			line.append(String.format(Constants.COMMAND_LOCALE, "-b%s", this.getEnableBuzzer().getArgument()));
		}

		if (this.getEnableVibrator() != TriState.NOT_SPECIFIED)
		{
			line.append(String.format(Constants.COMMAND_LOCALE, "-v%s", this.getEnableVibrator().getArgument()));
		}

		if (this.getDuration() != AlertDuration.NOT_SPECIFIED)
		{
			line.append(String.format(Constants.COMMAND_LOCALE, "-d%s", this.getDuration().getArgument()));
		}

		if (this.getTone() != BuzzerTone.NOT_SPECIFIED)
		{
			line.append(String.format(Constants.COMMAND_LOCALE, "-t%s", this.getTone().getArgument()));
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
			// Check for barcode specific command parameters
			if (parameter.length() > 1)
			{
				switch (parameter.charAt(0))
				{
					case 'b':
					{
							this.setEnableBuzzer(TriState.Parse(parameter.substring(1)));
							return true;
					}

					case 'd':
					{
							this.setDuration(AlertDuration.Parse(parameter.substring(1)));
							return true;
					}

					case 't':
					{
							this.setTone(BuzzerTone.Parse(parameter.substring(1)));
							return true;
					}

					case 'v':
					{
							this.setEnableVibrator(TriState.Parse(parameter.substring(1)));
							return true;
					}

					default:
						return false;
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