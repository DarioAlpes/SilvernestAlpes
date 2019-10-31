package com.uk.tsl.rfid.asciiprotocol.commands;

import com.uk.tsl.rfid.asciiprotocol.enumerations.DeleteConfirmation;
import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;
import com.uk.tsl.rfid.asciiprotocol.responders.IFileLineReceivedDelegate;

/**
 * A command to write commands to the device's autorun file
 * 
 * This command does not use the library marker and command index because they
 * would be interpreted as part of the command to be added to the autorun file.
 * So, by default, this command will capture non-library responses to allow
 * synchronous commands to execute correctly.
 *  
 */
public class ReadAutorunFileCommand extends AsciiSelfResponderCommandBase {
	
	/**
	 * @return the delete confirmation status
	 */
	public DeleteConfirmation getDeleteFile() {
		return privateDeleteFile;
	}


	/**
	 * @param deleteFile the delete confirmation status to set
	 */
	public void setDeleteFile(DeleteConfirmation deleteFile) {
		privateDeleteFile = deleteFile;
	}


	/**
	 * @return the fileLineReceivedDelegate
	 */
	public IFileLineReceivedDelegate getFileLineReceivedDelegate() {
		return privateFileLineReceivedDelegate;
	}


	/**
	 * @param fileLineReceivedDelegate the fileLineReceivedDelegate to set
	 */
	public void setFileLineReceivedDelegate(IFileLineReceivedDelegate fileLineReceivedDelegate) {
		privateFileLineReceivedDelegate = fileLineReceivedDelegate;
	}

	private DeleteConfirmation privateDeleteFile;
	private IFileLineReceivedDelegate privateFileLineReceivedDelegate;
	private boolean mFileLinesHaveStarted;

	public ReadAutorunFileCommand() {
		super(".ra");
		privateDeleteFile = DeleteConfirmation.NOT_SPECIFIED;
		privateFileLineReceivedDelegate = null;
		mFileLinesHaveStarted = false;
	}

	
	/** 
	 Returns a new instance of the ReadAutorunFileCommand class that will execute synchronously (as its own responder)
	 
	 @return A new synchronous command instance
	*/
	public static ReadAutorunFileCommand synchronousCommand()
	{
		ReadAutorunFileCommand command;
		command = new ReadAutorunFileCommand();
		command.setSynchronousCommandResponder(command);
		return command;
	}

	
	/** 
	 Builds the command line to send to the reader to execute the command
	 
	*/
	@Override
	public String getCommandLine()
	{
		// Replace the default command line build to prevent the library command marker being added to the autorun file
		String line = super.getCommandLine();

		if( getDeleteFile() == DeleteConfirmation.YES) {
			line += String.format("-d%s", getDeleteFile().getArgument());
		}
		
		return line;
	}

	
	/** 
	 Clears the response ready to receive a new one
	*/
	@Override
	public void clearLastResponse()
	{
		super.clearLastResponse();
		mFileLinesHaveStarted = false;
	}

	/** 
	 The is called when the responder received a line with an OK: or an ER: header. i.e. the command is complete
	 
	 @param async True if the command finished asynchronously
	*/
	@Override
	protected void responseDidFinish(boolean async)
	{
		// Provide delegate with notification that all file lines have been received
		if( getFileLineReceivedDelegate() != null ) {
			getFileLineReceivedDelegate().fileLineReceived(null, false);
		}
		super.responseDidFinish(async);
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
		if( getResponseStarted() && mFileLinesHaveStarted)
		{
			// Look for end marker
			if ("AE".equals(header))
			{
				mFileLinesHaveStarted = false;
			} else if( getFileLineReceivedDelegate() != null ) {
				// Allow delegate to see the line
				getFileLineReceivedDelegate().fileLineReceived(fullLine, moreAvailable);
			}
			
			// Consume all the lines
			return true;
		}
		else
		{
			if (super.processReceivedLine(fullLine, header, value, moreAvailable))
			{
				return true;
			}
			else if (this.getResponseStarted())
			{
				if ("AB".equals(header))
				{
					mFileLinesHaveStarted = true;
				}
				else
				{
					return false;
				}

				return true;
			}
		}

		return false;
	}


	
	
}
