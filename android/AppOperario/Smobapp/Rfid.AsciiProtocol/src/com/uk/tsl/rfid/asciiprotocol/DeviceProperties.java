package com.uk.tsl.rfid.asciiprotocol;

import com.uk.tsl.rfid.asciiprotocol.parameters.AntennaParameters;

//-----------------------------------------------------------------------
//Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//Authors: Brian Painter
//-----------------------------------------------------------------------

/**
 * Devices can have command limits that are different than those supported
 * by the ASCII Protocol e.g. Maximum Antenna power may vary between UHF
 * reader devices
 * 
 * This class provides access to these limits for the currently connected
 * Device
 *
 */
public class DeviceProperties
{
	public static final DeviceProperties DEVICE_DEFAULTS = new DeviceProperties();

	/**
	 * @return the minimumCarrierPower
	 */
	public final int getMinimumCarrierPower()
	{
		return mMinimumCarrierPower;
	}

	/**
	 * @return the maximumCarrierPower
	 */
	public final int getMaximumCarrierPower()
	{
		return mMaximumCarrierPower;
	}

	private int mMinimumCarrierPower;
	private int mMaximumCarrierPower;

	/**
	 * Default DeviceProperties
	 */
	public DeviceProperties()
	{
		// Default to the ASCII protocol limits
		mMinimumCarrierPower = AntennaParameters.MinimumCarrierPower;
		mMaximumCarrierPower = AntennaParameters.MaximumCarrierPower;
	}

	/**
	 * Creates an instance for the given device type
	 * 
	 * @param deviceType the 4 digit device identifier e.g. "1128"
	 */
	public DeviceProperties(String deviceType)
	{
		// Set defaults
		this();

		// Override defaults where necessary
		if( deviceType.contains("1153") )
		{
			mMaximumCarrierPower = 25;
		}
	}
}
