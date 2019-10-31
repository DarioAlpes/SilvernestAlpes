package com.uk.tsl.rfid.asciiprotocol;

import java.lang.ref.WeakReference;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import com.uk.tsl.rfid.asciiprotocol.commands.IAsciiCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.IAsciiCommandExecuting;
import com.uk.tsl.rfid.asciiprotocol.commands.VersionInformationCommand;
import com.uk.tsl.rfid.asciiprotocol.responders.IAsciiCommandResponder;
import com.uk.tsl.rfid.asciiprotocol.responders.SynchronousDispatchResponder;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v4.BuildConfig;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------


/** 
 AsciiCommander provides methods to communicate with TSL devices that use the TSL ASCII 2.0 Protocol
 Instances of this class support:    
 	connection and disconnection from any TSL Reader connected via bluetooth
 	execution of an IAsciiCommand either synchronously or asynchronously
 	management of the responder chain for data received from the TSL device
 
 Warning: Instances of AsciiCommander MUST be created on the UI thread
*/
public class AsciiCommander implements IAsciiCommandExecuting, Observer
{
	/**
	 * The connection states for AsciiCommander
	 * 
	 *
	 */
	public enum ConnectionState
	{
		/**
		 * The connection state is undefined
		 */
		UNDEFINED,

		/**
		 * The AsciiCommander is not connected to a device
		 */
		DISCONNECTED,

		/**
		 * The AsciiCommnader is trying to connect to  device
		 */
		CONNECTING,

		/**
		 * The AsciiCommander is connected to a device
		 */
		CONNECTED
	}

	OnHeadlineSelectedListener mCallback;

	
	/// The AsciiCommander indicates that there has been a change of state using this LocalBroadcastManager event
	public static final String STATE_CHANGED_NOTIFICATION = "TSLAsciiCommanderStateChangedNotification";

	public static final String REASON_KEY = "reason_key";

	/// The prefix for the connecting message
	public static final String CONNECTING_MESSAGE_PREFIX = "Conectando a lector...";

	/// The prefix for the connected message
	public static final String CONNECTED_MESSAGE_PREFIX = "Conectado a: ";

	/// The prefix for the disconnected message
	public static final String USER_DISCONNECTED_MESSAGE_PREFIX = "Desconectado";
	public static final String DISCONNECTED_MESSAGE_PREFIX = "Lector no conectado";

	/**
	 * @return The DeviceProperties for the currently connected device. If no device is
	 * connected then default values are returned
	 */
	public final DeviceProperties getDeviceProperties() { return deviceProperties; }
	private DeviceProperties deviceProperties;
	// The synchronous version command used to determine the device properties
	private VersionInformationCommand informationCommand;
	
	// Debugging
    private static final String TAG = "AsciiCommander";
    private static final boolean D = BuildConfig.DEBUG;

    public String getConnectedDeviceName() { return deviceName; }
    private String deviceName;

    private ConnectionState connectionState = ConnectionState.UNDEFINED;


    // The last device that was successfully connected to the App
    private BluetoothDevice lastSuccessfullyConnectedReader;

	/** 
	 Used to communicate with the reader
	*/
	private BluetoothDevice reader;

	/** 
	 Used to synchronize access to the responder chain
	*/
	private Object responderLock = new Object();

	/** 
	 Backing field for ResponderChain
	*/
	private java.util.ArrayList<IAsciiCommandResponder> responderChain;

	/** 
	 Holds the synchronous responder instance that relays to synchronous commands
	*/
	private SynchronousDispatchResponder synchronousResponder;

	/** 
	 True while waiting for a response to a synchronous command
	*/
	private boolean awaitingCommandResponse;

	/** 
	 True once a response is received
	*/
	private boolean responseReceived;

	/** 
	 Provides synchronization to command execution
	*/
	private Object commandSync = new Object();

	/**
	 * The background service that handles communication with the Bluetooth reader
	 */
	private BluetoothReaderService readerService;

	/** 
	 Signalled when a command completes
	*/
	private Object commandCondition;

	
	/**
	 * The Context the AsciiCommander is running in
	 */
	private Context context;
//	public Context getContext(){ return context; }

