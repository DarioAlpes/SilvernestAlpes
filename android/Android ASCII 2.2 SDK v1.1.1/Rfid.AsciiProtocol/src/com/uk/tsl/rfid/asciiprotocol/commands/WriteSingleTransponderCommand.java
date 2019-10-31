package com.uk.tsl.rfid.asciiprotocol.commands;

import com.uk.tsl.rfid.asciiprotocol.parameters.DatabankParameters;
import com.uk.tsl.rfid.asciiprotocol.responders.ITransponderReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.TransponderData;


//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------


/** 
 A command to write data to the memory banks of a single transponder only 
*/
public class WriteSingleTransponderCommand extends TransponderMemoryCommandBase
{
	/** 
	 Initializes a new instance of the WriteSingleTransponderCommand class
	*/
	public WriteSingleTransponderCommand()
	{
		super(".ws");

		mTransponderReceivedDelegate = null;
	}

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
	 Gets a value indicating whether a transponder responded to the command
	*/
	private boolean privateIsTransponderFound;
	public final boolean isTransponderFound()
	{
		return privateIsTransponderFound;
	}
	private void setIsTransponderFound(boolean value)
	{
		privateIsTransponderFound = value;
	}

	/** 
	 Gets the number of words successfully written to the transponder
	*/
	private int privateWordsWritten;
	public final int getWordsWritten()
	{
		return privateWordsWritten;
	}
	private void setWordsWritten(int value)
	{
		privateWordsWritten = value;
	}

	/** 
	 Returns a new instance of the command class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance
	*/
	public static WriteSingleTransponderCommand synchronousCommand()
	{
		WriteSingleTransponderCommand command;
		command = new WriteSingleTransponderCommand();
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
		this.setWordsWritten(0);
		this.setIsTransponderFound(false);
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
		if (!DatabankParameters.parseParameterFor(this, parameter))
		{
			return super.responseDidReceiveParameter(parameter);
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