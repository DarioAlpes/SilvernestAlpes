package com.uk.tsl.rfid.asciiprotocol.responders;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.uk.tsl.rfid.asciiprotocol.Constants;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TransponderAccessErrorCode;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TransponderBackscatterErrorCode;
import com.uk.tsl.utils.HexEncoding;
import com.uk.tsl.utils.StringHelper;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/** 
 Processes responses looking for transponder header and collects into a transponder.
 Calls the TransponderReceivedHandler for each transponder received
 
 
 <p>
 Captures the EP CR PC RI responses to form a TransponderData instance.
 Captures WW as WordsWritten and RD as data read
 Captures OK ER to raise TransponderReceived at the end of a response;
 </p>
 <p>
 The delegate is called if:
 - A new EPC is received and the current EPC is valid (moreAvailable = true). Just received the EPC of the next response
 - OK or ER is received and the current EPC is valid (moreAvailable = false). End of the command
 - TransponderComplete is called. This is provided for when a base class handles OR or ER and needs to complete a transponder
 </p>
 
*/
public class TransponderResponder
{
	public TransponderResponder()
	{
		clearLastResponse();
	}

	/** 
	 Gets the last received transponder CRC
	 
	 
	 The reader will only output this value if the command is enabled to do so.
	 This will return null (Nothing in Visual Basic) if not received for the current transponder
	 
	*/
	private Integer privateCrc;
	public final Integer getCrc()
	{
		return privateCrc;
	}
	private void setCrc(Integer value)
	{
		privateCrc = value;
	}

	/** 
	 Gets the last received transponder EPC
	*/
	private String privateEpc;
	public final String getEpc()
	{
		return privateEpc;
	}
	private void setEpc(String value)
	{
		privateEpc = value;
	}

	/** 
	 Gets the last received transponder index
	 
	 
	 The reader will only output this value if the command is enabled to do so.
	 This will return null (Nothing in Visual Basic) if not received for the current transponder
	 
	*/
	private Integer privateIndex;
	public final Integer getIndex()
	{
		return privateIndex;
	}
	private void setIndex(Integer value)
	{
		privateIndex = value;
	}

	/** 
	 Gets the last received transponder PC
	 
	 
	 The reader will only output this value if the command is enabled to do so.
	 This will return null (Nothing in Visual Basic) if not received for the current transponder
	 
	*/
	private Integer privatePc;
	public final Integer getPc()
	{
		return privatePc;
	}
	private void setPc(Integer value)
	{
		privatePc = value;
	}

	/**
	 * @return true if the transponder was successfully killed
	 */
	public final boolean didKill() { return privateDidKill; }
	private boolean privateDidKill;
	private void setKill(boolean value) { privateDidKill = value; }
	
	/**
	 * @return true if the transponder was successfully locked
	 */
	public final boolean didLock() { return privateDidLock; }
	private boolean privateDidLock;
	private void setLock(boolean value) { privateDidLock = value; }
	

	/** 
	 Gets the last received transponder RSSI
	 
	 
	 The reader will only output this value if the command is enabled to do so.
	 This will return null (Nothing in Visual Basic) if not received for the current transponder
	 
	*/
	private Integer privateRssi;
	public final Integer getRssi()
	{
		return privateRssi;
	}
	private void setRssi(Integer value)
	{
		privateRssi = value;
	}

	/**
	 * Gets the timestamp for this command RESPONSE
	 * This value will apply to all transponders from the same response
	 */
	public final Date getTimestamp() { return privateTimestamp; }
	private Date privateTimestamp;
	private void setTimestamp(Date value) { privateTimestamp = value; }

	/**
	 * Gets the access error code for this transponder
	 */
	public final TransponderAccessErrorCode getAccessErrorCode() { return privateAccessErrorCode; }
	private TransponderAccessErrorCode privateAccessErrorCode;
	private void setAccessErrorCode(TransponderAccessErrorCode value) { privateAccessErrorCode = value; }

	/**
	 * Gets the backscatter error for this transponder
	 */
	public final TransponderBackscatterErrorCode getBackscatterErrorCode() { return privateBackscatterErrorCode; }
	private TransponderBackscatterErrorCode privateBackscatterErrorCode;
	private void setBackscatterErrorCode(TransponderBackscatterErrorCode value) { privateBackscatterErrorCode = value; }

	
	/** 
	 Gets the data read from the transponder in hex
	*/
	private byte[] privateReadData;
	public final byte[] getReadData()
	{
		return privateReadData;
	}
	private void setReadData(byte[] value)
	{
		privateReadData = value;
	}

	/** 
	 Gets the TID data read from the transponder (only applicable when Impinj Monza Fast Id extension used)
	*/
	private byte[] privateFastIdData;
	public final byte[] getFastIdData()
	{
		return privateFastIdData;
	}
	private void setFastIdData(byte[] idData)
	{
		privateFastIdData = idData;
	}


	/** 
	 Gets the number of words successfully written to the transponder
	*/
	private int privateWordsWritten;
	public final int getWordsWritten()
	{
		return privateWordsWritten;
	}
	private void setWordsWritten(int value)
	{
		privateWordsWritten = value;
	}

