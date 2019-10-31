package com.uk.tsl.rfid.asciiprotocol.enumerations;

import java.util.HashMap;

/**---------------------------------------------------------------------------
* @author TSL Code Generator
*
*	 Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
----------------------------------------------------------------------------*/

/** 
* Specifies the error response for Access Errors
*/
public class TransponderAccessErrorCode extends EnumerationBase
{                    
	/** 
	* The instance of Not Specified
	*/
			public static final TransponderAccessErrorCode NOT_SPECIFIED = null;
    	                    
	/** 
	* The instance of Handle Mismatch
	*/
			public static final TransponderAccessErrorCode HANDLE_MISMATCH = new TransponderAccessErrorCode("001", "Handle mismatch.");        
		                    
	/** 
	* The instance of CRC error on transponder response
	*/
			public static final TransponderAccessErrorCode CRC_ERROR_ON_TRANSPONDER_RESPONSE = new TransponderAccessErrorCode("002", "CRC error on transponder response.");        
		                    
	/** 
	* The instance of No transponder reply
	*/
			public static final TransponderAccessErrorCode NO_TRANSPONDER_REPLY = new TransponderAccessErrorCode("003", "No transponder reply.");        
		                    
	/** 
	* The instance of Invalid password
	*/
			public static final TransponderAccessErrorCode INVALID_PASSWORD = new TransponderAccessErrorCode("004", "Invalid password.");        
		                    
	/** 
	* The instance of Zero kill password
	*/
			public static final TransponderAccessErrorCode ZERO_KILL_PASSWORD = new TransponderAccessErrorCode("005", "Zero kill password.");        
		                    
	/** 
	* The instance of Transponder lost
	*/
			public static final TransponderAccessErrorCode TRANSPONDER_LOST = new TransponderAccessErrorCode("006", "Transponder lost.");        
		                    
	/** 
	* The instance of Command format error
	*/
			public static final TransponderAccessErrorCode COMMAND_FORMAT_ERROR = new TransponderAccessErrorCode("007", "Command format error.");        
		                    
	/** 
	* The instance of Read count invalid
	*/
			public static final TransponderAccessErrorCode READ_COUNT_INVALID = new TransponderAccessErrorCode("008", "Read count invalid.");        
		                    
	/** 
	* The instance of Out of retries
	*/
			public static final TransponderAccessErrorCode OUT_OF_RETRIES = new TransponderAccessErrorCode("009", "Out of retries.");        
		                    
	/** 
	* The instance of Operation failed
	*/
			public static final TransponderAccessErrorCode OPERATION_FAILED = new TransponderAccessErrorCode("255", "Operation failed.");        
		
	/**
	 * Initializes a new instance of the TransponderAccessErrorCode class
	 * 
	 * @param argument The argument as output to the command line
	 * @param description A human-readable description of the value
	 */
	private TransponderAccessErrorCode(String argument, String description)
	{
		super(argument, description);
		if( parameterLookUp == null )
		{
			parameterLookUp = new HashMap<String,TransponderAccessErrorCode>();
		}
		parameterLookUp.put(argument, this);
	}

	
	public static final TransponderAccessErrorCode[] getValues()
	{
		return PRIVATE_VALUES;
	}

	public static final TransponderAccessErrorCode[] PRIVATE_VALUES = 
		{
			NOT_SPECIFIED,
			HANDLE_MISMATCH,
			CRC_ERROR_ON_TRANSPONDER_RESPONSE,
			NO_TRANSPONDER_REPLY,
			INVALID_PASSWORD,
			ZERO_KILL_PASSWORD,
			TRANSPONDER_LOST,
			COMMAND_FORMAT_ERROR,
			READ_COUNT_INVALID,
			OUT_OF_RETRIES,
			OPERATION_FAILED,
					};

	public static final TransponderAccessErrorCode Parse(String parameter)
	{
		String trimmedParameter = parameter.trim();
		if( !parameterLookUp.containsKey(trimmedParameter))
		{
			String message = String.format("'%s' is not recognised as a value of %s", parameter, TransponderAccessErrorCode.class.getName());
			throw new IllegalArgumentException(message);
		}

		return parameterLookUp.get(trimmedParameter);
	}

	
	private static HashMap<String,TransponderAccessErrorCode> parameterLookUp;
}
