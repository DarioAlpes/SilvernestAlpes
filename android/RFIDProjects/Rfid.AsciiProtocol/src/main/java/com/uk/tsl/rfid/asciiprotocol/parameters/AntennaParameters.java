package com.uk.tsl.rfid.asciiprotocol.parameters;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

import com.uk.tsl.rfid.asciiprotocol.DeviceProperties;

/**
 Helper class for implementing IAntennaParameters
*/
public final class AntennaParameters
{
	/** 
	 The value of output power if it is not specified (-1)
	*/
	public static final int OutputPowerNotSpecified = -1;

	/** 
	 The minimum permitted carrier power in dBm
	*/
	public static final int MinimumCarrierPower = DeviceProperties.MINIMUM_ANTENNA_OUTPUT_POWER_LIMIT;

	/**
	 The maximum permitted carrier power in dBm
	*/
	public static final int MaximumCarrierPower = DeviceProperties.MAXIMUM_ANTENNA_OUTPUT_POWER_LIMIT;

	/** 
	 Append the parameters from source that have been specified to the command line
	 
	 @param source The parameters to append
	 @param line The line to append the specified parameters to
	*/
	public static void appendToCommandLine(IAntennaParameters source, StringBuilder line)
	{
		if (source.getOutputPower() != OutputPowerNotSpecified)
		{
			if (source.getOutputPower() < MinimumCarrierPower || source.getOutputPower() > MaximumCarrierPower )
			{
				String reasonMessage = String.format("Output power argument (%s) is invalid", source.getOutputPower() );

				throw new IllegalArgumentException(reasonMessage);
			}

			line.append(String.format("-o%02d", source.getOutputPower()));
		}
	}

	/** 
	 Parses the specified parameter and sets the appropriate property in value
	 
	 @param value The parameters to update
	 @param parameter The parameter to parse
	 @return True if parameter was parsed
	*/
	public static boolean parseParameterFor(IAntennaParameters value, String parameter)
	{
		if (parameter.length() > 1)
		{
			// Decode parameters
			if (parameter.startsWith("o"))
			{
				value.setOutputPower( Integer.parseInt(String.format( parameter.substring(1).trim())));
				return true;
			}
		}

		return false;
	}

	/** 
	 Sets value to the parameter defaults
	 
	 @param value The parameters to set to defaults
	*/
	public static void setDefaultParametersFor(IAntennaParameters value)
	{
		value.setOutputPower(OutputPowerNotSpecified);
	}
}