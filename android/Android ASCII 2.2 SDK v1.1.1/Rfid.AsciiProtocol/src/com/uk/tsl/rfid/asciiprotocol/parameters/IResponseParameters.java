package com.uk.tsl.rfid.asciiprotocol.parameters;

import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 Generic parameters for command responses
 
 {@link ResponseParameters}
*/
public interface IResponseParameters
{
	/** 
	 Gets or sets a value indicating whether DateTime stamps appear in reader responses
	*/
	TriState getIncludeDateTime();
	void setIncludeDateTime(TriState value);

	/** 
	 Gets or sets a value indicating whether alerts are enabled for the executing commands
	*/
	TriState getUseAlert();
	void setUseAlert(TriState value);
}