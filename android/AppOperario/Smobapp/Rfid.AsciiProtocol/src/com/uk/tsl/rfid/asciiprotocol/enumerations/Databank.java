package com.uk.tsl.rfid.asciiprotocol.enumerations;

import java.util.HashMap;

/**---------------------------------------------------------------------------
* @author TSL Code Generator
*
*	 Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
----------------------------------------------------------------------------*/

/** 
* Specifies the memory databank to use
*/
public class Databank extends EnumerationBase
{                    
	/** 
	* The instance of Not Specified
	*/
			public static final Databank NOT_SPECIFIED = null;
    	                    
	/** 
	* The instance of Electronic Product Code
	*/
			public static final Databank ELECTRONIC_PRODUCT_CODE = new Databank("epc", "The EPC databank");        
		                    
	/** 
	* The instance of Transponder Identifier
	*/
			public static final Databank TRANSPONDER_IDENTIFIER = new Databank("tid", "The TID databank");        
		                    
	/** 
	* The instance of User
	*/
			public static final Databank USER = new Databank("usr", "The User databank");        
		                    
	/** 
	* The instance of Reserved
	*/
			public static final Databank RESERVED = new Databank("res", "The Reserved databank");        
		
	/**
	 * Initializes a new instance of the Databank class
	 * 
	 * @param argument The argument as output to the command line
	 * @param description A human-readable description of the value
	 */
	private Databank(String argument, String description)
	{
		super(argument, description);
		if( parameterLookUp == null )
		{
			parameterLookUp = new HashMap<String,Databank>();
		}
		parameterLookUp.put(argument, this);
	}

	
	public static final Databank[] getValues()
	{
		return PRIVATE_VALUES;
	}

	public static final Databank[] PRIVATE_VALUES = 
		{
			NOT_SPECIFIED,
			ELECTRONIC_PRODUCT_CODE,
			TRANSPONDER_IDENTIFIER,
			USER,
			RESERVED,
					};

	public static final Databank Parse(String parameter)
	{
		String trimmedParameter = parameter.trim();
		if( !parameterLookUp.containsKey(trimmedParameter))
		{
			String message = String.format("'%s' is not recognised as a value of %s", parameter, Databank.class.getName());
			throw new IllegalArgumentException(message);
		}

		return parameterLookUp.get(trimmedParameter);
	}

	
	private static HashMap<String,Databank> parameterLookUp;
}
