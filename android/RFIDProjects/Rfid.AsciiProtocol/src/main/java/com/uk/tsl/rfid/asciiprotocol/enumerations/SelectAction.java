package com.uk.tsl.rfid.asciiprotocol.enumerations;

import java.util.HashMap;

/**---------------------------------------------------------------------------
* @author TSL Code Generator
*
*	 Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
----------------------------------------------------------------------------*/

/** 
* The action to perform to a transponder as it is selected
*/
public class SelectAction extends EnumerationBase
{                    
	/** 
	* The instance of Not Specified
	*/
			public static final SelectAction NOT_SPECIFIED = null;
    	                    
	/** 
	* The instance of Assert Set A Not Deassert Set B
	*/
			public static final SelectAction ASSERT_SET_A_NOT_DEASSERT_SET_B = new SelectAction("0", "Match: Assert Select / Set Session A  Non Match: Deassert Select / Set Session B");        
		                    
	/** 
	* The instance of Assert Set A Not Nothing Nothing
	*/
			public static final SelectAction ASSERT_SET_A_NOT_NOTHING_NOTHING = new SelectAction("1", "Match: Assert Select / Set Session A  Non Match: Nothing / Nothing");        
		                    
	/** 
	* The instance of Nothing Nothing Not Deassert Set B
	*/
			public static final SelectAction NOTHING_NOTHING_NOT_DEASSERT_SET_B = new SelectAction("2", "Match: Nothing / Nothing  Non Match: Deassert Select / Set Session B");        
		                    
	/** 
	* The instance of Toggle Toggle Not Nothing Nothing
	*/
			public static final SelectAction TOGGLE_TOGGLE_NOT_NOTHING_NOTHING = new SelectAction("3", "Match: Toggle / Toggle  Non Match: Nothing / Nothing");        
		                    
	/** 
	* The instance of Deassert Set B Not Assert Set A
	*/
			public static final SelectAction DEASSERT_SET_B_NOT_ASSERT_SET_A = new SelectAction("4", "Match: Deassert Select / Set Session B  Non Match: Assert Select / Set Session A");        
		                    
	/** 
	* The instance of Deassert Set B Not Nothing Nothing
	*/
			public static final SelectAction DEASSERT_SET_B_NOT_NOTHING_NOTHING = new SelectAction("5", "Match: Deassert Select / Set Session B  Non Match: Nothing / Nothing");        
		                    
	/** 
	* The instance of Nothing Nothing Not Assert Set A
	*/
			public static final SelectAction NOTHING_NOTHING_NOT_ASSERT_SET_A = new SelectAction("6", "Match: Nothing / Nothing  Non Match: Assert Select / Set Session A");        
		                    
	/** 
	* The instance of Nothing Nothing Not Toggle Toggle
	*/
			public static final SelectAction NOTHING_NOTHING_NOT_TOGGLE_TOGGLE = new SelectAction("7", "Match: Nothing / Nothing  Non Match: Toggle / Toggle");        
		
	/**
	 * Initializes a new instance of the SelectAction class
	 * 
	 * @param argument The argument as output to the command line
	 * @param description A human-readable description of the value
	 */
	private SelectAction(String argument, String description)
	{
		super(argument, description);
		if( parameterLookUp == null )
		{
			parameterLookUp = new HashMap<String,SelectAction>();
		}
		parameterLookUp.put(argument, this);
	}

	
	public static final SelectAction[] getValues()
	{
		return PRIVATE_VALUES;
	}

	public static final SelectAction[] PRIVATE_VALUES = 
		{
			NOT_SPECIFIED,
			ASSERT_SET_A_NOT_DEASSERT_SET_B,
			ASSERT_SET_A_NOT_NOTHING_NOTHING,
			NOTHING_NOTHING_NOT_DEASSERT_SET_B,
			TOGGLE_TOGGLE_NOT_NOTHING_NOTHING,
			DEASSERT_SET_B_NOT_ASSERT_SET_A,
			DEASSERT_SET_B_NOT_NOTHING_NOTHING,
			NOTHING_NOTHING_NOT_ASSERT_SET_A,
			NOTHING_NOTHING_NOT_TOGGLE_TOGGLE,
					};

	public static final SelectAction Parse(String parameter)
	{
		String trimmedParameter = parameter.trim();
		if( !parameterLookUp.containsKey(trimmedParameter))
		{
			String message = String.format("'%s' is not recognised as a value of %s", parameter, SelectAction.class.getName());
			throw new IllegalArgumentException(message);
		}

		return parameterLookUp.get(trimmedParameter);
	}

	
	private static HashMap<String,SelectAction> parameterLookUp;
}
