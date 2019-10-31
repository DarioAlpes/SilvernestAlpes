package com.uk.tsl.rfid.asciiprotocol.parameters;

import com.uk.tsl.rfid.asciiprotocol.Constants;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.utils.StringHelper;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 Helper class for implementing ITransponderParameters
*/
public final class TransponderParameters
{
	/** 
	 Append the parameters from source that have been specified to the command line
	 
	 @param source The parameters to append
	 @param line The line to append the specified parameters to
	*/
	public static void appendToCommandLine(ITransponderParameters source, StringBuilder line)
	{
		if (source.getIncludeChecksum() != TriState.NOT_SPECIFIED)
		{
			line.append(String.format("-c%s", source.getIncludeChecksum().getArgument()));
		}

		if (source.getIncludePC() != TriState.NOT_SPECIFIED)
		{
			line.append(String.format("-e%s", source.getIncludePC().getArgument()));
		}

		if (source.getIncludeTransponderRssi() != TriState.NOT_SPECIFIED)
		{
			line.append(String.format("-r%s", source.getIncludeTransponderRssi().getArgument()));
		}

		if (source.getIncludeIndex() != TriState.NOT_SPECIFIED)
		{
			line.append(String.format("-ix%s", source.getIncludeIndex().getArgument()));
		}

		if (!StringHelper.isNullOrEmpty(source.getAccessPassword()))
		{
			if (source.getAccessPassword().length() != 8)
			{
				String reasonMessage = String.format(Constants.ERROR_LOCALE, "Invalid access password length (%s). Must be 8 Ascii hex chars.", source.getAccessPassword().length());
				throw new IllegalArgumentException(reasonMessage);
			}

			line.append(String.format("-ap%s", source.getAccessPassword()));
		}
	}

	/** 
	 Parses the specified parameter and sets the appropriate property in value
	 
	 @param value The parameters to update
	 @param parameter The parameter to parse
	 @return True if parameter was parsed
	*/
	public static boolean parseParameterFor(ITransponderParameters value, String parameter)
	{
		if (parameter.length() > 0)
		{
			// Decode parameters
			switch (parameter.charAt(0))
			{
				case 'i':
				{
						// Decode the 'i' parameters
						if (parameter.length() > 1)
						{
							switch (parameter.charAt(1))
							{
								case 'x':
								{
										value.setIncludeIndex(TriState.Parse(parameter.substring(2)));
										return true;
								}

								default:
									break;
							}
						}
				}

					break;

				case 'a':
				{
						// Decode the 'a' parameters
						if (parameter.length() > 1)
						{
							switch (parameter.charAt(1))
							{
								case 'p':
								{
										value.setAccessPassword(parameter.substring(2).trim());
										return true;
								}

								default:
									break;
							}
						}
				}

					break;

				case 'c':
				{
						value.setIncludeChecksum(TriState.Parse(parameter.substring(1)));
						return true;
				}

				case 'e':
				{
						value.setIncludePC(TriState.Parse(parameter.substring(1)));
						return true;
				}

				case 'r':
				{
						value.setIncludeTransponderRssi(TriState.Parse(parameter.substring(1)));
						return true;
				}

				default:
					break;
			}
		}

		return false;
	}

	/** 
	 Sets value to the parameter defaults
	 
	 @param value The parameters to set to defaults
	*/
	public static void setDefaultParametersFor(ITransponderParameters value)
	{
		value.setIncludeChecksum(TriState.NOT_SPECIFIED);
		value.setIncludePC(TriState.NOT_SPECIFIED);
		value.setIncludeTransponderRssi(TriState.NOT_SPECIFIED);
		value.setIncludeIndex(TriState.NOT_SPECIFIED);
		value.setAccessPassword("");
	}
}