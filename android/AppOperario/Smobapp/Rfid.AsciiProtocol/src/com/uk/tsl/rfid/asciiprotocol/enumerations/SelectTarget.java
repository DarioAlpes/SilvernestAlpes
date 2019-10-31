package com.uk.tsl.rfid.asciiprotocol.enumerations;

import java.util.HashMap;

/**---------------------------------------------------------------------------
* @author TSL Code Generator
*
*	 Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
----------------------------------------------------------------------------*/

/** 
* Species the session to target
*/
public class SelectTarget extends EnumerationBase
{                    
	/** 
	* The instance of Not Specified
	*/
			public static final SelectTarget NOT_SPECIFIED = null;
    	                    
	/** 
	* The instance of Session 0
	*/
			public static final SelectTarget SESSION_0 = new SelectTarget("s0", "Use session 0");        
		                    
	/** 
	* The instance of Session 1
	*/
			public static final SelectTarget SESSION_1 = new SelectTarget("s1", "Use session 1");        
		                    
	/** 
	* The instance of Session 2
	*/
			public static final SelectTarget SESSION_2 = new SelectTarget("s2", "Use session 2");        
		                    
	/** 
	* The instance of Session 3
	*/
			public static final SelectTarget SESSION_3 = new SelectTarget("s3", "Use session 3");        
		                    
	/** 
	* The instance of Selected
	*/
			public static final SelectTarget SELECTED = new SelectTarget("sl", "Use select flag");        
		
	/**
	 * Initializes a new instance of the SelectTarget class
	 * 
	 * @param argument The argument as output to the command line
	 * @param description A human-readable description of the value
	 */
	private SelectTarget(String argument, String description)
	{
		super(argument, description);
		if( parameterLookUp == null )
		{
			parameterLookUp = new HashMap<String,SelectTarget>();
		}
		parameterLookUp.put(argument, this);
	}

	
	public static final SelectTarget[] getValues()
	{
		return PRIVATE_VALUES;
	}

	public static final SelectTarget[] PRIVATE_VALUES = 
		{
			NOT_SPECIFIED,
			SESSION_0,
			SESSION_1,
			SESSION_2,
			SESSION_3,
			SELECTED,
					};

	public static final SelectTarget Parse(String parameter)
	{
		String trimmedParameter = parameter.trim();
		if( !parameterLookUp.containsKey(trimmedParameter))
		{
			String message = String.format("'%s' is not recognised as a value of %s", parameter, SelectTarget.class.getName());
			throw new IllegalArgumentException(message);
		}

		return parameterLookUp.get(trimmedParameter);
	}

	
	private static HashMap<String,SelectTarget> parameterLookUp;
}