	/** 
	 Initializes a new instance of the AsciiCommander class
	*/
	public AsciiCommander(Context context, Activity activity)
	{
		this.commandCondition = new Object();
		this.context = context;
		this.awaitingCommandResponse = false;
		this.responseReceived = false;
		this.responderChain = new java.util.ArrayList<IAsciiCommandResponder>();

		connectionState = ConnectionState.DISCONNECTED;
		informationCommand = VersionInformationCommand.synchronousCommand();
		deviceProperties = DeviceProperties.DEVICE_DEFAULTS;

		readerService = new BluetoothReaderService(mHandler);
		// Observe for incoming data
		readerService.addObserver(this);

    	SharedPreferences preferences = context.getSharedPreferences("AsciiCommanderPreferences", Context.MODE_PRIVATE);  

    	//--READ data       
    	String readerAddress = preferences.getString("lastConnectedReaderAddress", null);
    	if( readerAddress != null )
    	{
    		lastSuccessfullyConnectedReader = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(readerAddress);
    	}
    	else
    	{
    		lastSuccessfullyConnectedReader = null;
    	}
		try {
			mCallback = (OnHeadlineSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	public interface OnHeadlineSelectedListener {
		public void onArticleSelected();
	}

	/**
	 * Query for a previously successful connection to a device
	 * @return true if a previously successful connection has been made
	 */
	public final boolean hasConnectedSuccessfully()
	{
		return lastSuccessfullyConnectedReader != null;
	}
	
	/** 
	 Gets a value indicating whether a reader is connected
	*/
	public final boolean isConnected()
	{
		return this.reader != null && this.readerService.getState() == BluetoothReaderService.STATE_CONNECTED;
	}

	/**
	 * Query the AsciiCommander's connection state 
	 * 
	 * @return the connection state of the AsciiCommander
	 */
	public ConnectionState getConnectionState() {
		return connectionState;
	}
	
	/** 
	 Connect the AsciiCommander to the given reader

	 This is an asynchronous operation. AsciiCommander will raise state change
	 notifications when the reader connects/disconnects.
	 @param reader The UHF Reader that supports the TSL ASCII 2.0 protocol
	*/
	public final void connect(BluetoothDevice reader)
	{
		if (reader == null)
		{
			// Attempt to connect to the last connected reader
			reader = lastSuccessfullyConnectedReader;
		}

		if( reader != null )
		{
			// Disconnect from the current reader
			if (this.isConnected())
			{
				this.disconnect();
			}

			// TODO: can't change connection during a command?
			this.reader = reader;

			//BJP Always use the insecure connection method - 
			readerService.connect(reader, false);
		}
		else
		{
			Log.w("AsciiCommander.Connect", "Atempted to connect to a null reader");
		}

		this.setLastActivityTime(new java.util.Date());
	}

	
	/** 
	 Disconnects from the current device
	*/
	public final void disconnect()
	{
//		if (this.isConnected())
//		{
			//BJP - disconnect reader via the readerService
			this.readerService.stop();
			this.reader = null;

			this.connectionState = ConnectionState.DISCONNECTED;
			sendStateChangeNotification(USER_DISCONNECTED_MESSAGE_PREFIX);
//		}

		this.setLastActivityTime(new java.util.Date());
	}

	/** 
	 Sends the signal to the accessory to permanently disconnect
	 
	 
	 Once issued this will require waking and reconnecting to the reader to use it again.
	 This is a convenience method and is equivalent to sending a TSLSleepCommand to the reader 
	 
	*/
	public final void permanentlyDisconnect()
	{
		// Disconnect as usual
		if (this.isConnected())
		{
			this.send(".sl");
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Assume the sleep command succeeded
			this.disconnect();
		}
	}

	/** 
	 Send the given string as a CrLf terminated string, to the reader.
	 This method waits until the command has been successfully sent
	 
	 @param line line The ASCII string to send to the device
	 @exception UnsupportedOperationException if no device is connected  
	*/
	public void send(String line)
	{
		if (!this.isConnected())
		{
			throw new UnsupportedOperationException("Lector no conectado");
		}

		//BJP - Default getBytes uses UTF8 which should be okay - look at other conversion methods to ensure ASCII bytes
		// Convert the string to ASCII bytes and write them out to the reader
		if( line.codePointBefore(line.length()) != '\r' && line.codePointBefore(line.length()) != '\n')
		{
			line += "\r\n";
		}
		this.readerService.write(line.getBytes());
		if (D) { Log.d(TAG, String.format("Command sent (%s)\n%s", System.currentTimeMillis(), line)); }
	}


	/** 
	 Gets a value indicating whether the last command completed as expected i.e. did not timeout.
	*/
	private boolean privateIsResponsive;
	public final boolean isResponsive()
	{
		return privateIsResponsive;
	}
	private void setIsResponsive(boolean value)
	{
		privateIsResponsive = value;
	}

	/** 
	 Gets or sets the time of the readers last activity (send or receive) 
	*/
	private java.util.Date privateLastActivityTime = new java.util.Date(0);
	public final java.util.Date getLastActivityTime()
	{
		return privateLastActivityTime;
	}
	protected final void setLastActivityTime(java.util.Date value)
	{
		privateLastActivityTime = value;
	}

	/** 
	 Gets the last command line sent
	 
	 
	 Because TSLAsciiCommands add a unique id number each time their commandLine property is accessed this property can be used 
	 (primarily for debugging) to get the actual command line issued (if examined immediately after the command is executed) 
	 
	*/
	private String privateLastCommandLine;
	public final String getLastCommandLine()
	{
		return privateLastCommandLine;
	}
	private void setLastCommandLine(String value)
	{
		privateLastCommandLine = value;
	}

	/** 
	 Gets the chain of responders that handle responses to commands
	*/
	public final Iterable<IAsciiCommandResponder> getResponderChain()
	{
		return this.responderChain;
	}

	/** 
	 Gets a value indicating whether the chain has a synchronous responder
	*/
	public final boolean getHasSynchronousResponder()
	{
		return this.synchronousResponder != null;
	}


	/** 
	 Execute the given command
	 
	 @param command The command to execute
	 
	 For synchronous commands the responder's delegate is set to this object 
	 Otherwise does nothing
	 Derived classes should always call the inherited implementation.
	 
	*/
	public void executeCommand(IAsciiCommand command)
	{
		if (command == null)
		{
			throw new IllegalArgumentException("command is null");
		}

		if (command.getSynchronousCommandResponder() != null)
		{
			synchronized (this.responderLock)
			{
				if (this.synchronousResponder == null)
				{
					throw new UnsupportedOperationException("No synchronous command relay in chain");
				}

				if (this.synchronousResponder.getSynchronousCommandResponder() != null)
				{
					throw new UnsupportedOperationException("There is already a synchronous command executing");
				}

				this.synchronousResponder.setSynchronousCommandResponder(command.getSynchronousCommandResponder());
				command.getSynchronousCommandResponder().clearLastResponse();
			}
		}

		this.executeCommand(command, command.getMaxSynchronousWaitTime());
	}

	/** 
	 Add a responder to the responder chain
	 
	 @param responder The responder to add
	*/
	public final void addResponder(IAsciiCommandResponder responder)
	{
		synchronized (this.responderLock)
		{
			this.responderChain.add(responder);
		}
	}

	/** 
	 Remove a responder from the responder chain
	 
	 @param responder The responder to remove
	*/
	public final void removeResponder(IAsciiCommandResponder responder)
	{
		synchronized (this.responderLock)
		{
			this.responderChain.remove(responder);
		}
	}

	/** 
	 Add the synchronous responder into the chain
	*/
	public final void addSynchronousResponder()
	{
		synchronized (this.responderLock)
		{
			if (this.synchronousResponder == null)
			{
				// Create the dispatch responder
				this.synchronousResponder = new SynchronousDispatchResponder();
				this.addResponder(this.synchronousResponder);
			}
		}
	}

	/** 
	 Remove the synchronous responder from the chain
	*/
	public final void removeSynchronousResponder()
	{
		synchronized (this.responderLock)
		{
			if (this.synchronousResponder != null)
			{
				this.removeResponder(this.synchronousResponder);
				this.synchronousResponder = null;
			}
		}
	}

	/** 
	 Clear all responders from the responder chain
	*/
	public final void clearResponders()
	{
		synchronized (this.responderLock)
		{
			this.responderChain.clear();
			this.synchronousResponder = null;
		}
	}


	/** 
	 This should be called when new data is received from the reader
	 
	 @param receivedLines A number of complete lines received from the reader
	 * @throws Exception 
	*/
	synchronized protected void processReceivedLines(java.util.Collection<String> receivedLines) throws Exception
	{
		int lineNumber = 0;



		for (String line : receivedLines)
		{
			this.processReceivedLine(line, lineNumber++, lineNumber < receivedLines.size() - 1);
		}


	}

	/** 
	 Called from ProcessReceivedLines to process each line received from the reader
	 
	 @param line The received line
	 @param lineNumber The line number in the set of lines being processed
	 @param moreAvailable True if not the last line in the set
	 * @throws Exception 
	*/
	protected void processReceivedLine(String line, int lineNumber, boolean moreAvailable) throws Exception
	{
		boolean handled;



		// Iterate over the responder chain
		for (IAsciiCommandResponder responder : this.getResponderChain())
		{
			try
			{
				handled = responder.processReceivedLine(line, moreAvailable);
			}
			catch (Exception exception)
			{
				Log.e(TAG, "Exception while processing response line", exception);
				throw exception;
			}

			if (!this.responseReceived && responder.isResponseFinished() && (responder instanceof SynchronousDispatchResponder))
			{

				synchronized(this.commandCondition)
				{
					// signal the synchronous command completed
					this.responseReceived = true;

					this.commandCondition.notify();
//					this.commandCondition.notifyAll();
//					Log.d(TAG, String.format("CommandCondition notified (%s)\n\"%s\"", this.getLastCommandLine(),line));
				}
			}
			if (handled)
			{


				// no more responders get to see this
				break;
			}
		}
	}

	/** 
	 Execute the given command
	 
	 @param command The command to execute
	 @param timeout The time to wait for a response when executing a synchronous command
	*/
	private void executeCommand(IAsciiCommand command, double timeout)
	{
		try
		{
			synchronized (this.commandSync)
			{
				// Assume command will complete okay
				this.setIsResponsive(true);

				if (timeout <= 0.0001)
				{
					throw new InvalidParameterException("Timeout must be greater than 0.0001s");
				}

				if (this.awaitingCommandResponse)
				{
					throw new UnsupportedOperationException("Already executing a command");
				}

				this.awaitingCommandResponse = command.getSynchronousCommandResponder() != null;
				if (this.awaitingCommandResponse && !this.getHasSynchronousResponder())
				{
					Log.e(TAG, "!!! No synchronous responder in the responder chain !!!");
					throw new UnsupportedOperationException("No synchronous responder in the responder chain");
				}

				// Only wait for a response if command is synchronous otherwise assume received
				this.responseReceived = command.getSynchronousCommandResponder() == null;

				try
				{
					boolean timedOut = false;
					synchronized( this.commandCondition)
					{
						// Issue command
						this.setLastCommandLine(command.getCommandLine());
						this.send(this.getLastCommandLine());

						// Wait for background thread to receive the response (if synchronous)
						Date commandStartTime = new Date();

						while (!this.responseReceived && !timedOut)
						{
							long waitStartTime = System.currentTimeMillis();
							if (D) {
								Log.d(TAG, String.format("Waiting %.2fs for command (%s) completion\nStart time: %s",
										timeout,
										this.getLastCommandLine(),
										System.currentTimeMillis()
										));
							}
	
							try {

								this.commandCondition.wait((long)(timeout * 1000));

							} catch (InterruptedException e) {
							}

							// Determine if the condition timed out
							long waitFinishTime = System.currentTimeMillis();
							double waitDuration = (waitFinishTime - waitStartTime) / 1000.0;
							timedOut = waitDuration >= timeout;

							Date now = new Date();
			                double commandDuration = (now.getTime() - commandStartTime.getTime()) / 1000.0;
							

							if( !responseReceived  && timedOut ) {
				                double timeSinceLastActivty = (now.getTime() - getLastActivityTime().getTime()) / 1000.0;

				                // Some commands can have an indeterminate duration e.g. read log file
				                // the command timeout applies from the time of the last received data
				                // Override the timeout if there has been activity in the timeout period
				                if( timeSinceLastActivty < timeout )
				                {
				                	timedOut = false;
				                }
				                else
				                {
				                	Log.e(TAG, "Command timed out!" + String.format(" (%.2fs)", commandDuration ));
				                }
							} else {
								if (D) { Log.d(TAG, "Command completed" + String.format(" (%.2fs)", commandDuration )); }
							}
						}
					}

					if (timedOut)
					{
						this.setIsResponsive(false);
					}
				}
				catch (RuntimeException exception)
				{
					Log.e(TAG, "Command failed", exception);
					throw exception;
				}
				finally
				{
					this.awaitingCommandResponse = false;
				}
			}
		}
		catch (RuntimeException exception)
		{
			Log.e(TAG, "executeCommand failed", exception);

			// Propagate any exceptions
			throw exception;
		}
		finally
		{
			// Only call the super class commandDidFinish to tidy up but avoid commandCondition deadlock
			this.commandDidFinish();
		}
	}

	/** 
	 Must be called when a synchronous command finishes
	*/
	private void commandDidFinish()
	{
		synchronized (this.responderLock)
		{
			if (this.synchronousResponder != null)
			{
				this.synchronousResponder.setSynchronousCommandResponder(null);
			}
		}
	}


	private void sendStateChangeNotification( String reason )
	{
		Intent intent = new Intent(STATE_CHANGED_NOTIFICATION);
		intent.putExtra(REASON_KEY, reason);
		LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);	
	}
	
    // The Handler that gets information back from the BluetoothReaderService
	// Has to be static to avoid memory leaks
	private static class InnerHandler extends Handler {
		WeakReference<AsciiCommander> weakCommander;
		public InnerHandler(AsciiCommander commander) {
			super();
			weakCommander = new WeakReference<AsciiCommander>(commander);
		}

		@Override
        public void handleMessage(Message msg) {
            try {
            	AsciiCommander commander = weakCommander.get();

            	// Ensure AsciiCommander reference is still valid
            	// The reference is weak so it may have been garbage collected
            	if( commander != null)
            	{
            		switch (msg.what) {
            		case BluetoothReaderService.MESSAGE_STATE_CHANGE:
            			if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
            			String reasonMsg = "";
            			switch (msg.arg1)
            			{
            			case BluetoothReaderService.STATE_CONNECTED:
            				// Extract the name of the connected device
            				commander.deviceName = msg.getData().getString(BluetoothReaderService.DEVICE_NAME_KEY);
            				reasonMsg = CONNECTED_MESSAGE_PREFIX + commander.deviceName;
            				Log.d(TAG, reasonMsg);

            				commander.lastSuccessfullyConnectedReader = commander.reader;

            				SharedPreferences preferences = commander.context.getSharedPreferences("AsciiCommanderPreferences", Context.MODE_PRIVATE);  
            				SharedPreferences.Editor editor = preferences.edit();
            				editor.putString("lastConnectedReaderAddress", commander.reader.getAddress());
            				editor.commit();

            				// Determine the device properties
            				commander.executeCommand(commander.informationCommand);
            				if( commander.informationCommand.isSuccessful())
            				{
            					String serialNumber = commander.informationCommand.getSerialNumber();
            					if( serialNumber.length() >= 4)
            					{
            						commander.deviceProperties = new DeviceProperties(serialNumber.substring(0, 4));
            					}
            				}
            				commander.connectionState = ConnectionState.CONNECTED;
            				commander.sendStateChangeNotification(reasonMsg);
            				break;

            			case BluetoothReaderService.STATE_CONNECTING:
            				reasonMsg = CONNECTING_MESSAGE_PREFIX;
            				commander.connectionState = ConnectionState.CONNECTING;
            				commander.sendStateChangeNotification(reasonMsg);
            				break;

            			case BluetoothReaderService.STATE_DISCONNECTED:
            				String btServiceReasonMsg = msg.getData().getString(BluetoothReaderService.REASON_KEY);
            				Log.d(TAG, "Disconnected: " + btServiceReasonMsg);
            				reasonMsg = DISCONNECTED_MESSAGE_PREFIX;
            				commander.connectionState = ConnectionState.DISCONNECTED;
            				commander.deviceProperties = DeviceProperties.DEVICE_DEFAULTS;
            				commander.sendStateChangeNotification(reasonMsg);
            				break;

            			case BluetoothReaderService.STATE_NONE:
            				break;
            			}
            			break;
            		}
            	}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
    private final Handler mHandler = new InnerHandler(this);

	@Override
	public void update(Observable arg0, Object arg1) {
		if( arg0.equals(readerService))
		{
        	//BJP - Bluetooth service is currently sending single lines so 
			// create a list of one line - future implementations may restore
			// multiple lines from the reader service
    		ArrayList<String> receivedLines = new ArrayList<String>();
    		receivedLines.add((String) (String)arg1);

    		// Iterate over the received lines passing each one through the responder chain
    		try
    		{
				processReceivedLines(receivedLines);
			}
    		catch (Exception e)
			{
				Log.e(TAG, "Unhandled exception: " + e.getMessage());
			} finally {
				if (isConnected()) {
					if (getConnectionState().equals(ConnectionState.CONNECTED)){
						mCallback.onArticleSelected();
					}
				}
			}
        	
    		setLastActivityTime(new java.util.Date());
		}
		
	}

}