package com.uk.tsl.rfid.asciiprotocol.commands;

import com.uk.tsl.rfid.asciiprotocol.enumerations.SwitchState;
import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;

/** 
 ASCII command to query the switch state
*/
public class SwitchStateCommand extends AsciiSelfResponderCommandBase
{
	/** 
	 Initializes a new instance of the SwitchStateCommand class
	*/
	public SwitchStateCommand()
	{
		super(".ss");
	}

	/** 
	 Gets the last switch state received from the device
	*/
	private SwitchState privateState = SwitchState.getValues()[0];
	public final SwitchState getState()
	{
		return privateState;
	}
	private void setState(SwitchState value)
	{
		privateState = value;
	}

	/** 
	 Returns a new instance of the command class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance
	*/
	public static SwitchStateCommand synchronousCommand()
	{
		SwitchStateCommand command;
		command = new SwitchStateCommand();
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
		this.setState(SwitchState.OFF);
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
		// Allow super class to detect the command start, messages, parameters and command end
		boolean superDidProcess;

		superDidProcess = super.processReceivedLine(fullLine, header, value, moreAvailable);

		if (superDidProcess)
		{
			// This line has been handled (no more processing needed)
			return true;
		}
		else if( getResponseStarted() )
		{
			if ("SW".equals(header))
			{
				this.setState(SwitchState.Parse(value));
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
		return false;
	}
}