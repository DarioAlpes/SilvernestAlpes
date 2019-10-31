package com.uk.tsl.rfid.asciiprotocol.parameters;

import com.uk.tsl.rfid.asciiprotocol.enumerations.SelectAction;
import com.uk.tsl.rfid.asciiprotocol.enumerations.SelectTarget;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 Parameters for Select Control operations in commands and responses
*/
public interface ISelectControlParameters
{
	/**
	 * Gets or sets the action to perform in the Select operation
	 */
	SelectAction getSelectAction();
	void setSelectAction(SelectAction value);

	/** 
	 * Gets or sets the target flag for the Select operation
	 */
	SelectTarget getSelectTarget();
	void setSelectTarget(SelectTarget value);
}