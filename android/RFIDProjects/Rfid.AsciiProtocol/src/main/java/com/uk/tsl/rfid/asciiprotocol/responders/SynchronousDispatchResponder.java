package com.uk.tsl.rfid.asciiprotocol.responders;


//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 This is a special TSLAsciiCommandResponder that is inserted into the responder chain to handle synchronous commands. 
 This responder uses its synchronousCommandDelegate to find the currently executing synchronous command. 
 If such a command exists then this responder forwards calls to processReceivedLine:moreLinesAvailable: to the synchronous command's synchronousCommandResponder 
*/
public class SynchronousDispatchResponder implements IAsciiCommandResponder
{
	/////// <summary>
	/////// Gets/sets the delegate to be called to handle synchronous command messages
	/////// </summary>
	////public IAsciiCommandResponderDelegate SynchronousCommandDelegate { get; set; }

	/** 
	 Gets or sets the IAsciiCommandResponder"/> of the currently executing synchronous <see cref="IAsciiCommand
	 
	 
	 This property should be set to a synchronous command while it is executing and set to null once the command is complete
	 
	*/
	private IAsciiCommandResponder privateSynchronousCommandResponder;
	public final IAsciiCommandResponder getSynchronousCommandResponder()
	{
		return privateSynchronousCommandResponder;
	}
	public final void setSynchronousCommandResponder(IAsciiCommandResponder value)
	{
		privateSynchronousCommandResponder = value;
	}

	/** 
	 Gets a value indicating whether the response is complete (i.e. received OK: or ER:)
	*/
	public final boolean isResponseFinished()
	{
		IAsciiCommandResponder responder;

		responder = this.getSynchronousCommandResponder();
		if (responder != null)
		{
			return responder.isResponseFinished();
		}

		return false;
	}

	/** 
	 Clears the values from the last response
	 
	 
	 Derived classes must call super class to ensure correct operation
	 
	*/
	public final void clearLastResponse()
	{
		IAsciiCommandResponder responder;

		responder = this.getSynchronousCommandResponder();
		if (responder != null)
		{
			responder.clearLastResponse();
		}
	}

	/** 
	 Each correctly terminated line from the device is passed to this method for processing
	 
	 @param fullLine The line to be processed
	 @param moreLinesAvailable When YES indicates there are additional lines to be processed (and will also be passed to this method)
	 @return YES if this line should not be passed to any other responder
	 * @throws Exception 
	*/
	public final boolean processReceivedLine(String fullLine, boolean moreLinesAvailable) throws Exception
	{
		IAsciiCommandResponder responder;

		responder = this.getSynchronousCommandResponder();
		if (responder != null)
		{
			return responder.processReceivedLine(fullLine, moreLinesAvailable);
		}

		return false;
	}
}