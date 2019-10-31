package com.uk.tsl.rfid.asciiprotocol.commands;

import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 A command to query the reader for version information 
*/
public class VersionInformationCommand extends AsciiSelfResponderCommandBase
{
	/** 
	 Initializes a new instance of the VersionInformationCommand class
	*/
	public VersionInformationCommand()
	{
		super(".vr");
	}

	/** 
	 Gets the manufacturer name retrieved from the reader
	*/
	private String privateManufacturer;
	public final String getManufacturer()
	{
		return privateManufacturer;
	}
	private void setManufacturer(String value)
	{
		privateManufacturer = value;
	}

	/** 
	 Gets the serial number retrieved from the reader
	*/
	private String privateSerialNumber;
	public final String getSerialNumber()
	{
		return privateSerialNumber;
	}
	private void setSerialNumber(String value)
	{
		privateSerialNumber = value;
	}

	/** 
	 Gets the FirmwareVersion retrieved from the reader
	*/
	private String privateFirmwareVersion;
	public final String getFirmwareVersion()
	{
		return privateFirmwareVersion;
	}
	private void setFirmwareVersion(String value)
	{
		privateFirmwareVersion = value;
	}

	/** 
	 Gets the bootloader version name retrieved from the reader
	*/
	private String privateBootloaderVersion;
	public final String getBootloaderVersion()
	{
		return privateBootloaderVersion;
	}
	private void setBootloaderVersion(String value)
	{
		privateBootloaderVersion = value;
	}

	/** 
	 Gets the radio serial number retrieved from the reader
	*/
	private String privateRadioSerialNumber;
	public final String getRadioSerialNumber()
	{
		return privateRadioSerialNumber;
	}
	private void setRadioSerialNumber(String value)
	{
		privateRadioSerialNumber = value;
	}

	/** 
	 Gets the radio firmware version retrieved from the reader
	*/
	private String privateRadioFirmwareVersion;
	public final String getRadioFirmwareVersion()
	{
		return privateRadioFirmwareVersion;
	}
	private void setRadioFirmwareVersion(String value)
	{
		privateRadioFirmwareVersion = value;
	}

	/** 
	 Gets the radio bootloader version retrieved from the reader
	*/
	private String privateRadioBootloaderVersion;
	public final String getRadioBootloaderVersion()
	{
		return privateRadioBootloaderVersion;
	}
	private void setRadioBootloaderVersion(String value)
	{
		privateRadioBootloaderVersion = value;
	}

	/** 
	 Gets the antenna serial number retrieved from the reader
	*/
	private String privateAntennaSerialNumber;
	public final String getAntennaSerialNumber()
	{
		return privateAntennaSerialNumber;
	}
	private void setAntennaSerialNumber(String value)
	{
		privateAntennaSerialNumber = value;
	}

	/** 
	 Gets the ASCII protocol retrieved from the reader
	*/
	private String privateAsciiProtocol;
	public final String getAsciiProtocol()
	{
		return privateAsciiProtocol;
	}
	private void setAsciiProtocol(String value)
	{
		privateAsciiProtocol = value;
	}

	/**
	 * Gets the Bluetooth Address of the currently connected reader
	 */
	private String privateBluetoothVersion;
	public final String getBluetoothVersion()
	{
		return privateBluetoothVersion;
	}
	public final void setBluetoothVersion(String bluetoothVersion)
	{
		privateBluetoothVersion = bluetoothVersion;
	}

	/**
	 * Gets the Bluetooth Address of the currently connected reader
	 */
	private String privateBluetoothAddress;
	public final String getBluetoothAddress()
	{
		return privateBluetoothAddress;
	}
	public final void setBluetoothAddress(String bluetoothAddress)
	{
		privateBluetoothAddress = bluetoothAddress;
	}


	/** 
	 Returns a new instance of the VersionInformationCommand class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance
	*/
	public static VersionInformationCommand synchronousCommand()
	{
		VersionInformationCommand command;
		command = new VersionInformationCommand();
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
		this.setAntennaSerialNumber("");
		this.setAsciiProtocol("");
		this.setBootloaderVersion("");
		this.setFirmwareVersion("");
		this.setManufacturer("");
		this.setRadioBootloaderVersion("");
		this.setRadioFirmwareVersion("");
		this.setRadioSerialNumber("");
		this.setSerialNumber("");
        this.setBluetoothAddress("");
        this.setBluetoothVersion("");
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
		boolean superDidProcess = super.processReceivedLine(fullLine, header, value, moreAvailable);

		if (superDidProcess)
		{
			// This line has been handled (no more processing needed)
			return true;
		}
		else
		{
			if ("MF".equals(header))
			{
				this.setManufacturer(value.trim());
			}
			else if ("US".equals(header))
			{
				this.setSerialNumber(value.trim());
			}
			else if ("PV".equals(header))
			{
				this.setAsciiProtocol(value.trim());
			}
			else if ("UF".equals(header))
			{
				this.setFirmwareVersion(value.trim());
			}
			else if ("UB".equals(header))
			{
				this.setBootloaderVersion(value.trim());
			}
			else if ("RS".equals(header))
			{
				this.setRadioSerialNumber(value.trim());
			}
			else if ("RF".equals(header))
			{
				this.setRadioFirmwareVersion(value.trim());
			}
			else if ("RB".equals(header))
			{
				this.setRadioBootloaderVersion(value.trim());
			}
			else if ("AS".equals(header))
			{
				this.setAntennaSerialNumber(value.trim());
			}
            else if ("BA".equals(header))
            {
                this.setBluetoothAddress(value.trim());
            }
            else if ("BV".equals(header))
            {
                this.setBluetoothVersion(value.trim());
            }
			else
			{
				// Not recognised so allow others to see it
				return false;
			}

			// Something was recognised so no more processing needed
			this.appendToResponse(fullLine);
			return true;
		}
	}
}