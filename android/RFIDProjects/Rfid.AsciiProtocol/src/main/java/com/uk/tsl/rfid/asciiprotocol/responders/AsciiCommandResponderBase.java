package com.uk.tsl.rfid.asciiprotocol.responders;

import com.uk.tsl.utils.StringHelper;

import android.util.Log;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------


/** 
 Base implementation of IAsciiCommandResponder
*/
public abstract class AsciiCommandResponderBase extends AsciiResponseBase implements IAsciiCommandResponder
{
	// Logging tag
	private static final String TAG = "AsciiCommandResponderBase";

	/** 
	 Initializes a new instance of the AsciiCommandResponderBase class to respond to all commands
	*/
	protected AsciiCommandResponderBase()
	{
		this("");
	}

	/** 
	 Initializes a new instance of the AsciiCommandResponderBase class to respond to a specific command
	 
	 @param commandName The command name e.g. '.iv' for Inventory or string.Empty to respond to all commands
	*/
	protected AsciiCommandResponderBase(String commandName)
	{
		super();
		this.setCommandName(commandName);
	}

	/** 
	 Gets or sets the command name of the command to capture the response of.
	 If this is set to string.Empty then all responses can be captured
	*/
	private String privateCommandName;
	public final String getCommandName()
	{
		return privateCommandName;
	}
	protected final void setCommandName(String value)
	{
		privateCommandName = value;
	}

	/** 
	 Gets a value indicating whether the response is complete (i.e. received OK: or ER:)
	*/
	private boolean privateIsResponseFinished;
	public final boolean isResponseFinished()
	{
		return privateIsResponseFinished;
	}
	private void setIsResponseFinished(boolean value)
	{
		privateIsResponseFinished = value;
	}

	/** 
	 Gets or sets a value indicating whether a 'CS:' header beginning with commandName has been seen
	*/
	private boolean privateResponseStarted;
	protected final boolean getResponseStarted()
	{
		return privateResponseStarted;
	}
	protected final void setResponseStarted(boolean value)
	{
		privateResponseStarted = value;
	}

	public ICommandResponseLifecycleDelegate getResponseLifecycleDelegate()
	{
		return privateResponseLifecycleDelegate;
	}
	private ICommandResponseLifecycleDelegate privateResponseLifecycleDelegate;
	public void setResponseLifecycleDelegate(ICommandResponseLifecycleDelegate delegate)
	{
		privateResponseLifecycleDelegate = delegate;
	}
	
	/** 
	 Splits the given line into parameters separated by '-' but preserving quoted ("like - this") strings
	 
	 @param value The parameters to parse
	 @return The individual parameters parsed from the line
	*/
	public static java.util.Collection<String> splitParameters(String value)
	{
		java.util.ArrayList<String> separatedParameters;

		separatedParameters = new java.util.ArrayList<String>();

		if (value == null)
		{
			// null input returns no parameters
		}
		else if (value.indexOf('"') < 0)
		{
			// Nothing is quoted so use fast splitter
			String temp;
			for (String parameter : value.split("[-]", -1))
			{
				temp = parameter.trim();
				if (!StringHelper.isNullOrEmpty(temp))
				{
					separatedParameters.add(temp);
				}
			}
		}
		else
		{
			// Parse so as to not split quoted sections that may contain '-'
			int parameterStartPos;
			String currentParameter;
			int testPoint;

			parameterStartPos = 0;
			currentParameter = "";
			testPoint = nextQuoteDash(value, parameterStartPos);

			while (parameterStartPos < value.length())
			{
				if (testPoint < 0)
				{
					// End of string so add to existing and finish
					currentParameter = value.substring(parameterStartPos).trim();
					if (!StringHelper.isNullOrEmpty(currentParameter))
					{
						separatedParameters.add(currentParameter);
					}

					parameterStartPos = value.length();
				}
				else if (value.charAt(testPoint) == '"')
				{
					// Find the end of the string
					testPoint = value.indexOf('"', testPoint + 1);
					if (testPoint >= 0)
					{
						testPoint = nextQuoteDash(value, testPoint + 1);
					}
				}
				else if (value.charAt(testPoint) == '\'')
				{
					// Find the end of the string
					testPoint = value.indexOf('\'', testPoint + 1);
					if (testPoint >= 0)
					{
						testPoint = nextQuoteDash(value, testPoint + 1);
					}
				}
				else
				{
					// Next parameter found
					currentParameter = value.substring(parameterStartPos, testPoint).trim();
					if (!StringHelper.isNullOrEmpty(currentParameter))
					{
						separatedParameters.add(currentParameter);
					}

					parameterStartPos = testPoint + 1;

					// find next separator after this one
					testPoint = nextQuoteDash(value, parameterStartPos);
				}
			}
		}

		return separatedParameters;
	}

