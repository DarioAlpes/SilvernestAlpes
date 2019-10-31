package com.uk.tsl.rfid.asciiprotocol.parameters;

import com.uk.tsl.rfid.asciiprotocol.enumerations.SelectAction;
import com.uk.tsl.rfid.asciiprotocol.enumerations.SelectTarget;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 Helper class for implementing ISelectControlParameters
*/
public final class SelectControlParameters
{
	/** 
	 Append the parameters from source that have been specified to the command line
	 
	 @param source The parameters to append
	 @param line The line to append the specified parameters to
	*/
	public static void appendToCommandLine(ISelectControlParameters source, StringBuilder line)
	{
		if (source.getSelectTarget() != SelectTarget.NOT_SPECIFIED)
		{
			line.append(String.format("-st%s", source.getSelectTarget().getArgument()));
		}

		if (source.getSelectAction() != SelectAction.NOT_SPECIFIED)
		{
			line.append(String.format("-sa%s", source.getSelectAction().getArgument()));
		}
	}

	/** 
	 Parses the specified parameter and sets the appropriate property in value
	 
	 @param parameter The parameter to parse
	 @param value The parameters to update
	 @return True if parameter was parsed
	*/
	public static boolean parseParameterFor(ISelectControlParameters value, String parameter)
	{
		if (parameter.length() >= 2)
		{
			if (parameter.charAt(0) == 's')
			{
				switch (parameter.charAt(1))
				{
					case 't':
					{
							value.setSelectTarget(SelectTarget.Parse(parameter.substring(2)));
							return true;
					}

					case 'a':
					{
							value.setSelectAction(SelectAction.Parse(parameter.substring(2)));
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
	public static void setDefaultParametersFor(ISelectControlParameters value)
	{
		value.setSelectAction(SelectAction.NOT_SPECIFIED);
		value.setSelectTarget(SelectTarget.NOT_SPECIFIED);
	}
}