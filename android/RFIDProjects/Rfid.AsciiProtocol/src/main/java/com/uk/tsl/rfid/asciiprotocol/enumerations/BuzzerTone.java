package com.uk.tsl.rfid.asciiprotocol.enumerations;

import java.util.HashMap;

/**---------------------------------------------------------------------------
* @author TSL Code Generator
*
*	 Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
----------------------------------------------------------------------------*/

/** 
* Defines the tone for the buzzer on a alert
*/
public class BuzzerTone extends EnumerationBase
{                    
	/** 
	* The instance of Not Specified
	*/
			public static final BuzzerTone NOT_SPECIFIED = null;
    	                    
	/** 
	* The instance of Low
	*/
			public static final BuzzerTone LOW = new BuzzerTone("low", "A low pitch tone");        
		                    
	/** 
	* The instance of Medium
	*/
			public static final BuzzerTone MEDIUM = new BuzzerTone("med", "A medium pitch tone");        
		                    
	/** 
	* The instance of High
	*/
			public static final BuzzerTone HIGH = new BuzzerTone("hig", "A high pitch tone");        
		
	/**
	 * Initializes a new instance of the BuzzerTone class
	 * 
	 * @param argument The argument as output to the command line
	 * @param description A human-readable description of the value
	 */
	private BuzzerTone(String argument, String description)
	{
		super(argument, description);
		if( parameterLookUp == null )
		{
			parameterLookUp = new HashMap<String,BuzzerTone>();
		}
		parameterLookUp.put(argument, this);
	}

	
	public static final BuzzerTone[] getValues()
	{
		return PRIVATE_VALUES;
	}

	public static final BuzzerTone[] PRIVATE_VALUES = 
		{
			NOT_SPECIFIED,
			LOW,
			MEDIUM,
			HIGH,
					};

	public static final BuzzerTone Parse(String parameter)
	{
		String trimmedParameter = parameter.trim();
		if( !parameterLookUp.containsKey(trimmedParameter))
		{
			String message = String.format("'%s' is not recognised as a value of %s", parameter, BuzzerTone.class.getName());
			throw new IllegalArgumentException(message);
		}

		return parameterLookUp.get(trimmedParameter);
	}

	
	private static HashMap<String,BuzzerTone> parameterLookUp;
}
