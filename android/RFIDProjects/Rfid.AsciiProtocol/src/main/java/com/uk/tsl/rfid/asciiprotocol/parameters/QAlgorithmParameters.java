package com.uk.tsl.rfid.asciiprotocol.parameters;

import com.uk.tsl.rfid.asciiprotocol.Constants;
import com.uk.tsl.rfid.asciiprotocol.enumerations.QAlgorithm;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 Helper class for implementing IQAlgorithmParameters
*/
public final class QAlgorithmParameters
{
	/** 
	 Append the parameters from source that have been specified to the command line
	 
	 @param source The parameters to append
	 @param line The line to append the specified parameters to
	*/
	public static void appendToCommandLine(IQAlgorithmParameters source, StringBuilder line)
	{
		appendToCommandLine(source, line, true);
	}

	/** 
	 Append the parameters from source that have been specified to the command line
	 
	 @param source The parameters to append
	 @param line The line to append the specified parameters to
	 * @param useAlgorithm control the inclusion of the QAlgorithm parameter
	*/
	public static void appendToCommandLine(IQAlgorithmParameters source, StringBuilder line, boolean useAlgorithm)
	{
		if (useAlgorithm && source.getQAlgorithm() != QAlgorithm.NOT_SPECIFIED)
		{
			line.append(String.format(Constants.COMMAND_LOCALE, "-qa%s", source.getQAlgorithm().getArgument()));
		}

		if (source.getQValue() >= 0)
		{
			if (source.getQValue() >= 16)
			{
				String reasonMessage = String.format(Constants.ERROR_LOCALE, "Q value argument (%s) should be in the range [%s - %s].", source.getQValue(), 0, 15);

				throw new IllegalArgumentException(reasonMessage);
			}

			// line = [line stringByAppendingFormat:@"-qv%d", source.qValue];
			line.append(String.format(Constants.COMMAND_LOCALE, "-qv%d", source.getQValue()));
		}
	}

	/** 
	 Parses the specified parameter and sets the appropriate property in value
	 
	 @param value The parameters to update
	 @param parameter The parameter to parse
	 @return True if parameter was parsed
	*/
	public static boolean parseParameterFor(IQAlgorithmParameters value, String parameter)
	{
		if (parameter.length() > 2)
		{
			if (parameter.charAt(0) == 'q')
			{
				switch (parameter.charAt(1))
				{
					case 'a':
					{
							value.setQAlgorithm(QAlgorithm.Parse(parameter.substring(2)));
							return true;
					}

					case 'v':
					{
							value.setQValue(Integer.parseInt(String.format(Constants.COMMAND_LOCALE, parameter.substring(2).trim())));
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
	public static void setDefaultParametersFor(IQAlgorithmParameters value)
	{
		value.setQAlgorithm(QAlgorithm.NOT_SPECIFIED);
		value.setQValue(-1);
	}
}