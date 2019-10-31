package com.uk.tsl.rfid.asciiprotocol.commands;

import java.util.Locale;

import com.uk.tsl.rfid.asciiprotocol.Constants;
import com.uk.tsl.rfid.asciiprotocol.enumerations.DeleteConfirmation;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.rfid.asciiprotocol.parameters.CommandParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.ICommandParameters;
import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;

/** 
	ASCII command to manage the licence key on the currently connected reader
	A single licence key can be stored, read or deleted from the currently connected reader using this command
*/
public class LicenceKeyCommand extends AsciiSelfResponderCommandBase implements ICommandParameters
{
	/** 
	 Initializes a new instance of the SleepCommand class
	*/
	public LicenceKeyCommand()
	{
		super(".lk");
		CommandParameters.setDefaultParametersFor(this);

		mLicenceKey = null;
		mDeleteLicenceKey = DeleteConfirmation.NOT_SPECIFIED;
	}

	/** 
	 Returns a new instance of the LicenceKeyCommand class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance
	*/
	public static LicenceKeyCommand synchronousCommand()
	{
		LicenceKeyCommand command;

		command = new LicenceKeyCommand();
		command.setSynchronousCommandResponder(command);
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
	public final boolean implementsResetParameters() { return false; }

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


	///#region LicenceKeyCommand specific Members

	public static int maximumLicenceKeyLength()
	{
		return 255;
	}
	
	
	private String mLicenceKey;
	/**
	 * @return the licenceKey
	 */
	public final String getLicenceKey()
	{
		return mLicenceKey;
	}

	/**
	 * @param licenceKey the licenceKey to set
	 */
	public final void setLicenceKey(String licenceKey)
	{
		if( licenceKey != null && licenceKey.length() > maximumLicenceKeyLength() )
		{
			throw new IllegalArgumentException(String.format( Locale.US, "Licence key is too long (%d) - maximum length is %d characters",
					licenceKey,
					maximumLicenceKeyLength()
					));
		}
		mLicenceKey = licenceKey;
	}

	private DeleteConfirmation mDeleteLicenceKey;
	/**
	 * @return the deleteLicenceKey
	 */
	public final DeleteConfirmation getDeleteLicenceKey()
	{
		return mDeleteLicenceKey;
	}

	/**
	 * @param deleteLicenceKey the deleteLicenceKey to set
	 */
	public final void setDeleteLicenceKey(DeleteConfirmation deleteLicenceKey)
	{
		mDeleteLicenceKey = deleteLicenceKey;
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

		if( this.getLicenceKey() != null )
		{
			line.append(String.format(Constants.COMMAND_LOCALE, "-s\"%s\"", this.getLicenceKey()));
		}

		if( this.getDeleteLicenceKey() == DeleteConfirmation.YES )
		{
			line.append(String.format(Constants.COMMAND_LOCALE, "-d%s", getDeleteLicenceKey().getArgument()));
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
			return super.responseDidReceiveParameter(parameter);
		}

		return true;
	}


	/** 
	 Each correctly terminated line from the device is passed to this method for processing
	 
	 @param fullLine The line to be processed
	 @param header The response line header excluding the colon e.g. 'CS' for a command started response
	 @param value The response line following the colon e.g. '.iv'
	 @param moreAvailable When true indicates there are additional lines to be processed (and will also be passed to this method)
	 @return Return true if this line should NOT be passed to any other responder.
	 @throws Exception 
	 
	*/
	@Override
	protected boolean processReceivedLine(String fullLine, String header, String value, boolean moreAvailable) throws Exception
	{
		// Allow super class to detect the command start, messages, parameters and command end
		boolean superDidProcess;

		superDidProcess = super.processReceivedLine(fullLine, header, value, moreAvailable);

		if (superDidProcess)
		{
			// This line has been handled (no more processing needed)
			return true;
		}
		else
		{
			if ("LK".equals(header))
			{
				this.setLicenceKey(value);
			}
			else
			{
				// Not recognised so allow others to see it
				return false;
			}

			// This line has been handled (no more processing needed)
			this.appendToResponse(fullLine);
			return true;
		}
	}
}
