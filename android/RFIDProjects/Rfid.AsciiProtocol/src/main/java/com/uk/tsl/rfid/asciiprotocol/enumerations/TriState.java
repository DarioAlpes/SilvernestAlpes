package com.uk.tsl.rfid.asciiprotocol.enumerations;

import java.util.HashMap;

/**---------------------------------------------------------------------------
* @author TSL Code Generator
*
*	 Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
----------------------------------------------------------------------------*/

/** 
* Allows an action to be set on or off or not specified to leave the current value unchanged
*/
public class TriState extends EnumerationBase
{                    
	/** 
	* The instance of Not Specified
	*/
			public static final TriState NOT_SPECIFIED = null;
    	                    
	/** 
	* The instance of Yes
	*/
			public static final TriState YES = new TriState("on", "The value is specified on");        
		                    
	/** 
	* The instance of No
	*/
			public static final TriState NO = new TriState("off", "The value is specified off");        
		
	/**
	 * Initializes a new instance of the TriState class
	 * 
	 * @param argument The argument as output to the command line
	 * @param description A human-readable description of the value
	 */
	private TriState(String argument, String description)
	{
		super(argument, description);
		if( parameterLookUp == null )
		{
			parameterLookUp = new HashMap<String,TriState>();
		}
		parameterLookUp.put(argument, this);
	}

	
	public static final TriState[] getValues()
	{
		return PRIVATE_VALUES;
	}

	public static final TriState[] PRIVATE_VALUES = 
		{
			NOT_SPECIFIED,
			YES,
			NO,
					};

	public static final TriState Parse(String parameter)
	{
		String trimmedParameter = parameter.trim();
		if( !parameterLookUp.containsKey(trimmedParameter))
		{
			String message = String.format("'%s' is not recognised as a value of %s", parameter, TriState.class.getName());
			throw new IllegalArgumentException(message);
		}

		return parameterLookUp.get(trimmedParameter);
	}

	
	private static HashMap<String,TriState> parameterLookUp;
}