	/** 
	 Gets or sets the delegate to call when a transponder is completed
	*/
	private ITransponderReceivedDelegate privateTransponderReceivedHandler;
	public final ITransponderReceivedDelegate getTransponderReceivedHandler()
	{
		return privateTransponderReceivedHandler;
	}
	public final void setTransponderReceivedHandler(ITransponderReceivedDelegate value)
	{
		privateTransponderReceivedHandler = value;
	}

	/** 
	 Clears the cache of values ready to receive a new transponder
	*/
	public final void clearLastResponse()
	{
		setEpc("");
		setCrc(null);
		setIndex(null);
		setPc(null);
		setLock(false);
		setKill(false);
		setReadData(null);
		setRssi(null);
		setAccessErrorCode(TransponderAccessErrorCode.NOT_SPECIFIED);
		setBackscatterErrorCode(TransponderBackscatterErrorCode.NOT_SPECIFIED);
		setFastIdData(null);
		setWordsWritten(TransponderData.NO_WORDS_WRITTEN);
	}

	/** 
	 Each correctly terminated line from the device is passed to this method for processing
	 
	 @param header The response line header excluding the colon e.g. 'CS' for a command started response
	 @param value The response line following the colon e.g. '.iv'
	 @return 
	 Return true if this line should NOT be passed to any other responder.
	 
	*/
	public final boolean processReceivedLine(String header, String value)
	{
		boolean result = true;

		// Look for responses from trigger events
		if ("OK".equals(header))
		{
			// command completed successfully
			transponderComplete(false);

			// Do not consume the end of command marker as this responder is used by another responder
			// The other responder is responsible for detecting the end of command
			result = false;
		}
		else if ("ER".equals(header))
		{
			// command completed with error code
			transponderComplete(false);

			// Do not consume the end of command marker as this responder is used by another responder
			// The other responder is responsible for detecting the end of command
			result = false;
		}
		else if ("DT".equals(header))
		{
			// Parse the received time stamp
			DateFormat parser = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss", Constants.COMMAND_LOCALE);
			try {
				setTimestamp(parser.parse(value));
			} catch (ParseException e) {
				setTimestamp(null);
			}
		}
		else if ("EP".equals(header))
		{
			// received current transponder EPC value in hex
			transponderComplete(true);

			// Start receiving new transponder's data
			setEpc(value);
		}
		else if ("CR".equals(header))
		{
			// received current transponder CRC value in hex
			setCrc(Integer.parseInt(value, 16));
		}
		else if ("PC".equals(header))
		{
			// received current transponder PC value in hex
			setPc(Integer.parseInt(value, 16));
		}
		else if ("IX".equals(header))
		{
			// received current transponder index
			setIndex(Integer.parseInt(value, 16));
		}
		else if ("RI".equals(header))
		{
			// received current transponder RSSI
			setRssi(Integer.parseInt(value));
		}
		else if ("LS".equals(header))
		{
			// received current transponder lock success
			setLock(value.contains("Lock Success"));
		}
		else if ("KS".equals(header))
		{
			// received current transponder kill success
			setKill(value.contains("Kill Success"));
		}
		else if ("RD".equals(header))
		{
			// received transponder data
			setReadData(HexEncoding.stringToBytes(value));
		}
		else if ("TD".equals(header))
		{
			// received transponder TID data
			setFastIdData(HexEncoding.stringToBytes(value));
		}
		else if ("EA".equals(header))
		{
			// received transponder access error code
			setAccessErrorCode(TransponderAccessErrorCode.Parse(value));
		}
		else if ("EB".equals(header))
		{
			// received transponder backscatter error code
			setBackscatterErrorCode(TransponderBackscatterErrorCode.Parse(value));
		}
		else if ("WW".equals(header))
		{
			// received transponder words written
			setWordsWritten(Integer.parseInt(value));
		}
		else
		{
			// Not recognised so allow others to see it
			result = false;
		}

		return result;
	}

	/** 
	 When called this method checks to see if the Epc is not empty. If the EPC is valid then OnTransponderComplete
	 if called to notify the delegate of the transponder received. Once called the response is reset with ClearLastResponse
	 
	 @param moreAvailable True if more transponders are pending to be notified
	*/
	public final void transponderComplete(boolean moreAvailable)
	{
		if (!StringHelper.isNullOrEmpty(getEpc()))
		{
			if( privateTransponderReceivedHandler != null )
			{
				TransponderData transponder;
	
				transponder = new TransponderData(
						getCrc(),
						getEpc(),
						getIndex(),
						didKill(),
						didLock(),
						getPc(),
						getReadData(),
						getRssi(),
						getTimestamp(),
						getAccessErrorCode(),
						getBackscatterErrorCode(),
						getFastIdData(),
						getWordsWritten());
	
				// Inform listener of new transponder
				privateTransponderReceivedHandler.transponderReceived(transponder, moreAvailable);
			}
		}

		clearLastResponse();

		// The timestamp is used for all transponders so only clear it when there are no more transponders from
		// the current response
		if(!moreAvailable)
		{
			setTimestamp(null);
		}
	}

}