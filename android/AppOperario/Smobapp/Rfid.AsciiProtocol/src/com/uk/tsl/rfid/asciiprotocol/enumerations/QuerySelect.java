package com.uk.tsl.rfid.asciiprotocol.enumerations;

import java.util.HashMap;

/**---------------------------------------------------------------------------
* @author TSL Code Generator
*
*	 Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
----------------------------------------------------------------------------*/

/** 
* Specifies whether and how the select flag is used to determine which transponders to return
*/
public class QuerySelect extends EnumerationBase
{                    
	/** 
	* The instance of Not Specified
	*/
			public static final QuerySelect NOT_SPECIFIED = null;
    	                    
	/** 
	* The instance of All
	*/
			public static final QuerySelect ALL = new QuerySelect("all", "All (selected and not selected transponders)");        
		                    
	/** 
	* The instance of Not Selected
	*/
			public static final QuerySelect NOT_SELECTED = new QuerySelect("nsl", "Not selected transponders only");        
		                    
	/** 
	* The instance of Selected
	*/
			public static final QuerySelect SELECTED = new QuerySelect("sl", "Selected transponders only");        
		
	/**
	 * Initializes a new instance of the QuerySelect class
	 * 
	 * @param argument The argument as output to the command line
	 * @param description A human-readable description of the value
	 */
	private QuerySelect(String argument, String description)
	{
		super(argument, description);
		if( parameterLookUp == null )
		{
			parameterLookUp = new HashMap<String,QuerySelect>();
		}
		parameterLookUp.put(argument, this);
	}

	
	public static final QuerySelect[] getValues()
	{
		return PRIVATE_VALUES;
	}

	public static final QuerySelect[] PRIVATE_VALUES = 
		{
			NOT_SPECIFIED,
			ALL,
			NOT_SELECTED,
			SELECTED,
					};

	public static final QuerySelect Parse(String parameter)
	{
		String trimmedParameter = parameter.trim();
		if( !parameterLookUp.containsKey(trimmedParameter))
		{
			String message = String.format("'%s' is not recognised as a value of %s", parameter, QuerySelect.class.getName());
			throw new IllegalArgumentException(message);
		}

		return parameterLookUp.get(trimmedParameter);
	}

	
	private static HashMap<String,QuerySelect> parameterLookUp;
}
