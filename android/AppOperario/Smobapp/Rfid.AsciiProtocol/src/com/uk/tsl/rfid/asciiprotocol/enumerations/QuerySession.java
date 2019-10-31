package com.uk.tsl.rfid.asciiprotocol.enumerations;

import java.util.HashMap;

/**---------------------------------------------------------------------------
* @author TSL Code Generator
*
*	 Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
----------------------------------------------------------------------------*/

/** 
* Specifies the Query session
*/
public class QuerySession extends EnumerationBase
{                    
	/** 
	* The instance of Not Specified
	*/
			public static final QuerySession NOT_SPECIFIED = null;
    	                    
	/** 
	* The instance of Session 0
	*/
			public static final QuerySession SESSION_0 = new QuerySession("s0", "Target session 0");        
		                    
	/** 
	* The instance of Session 1
	*/
			public static final QuerySession SESSION_1 = new QuerySession("s1", "Target session 1");        
		                    
	/** 
	* The instance of Session 2
	*/
			public static final QuerySession SESSION_2 = new QuerySession("s2", "Target session 2");        
		                    
	/** 
	* The instance of Session 3
	*/
			public static final QuerySession SESSION_3 = new QuerySession("s3", "Target session 3");        
		
	/**
	 * Initializes a new instance of the QuerySession class
	 * 
	 * @param argument The argument as output to the command line
	 * @param description A human-readable description of the value
	 */
	private QuerySession(String argument, String description)
	{
		super(argument, description);
		if( parameterLookUp == null )
		{
			parameterLookUp = new HashMap<String,QuerySession>();
		}
		parameterLookUp.put(argument, this);
	}

	
	public static final QuerySession[] getValues()
	{
		return PRIVATE_VALUES;
	}

	public static final QuerySession[] PRIVATE_VALUES = 
		{
			NOT_SPECIFIED,
			SESSION_0,
			SESSION_1,
			SESSION_2,
			SESSION_3,
					};

	public static final QuerySession Parse(String parameter)
	{
		String trimmedParameter = parameter.trim();
		if( !parameterLookUp.containsKey(trimmedParameter))
		{
			String message = String.format("'%s' is not recognised as a value of %s", parameter, QuerySession.class.getName());
			throw new IllegalArgumentException(message);
		}

		return parameterLookUp.get(trimmedParameter);
	}

	
	private static HashMap<String,QuerySession> parameterLookUp;
}
