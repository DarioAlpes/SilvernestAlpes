package com.uk.tsl.rfid.asciiprotocol;

import com.uk.tsl.rfid.asciiprotocol.parameters.AntennaParameters;

//-----------------------------------------------------------------------
//Copyright (c) 2013-2016 Technology Solutions UK Ltd. All rights reserved.
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


    public static final int MINIMUM_ANTENNA_OUTPUT_POWER_LIMIT = 2;
    public static final int MAXIMUM_ANTENNA_OUTPUT_POWER_LIMIT = 30;

    private static final int MINIMUM_ANTENNA_OUTPUT_POWER_DEFAULT = 10;
    private static final int MAXIMUM_ANTENNA_OUTPUT_POWER_DEFAULT = 29;

    private static final int MINIMUM_ANTENNA_OUTPUT_POWER_1128 = 10;
    private static final int MAXIMUM_ANTENNA_OUTPUT_POWER_1128 = 29;
    private static final int MAXIMUM_ANTENNA_OUTPUT_POWER_1128_JP = 27;

    private static final int MINIMUM_ANTENNA_OUTPUT_POWER_1153 = 10;
    private static final int MAXIMUM_ANTENNA_OUTPUT_POWER_1153 = 25;

    private static final int MINIMUM_ANTENNA_OUTPUT_POWER_1166 = 2;
    private static final int MAXIMUM_ANTENNA_OUTPUT_POWER_1166 = 30;

    private int mMinimumCarrierPower;
	private int mMaximumCarrierPower;

	/**
	 * Default DeviceProperties
	 */
	public DeviceProperties()
	{
        setDefaults();
	}


	/**
	 * Creates an instance for the device identified by the given serial number
	 * 
	 * @param serialNumber the serial number - starting with 4 digit device identifier e.g. "1128"
	 */
	public DeviceProperties(String serialNumber)
	{
        if( serialNumber == null || serialNumber.length() < 4 )
        {
            setDefaults();
        }
        else
        {

            String ucSerialNumber = serialNumber.toUpperCase(Constants.COMMAND_LOCALE);

            if (ucSerialNumber.startsWith("1128"))
            {
                mMinimumCarrierPower = MINIMUM_ANTENNA_OUTPUT_POWER_1128;

                if (ucSerialNumber.length() >= 7 && ucSerialNumber.startsWith("1128-JP"))
                {
                    mMaximumCarrierPower = MAXIMUM_ANTENNA_OUTPUT_POWER_1128_JP;
                }
                else
                {
                    mMaximumCarrierPower = MAXIMUM_ANTENNA_OUTPUT_POWER_1128;
                }

            }
            else if (ucSerialNumber.startsWith("1153"))
            {
                mMinimumCarrierPower = MINIMUM_ANTENNA_OUTPUT_POWER_1153;
                mMaximumCarrierPower = MAXIMUM_ANTENNA_OUTPUT_POWER_1153;
            }
            else if (ucSerialNumber.startsWith("1166"))
            {
                mMinimumCarrierPower = MINIMUM_ANTENNA_OUTPUT_POWER_1166;
                mMaximumCarrierPower = MAXIMUM_ANTENNA_OUTPUT_POWER_1166;
            }
            else
            {
                // Unrecognised reader so use defaults
                setDefaults();
            }
        }
	}


    private void setDefaults()
    {
        // Default to the typical ASCII protocol limits
        mMinimumCarrierPower = MINIMUM_ANTENNA_OUTPUT_POWER_DEFAULT;
        mMaximumCarrierPower = MAXIMUM_ANTENNA_OUTPUT_POWER_DEFAULT;
    }
}
