package com.uk.tsl.rfid.asciiprotocol.parameters;

import com.uk.tsl.rfid.asciiprotocol.Constants;
import com.uk.tsl.rfid.asciiprotocol.enumerations.Databank;
import com.uk.tsl.utils.HexEncoding;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 Helper class for implementing IDatabankParameters
*/
public final class DatabankParameters
{
	/** 
	 Append the parameters from source that have been specified to the command line
	 
	 @param source The parameters to append
	 @param line The line to append the specified parameters to
	*/
	public static void appendToCommandLine(IDatabankParameters source, StringBuilder line)
	{
		if (source.getBank() != Databank.NOT_SPECIFIED)
		{
//			source.getBank().Validate();

			line.append(String.format(Constants.COMMAND_LOCALE, "-db%s", source.getBank().getArgument()));
		}

		if (source.getOffset() >= 0)
		{
			line.append(String.format(Constants.COMMAND_LOCALE, "-do%04x", source.getOffset()));
		}

		if (source.getLength() >= 0)
		{
			line.append(String.format(Constants.COMMAND_LOCALE, "-dl%02x", source.getLength()));
		}

		if (source.getData() != null)
		{
			if (source.getData().length > 64)
			{
				String reasonMessage = String.format(Constants.COMMAND_LOCALE, "Data block too big (%d bytes) should be up to 32 words.", source.getData().length);
				throw new IllegalArgumentException(reasonMessage);
			}

			line.append("-da");
			line.append(HexEncoding.bytesToString(source.getData()));
		}
	}

	/** 
	 Parses the specified parameter and sets the appropriate property in value
	 
	 @param value The parameters to update
	 @param parameter The parameter to parse
	 @return True if parameter was parsed
	*/
	public static boolean parseParameterFor(IDatabankParameters value, String parameter)
	{
		if (parameter.length() >= 2)
		{
			if (parameter.charAt(0) == 'd')
			{
				switch (parameter.charAt(1))
				{
					case 'a':
					{
							// No data is ever stored and returned for this parameter
							return true;
					}

					case 'b':
					{
							value.setBank(Databank.Parse(parameter.substring(2)));
							return true;
					}

					case 'o':
					{
							value.setOffset(Integer.parseInt(parameter.substring(2).trim(), 16));
							return true;
					}

					case 'l':
					{
							value.setLength(Integer.parseInt(parameter.substring(2).trim(), 16));
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
	public static void setDefaultParametersFor(IDatabankParameters value)
	{
		value.setBank(Databank.NOT_SPECIFIED);
		value.setOffset(-1);
		value.setLength(-1);
	}
}