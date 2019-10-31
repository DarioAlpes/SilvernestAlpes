package com.uk.tsl.rfid.asciiprotocol.parameters;

import com.uk.tsl.rfid.asciiprotocol.enumerations.QuerySelect;
import com.uk.tsl.rfid.asciiprotocol.enumerations.QuerySession;
import com.uk.tsl.rfid.asciiprotocol.enumerations.QueryTarget;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 Helper class for implementing IQueryParameters
*/
public final class QueryParameters
{
	/** 
	 Append the parameters from source that have been specified to the command line
	 
	 @param source The parameters to append
	 @param line The line to append the specified parameters to
	*/
	public static void appendToCommandLine(IQueryParameters source, StringBuilder line)
	{
		if (source.getQuerySelect() != QuerySelect.NOT_SPECIFIED)
		{
			line.append(String.format("-ql%s", source.getQuerySelect().getArgument()));
		}

		if (source.getQuerySession() != QuerySession.NOT_SPECIFIED)
		{
			line.append(String.format("-qs%s", source.getQuerySession().getArgument()));
		}

		if (source.getQueryTarget() != QueryTarget.NOT_SPECIFIED)
		{
			line.append(String.format("-qt%s", source.getQueryTarget().getArgument()));
		}
	}

	/** 
	 Parses the specified parameter and sets the appropriate property in value
	 
	 @param value The parameters to update
	 @param parameter The parameter to parse
	 @return True if parameter was parsed
	*/
	public static boolean parseParameterFor(IQueryParameters value, String parameter)
	{
		if (parameter.length() > 2)
		{
			if (parameter.charAt(0) == 'q')
			{
				switch (parameter.charAt(1))
				{
					case 'l':
					{
							value.setQuerySelect(QuerySelect.Parse(parameter.substring(2)));
							return true;
					}

					case 's':
					{
							value.setQuerySession(QuerySession.Parse(parameter.substring(2)));
							return true;
					}

					case 't':
					{
							value.setQueryTarget(QueryTarget.Parse(parameter.substring(2)));
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
	public static void setDefaultParametersFor(IQueryParameters value)
	{
		value.setQuerySelect(QuerySelect.NOT_SPECIFIED);
		value.setQuerySession(QuerySession.NOT_SPECIFIED);
		value.setQueryTarget(QueryTarget.NOT_SPECIFIED);
	}
}