package com.uk.tsl.rfid.asciiprotocol.commands;

import android.util.Log;

import com.uk.tsl.rfid.asciiprotocol.enumerations.Databank;
import com.uk.tsl.rfid.asciiprotocol.enumerations.QAlgorithm;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.rfid.asciiprotocol.parameters.AntennaParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.CommandParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.DatabankParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.IAntennaParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.ICommandParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.IDatabankParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.IQAlgorithmParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.IResponseParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.ISelectMaskParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.ITransponderParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.QAlgorithmParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.ResponseParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.SelectMaskParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.TransponderParameters;
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
 Base class for commands that read or write transponder memory
*/
public abstract class TransponderMemoryCommandBase extends AsciiSelfResponderCommandBase
	implements ICommandParameters, IDatabankParameters, IAntennaParameters, IResponseParameters,
	ISelectMaskParameters, ITransponderParameters,IQAlgorithmParameters,
	ITransponderReceivedDelegate
{

	private TransponderResponder transponderResponder;
	
	/** 
	 Initializes a new instance of the TransponderMemoryCommandBase class
	 
	 @param commandName The command name (e.g. ".iv" for inventory)
	*/
	protected TransponderMemoryCommandBase(String commandName)
	{
		super(commandName);
		CommandParameters.setDefaultParametersFor(this);
		DatabankParameters.setDefaultParametersFor(this);
		AntennaParameters.setDefaultParametersFor(this);
		ResponseParameters.setDefaultParametersFor(this);
		SelectMaskParameters.setDefaultParametersFor(this);
		TransponderParameters.setDefaultParametersFor(this);
		QAlgorithmParameters.setDefaultParametersFor(this);
		

		this.transponderResponder = new TransponderResponder();
		transponderResponder.setTransponderReceivedHandler(this);
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

	///#region IDataBankParameters Members

	/** 
	 Gets or sets the transponder data bank to be used
	*/
	private Databank privateBank;
	public final Databank getBank()
	{
		return privateBank;
	}
	public final void setBank(Databank value)
	{
		privateBank = value;
	}

	/** 
	 Gets or sets the data read from or written to a transponder memory bank
	*/
	private byte[] privateData;
	public final byte[] getData()
	{
		return privateData;
	}
	public final void setData(byte[] value)
	{
		privateData = value;
	}

	/** 
	 Gets or sets the length in words of the data to write
	*/
	private int privateLength;
	public final int getLength()
	{
		return privateLength;
	}
	public final void setLength(int value)
	{
		privateLength = value;
	}

	/** 
	 Gets or sets the offset, in 16 bit words, from the start of the memory bank to where the data will be written
	*/
	private int privateOffset;
	public final int getOffset()
	{
		return privateOffset;
	}
	public final void setOffset(int value)
	{
		privateOffset = value;
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

	///#region ISelectMaskParameters Members

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

	///

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
		AntennaParameters.appendToCommandLine(this, line);
		TransponderParameters.appendToCommandLine(this, line);
		ResponseParameters.appendToCommandLine(this, line);
		DatabankParameters.appendToCommandLine(this, line);
		SelectMaskParameters.appendToCommandLine(this, line);
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
	 * Delegate method invoked for each transponder received 
	 * 
	 * Note: This method will be called on a non-UI thread
	 * 
	 * @param transponder  a transponder response from an Inventory, Read or Write command
	 * @param moreAvailable true if there are more transponders
	 */
	public void transponderReceived( TransponderData transponder, boolean moreAvailable )
	{
		Log.d("TMCB", "Base implementation of transponderReceived(): " + transponder.getEpc() );
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
					if (!DatabankParameters.parseParameterFor(this, parameter))
					{
						if (!SelectMaskParameters.parseParameterFor(this, parameter))
						{
							if (!CommandParameters.parseParameterFor(this, parameter))
							{
								return super.responseDidReceiveParameter(parameter);
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
	 @return 
	 Return true if this line should NOT be passed to any other responder.
	 * @throws Exception 
	 
	*/
	@Override
	protected boolean processReceivedLine(String fullLine, String header, String value, boolean moreAvailable) throws Exception
	{
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

			// Not recognised so allow others to see it
			return false;
		}
	}
}