package com.uk.tsl.rfid.asciiprotocol.enumerations;

import java.util.HashMap;

/**---------------------------------------------------------------------------
* @author TSL Code Generator
*
*	 Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
----------------------------------------------------------------------------*/

/** 
* Allows the write mode to be set to single, block or not specified to leave the current value unchanged
*/
public class ImpinjBlockWriteMode extends EnumerationBase
{                    
	/** 
	* The instance of Not Specified
	*/
			public static final ImpinjBlockWriteMode NOT_SPECIFIED = null;
    	                    
	/** 
	* The instance of Auto
	*/
			public static final ImpinjBlockWriteMode AUTO = new ImpinjBlockWriteMode("a", "The value is specified as Auto");        
		                    
	/** 
	* The instance of Force 1
	*/
			public static final ImpinjBlockWriteMode FORCE_1 = new ImpinjBlockWriteMode("1", "The value is specified as Force 1");        
		                    
	/** 
	* The instance of Force 2
	*/
			public static final ImpinjBlockWriteMode FORCE_2 = new ImpinjBlockWriteMode("2", "The value is specified as Force 2");        
		
	/**
	 * Initializes a new instance of the ImpinjBlockWriteMode class
	 * 
	 * @param argument The argument as output to the command line
	 * @param description A human-readable description of the value
	 */
	private ImpinjBlockWriteMode(String argument, String description)
	{
		super(argument, description);
		if( parameterLookUp == null )
		{
			parameterLookUp = new HashMap<String,ImpinjBlockWriteMode>();
		}
		parameterLookUp.put(argument, this);
	}

	
	public static final ImpinjBlockWriteMode[] getValues()
	{
		return PRIVATE_VALUES;
	}

	public static final ImpinjBlockWriteMode[] PRIVATE_VALUES = 
		{
			NOT_SPECIFIED,
			AUTO,
			FORCE_1,
			FORCE_2,
					};

	public static final ImpinjBlockWriteMode Parse(String parameter)
	{
		String trimmedParameter = parameter.trim();
		if( !parameterLookUp.containsKey(trimmedParameter))
		{
			String message = String.format("'%s' is not recognised as a value of %s", parameter, ImpinjBlockWriteMode.class.getName());
			throw new IllegalArgumentException(message);
		}

		return parameterLookUp.get(trimmedParameter);
	}

	
	private static HashMap<String,ImpinjBlockWriteMode> parameterLookUp;
}
