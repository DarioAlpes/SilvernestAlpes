package com.uk.tsl.rfid.asciiprotocol.parameters;

import com.uk.tsl.rfid.asciiprotocol.Constants;
import com.uk.tsl.rfid.asciiprotocol.enumerations.Databank;

//-----------------------------------------------------------------------
//  Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
Helper class for implementing ISelectMaskParameters
*/
public final class SelectMaskParameters {
	/** 
	 Append the parameters from source that have been specified to the command line
	 
	 @param source The parameters to append
	 @param line The line to append the specified parameters to
	*/
	public static void appendToCommandLine(ISelectMaskParameters source, StringBuilder line)
	{
		if (source.getSelectBank() != Databank.NOT_SPECIFIED)
		{
			line.append(String.format("-sb%s", source.getSelectBank().getArgument()));
		}

		if (source.getSelectOffset() >= 0)
		{
			line.append(String.format("-so%04x", source.getSelectOffset()));
		}

		if (source.getSelectLength() >= 0)
		{
			line.append(String.format("-sl%02x", source.getSelectLength()));
		}

		if (source.getSelectData() != null)
		{
			// TODO: validate hex string
			if (source.getSelectData().length() > 64)
			{
				String reasonMessage = String.format(Constants.ERROR_LOCALE, "Data block too big (%d ascii hex bytes) should be up to 32 ascii hex .", source.getSelectData().length());

				throw new IllegalArgumentException(reasonMessage);
			}

			line.append(String.format("-sd%s", source.getSelectData()));
		}
	}

	/** 
	 Parses the specified parameter and sets the appropriate property in value
	 
	 @param parameter The parameter to parse
	 @param value The parameters to update
	 @return True if parameter was parsed
	*/
	public static boolean parseParameterFor(ISelectMaskParameters value, String parameter)
	{
		if (parameter.length() >= 2)
		{
			if (parameter.charAt(0) == 's')
			{
				switch (parameter.charAt(1))
				{
					case 'd':
					{
							// No data is ever stored and returned for this parameter
							return true;
					}

					case 'b':
					{
							value.setSelectBank(Databank.Parse(parameter.substring(2)));
							return true;
					}

					case 'o':
					{
							value.setSelectOffset(Integer.parseInt(String.format(Constants.COMMAND_LOCALE, parameter.substring(2).trim()), 16));
							return true;
					}

					case 'l':
					{
							value.setSelectLength(Integer.parseInt(String.format(Constants.COMMAND_LOCALE, parameter.substring(2).trim()), 16));
							return true;
					}

					default:
						break;
				}
			}
		}

		return false;
	}

	/** 
	 Sets value to the parameter defaults
	 
	 @param value The parameters to set to defaults
	*/
	public static void setDefaultParametersFor(ISelectMaskParameters value)
	{
		value.setSelectBank(Databank.NOT_SPECIFIED);
		value.setSelectData(null);
		value.setSelectOffset(-1);
		value.setSelectLength(-1);
	}

}
