package com.uk.tsl.rfid.asciiprotocol.enumerations;

import java.util.HashMap;

/**---------------------------------------------------------------------------
* @author TSL Code Generator
*
*	 Copyright (c) 2013-2016 Technology Solutions UK Ltd. All rights reserved. 
----------------------------------------------------------------------------*/

/** 
* Allows the Impinj QT mode to be set to None, AccessPrivateMemory, AccessPrivateMemoryShortRange, AccessControlWord or not specified to leave the current value unchanged
*/
public class QtMode extends EnumerationBase
{                    
	/** 
	* The instance of Not Specified
	*/
			public static final QtMode NOT_SPECIFIED = null;
    	                    
	/** 
	* The instance of None
	*/
			public static final QtMode NONE = new QtMode("0", "The value is specified as standard read/write");        
		                    
	/** 
	* The instance of Access Private Memory
	*/
			public static final QtMode ACCESS_PRIVATE_MEMORY = new QtMode("1", "The value is specified as read or write the private memory");        
		                    
	/** 
	* The instance of Access Private Memory Short Range
	*/
			public static final QtMode ACCESS_PRIVATE_MEMORY_SHORT_RANGE = new QtMode("2", "The value is specified as read or write the private memory with short range");        
		                    
	/** 
	* The instance of Access Control Word
	*/
			public static final QtMode ACCESS_CONTROL_WORD = new QtMode("3", "The value is specified as read or write the QT control word");        
		
	/**
	 * Initializes a new instance of the QtMode class
	 * 
	 * @param argument The argument as output to the command line
	 * @param description A human-readable description of the value
	 */
	private QtMode(String argument, String description)
	{
		super(argument, description);
		if( parameterLookUp == null )
		{
			parameterLookUp = new HashMap<String,QtMode>();
		}
		parameterLookUp.put(argument, this);
	}

	
	public static final QtMode[] getValues()
	{
		return PRIVATE_VALUES;
	}

	public static final QtMode[] PRIVATE_VALUES = 
		{
			NOT_SPECIFIED,
			NONE,
			ACCESS_PRIVATE_MEMORY,
			ACCESS_PRIVATE_MEMORY_SHORT_RANGE,
			ACCESS_CONTROL_WORD,
					};

	public static final QtMode Parse(String parameter)
	{
		String trimmedParameter = parameter.trim();
		if( !parameterLookUp.containsKey(trimmedParameter))
		{
			String message = String.format("'%s' is not recognised as a value of %s", parameter, QtMode.class.getName());
			throw new IllegalArgumentException(message);
		}

		return parameterLookUp.get(trimmedParameter);
	}

	
	private static HashMap<String,QtMode> parameterLookUp;
}
