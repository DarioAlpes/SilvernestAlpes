package com.uk.tsl.rfid.asciiprotocol.enumerations;

import java.util.HashMap;

/**---------------------------------------------------------------------------
* @author TSL Code Generator
*
*	 Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
----------------------------------------------------------------------------*/

/** 
* Describes the battery's charging status
*/
public class ChargeState extends EnumerationBase
{                    
	/** 
	* The instance of Not Specified
	*/
			public static final ChargeState NOT_SPECIFIED = null;
    	                    
	/** 
	* The instance of Off
	*/
			public static final ChargeState OFF = new ChargeState("Off", "The battery is not being charged");        
		                    
	/** 
	* The instance of Charging
	*/
			public static final ChargeState CHARGING = new ChargeState("Charging", "The battery is charging");        
		                    
	/** 
	* The instance of Complete
	*/
			public static final ChargeState COMPLETE = new ChargeState("Complete", "Battery charging is complete");        
		                    
	/** 
	* The instance of Error
	*/
			public static final ChargeState ERROR = new ChargeState("Error", "There is a battery charging error");        
		
	/**
	 * Initializes a new instance of the ChargeState class
	 * 
	 * @param argument The argument as output to the command line
	 * @param description A human-readable description of the value
	 */
	private ChargeState(String argument, String description)
	{
		super(argument, description);
		if( parameterLookUp == null )
		{
			parameterLookUp = new HashMap<String,ChargeState>();
		}
		parameterLookUp.put(argument, this);
	}

	
	public static final ChargeState[] getValues()
	{
		return PRIVATE_VALUES;
	}

	public static final ChargeState[] PRIVATE_VALUES = 
		{
			NOT_SPECIFIED,
			OFF,
			CHARGING,
			COMPLETE,
			ERROR,
					};

	public static final ChargeState Parse(String parameter)
	{
		String trimmedParameter = parameter.trim();
		if( !parameterLookUp.containsKey(trimmedParameter))
		{
			String message = String.format("'%s' is not recognised as a value of %s", parameter, ChargeState.class.getName());
			throw new IllegalArgumentException(message);
		}

		return parameterLookUp.get(trimmedParameter);
	}

	
	private static HashMap<String,ChargeState> parameterLookUp;
}
