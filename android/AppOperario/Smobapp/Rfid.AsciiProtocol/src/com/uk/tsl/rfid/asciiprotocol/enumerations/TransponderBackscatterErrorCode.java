package com.uk.tsl.rfid.asciiprotocol.enumerations;

import java.util.HashMap;

/**---------------------------------------------------------------------------
* @author TSL Code Generator
*
*	 Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
----------------------------------------------------------------------------*/

/** 
* Specifies the error response for Backscatter Errors
*/
public class TransponderBackscatterErrorCode extends EnumerationBase
{                    
	/** 
	* The instance of Not Specified
	*/
			public static final TransponderBackscatterErrorCode NOT_SPECIFIED = null;
    	                    
	/** 
	* The instance of General error
	*/
			public static final TransponderBackscatterErrorCode GENERAL_ERROR = new TransponderBackscatterErrorCode("000", "General error.");        
		                    
	/** 
	* The instance of Memory does not exist or the PC value is not supported
	*/
			public static final TransponderBackscatterErrorCode MEMORY_DOES_NOT_EXIST_OR_THE_PC_VALUE_IS_NOT_SUPPORTED = new TransponderBackscatterErrorCode("003", "Memory does not exist or the PC value is not supported.");        
		                    
	/** 
	* The instance of Memory is lock or permalocked
	*/
			public static final TransponderBackscatterErrorCode MEMORY_IS_LOCK_OR_PERMALOCKED = new TransponderBackscatterErrorCode("004", "Memory is lock or permalocked.");        
		                    
	/** 
	* The instance of Transponder has insufficient power
	*/
			public static final TransponderBackscatterErrorCode TRANSPONDER_HAS_INSUFFICIENT_POWER = new TransponderBackscatterErrorCode("011", "Transponder has insufficient power.");        
		                    
	/** 
	* The instance of Transponder does not support error specific codes
	*/
			public static final TransponderBackscatterErrorCode TRANSPONDER_DOES_NOT_SUPPORT_ERROR_SPECIFIC_CODES = new TransponderBackscatterErrorCode("015", "Transponder does not support error specific codes.");        
		
	/**
	 * Initializes a new instance of the TransponderBackscatterErrorCode class
	 * 
	 * @param argument The argument as output to the command line
	 * @param description A human-readable description of the value
	 */
	private TransponderBackscatterErrorCode(String argument, String description)
	{
		super(argument, description);
		if( parameterLookUp == null )
		{
			parameterLookUp = new HashMap<String,TransponderBackscatterErrorCode>();
		}
		parameterLookUp.put(argument, this);
	}

	
	public static final TransponderBackscatterErrorCode[] getValues()
	{
		return PRIVATE_VALUES;
	}

	public static final TransponderBackscatterErrorCode[] PRIVATE_VALUES = 
		{
			NOT_SPECIFIED,
			GENERAL_ERROR,
			MEMORY_DOES_NOT_EXIST_OR_THE_PC_VALUE_IS_NOT_SUPPORTED,
			MEMORY_IS_LOCK_OR_PERMALOCKED,
			TRANSPONDER_HAS_INSUFFICIENT_POWER,
			TRANSPONDER_DOES_NOT_SUPPORT_ERROR_SPECIFIC_CODES,
					};

	public static final TransponderBackscatterErrorCode Parse(String parameter)
	{
		String trimmedParameter = parameter.trim();
		if( !parameterLookUp.containsKey(trimmedParameter))
		{
			String message = String.format("'%s' is not recognised as a value of %s", parameter, TransponderBackscatterErrorCode.class.getName());
			throw new IllegalArgumentException(message);
		}

		return parameterLookUp.get(trimmedParameter);
	}

	
	private static HashMap<String,TransponderBackscatterErrorCode> parameterLookUp;
}
