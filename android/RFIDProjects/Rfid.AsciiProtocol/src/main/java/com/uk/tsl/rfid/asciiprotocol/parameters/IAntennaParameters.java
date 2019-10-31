package com.uk.tsl.rfid.asciiprotocol.parameters;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 Parameters related to the reader Antenna operation during commands
 
 {@link AntennaParameters}
*/
public interface IAntennaParameters
{
	/** 
	 Gets or sets the output power. Valid power range is 10 - 29.
	 Use @see AntennaParameters.OutputPowerNotSpecified  to read the output power.
	*/
	int getOutputPower();
	void setOutputPower(int value);
}