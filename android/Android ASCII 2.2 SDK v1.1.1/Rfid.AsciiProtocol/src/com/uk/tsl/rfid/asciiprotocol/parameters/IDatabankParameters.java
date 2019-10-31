package com.uk.tsl.rfid.asciiprotocol.parameters;

import com.uk.tsl.rfid.asciiprotocol.enumerations.Databank;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 Parameters related to Data Banks in commands and responses
 
 {@link DatabankParameters}
*/
public interface IDatabankParameters
{
	/** 
	 Gets or sets the transponder data bank to be used
	*/
	Databank getBank();
	void setBank(Databank value);

	/** 
	 Gets or sets the data read from or written to a transponder memory bank
	*/
	byte[] getData();
	void setData(byte[] value);

	/** 
	 Gets or sets the length in words of the data to write
	*/
	int getLength();
	void setLength(int value);

	/** 
	 Gets or sets the offset, in 16 bit words, from the start of the memory bank to where the data will be written
	*/
	int getOffset();
	void setOffset(int value);
}