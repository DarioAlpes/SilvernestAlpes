package com.uk.tsl.rfid.asciiprotocol.parameters;

import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 Parameters for generic control of ASCII commands
 
 {@link CommandParameters}
*/
public interface ICommandParameters
{
	/** 
	 Gets a value indicating whether the implementing command uses the  ReadParameters property
	*/
	boolean implementsReadParameters();

	/** 
	 Gets a value indicating whether the implementing command uses the  ResetParameters property
	*/
	boolean implementsResetParameters();

	/** 
	 Gets a value indicating whether the implementing command uses the  TakeNoAction property
	*/
	boolean implementsTakeNoAction();

	/** 
	 Gets or sets a value indicating whether the command should include a list of supported parameters and their current values
	*/
	TriState getReadParameters();
	void setReadParameters(TriState value);

	/** 
	 Gets or sets a value indicating whether the command should reset all its parameters to default values
	*/
	TriState getResetParameters();
	void setResetParameters(TriState value);

	/** 
	 Gets or sets a value indicating whether the command primary action should not be performed
	 (e.g. InventoryCommand will not perform the inventory action)
	 All other actions, such as setting parameters in the reader are performed
	*/
	TriState getTakeNoAction();
	void setTakeNoAction(TriState value);
}