	/** 
	 Returns the Ascii command line (including terminators) to be sent to the device
	 
	 @return The command line to send this command
	*/
	public String getCommandLine()
	{
		StringBuilder line;

		line = new StringBuilder();
		this.buildCommandLine(line);
		return line.toString();
	}

	/** 
	 Parses a PR: value for parameters and updates the command with the values parsed
	 
	 @param parameterLine The line containing parameters to parse
	 @return The individual parameters from the line
	*/
	public final Iterable<String> parseParameters(String parameterLine)
	{
		Iterable<String> parameters;

		parameters = splitParameters(parameterLine);
		for (String parameter : parameters)
		{
			this.responseDidReceiveParameter(parameter);
		}

		return parameters;
	}


	/** 
	 Clears the response ready to receive a new one
	*/
	@Override
	public void clearLastResponse()
	{
		super.clearLastResponse();

		// TODO: RJS Moved from reset to clear last response
		this.setResponseStarted(false);
		this.setIsResponseFinished(false);
	}

	/** 
	 Each correctly terminated line from the device is passed to this method for processing
	 
	 @param fullLine The line to be processed
	 @param moreLinesAvailable When true indicates there are additional lines to be processed (and will also be passed to this method)
	 
	 @return True if this line should not be passed to any other responder
	 * @throws Exception 
	*/
	public final boolean processReceivedLine(String fullLine, boolean moreLinesAvailable) throws Exception
	{
		boolean result;

		try
		{
			// Separate the full line into header and value
			String trimmedLine;
			String header;
			String value;
			int colonLocation;

			// Command response headers should always have 2 chars then colon
			trimmedLine = fullLine.trim();
			colonLocation = trimmedLine.indexOf(":");
			if (colonLocation == 2)
			{
				header = trimmedLine.substring(0, 2); // Get header without colon
				value = trimmedLine.substring(3).trim(); // Get any value present
			}
			else
			{
				header = ""; // Line is not valid
				value = "";
			}

			result = this.processReceivedLine(fullLine, header, value, moreLinesAvailable);
		}
		catch (Exception exception)
		{
			Log.e(TAG, "processReceivedLine", exception);

			// Abort any command
			this.appendToResponse(fullLine);
			this.commandComplete(false);
			result = true;

			throw exception;
		}

		return result;
	}

	/** 
	 Builds the command line to send to the reader to execute the command
	 
	 @param line The command line to append to
	 
	 When overriding this method call the base class to construct the command line as known to the base class and
	 then append the additional parameters to the end of the line
	 
	*/
	protected void buildCommandLine(StringBuilder line)
	{
		line.append(this.getCommandName());
		line.append(" ");
	}

