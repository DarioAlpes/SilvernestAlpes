package com.uk.tsl.rfid.asciiprotocol.commands;

import com.uk.tsl.rfid.asciiprotocol.enumerations.QuerySelect;
import com.uk.tsl.rfid.asciiprotocol.enumerations.QuerySession;
import com.uk.tsl.rfid.asciiprotocol.enumerations.QueryTarget;
import com.uk.tsl.rfid.asciiprotocol.enumerations.SelectAction;
import com.uk.tsl.rfid.asciiprotocol.enumerations.SelectTarget;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.rfid.asciiprotocol.parameters.IQAlgorithmParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.IQueryParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.ISelectControlParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.QAlgorithmParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.QueryParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.SelectControlParameters;
import com.uk.tsl.rfid.asciiprotocol.responders.ITransponderReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.TransponderData;


//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------


/** 
 A command to read data from the memory banks of one or more transponders 
*/
public class ReadTransponderCommand extends TransponderMemoryCommandBase
	implements ISelectControlParameters, IQAlgorithmParameters, IQueryParameters
{
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



	///#region ISelectControlParameters

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

	/// IQueryParameters Members

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

	//

	/** 
	 Initializes a new instance of the ReadTransponderCommand class
	*/
	public ReadTransponderCommand()
	{
		super(".rd");

		SelectControlParameters.setDefaultParametersFor(this);
		QueryParameters.setDefaultParametersFor(this);
		QAlgorithmParameters.setDefaultParametersFor(this);

		mTransponderReceivedDelegate = null;
		privateInventoryOnly = TriState.NOT_SPECIFIED;
	}

	/** 
	 Returns a new instance of the command class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance
	*/
	public static ReadTransponderCommand synchronousCommand()
	{
		ReadTransponderCommand command;

		command = new ReadTransponderCommand();
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

		SelectControlParameters.appendToCommandLine(this, line);
		QueryParameters.appendToCommandLine(this, line);
		QAlgorithmParameters.appendToCommandLine(this, line, false);

		if (getInventoryOnly() != TriState.NOT_SPECIFIED)
		{
			line.append(String.format("-io%s", getInventoryOnly().getArgument()));
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
		if (!SelectControlParameters.parseParameterFor(this, parameter))
		{
			if (!QueryParameters.parseParameterFor(this, parameter))
			{
				if (!QAlgorithmParameters.parseParameterFor(this, parameter))
				{
					if ( parameter.length() >= 2 && parameter.startsWith("io"))
					{
						setInventoryOnly(TriState.Parse(parameter.substring(2)));
					}
					else
					{
						return super.responseDidReceiveParameter(parameter);
					}
				}
			}
		}
		return true;
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
		// Forward the data to 
		if( mTransponderReceivedDelegate != null )
		{
			// Inform listener of new transponder
			mTransponderReceivedDelegate.transponderReceived(transponder, moreAvailable);
		}
	}

}