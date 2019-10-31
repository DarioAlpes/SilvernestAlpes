package com.uk.tsl.rfid.asciiprotocol.responders;

import com.uk.tsl.rfid.asciiprotocol.enumerations.SwitchState;

public class SwitchResponder extends AsciiCommandResponderBase {

	public SwitchResponder() {
		super(".ss");
		privateDelegate = null;
	}
	
	public ISwitchStateReceivedDelegate getSwitchStateReceivedDelegate() { return privateDelegate; }
	private ISwitchStateReceivedDelegate privateDelegate;
	public void setSwitchStateReceivedDelegate(ISwitchStateReceivedDelegate delegate) { privateDelegate = delegate; }
	
	/**
	 * @return the most recently reported switch state
	 */
	public SwitchState getState() { return privateState; }
	private SwitchState privateState;
	protected void setState(SwitchState state) { privateState = state; }

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
	        // This line has been recognised as part of an .ss command response
	        // These need to be ignored - this responder is to detect SW: responses that are NOT from the .ss command
			return false;
		}
		else if( !getResponseStarted() )
		{
			if ("SW".equals(header))
			{
				this.setState(SwitchState.Parse(value));

	            // Inform the delegate of the newly received state
				if( getSwitchStateReceivedDelegate() != null)
				{
					getSwitchStateReceivedDelegate().switchStateReceived(getState());
				}
				
				return true;
			}
		}

		// Not recognised so allow others to see it
		return false;
	}
	
}
