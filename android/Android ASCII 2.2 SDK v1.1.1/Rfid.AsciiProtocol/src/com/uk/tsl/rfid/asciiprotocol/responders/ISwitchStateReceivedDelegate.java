package com.uk.tsl.rfid.asciiprotocol.responders;

import com.uk.tsl.rfid.asciiprotocol.enumerations.SwitchState;

public interface ISwitchStateReceivedDelegate {

	/**
	 * Delegate method invoked for each switch state notification received 
	 * 
	 * Note: This method will be called on a non-UI thread
	 * 
	 * @param state the new switch state 
	 */
	public void switchStateReceived(SwitchState state);
}
