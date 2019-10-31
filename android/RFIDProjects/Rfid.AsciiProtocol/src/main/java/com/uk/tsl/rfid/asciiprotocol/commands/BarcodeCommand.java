package com.uk.tsl.rfid.asciiprotocol.commands;

import java.util.Locale;

import com.uk.tsl.rfid.asciiprotocol.Constants;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.rfid.asciiprotocol.parameters.CommandParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.ICommandParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.IResponseParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.ResponseParameters;
import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;
import com.uk.tsl.rfid.asciiprotocol.responders.IBarcodeReceivedDelegate;
import com.uk.tsl.utils.StringHelper;


//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------


/** 
 A command to read a barcode
*/
public class BarcodeCommand extends AsciiSelfResponderCommandBase implements ICommandParameters, IResponseParameters
{
	/** 
	 Backing field for ScanTime
	*/
	private int scanTime;

	/**
	 * The delegate/listener for received transponders
	 */
	private IBarcodeReceivedDelegate mBarcodeReceivedDelegate;
	public final IBarcodeReceivedDelegate getBarcodeReceivedDelegate()
	{
		return mBarcodeReceivedDelegate;
	}

	public final void setBarcodeReceivedDelegate(IBarcodeReceivedDelegate delegate)
	{
		mBarcodeReceivedDelegate = delegate;
	}
	
