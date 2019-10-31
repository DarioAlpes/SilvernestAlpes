package com.uk.tsl.rfid.asciiprotocol.enumerations;

import java.util.HashMap;

/**---------------------------------------------------------------------------
* @author TSL Code Generator
*
*	 Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
----------------------------------------------------------------------------*/

/** 
* Specifies the Q algorithm to use
*/
public class QAlgorithm extends EnumerationBase
{                    
	/** 
	* The instance of Not Specified
	*/
			public static final QAlgorithm NOT_SPECIFIED = null;
    	                    
	/** 
	* The instance of Fixed
	*/
			public static final QAlgorithm FIXED = new QAlgorithm("fix", "Fixed time-slots has been specified");        
		                    
	/** 
	* The instance of Dynamic
	*/
			public static final QAlgorithm DYNAMIC = new QAlgorithm("dyn", "Dynamic time-slots has been specified");        
		
	/**
	 * Initializes a new instance of the QAlgorithm class
	 * 
	 * @param argument The argument as output to the command line
	 * @param description A human-readable description of the value
	 */
	private QAlgorithm(String argument, String description)
	{
		super(argument, description);
		if( parameterLookUp == null )
		{
			parameterLookUp = new HashMap<String,QAlgorithm>();
		}
		parameterLookUp.put(argument, this);
	}

	
	public static final QAlgorithm[] getValues()
	{
		return PRIVATE_VALUES;
	}

	public static final QAlgorithm[] PRIVATE_VALUES = 
		{
			NOT_SPECIFIED,
			FIXED,
			DYNAMIC,
					};

	public static final QAlgorithm Parse(String parameter)
	{
		String trimmedParameter = parameter.trim();
		if( !parameterLookUp.containsKey(trimmedParameter))
		{
			String message = String.format("'%s' is not recognised as a value of %s", parameter, QAlgorithm.class.getName());
			throw new IllegalArgumentException(message);
		}

		return parameterLookUp.get(trimmedParameter);
	}

	
	private static HashMap<String,QAlgorithm> parameterLookUp;
}
