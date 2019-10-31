package com.uk.tsl.rfid.asciiprotocol.parameters;

import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 Helper class for implementing IResponseParameters
*/
public final class ResponseParameters
{
	/** 
	 Append the parameters from source that have been specified to the command line
	 
	 @param source The parameters to append
	 @param line The line to append the specified parameters to
	*/
	public static void appendToCommandLine(IResponseParameters source, StringBuilder line)
	{
		appendToCommandLine(source, line, true);
	}

	/** 
	 Append the parameters from source that have been specified to the command line
	 
	 @param source The parameters to append
	 @param line The line to append the specified parameters to
	 @param includeAlert true if the alert parameter should be included on the command line (lock and kill do not use this currently)
	*/
	public static void appendToCommandLine(IResponseParameters source, StringBuilder line, boolean includeAlert)
	{
		if (includeAlert && source.getUseAlert() != TriState.NOT_SPECIFIED)
		{
			// line = [line stringByAppendingFormat:@"-al%@", [TSLTriState parameterValueForState:source.useAlert]];
			line.append(String.format("-al%s", source.getUseAlert().getArgument()));
		}

		if (source.getIncludeDateTime() != TriState.NOT_SPECIFIED)
		{
			// line = [line stringByAppendingFormat:@"-dt%@", [TSLTriState parameterValueForState:source.includeDateTime]];
			line.append(String.format("-dt%s", source.getIncludeDateTime().getArgument()));
		}
	}

	/** 
	 Parses the specified parameter and sets the appropriate property in value
	 
	 @param value The parameters to update
	 @param parameter The parameter to parse
	 @return True if parameter was parsed
	*/
	public static boolean parseParameterFor(IResponseParameters value, String parameter)
	{
		if (parameter.length() > 3)
		{
			// Decode parameters
			if (parameter.startsWith("al"))
			{
				value.setUseAlert(TriState.Parse(parameter.substring(2)));
				return true;
			}

			if (parameter.startsWith("dt"))
			{
				value.setIncludeDateTime(TriState.Parse(parameter.substring(2)));
				return true;
			}
		}

		return false;
	}

	/** 
	 Sets value to the parameter defaults
	 
	 @param value The parameters to set to defaults
	*/
	public static void setDefaultParametersFor(IResponseParameters value)
	{
		value.setUseAlert(TriState.NOT_SPECIFIED);
		value.setIncludeDateTime(TriState.NOT_SPECIFIED);
	}
}