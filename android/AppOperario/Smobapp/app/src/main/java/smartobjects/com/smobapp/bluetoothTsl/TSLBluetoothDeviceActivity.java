//----------------------------------------------------------------------------------------------
// Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved.
//----------------------------------------------------------------------------------------------

package smartobjects.com.smobapp.bluetoothTsl;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.uk.tsl.rfid.DeviceListActivity;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;

import java.util.Timer;
import java.util.TimerTask;

import smartobjects.com.smobapp.utils.BaseActivity;
import smartobjects.com.smobapp.utils.GlobalClass;

public class TSLBluetoothDeviceActivity extends BaseActivity {
    // Debugging
    private static final String TAG = "TSLBluetoothDeviceActivity";
//    private static final boolean D = true;
//    private static final boolean D = BuildConfig.DEBUG;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mDevice = null;

    /**
     * @return the current AsciiCommander
     */
    protected AsciiCommander getCommander()
    {
    	return ((GlobalClass)getApplication()).getCommander();
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        bluetooth();

		// Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
        	bluetoothNotAvailableError("Bluetooth is not available on this device\nApplication Quitting...");
            return;
        }

        // Create the AsciiCommander to talk to the reader (if it doesn't already exist)
        if( getCommander() == null)
        {
        	try {
                GlobalClass app = (GlobalClass)getApplication();
				AsciiCommander commander = new AsciiCommander(getApplicationContext(), this);
	        	app.setCommander(commander);

        	} catch (Exception e) {
				fatalError("Unable to create AsciiCommander!");
			}
        }
	}

	//
	// Terminate the app with the given message
	//
	private void fatalError(String message)
	{
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Timer t = new Timer();
        t.schedule(new TimerTask() {
        	public void run() {
        		finish();
        	}
        }, 1800);
	}

	/**
	 *  Override this method to provide custom handling of the (fatal)
	 *  situation when bluetooth is not available
	 * 
	 * @param message the message describing the cause of the error
	 */
	protected void bluetoothNotAvailableError(String message)
	{
		fatalError(message);
	}


    @Override
    public void onStart()
    {
    	super.onStart();

        // If no other attempt to connect is ongoing try to connect to last used reader
    	// Note: When returning from the Device List activity 
        if( mDevice == null )
        {
            // Attempt to reconnect to the last reader used
            Toast.makeText(this, "Reconectando al último lector utilizado...", Toast.LENGTH_SHORT).show();
//            getCommander().connect(null);
        }
    }
    
    @Override
    public void onStop() {
        super.onStop();

        getCommander().disconnect();
        mDevice = null;
    }

    /**
     * Connect the current AsciiCommander to the given device
     * 
     * @param deviceData the device information received from the DeviceListActivity
     * @param secure true if a secure connection should be requested
     */
    private void connectToDevice(Intent deviceData, boolean secure) {
        Toast.makeText(this.getApplicationContext(), "Conectando...", Toast.LENGTH_LONG).show();
        // Get the device MAC address
        String address = deviceData.getExtras()
            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        mDevice = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        if( mDevice != null )
        {
        	getCommander().connect(mDevice);
        } else {
//        	if(D) Log.e(TAG, "Unable to obtain BluetoothDevice!");
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

//    	if(D) Log.d(TAG, "selectDevice() onActivityResult: " + resultCode + " for request: " + requestCode);

    	switch (requestCode) {
        case REQUEST_CONNECT_DEVICE_SECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectToDevice(data, true);
            }
            break;
        case REQUEST_CONNECT_DEVICE_INSECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectToDevice(data, false);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode != Activity.RESULT_OK) {
                // User did not enable Bluetooth or an error occurred
//                Log.d(TAG, "BT not enabled");
                bluetoothNotAvailableError("Bluetooth was not enabled\nApplication Quitting...");
            }
        }
    }

    /**
     * Launches an activity that allows user to select a device to use
     */
    public void  selectDevice()
    {
        // Launch the DeviceListActivity to see devices and do scan
    	Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
    }
    

    /**
     * Disconnects the currently connected device
     */
    public void disconnectDevice()
    {
    	mDevice = null;
    	getCommander().disconnect();
    }

    /**
     * Reconnects to the last successfully connected reader
     */
    public void reconnectDevice()
    {
    	getCommander().connect(null);
    }

    private void bluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                        }
                    }
                });
                builder.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }

}
