
package com.uk.tsl.rfid.asciiprotocol.enumerations;

import java.util.HashMap;

/**---------------------------------------------------------------------------
* @author TSL Code Generator
*
*	 Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
----------------------------------------------------------------------------*/

/** 
* Defines the duration of an alert as set by the Alert command ".al"
*/
public class AlertDuration extends EnumerationBase
{                    
	/** 
	* The instance of Not Specified
	*/
			public static final AlertDuration NOT_SPECIFIED = null;
    	                    
	/** 
	* The instance of Short
	*/
			public static final AlertDuration SHORT = new AlertDuration("sho", "A short duration");        
		                    
	/** 
	* The instance of Medium
	*/
			public static final AlertDuration MEDIUM = new AlertDuration("med", "A medium duration ");        
		                    
	/** 
	* The instance of Long
	*/
			public static final AlertDuration LONG = new AlertDuration("lon", "A long duration");        
		
	/**
	 * Initializes a new instance of the AlertDuration class
	 * 
	 * @param argument The argument as output to the command line
	 * @param description A human-readable description of the value
	 */
	private AlertDuration(String argument, String description)
	{
		super(argument, description);
		if( parameterLookUp == null )
		{
			parameterLookUp = new HashMap<String,AlertDuration>();
		}
		parameterLookUp.put(argument, this);
	}

	
	public static final AlertDuration[] getValues()
	{
		return PRIVATE_VALUES;
	}

	public static final AlertDuration[] PRIVATE_VALUES = 
		{
			NOT_SPECIFIED,
			SHORT,
			MEDIUM,
			LONG,
					};

	public static final AlertDuration Parse(String parameter)
	{
		String trimmedParameter = parameter.trim();
		if( !parameterLookUp.containsKey(trimmedParameter))
		{
			String message = String.format("'%s' is not recognised as a value of %s", parameter, AlertDuration.class.getName());
			throw new IllegalArgumentException(message);
		}

		return parameterLookUp.get(trimmedParameter);
	}

	
	private static HashMap<String,AlertDuration> parameterLookUp;
}