	/** 
	 Initializes a new instance of the BarcodeCommand class
	*/
	public BarcodeCommand()
	{
		super(".bc");
		CommandParameters.setDefaultParametersFor(this);
		ResponseParameters.setDefaultParametersFor(this);

		// Default to long synchronous wait time
		// This will be changed IF a scan time is specified
		this.setMaxSynchronousWaitTime(11.0);

		mBarcodeReceivedDelegate = null;
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

	/// IResponseParameters Members

	/** 
	 Gets or sets a value indicating whether DateTime stamps appear in reader responses
	*/
	private TriState privateIncludeDateTime;
	public final TriState getIncludeDateTime()
	{
		return privateIncludeDateTime;
	}
	public final void setIncludeDateTime(TriState value)
	{
		privateIncludeDateTime = value;
	}

	/** 
	 Gets or sets a value indicating whether alerts are enabled for the executing commands
	*/
	private TriState privateUseAlert;
	public final TriState getUseAlert()
	{
		return privateUseAlert;
	}
	public final void setUseAlert(TriState value)
	{
		privateUseAlert = value;
	}



	/** 
	 Gets the data received from the barcode scan.
	*/
	private String privateData;
	public final String getData()
	{
		return privateData;
	}
	private void setData(String value)
	{
		privateData = value;
	}

	/** 
	 Gets or sets the scan duration in seconds (1-9) 
	*/
	public final int getScanTime()
	{
		return this.scanTime;
	}

	public final void setScanTime(int value)
	{
		if ((value < 1) || (value > 9))
		{
			throw new IllegalArgumentException(String.format( Locale.US, "Scan time (%d) is not in the range of 1 to 9 seconds", value));
		}

		this.scanTime = value;
		this.setMaxSynchronousWaitTime( this.getScanTime() + 2);
	}

	/** 
	 Gets or sets a value indicating whether to escape the barcode
	 
	 
	 Barcode strings can contain any ASCII character including CrLf line ends that may confuse the parses.
	 Set this property to Yes to escape the barcode string
	 
	*/
	private TriState privateUseEscapeCharacter;
	public final TriState getUseEscapeCharacter()
	{
		return privateUseEscapeCharacter;
	}
	public final void setUseEscapeCharacter(TriState value)
	{
		privateUseEscapeCharacter = value;
	}

	/** 
	 Returns a new instance of the command class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance
	*/
	public static BarcodeCommand synchronousCommand()
	{
		BarcodeCommand command;

		command = new BarcodeCommand();
		command.setSynchronousCommandResponder(command);
		return command;
	}

	/** 
	 Returns a synchronous TSLBarcodeCommand for the given scan timeout
	 
	 @param timeout timeout the timeout in seconds (1-9)  
	 @return A new synchronous command instance
	*/
	public static BarcodeCommand synchronousCommand(int timeout)
	{
		BarcodeCommand command;
		command = new BarcodeCommand();
		command.setSynchronousCommandResponder(command);
		command.setScanTime(timeout);
		return command;
	}

	/** 
	 Clears the response ready to receive a new one
	*/
	@Override
	public void clearLastResponse()
	{
		super.clearLastResponse();
		this.setData("");
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

		// line = [line stringByAppendingString: [TSLResponseParameters commandLine:self]];
		ResponseParameters.appendToCommandLine(this, line);

		if (this.getScanTime() >= 1 && this.getScanTime() <= 9)
		{
			// line = [line stringByAppendingFormat:@"-t%d", self.scanTime];
			line.append(String.format(Constants.COMMAND_LOCALE, "-t%d", this.getScanTime()));
		}

		if (this.getUseEscapeCharacter() != TriState.NOT_SPECIFIED)
		{
			// line = [line stringByAppendingFormat:@"-e%@", [TSLTriState parameterValueForState:self.useEscapeCharacter]];
			line.append(String.format(Constants.COMMAND_LOCALE, "-e%s", this.getUseEscapeCharacter().getArgument()));
		}
	}

	/** 
	 The is called when the responder received a line with an OK: or an ER: header. i.e. the command is complete
	 
	 @param async True if the command finished asynchronously
	*/
	@Override
	protected void responseDidFinish(boolean async)
	{
		super.responseDidFinish(async);

		this.informDelegateResponseDidComplete();
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
		if (!ResponseParameters.parseParameterFor(this, parameter))
		{
			if (!CommandParameters.parseParameterFor(this, parameter))
			{
				// Check for barcode specific command parameters
				if (!StringHelper.isNullOrEmpty(parameter))
				{
					switch (parameter.charAt(0))
					{
						case 'e':
						{
								this.setUseEscapeCharacter(TriState.Parse(parameter.substring(1)));
								return true;
						}

						case 't':
						{
								this.setScanTime(Integer.parseInt(String.format(Constants.COMMAND_LOCALE, parameter.substring(1).trim())));
								return true;
						}

						default:
							break;
					}
				}
				else
				{
					return super.responseDidReceiveParameter(parameter);
				}
			}
		}

		return true;
	}

	/** 
	 Each correctly terminated line from the device is passed to this method for processing
	 
	 @param fullLine The line to be processed
	 @param header The response line header excluding the colon e.g. 'CS' for a command started response
	 @param value The response line following the colon e.g. '.iv'
	 @param moreAvailable When true indicates there are additional lines to be processed (and will also be passed to this method)
	 @return 
	 Return true if this line should NOT be passed to any other responder.
	 * @throws Exception 
	 
	*/
	@Override
	protected boolean processReceivedLine(String fullLine, String header, String value, boolean moreAvailable) throws Exception
	{
		if (super.processReceivedLine(fullLine, header, value, moreAvailable))
		{
			return true;
		}
		else if (this.getResponseStarted())
		{
			if ("OK".equals(header))
			{
				this.informDelegateResponseDidComplete();
			}
			else if ("ER".equals(header))
			{
				this.informDelegateResponseDidComplete();
			}
			else if ("BR".equals(header))
			{
				this.setData(value);
			}
			else if ("BC".equals(header))
			{
				this.setData(convertEscapeSequences(value));
			}
			else
			{
				return false;
			}

			this.appendToResponse(fullLine);
			return true;
		}

		return false;
	}

	//
	// Converts any escaped characters to their non-escaped versions in the given string 
	//
	private String convertEscapeSequences(String source)
	{
        String result= source.replace(Constants.BARCODE_ESCAPE_CHARACTER +"\r", "\r");
        result= result.replace(Constants.BARCODE_ESCAPE_CHARACTER + "\n", "\n");
        result= result.replace(Constants.BARCODE_ESCAPE_CHARACTER + Constants.BARCODE_ESCAPE_CHARACTER, Constants.BARCODE_ESCAPE_CHARACTER);
        if(result.length() > 0 && result.charAt(result.length()-1) == Constants.BARCODE_ESCAPE_CHARACTER.charAt(0))
        {
        	result = result.substring(0, result.length()-1);
        }
        return result;
	}

	/** 
	 Notifies the delegate of the barcode data received
	*/
	private void informDelegateResponseDidComplete()
	{
		if( mBarcodeReceivedDelegate != null && !StringHelper.isNullOrEmpty(this.getData()))
		{
			// Inform listener of new barcode
			mBarcodeReceivedDelegate.barcodeReceived(this.getData());
		}
	}
}