	/** 
	 Each correctly terminated line from the device is passed to this method for processing
	 
	 @param fullLine The line to be processed
	 @param header The response line header excluding the colon e.g. 'CS' for a command started response
	 @param value The response line following the colon e.g. '.iv'
	 @param moreAvailable When true indicates there are additional lines to be processed (and will also be passed to this method)
	 @return Derived classes should return true if this line should NOT be passed to any other responder.
	 
	 This base class provides default handling for the following responses:
 	 	CS Sets ResponseStarted to true
	 	PR Populates Parameters with parameters occurring within command responses
	 	ME Populates Messages with messages occurring within command responses
	 
	*/
	protected boolean processReceivedLine(String fullLine, String header, String value, boolean moreAvailable) throws Exception
	{
		if (this.getResponseStarted())
		{
			if ("OK".equals(header))
			{
				// This line was recognised so add it to the response
				this.appendToResponse(fullLine);

				this.commandComplete(true);
			}
			else if ("ER".equals(header))
			{
				// Set the error code
				this.setErrorCode(value.trim());

				this.appendToResponse(fullLine);

				this.commandComplete(false);
			}
			else if ("ME".equals(header))
			{
				this.appendToMessages(value);
			}
			else if ("PR".equals(header))
			{
				Iterable<String> parameters;

				parameters = this.parseParameters(value);

				this.appendToParameters(parameters);
			}
			else
			{
				// Not recognised so allow others to parse the line
				return false;
			}

			if (this.getResponseStarted())
			{
				// This line was recognised so add it to the response
				this.appendToResponse(fullLine);
			}

			return true;
		}
		else
		{
			// Test for response to be handled
			if ("CS".equals(header))
			{
				// Match any command if none specified
				// Or only match the current commandName
				if (StringHelper.isNullOrEmpty(this.getCommandName()) || value.startsWith(this.getCommandName()))
				{
					this.clearLastResponse();
					this.setResponseStarted(true);

					// Start collecting the total response
					this.appendToResponse(fullLine);

					responseDidStart();

					return true;
				}
			}
		}

		return false;
	}

	/** 
	 The is called when the responder received a line with an OK: or an ER: header. i.e. the command is complete
	 
	 @param async True if the command finished asynchronously
	 
	 Warning: To ensure correct operation of synchronous commands the super class method must be invoked
	 
	*/
	protected void responseDidFinish(boolean async)
	{
		this.setIsResponseFinished(true);

		if( getResponseLifecycleDelegate() != null ) {
			getResponseLifecycleDelegate().responseEnded();
		}
		Log.v(getClass().getSimpleName(), "responseDidFinish");
	}

	/** 
	 This method is called for each parameter in the parameters (PR:) list. 
	 Returns true if the parameter was handled or false otherwise.
	 Derived classes can override this method to extract individual parameters from the PR: line
	 
	 @param parameter
	 A single parameter extracted from the PR: response, excluding the '-' and trimmed of leading and trailing whitespace
	 
	 @return Return true if the parameter was handled
	*/
	protected boolean responseDidReceiveParameter(String parameter)
	{
		Log.w(TAG, "ResponseDidReceiveParameter. Unrecognised parameter: " + parameter);
		return false;
	}

	/** 
	 This is called when the responder receives the CS: line
	*/
	protected void responseDidStart()
	{
		Log.v(getClass().getSimpleName(), "ResponseDidStart");

		if( getResponseLifecycleDelegate() != null ) {
			getResponseLifecycleDelegate().responseBegan();
		}
	}

	/** 
	 Returns the index of the next quote, double quote or dash in value after strartIndex
	 
	 @param value The string to search
	 @param startIndex The index to search from
	 @return The index of the first character found after startIndex or -1 if no character found
	*/
	private static int nextQuoteDash(String value, int startIndex)
	{
		char[] search = new char[] {'\'', '"', '-'};
		int test;
		int result;

		result = Integer.MAX_VALUE;
		for (int i = 0; i < search.length; i++)
		{
			test = value.indexOf(search[i], startIndex);

			// if result found and less than other characters set at index of interest
			if ((test >= 0) && (test < result))
			{
				result = test;
			}
		}

		// if we have found an index return it
		return result == Integer.MAX_VALUE ? - 1 : result;
	}

	/** 
	 Call to signal the command is complete
	 
	 @param success True if the command was successful, false otherwise
	*/
	private void commandComplete(boolean success)
	{
		this.setIsSuccessful(success);
		this.setResponseStarted(false);
		this.responseDidFinish(false); // TODO: we no longer know if we're async!
	}
}