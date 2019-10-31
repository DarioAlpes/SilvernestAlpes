package com.uk.tsl.rfid.asciiprotocol.commands;

import com.uk.tsl.rfid.asciiprotocol.enumerations.Databank;
import com.uk.tsl.rfid.asciiprotocol.enumerations.SelectAction;
import com.uk.tsl.rfid.asciiprotocol.enumerations.SelectTarget;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.rfid.asciiprotocol.parameters.AntennaParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.CommandParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.IAntennaParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.ICommandParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.ISelectControlParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.ISelectMaskParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.SelectControlParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.SelectMaskParameters;
import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;

public class TransponderSelectCommand extends AsciiSelfResponderCommandBase
		implements ICommandParameters, IAntennaParameters,
		ISelectControlParameters, ISelectMaskParameters {

	protected TransponderSelectCommand() {
		super(".ts");

		CommandParameters.setDefaultParametersFor(this);
		AntennaParameters.setDefaultParametersFor(this);
		SelectControlParameters.setDefaultParametersFor(this);
		SelectMaskParameters.setDefaultParametersFor(this);

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
	public final boolean implementsTakeNoAction() { return true; }

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


	///#region ISelectControlParameters & ISelectMaskParameters Members


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
	 Returns a new instance of the command class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance
	*/
	public static TransponderSelectCommand synchronousCommand()
	{
		TransponderSelectCommand command;

		command = new TransponderSelectCommand();
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
		AntennaParameters.appendToCommandLine(this, line);
		SelectControlParameters.appendToCommandLine(this, line);
		SelectMaskParameters.appendToCommandLine(this, line);

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
			if (!AntennaParameters.parseParameterFor(this, parameter))
			{
				if (!SelectControlParameters.parseParameterFor(this, parameter))
				{
					if (!SelectMaskParameters.parseParameterFor(this, parameter))
					{
						return super.responseDidReceiveParameter(parameter);
					}
				}
			}
		}
		return true;
	}


}
