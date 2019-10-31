package com.uk.tsl.rfid.asciiprotocol.commands;

import com.uk.tsl.rfid.asciiprotocol.enumerations.DeleteConfirmation;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.rfid.asciiprotocol.parameters.CommandParameters;
import com.uk.tsl.rfid.asciiprotocol.parameters.ICommandParameters;
import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;
import com.uk.tsl.rfid.asciiprotocol.responders.IFileLineReceivedDelegate;

public class ReadLogFileCommand extends AsciiSelfResponderCommandBase implements
        ICommandParameters {

    /**
     Gets a value indicating whether the implementing command uses the  ReadParameters property
    */
    public final boolean implementsReadParameters() { return true; }

    /**
     Gets a value indicating whether the implementing command uses the  ResetParameters property
    */
    public final boolean implementsResetParameters() { return true; }

    /**
     Gets a value indicating whether the implementing command uses the  TakeNoAction property
    */
    public final boolean implementsTakeNoAction() { return true; }

    /**
     Gets or sets a value indicating whether the command should include a list of supported parameters and their current values
    */
    private TriState privateReadParameters;
    public final TriState getReadParameters()
    {
        return privateReadParameters;
    }
    public final void setReadParameters(TriState value)
    {
        privateReadParameters = value;
    }

    /**
     Gets or sets a value indicating whether the command should reset all its parameters to default values
    */
    private TriState privateResetParameters;
    public final TriState getResetParameters()
    {
        return privateResetParameters;
    }
    public final void setResetParameters(TriState value)
    {
        privateResetParameters = value;
    }

    /**
     Gets or sets a value indicating whether the command primary action should not be performed
     (e.g. InventoryCommand will not perform the inventory action)
     All other actions, such as setting parameters in the reader are performed
    */
    private TriState privateTakeNoAction;
    public final TriState getTakeNoAction()
    {
        return privateTakeNoAction;
    }
    public final void setTakeNoAction(TriState value)
    {
        privateTakeNoAction = value;
    }



    /**
     * @return the command logging state
     */
    public TriState getCommandLoggingEnabled() {
        return privateCommandLoggingEnabled;
    }
    private TriState privateCommandLoggingEnabled;

    /**
     * @param commandLoggingEnabled set the command logging state
     */
    public void setCommandLoggingEnabled(TriState commandLoggingEnabled) {
        privateCommandLoggingEnabled = commandLoggingEnabled;
    }

    /**
     * @return the delete confirmation status
     */
    public DeleteConfirmation getDeleteFile() {
        return privateDeleteFile;
    }
    private DeleteConfirmation privateDeleteFile;

    /**
     * @param deleteFile the delete confirmation status to set
     */
    public void setDeleteFile(DeleteConfirmation deleteFile) {
        privateDeleteFile = deleteFile;
    }

    /**
     * Get or set the include Log Length state
     */
    public TriState getIncludeLogLength() { return privateIncludeLogLength; }
    public void setIncludeLogLength(TriState includeLogLength){ privateIncludeLogLength = includeLogLength; }
    private TriState privateIncludeLogLength;

    /**
     * Get the Log Length (in Bytes)
     */
    public int getLogLength() { return privateLogLength; }
    protected void setLogLength(int logLength) { privateLogLength = logLength; }
    private int privateLogLength;

    /** Get or set the offset for the log block to be read
     * */
    public int getBlockOffset() { return privateBlockOffset; }
    public void setBlockOffset(int blockOffset) { privateBlockOffset = blockOffset; }
    private int privateBlockOffset;

    /**
     * @return the fileLineReceivedDelegate
     */
    public IFileLineReceivedDelegate getFileLineReceivedDelegate() {
        return privateFileLineReceivedDelegate;
    }
    private IFileLineReceivedDelegate privateFileLineReceivedDelegate;

    /**
     * @param fileLineReceivedDelegate the fileLineReceivedDelegate to set
     */
    public void setFileLineReceivedDelegate(IFileLineReceivedDelegate fileLineReceivedDelegate) {
        privateFileLineReceivedDelegate = fileLineReceivedDelegate;
    }


    private boolean mFileLinesHaveStarted;

    public ReadLogFileCommand()
    {
        super(".rl");

        privateCommandLoggingEnabled = TriState.NOT_SPECIFIED;
        privateDeleteFile = DeleteConfirmation.NOT_SPECIFIED;
        privateFileLineReceivedDelegate = null;
        privateIncludeLogLength = TriState.NOT_SPECIFIED;
        privateLogLength = -1;
        privateBlockOffset = -1;

        mFileLinesHaveStarted = false;
    }

    /**
     Returns a new instance of the ReadLogFileCommand class that will execute synchronously (as its own responder)

     @return A new synchronous command instance
    */
    public static ReadLogFileCommand synchronousCommand()
    {
        ReadLogFileCommand command;
        command = new ReadLogFileCommand();
        command.setSynchronousCommandResponder(command);
        return command;
    }

    /**
     Builds the command line to send to the reader to execute the command

     @param line The command line to append to

     When overriding this method call the base class to construct the command line as known to the base class and
     then append the additional parameters to the end of the line

    */
    @Override
    protected void buildCommandLine(StringBuilder line)
    {
        super.buildCommandLine(line);

        CommandParameters.appendToCommandLine(this, line);

        if( getCommandLoggingEnabled() != TriState.NOT_SPECIFIED) {
            line.append(String.format("-c%s", getCommandLoggingEnabled().getArgument()));
        }
        if( getDeleteFile() == DeleteConfirmation.YES) {
            line.append(String.format("-d%s", getDeleteFile().getArgument()));
        }
        if( getCommandLoggingEnabled() != TriState.NOT_SPECIFIED) {
            line.append(String.format("-c%s", getCommandLoggingEnabled().getArgument()));
        }
        if( getIncludeLogLength() == TriState.YES) {
            line.append("-l");
        }
        if( getBlockOffset() >= 0 ) {
            line.append(String.format("-b%06x", this.getBlockOffset()));
        }

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
     This method is called for each parameter in the parameters (PR:) list.
     Returns true if the parameter was handled or false otherwise.
     Derived classes can override this method to extract individual parameters from the PR: line

     @param parameter
     A single parameter extracted from the PR: response, excluding the '-' and trimmed of leading and trailing whitespace

     @return Return true if the parameter was handled
    */
    @Override
    protected boolean responseDidReceiveParameter(String parameter)
    {
        if (!CommandParameters.parseParameterFor(this, parameter))
        {
            if ( parameter.length() >= 1 && parameter.startsWith("c"))
            {
                setCommandLoggingEnabled(TriState.Parse(parameter.substring(1)));
            }
            else if (parameter.length() >= 1 && parameter.startsWith("d"))
            {
                if( parameter.length() > 1) {
                    setDeleteFile(DeleteConfirmation.Parse(parameter.substring(1)));
                }
            }
            else if (parameter.length() >= 1 && parameter.startsWith("b"))
            {
                // Nothing to do - do not want to overwrite any existing block offset
            }
            else if (parameter.length() >= 1 && parameter.startsWith("l"))
            {
                setIncludeLogLength(TriState.YES);
            }
            else
            {
                return super.responseDidReceiveParameter(parameter);
            }
        }

        return true;
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
            if ("LE".equals(header))
            {
                mFileLinesHaveStarted = false;
            }
            else if (getFileLineReceivedDelegate() != null)
            {
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
                if ("LB".equals(header))
                {
                    mFileLinesHaveStarted = true;
                }
                else if("LL".equals(header))
                {
                    setLogLength(Integer.parseInt(value, 16));
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
