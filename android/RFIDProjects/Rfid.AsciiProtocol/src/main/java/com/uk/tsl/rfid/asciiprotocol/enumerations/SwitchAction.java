package com.uk.tsl.rfid.asciiprotocol.enumerations;

import java.util.HashMap;

/**---------------------------------------------------------------------------
* @author TSL Code Generator
*
*	 Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
----------------------------------------------------------------------------*/

/** 
* Specifies the action to perform which a single or double press is active
*/
public class SwitchAction extends EnumerationBase
{                    
	/** 
	* The instance of No Change
	*/
	public static final SwitchAction NO_CHANGE = null;

    /**
     * The instance of Off
     */
    public static final SwitchAction OFF = new SwitchAction("off", "Off - no action");

    /**
     * The instance of Read
     */
    public static final SwitchAction READ = new SwitchAction("rd", "Read - perform the command with the last specified parameters");

    /**
     * The instance of Write
     */
    public static final SwitchAction WRITE = new SwitchAction("wr", "Write - perform the command with the last specified parameters");

    /**
     * The instance of Inventory
     */
    public static final SwitchAction INVENTORY = new SwitchAction("inv", "Inventory - perform the command with the last specified parameters");

    /**
     * The instance of Barcode
     */
    public static final SwitchAction BARCODE = new SwitchAction("bar", "Barcode - perform the command with the last specified parameters");

    /**
     * The instance of User
     */
    public static final SwitchAction USER = new SwitchAction("usr", "Perform the user specified command");

	/**
	 * The instance of AntennaDefault
     *
     * Different antennas have different primary and secondary functions. Where an antenna supports
     * RFID and barcode then the primary (single press) default may be RFID inventory and the secondary
     * (double press) default may be barcode. If the antenna is a barcode only both actions may default
     * to barcode. This value states that the switch should perform the default action for the antenna
	 */
    public static final SwitchAction ANTENNA_DEFAULT = new SwitchAction("ad", "Antenna Default");

	/**
	 * Initializes a new instance of the SwitchAction class
	 * 
	 * @param argument The argument as output to the command line
	 * @param description A human-readable description of the value
	 */
	private SwitchAction(String argument, String description)
	{
		super(argument, description);
		if( parameterLookUp == null )
		{
			parameterLookUp = new HashMap<String,SwitchAction>();
		}
		parameterLookUp.put(argument, this);
	}

	
	public static final SwitchAction[] getValues()
	{
		return PRIVATE_VALUES;
	}

	public static final SwitchAction[] PRIVATE_VALUES = 
    {
        NO_CHANGE,
        OFF,
        READ,
        WRITE,
        INVENTORY,
        BARCODE,
        USER,
        ANTENNA_DEFAULT,
    };

	public static final SwitchAction Parse(String parameter)
	{
		String trimmedParameter = parameter.trim();
		if( !parameterLookUp.containsKey(trimmedParameter))
		{
			String message = String.format("'%s' is not recognised as a value of %s", parameter, SwitchAction.class.getName());
			throw new IllegalArgumentException(message);
		}

		return parameterLookUp.get(trimmedParameter);
	}

	
	private static HashMap<String,SwitchAction> parameterLookUp;
}
