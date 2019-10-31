package com.uk.tsl.rfid.asciiprotocol.parameters;

import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 Helper class for implementing ICommandParameters
 @see ICommandParameters
*/
public final class CommandParameters
{
	/** 
	 Append the parameters from source that have been specified to the command line
	 
	 @param source The parameters to append
	 @param line The line to append the specified parameters to
	*/
	public static void appendToCommandLine(ICommandParameters source, StringBuilder line)
	{
		if (source.implementsTakeNoAction() && source.getTakeNoAction() == TriState.YES)
		{
			line.append("-n");
		}

		if (source.implementsReadParameters() && source.getReadParameters() == TriState.YES)
		{
			line.append("-p");
		}

		if (source.implementsResetParameters() && source.getResetParameters() == TriState.YES)
		{
			line.append("-x");
		}
	}

	/** 
	 Parses the specified parameter and sets the appropriate property in value
	 
	 @param value The parameters to update
	 @param parameter The parameter to parse
	 @return True if parameter was parsed
	*/
	public static boolean parseParameterFor(ICommandParameters value, String parameter)
	{
		boolean result = false;

		if (parameter.length() == 1)
		{
			switch (parameter.charAt(0))
			{
				case 'n':
					value.setTakeNoAction(TriState.YES);
					result = true;
					break;

				case 'p':
					value.setReadParameters(TriState.YES);
					result = true;
					break;

				case 'x':
					value.setResetParameters(TriState.YES);
					result = true;
					break;
			}
		}

		return result;
	}

	/** 
	 Sets value to the parameter defaults
	 
	 @param value The parameters to set to defaults
	*/
	public static void setDefaultParametersFor(ICommandParameters value)
	{
		value.setTakeNoAction(TriState.NOT_SPECIFIED);
		value.setReadParameters(TriState.NOT_SPECIFIED);
		value.setResetParameters(TriState.NOT_SPECIFIED);
	}
}