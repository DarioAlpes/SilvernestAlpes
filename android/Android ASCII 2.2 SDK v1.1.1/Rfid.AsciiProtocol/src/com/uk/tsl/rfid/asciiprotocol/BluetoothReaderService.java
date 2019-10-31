/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//
//
//
// This is derived from the Android BluetoothChat sample code
//
//


package com.uk.tsl.rfid.asciiprotocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Observable;
import java.util.Scanner;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothReaderService extends Observable {
    // Debugging
    private static final String TAG = "BluetoothReaderService";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;

    // Key names sent from the BluetoothChatService Handler
    public static final String DEVICE_NAME_KEY = "device_name";
    public static final String REASON_KEY = "reason";


    // Serial Port Profile UUID for this application
    private static final UUID MY_UUID_SECURE =
        UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final UUID MY_UUID_INSECURE =
        UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
//    private AcceptThread mSecureAcceptThread;
//    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;			// we're doing nothing
    public static final int STATE_DISCONNECTED = 1;	// now awaiting next outgoing connection
    public static final int STATE_CONNECTING = 2;	// now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;	// now connected to a remote device


    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothReaderService(Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_DISCONNECTED;
        mHandler = handler;
        mConnectThread = null;
        mConnectedThread = null;
    }

    /**
     * Set the current state of the reader connection and notify the AsciiCommander
     * 
     * @param state  An integer defining the current connection state
     * @param bundle Additional data relating to the state change
     */
    private synchronized void setState(int state, Bundle bundle) {
//        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
    	int oldState = mState;
        mState = state;

        // Only send message if this is a change of state
        if( oldState != state )
        {
        	Message msg = mHandler.obtainMessage(BluetoothReaderService.MESSAGE_STATE_CHANGE, state, -1);
        	if( bundle != null )
        	{
        		msg.setData(bundle);
        	}
        	mHandler.sendMessage(msg);
        }
    }

    private synchronized void setStateForReason( int state, String reason)
    {
        Bundle bundle = new Bundle();
        bundle.putString(REASON_KEY, reason);
    	setState(state, bundle);
    }

    private synchronized void setState(int state)
    {
    	mState = state;
    }
    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

//    /**
//     * Start the reader service. Specifically start AcceptThread to begin a
//     * session in listening (server) mode. Called by the Activity onResume() */
//    public synchronized void start() {
//        if (D) Log.d(TAG, "start");
//
//        // Cancel any thread attempting to make a connection
//        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
//
//        // Cancel any thread currently running a connection
//        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
//
//        setStateForReason(STATE_DISCONNECTED, "Initial state");
//
//    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        setState(STATE_CONNECTING, null);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        if (D) Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Send the name of the connected device back to the AsciiCommander
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_NAME_KEY, device.getName());
        bundle.putString(REASON_KEY, "Device connected.");

        setState(STATE_CONNECTED, bundle);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");

        if (mConnectThread != null) {
            if (D) Log.d(TAG, "cancelling connect thread");
            mConnectThread.cancel();
            if (D) Log.d(TAG, "cancelled connect thread");
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            if (D) Log.d(TAG, "cancelling connected thread");
            mConnectedThread.cancel();
            if (D) Log.d(TAG, "cancelled connected thread");
            mConnectedThread = null;
        }

        //setStateForReason(STATE_DISCONNECTED, "Stopped");
        setState(STATE_DISCONNECTED);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            if (D) Log.d(TAG, "- ConnectThread Created");
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            if (D) Log.d(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);

            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                	synchronized (this) {
                		mmSocket.close();
    				}
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }

                setStateForReason(STATE_DISCONNECTED, "Connection failed");
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothReaderService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);

            if (D) Log.d(TAG, "END mConnectThread");
        }

        public synchronized void cancel() {
            if (D) Log.d(TAG, "connect thread cancel() BEGAN");
            try {
            	synchronized (this) {
            		mmSocket.close();
				}
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
            if (D) Log.d(TAG, "connect thread cancel() ENDED");
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        private volatile boolean mCancelled = false;

        public ConnectedThread(BluetoothSocket socket, String socketType)
        {
            if (D) Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e)
            {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
        	mCancelled = false;
            if (D) Log.d(TAG, "BEGIN mConnectedThread");
            InputStreamReader isr = new InputStreamReader(mmInStream);

            BufferedReader receivedDataReader = new BufferedReader(isr); 
            String messageLine = null;

            // Keep listening to the InputStream while connected
            while (!mCancelled)
            {
                try
                {
                	while( (messageLine = receivedDataReader.readLine()) != null )
                	{
                    	// Inform observers that a line has been received
                    	setChanged();
                    	notifyObservers(messageLine);
                	}

                	// 
                	if( !mCancelled )
                	{
                		Log.d(TAG, "Connection appears to be lost");

                		setStateForReason(STATE_DISCONNECTED, "Connection appears to be lost");
                		cancel();
                		mConnectedThread = null;
                	}
                }
                catch (Exception e)
                {
                    Log.d(TAG, "Disconnected", e);
                    setStateForReason(STATE_DISCONNECTED, "Connection lost");
                    break;
                }
            }

            try
            {
            	if( isr != null ) isr.close();
                if(receivedDataReader != null) receivedDataReader.close();
			}
            catch (IOException e)
            {
                if (D) Log.d(TAG, "Failed to close input stream reader (mConnectedThread)");
			}

            if (D) Log.d(TAG, "END mConnectedThread");
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer)
        {
            try
            {
                mmOutStream.write(buffer);

            }
            catch (IOException e)
            {
                Log.e(TAG, "Exception during write", e);
                setStateForReason(STATE_DISCONNECTED, "Connection lost");
                cancel();
                mConnectedThread = null;
            }
        }

        public synchronized void cancel()
        {
            if (D) Log.d(TAG, "connected thread cancel() BEGAN");
            try
            {
            	synchronized (this)
            	{
            		mCancelled = true;
            		mmSocket.close();
				}
            }
            catch (IOException e)
            {
                Log.e(TAG, "close() of connect socket failed", e);
            }
            if (D) Log.d(TAG, "connected thread cancel() ENDED");
        }
    }
}
