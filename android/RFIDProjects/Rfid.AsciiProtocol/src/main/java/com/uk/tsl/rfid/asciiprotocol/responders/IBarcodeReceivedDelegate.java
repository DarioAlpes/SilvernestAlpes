package com.uk.tsl.rfid.asciiprotocol.responders;

public interface IBarcodeReceivedDelegate
{
	/// This method will be called on a non-UI thread
	/**
	 * Delegate method invoked for each barcode received 
	 * 
	 * Note: This method will be called on a non-UI thread
	 * 
	 * @param barcode The barcode scanned as a String
	 */
	public void barcodeReceived( String barcode );

}
