package com.uk.tsl.rfid.asciiprotocol.commands;

import com.uk.tsl.rfid.asciiprotocol.enumerations.*;
import com.uk.tsl.rfid.asciiprotocol.parameters.*;
import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;
import com.uk.tsl.rfid.asciiprotocol.responders.ITransponderReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.TransponderData;
import com.uk.tsl.rfid.asciiprotocol.responders.TransponderResponder;


//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------
/**
 * 
 */

/** 
 ASCII command to perform an inventory
*/
public class InventoryCommand extends AsciiSelfResponderCommandBase
	implements IAntennaParameters, ICommandParameters, IQAlgorithmParameters,
	IQueryParameters, IResponseParameters, ISelectControlParameters, ISelectMaskParameters, ITransponderParameters,
	ITransponderReceivedDelegate
{

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

	///#region IAntennaParameters Members

	/** 
	 Gets or sets the output power
	 
	 
	 Valid power range is 10 - 29.
	 Use AntennaParameters.OutputPowerNotSpecified to read the output power.
	 
	*/
	private int privateOutputPower;
	public final int getOutputPower()
	{
		return privateOutputPower;
	}
	public final void setOutputPower(int value)
	{
		privateOutputPower = value;
	}

	///#region ITransponderParameters Members

	/**
	 Gets or sets the password required to access transponders
	*/
	private String privateAccessPassword;
	public final String getAccessPassword()
	{
		return privateAccessPassword;
	}
	public final void setAccessPassword(String value)
	{
		privateAccessPassword = value;
	}

	/**
	 Gets or sets a value indicating whether to include the EPC PC value in reader responses
	 */
	private TriState privateIncludeEpc;
	public final TriState getIncludeEpc()
	{
		return privateIncludeEpc;
	}
	public final void setIncludeEpc(TriState value)
	{
		privateIncludeEpc = value;
	}

	/**
	 Gets or sets a value indicating whether to include checksum information in reader responses
	*/
	private TriState privateIncludeChecksum;
	public final TriState getIncludeChecksum()
	{
		return privateIncludeChecksum;
	}
	public final void setIncludeChecksum(TriState value)
	{
		privateIncludeChecksum = value;
	}

	/** 
	 Gets or sets a value indicating whether to include index numbers for multiple values in reader responses
	*/
	private TriState privateIncludeIndex;
	public final TriState getIncludeIndex()
	{
		return privateIncludeIndex;
	}
	public final void setIncludeIndex(TriState value)
	{
		privateIncludeIndex = value;
	}

	/** 
	 Gets or sets a value indicating whether to include the EPC PC value in reader responses
	*/
	private TriState privateIncludePC;
	public final TriState getIncludePC()
	{
		return privateIncludePC;
	}
	public final void setIncludePC(TriState value)
	{
		privateIncludePC = value;
	}

	/** 
	 Gets or sets a value indicating whether to include RSSI value in reader responses
	*/
	private TriState privateIncludeTransponderRssi;
	public final TriState getIncludeTransponderRssi()
	{
		return privateIncludeTransponderRssi;
	}
	public final void setIncludeTransponderRssi(TriState value)
	{
		privateIncludeTransponderRssi = value;
	}

	///#region IResponseParameters Members

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

	///#region IQueryParameters Members

	/** 
	 Gets or sets the transponders to include based on the select flag state
	*/
	private QuerySelect privateQuerySelect;
	public final QuerySelect getQuerySelect()
	{
		return privateQuerySelect;
	}
	public final void setQuerySelect(QuerySelect value)
	{
		privateQuerySelect = value;
	}

	/** 
	 Gets or sets the transponders to include based on the select flag state
	*/
	private QuerySession privateQuerySession;
	public final QuerySession getQuerySession()
	{
		return privateQuerySession;
	}
	public final void setQuerySession(QuerySession value)
	{
		privateQuerySession = value;
	}

	/** 
	 Gets or sets the session state of the transponders to be included in this operation
	*/
	private QueryTarget privateQueryTarget;
	public final QueryTarget getQueryTarget()
	{
		return privateQueryTarget;
	}
	public final void setQueryTarget(QueryTarget value)
	{
		privateQueryTarget = value;
	}


	///#region IQAlgorithmParameters Members

	/** 
	 Gets or sets the Q algorithm type
	*/
	private QAlgorithm privateQAlgorithm;
	public final QAlgorithm getQAlgorithm()
	{
		return privateQAlgorithm;
	}
	public final void setQAlgorithm(QAlgorithm value)
	{
		privateQAlgorithm = value;
	}

	/** 
	 Gets or sets the Q value for fixed Q operations (0-15)
	*/
	private int privateQValue;
	public final int getQValue()
	{
		return privateQValue;
	}
	public final void setQValue(int value)
	{
		privateQValue = value;
	}

	///#region ISelectControlParameters Members

	/** 
	 Gets or sets a value indicating whether only the inventory is performed (the select operation is not performed before the inventory when set to Yes)
	*/
	private TriState privateInventoryOnly;
	public final TriState getInventoryOnly()
	{
		return privateInventoryOnly;
	}
	public final void setInventoryOnly(TriState value)
	{
		privateInventoryOnly = value;
	}

	/** 
	 Gets or sets the action to perform in the Select operation
	*/
	private SelectAction privateSelectAction;
	public final SelectAction getSelectAction()
	{
		return privateSelectAction;
	}
	public final void setSelectAction(SelectAction value)
	{
		privateSelectAction = value;
	}

	/** 
	 Gets or sets the Bank to use for the select mask
	*/
	private Databank privateSelectBank;
	public final Databank getSelectBank()
	{
		return privateSelectBank;
	}
	public final void setSelectBank(Databank value)
	{
		privateSelectBank = value;
	}

	/** 
	 Gets or sets the select mask data in 2 character ASCII Hex pairs padded to ensure full bytes
	*/
	private String privateSelectData;
	public final String getSelectData()
	{
		return privateSelectData;
	}
	public final void setSelectData(String value)
	{
		privateSelectData = value;
	}

	/** 
	 Gets or sets the length in bits of the select mask
	*/
	private int privateSelectLength;
	public final int getSelectLength()
	{
		return privateSelectLength;
	}
	public final void setSelectLength(int value)
	{
		privateSelectLength = value;
	}

	/** 
	 Gets or sets the number of bits from the start of the block to the start of the select mask
	*/
	private int privateSelectOffset;
	public final int getSelectOffset()
	{
		return privateSelectOffset;
	}
	public final void setSelectOffset(int value)
	{
		privateSelectOffset = value;
	}

	/** 
	 Gets or sets the target flag for the Select operation
	*/
	private SelectTarget privateSelectTarget;
	public final SelectTarget getSelectTarget()
	{
		return privateSelectTarget;
	}
	public final void setSelectTarget(SelectTarget value)
	{
		privateSelectTarget = value;
	}

	///#region Unique Members

	/**
	 * The delegate/listener for received transponders
	 */
	private ITransponderReceivedDelegate mTransponderReceivedDelegate;
	public final ITransponderReceivedDelegate getTransponderReceivedDelegate()
	{
		return mTransponderReceivedDelegate;
	}

	public final void setTransponderReceivedDelegate(ITransponderReceivedDelegate listener)
	{
		mTransponderReceivedDelegate = listener;
	}
	

	/** 
	 * Gets or sets a value indicating whether Monza tag focus is used
	 * (Impinj Specific)
	*/
	private TriState privateUseTagFocus;
	public final TriState getUseTagFocus()
	{
		return privateUseTagFocus;
	}
	public final void setUseTagFocus(TriState value)
	{
		privateUseTagFocus = value;
	}


	/** 
	 * Gets or sets a value indicating whether Monza Fast Id is used
	 * (Impinj Specific)
	*/
	private TriState privateUseFastId;
	public final TriState getUsefastId()
	{
		return privateUseFastId;
	}
	public final void setUsefastId(TriState value)
	{
		privateUseFastId = value;
	}


	/**
	 Gets or sets a value indicating if "filter to strongest tag" is performed
	 */
	private TriState privateFilterStrongest;
	public final TriState getFilterStrongest()
	{
		return privateFilterStrongest;
	}
	public final void setFilterStrongest(TriState value)
	{
		privateFilterStrongest = value;
	}


	// Each transponder response is handled by this TransponderResponder
	private TransponderResponder transponderResponder;
	
	/** 
	 Initializes a new instance of the InventoryCommand class
	*/
	public InventoryCommand()
	{
		super(".iv");

		AntennaParameters.setDefaultParametersFor(this);
		CommandParameters.setDefaultParametersFor(this);
		QAlgorithmParameters.setDefaultParametersFor(this);
		QueryParameters.setDefaultParametersFor(this);
		ResponseParameters.setDefaultParametersFor(this);
		SelectControlParameters.setDefaultParametersFor(this);
		SelectMaskParameters.setDefaultParametersFor(this);
		TransponderParameters.setDefaultParametersFor(this);

		mTransponderReceivedDelegate = null;

		privateInventoryOnly = TriState.NOT_SPECIFIED;
		privateUseFastId = TriState.NOT_SPECIFIED;
		privateUseTagFocus = TriState.NOT_SPECIFIED;
		privateIncludeEpc = TriState.NOT_SPECIFIED;
		privateFilterStrongest = TriState.NOT_SPECIFIED;

		this.transponderResponder = new TransponderResponder();
		transponderResponder.setTransponderReceivedHandler(this);
	}



	/** 
	 Returns a new instance of the command class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance
	*/
	public static InventoryCommand synchronousCommand()
	{
		InventoryCommand command;

		command = new InventoryCommand();
		command.setSynchronousCommandResponder(command);
		return command;
	}

	/** 
	 Clears the response ready to receive a new one
	*/
	@Override
	public void clearLastResponse()
	{
		super.clearLastResponse();
		this.transponderResponder.clearLastResponse();
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
		TransponderParameters.appendToCommandLine(this, line);
		ResponseParameters.appendToCommandLine(this, line);
		AntennaParameters.appendToCommandLine(this, line);
		QueryParameters.appendToCommandLine(this, line);
		QAlgorithmParameters.appendToCommandLine(this, line);
		SelectControlParameters.appendToCommandLine(this, line);
		SelectMaskParameters.appendToCommandLine(this, line);

		if (getIncludeEpc() != TriState.NOT_SPECIFIED)
		{
			line.append(String.format("-ie%s", getIncludeEpc().getArgument()));
		}

		if (getInventoryOnly() != TriState.NOT_SPECIFIED)
		{
			line.append(String.format("-io%s", getInventoryOnly().getArgument()));
		}

		if (getUseTagFocus() != TriState.NOT_SPECIFIED)
		{
			line.append(String.format("-tf%s", getUseTagFocus().getArgument()));
		}

		if (getUsefastId() != TriState.NOT_SPECIFIED)
		{
			line.append(String.format("-fi%s", getUsefastId().getArgument()));
		}

		if (getFilterStrongest() != TriState.NOT_SPECIFIED)
		{
			line.append(String.format("-fs%s", getFilterStrongest().getArgument()));
		}
	}

	/** 
	 The is called when the responder received a line with an OK: or an ER: header. i.e. the command is complete
	 
	 @param async True if the command finished asynchronously
	*/
	@Override
	protected void responseDidFinish(boolean async)
	{
		this.transponderResponder.transponderComplete(false);

		super.responseDidFinish(async);
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
		if (!AntennaParameters.parseParameterFor(this, parameter))
		{
			if (!ResponseParameters.parseParameterFor(this, parameter))
			{
				if (!TransponderParameters.parseParameterFor(this, parameter))
				{
					if (!QueryParameters.parseParameterFor(this, parameter))
					{
						if (!QAlgorithmParameters.parseParameterFor(this, parameter))
						{
							if (!SelectControlParameters.parseParameterFor(this, parameter))
							{
								if (!SelectMaskParameters.parseParameterFor(this, parameter))
								{
									if (!CommandParameters.parseParameterFor(this, parameter))
									{
										// Look for inventory only
										if ( parameter.length() >= 2 && parameter.startsWith("io"))
										{
											setInventoryOnly(TriState.Parse(parameter.substring(2)));
										}
                                        // Look for the include EPC
                                        else if ( parameter.length() >= 2 && parameter.startsWith("ie"))
                                        {
                                            setIncludeEpc(TriState.Parse(parameter.substring(2)));
                                        }
										// Look for the Impinj Monza extensions
										else if ( parameter.length() >= 2 && parameter.startsWith("fi"))
										{
											setUsefastId(TriState.Parse(parameter.substring(2)));
										}
										// Look for the Filter Strongest extensions
										else if ( parameter.length() >= 2 && parameter.startsWith("fs"))
										{
											setFilterStrongest(TriState.Parse(parameter.substring(2)));
										}
										else if (parameter.length() >= 2 && parameter.startsWith("tf"))
										{
											setUseTagFocus(TriState.Parse(parameter.substring(2)));
										}
										else
										{
											return super.responseDidReceiveParameter(parameter);
										}
									}
								}
							}
						}					
					}
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
	 @return true if this line should NOT be passed to any other responder.
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
		else if (this.getResponseStarted())
		{
			if (this.transponderResponder.processReceivedLine(header, value))
			{
				this.appendToResponse(fullLine);
				return true;
			}
		}

		return false;
	}

	/** 
	 * Called for each transponder received in the response
	 * Note: Invoked on a non-UI thread
	 * 
	 * @param transponder  a transponder response from an Inventory, Read or Write command
	 * @param moreAvailable true if there are more transponders
	 */
	public void transponderReceived( TransponderData transponder, boolean moreAvailable )
	{
		// Forward the data 
		if( mTransponderReceivedDelegate != null )
		{
			// Inform listener of new transponder
			mTransponderReceivedDelegate.transponderReceived(transponder, moreAvailable);
		}
	}
}