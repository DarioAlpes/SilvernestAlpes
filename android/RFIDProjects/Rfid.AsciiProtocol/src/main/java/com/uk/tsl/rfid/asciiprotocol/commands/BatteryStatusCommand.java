package com.uk.tsl.rfid.asciiprotocol.commands;

import com.uk.tsl.rfid.asciiprotocol.enumerations.ChargeState;
import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 A command to query the reader for battery status information
 Note: This does not yet expose the charging status via a property but the 'CH:' line is captured in the response property 
*/
public class BatteryStatusCommand extends AsciiSelfResponderCommandBase
{
	/** 
	 Initializes a new instance of the BatteryStatusCommand class
	*/
	public BatteryStatusCommand()
	{
		super(".bl");
	}

	/** 
	 Gets the battery level retrieved from the reader. 
	*/
	private int privateBatteryLevel;
	public final int getBatteryLevel()
	{
		return privateBatteryLevel;
	}
	private void setBatteryLevel(int value)
	{
		privateBatteryLevel = value;
	}

	/** 
	 Gets the battery charging status from the reader. 
	*/
	private ChargeState privateChargeStatus;
	public final ChargeState getChargeStatus()
	{
		return privateChargeStatus;
	}
	private void setChargeState(ChargeState state)
	{
		privateChargeStatus = state;
	}

	/** 
	 Returns a new instance of the BatteryStatusCommand class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance
	*/
	public static BatteryStatusCommand synchronousCommand()
	{
		BatteryStatusCommand command;
		command = new BatteryStatusCommand();
		command.setSynchronousCommandResponder(command);
		return command;
	}

	/** 
	 Clears the response ready to receive a new one
	*/
	@Override
	public void clearLastResponse()
	{
		super.clearLastResponse();
		this.setBatteryLevel(0);
	}

	/** 
	 Each correctly terminated line from the device is passed to this method for processing
	 
	 @param fullLine The line to be processed
	 @param header The response line header excluding the colon e.g. 'CS' for a command started response
	 @param value The response line following the colon e.g. '.iv'
	 @param moreAvailable When true indicates there are additional lines to be processed (and will also be passed to this method)
	 @return 
	 Return true if this line should NOT be passed to any other responder.
	 * @throws Exception 
	 
	*/
	@Override
	protected boolean processReceivedLine(String fullLine, String header, String value, boolean moreAvailable) throws Exception
	{
		// Allow super class to detect the command start, messages, parameters and command end
		boolean superDidProcess;

		superDidProcess = super.processReceivedLine(fullLine, header, value, moreAvailable);

		if (superDidProcess)
		{
			// This line has been handled (no more processing needed)
			return true;
		}
		else
		{
			if ("BP".equals(header))
			{
				String percentageValue = value.substring(0, value.indexOf('%'));
				this.setBatteryLevel(Integer.parseInt(percentageValue));
			}
			else if ("CH".equals(header))
			{
				this.setChargeState(ChargeState.Parse(value.trim()));
			}
			else
			{
				// Not recognised so allow others to see it
				return false;
			}

			// This line has been handled (no more processing needed)
			this.appendToResponse(fullLine);
			return true;
		}
	}
}