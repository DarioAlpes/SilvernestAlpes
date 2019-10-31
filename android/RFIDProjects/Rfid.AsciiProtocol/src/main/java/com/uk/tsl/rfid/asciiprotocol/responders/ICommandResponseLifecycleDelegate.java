package com.uk.tsl.rfid.asciiprotocol.responders;

public interface ICommandResponseLifecycleDelegate {

	/**
	 * Invoked (from a non-UI thread) when the start of a command response has been received
	 */
	void responseBegan();

	/**
	 * Invoked (from a non-UI thread) when a command response has finished
	 */
	void responseEnded();
}
