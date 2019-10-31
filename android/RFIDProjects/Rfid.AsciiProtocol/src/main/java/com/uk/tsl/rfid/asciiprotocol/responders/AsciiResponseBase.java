package com.uk.tsl.rfid.asciiprotocol.responders;

import java.util.ArrayList;
import java.util.List;


//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------


/** 
 Provides a base implementation of IAsciiResponse
*/
public abstract class AsciiResponseBase implements IAsciiResponse
{
	/** 
	 Backing field for Response
	*/
	private java.util.ArrayList<String> response;

	/** 
	 Backing field for Messages
	*/
	private java.util.ArrayList<String> messages;

	/** 
	 Backing field for Parameters
	*/
	private java.util.ArrayList<String> parameters;

	/** 
	 Initializes a new instance of the AsciiResponseBase class
	*/
	protected AsciiResponseBase()
	{
		this.messages = new java.util.ArrayList<String>();
		this.parameters = new java.util.ArrayList<String>();
		this.response = new java.util.ArrayList<String>();
	}

	/** 
	 Gets or sets the error code received
	*/
	private String privateErrorCode;
	public final String getErrorCode()
	{
		return privateErrorCode;
	}
	protected final void setErrorCode(String value)
	{
		privateErrorCode = value;
	}

	/** 
	 Gets or sets a value indicating whether the response is successful (Ended with OK:)
	*/
	private boolean privateIsSuccessful;
	public final boolean isSuccessful()
	{
		return privateIsSuccessful;
	}
	protected final void setIsSuccessful(boolean value)
	{
		privateIsSuccessful = value;
	}

	/** 
	 Gets the messages received during the response
	*/
	public final List<String> getMessages()
	{
		return this.messages;
	}

	/** 
	 Gets the parameters received during the response
	*/
	public final List<String> getParameters()
	{
		return this.parameters;
	}

	/** 
	 Gets the lines received during the response
	*/
	public final List<String> getResponse()
	{
		return this.response;
	}

	/** 
	 Clears the response ready to receive a new one
	*/
	public void clearLastResponse()
	{
		this.setIsSuccessful(false);
		this.setErrorCode("");
		this.messages = new java.util.ArrayList<String>();
		this.parameters = new java.util.ArrayList<String>();
		this.response = new java.util.ArrayList<String>();
	}

	/** 
	 Append the given string to the current Messages
	 
	 @param message The message to append to the current messages
	*/
	protected void appendToMessages(String message)
	{

		this.messages.add(message);
	}

	/** 
	 Append the given strings to the current Parameters
	 
	 @param parameters The parameters to append to the current parameters
	*/
	protected void appendToParameters(Iterable<String> parameters)
	{

		for(String s : parameters)
		{
			this.parameters.add(s);
		}
	}

	/** 
	 Append the given string to the current Response
	 
	 @param line The line to append to the current response
	*/
	protected void appendToResponse(String line)
	{

		this.response.add(line);
	}

	/**
	 * @param values The new response strings
	 */
	protected void setResponse(ArrayList<String> values)
	{
		this.response = values;
	}
}