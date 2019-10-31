package com.uk.tsl.rfid.asciiprotocol.parameters;

import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 Parameters related to the Transponder information in command responses
 
 {@link TransponderParameters}
*/
public interface ITransponderParameters
{
	/** 
	 Gets or sets the password required to access transponders
	*/
	String getAccessPassword();
	void setAccessPassword(String value);

	/** 
	 Gets or sets a value indicating whether to include checksum information in reader responses
	*/
	TriState getIncludeChecksum();
	void setIncludeChecksum(TriState value);

	/** 
	 Gets or sets a value indicating whether to include index numbers for multiple values in reader responses
	*/
	TriState getIncludeIndex();
	void setIncludeIndex(TriState value);

	/** 
	 Gets or sets a value indicating whether to include the EPC PC value in reader responses
	*/
	TriState getIncludePC();
	void setIncludePC(TriState value);

	/** 
	 Gets or sets a value indicating whether to include RSSI value in reader responses
	*/
	TriState getIncludeTransponderRssi();
	void setIncludeTransponderRssi(TriState value);
}