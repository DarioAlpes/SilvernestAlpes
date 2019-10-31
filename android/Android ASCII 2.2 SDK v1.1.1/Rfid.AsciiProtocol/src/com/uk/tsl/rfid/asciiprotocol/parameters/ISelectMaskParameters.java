package com.uk.tsl.rfid.asciiprotocol.parameters;

import com.uk.tsl.rfid.asciiprotocol.enumerations.Databank;

//-----------------------------------------------------------------------
//  Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 * Parameters for Select operations in commands and responses
*/
public interface ISelectMaskParameters {
	/** 
	 Gets or sets the Bank to use for the select mask
	*/
	Databank getSelectBank();
	void setSelectBank(Databank value);

	/** 
	 Gets or sets the select mask data in 2 character ASCII Hex pairs padded to ensure full bytes
	*/
	String getSelectData();
	void setSelectData(String value);

	/** 
	 Gets or sets the length in bits of the select mask
	*/
	int getSelectLength();
	void setSelectLength(int value);

	/** 
	 Gets or sets the number of bits from the start of the block to the start of the select mask
	*/
	int getSelectOffset();
	void setSelectOffset(int value);
}
