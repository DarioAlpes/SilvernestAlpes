//----------------------------------------------------------------------------------------------
// Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved.
//----------------------------------------------------------------------------------------------

package smartobjects.com.smobapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.commands.BatteryStatusCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.FactoryDefaultsCommand;
import com.uk.tsl.rfid.asciiprotocol.enumerations.Databank;
import com.uk.tsl.rfid.asciiprotocol.enumerations.EnumerationBase;
import com.uk.tsl.rfid.asciiprotocol.parameters.AntennaParameters;
import com.uk.tsl.rfid.asciiprotocol.responders.LoggerResponder;
import com.uk.tsl.utils.HexEncoding;

import smartobjects.com.smobapp.bluetoothTsl.ModelBase;
import smartobjects.com.smobapp.bluetoothTsl.ReadWriteModel;
import smartobjects.com.smobapp.bluetoothTsl.TSLBluetoothDeviceActivity;
import smartobjects.com.smobapp.bluetoothTsl.WeakHandler;
import smartobjects.com.smobapp.objects.ObjectItem;

public class ReadWriteActivity extends TSLBluetoothDeviceActivity
					implements AsciiCommander.OnHeadlineSelectedListener{

	// Debug control
	private static final boolean D = BuildConfig.DEBUG;

	// The text view to display the RF Output Power used in RFID commands
	private TextView mPowerLevelTextView;
	// The seek bar used to adjust the RF Output Power for RFID commands
	private SeekBar mPowerSeekBar;
	// The current setting of the power level
	private int mPowerLevel = AntennaParameters.MaximumCarrierPower;
	// Time between every found
	private int mTimeAntenna = 3000;

	private Spinner mSpAntennas;

	String[] items = { "Antena Uno", "Antena Dos", "Antena Tres"};

	@Override
	public void onArticleSelected() {
		try{
			if (null != mModel){
				find();
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public synchronized void findAutomatic() throws Exception {
		Handler handler = new Handler();
		try{
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					AsciiCommander commander = getCommander();
					try {
						if (isNotNull(commander)){
							if (commander.isConnected() && !mModel.isBusy()){
								find();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							findAutomatic();
							commander = null;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}, mTimeAntenna);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			handler = null;
		}
	}

	public void find(){
		mModel.read();
	}


	// Custom adapter for the Ascii command enumerated parameter values to display the description rather than the toString() value
	public class ParameterEnumerationArrayAdapter<T extends EnumerationBase> extends ArrayAdapter<T> {
		private final T[] mValues;

		public ParameterEnumerationArrayAdapter(Context context, int textViewResourceId, T[] objects) {
			super(context, textViewResourceId, objects);
			mValues = objects;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView view = (TextView)super.getView(position, convertView, parent);
			view.setText(mValues[position].getDescription());
			return view;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			TextView view = (TextView)super.getDropDownView(position, convertView, parent);
			view.setText(mValues[position].getDescription());
			return view;
		}
	}
	
	private Databank[] mDatabanks = new Databank[] {
		Databank.ELECTRONIC_PRODUCT_CODE,
		Databank.TRANSPONDER_IDENTIFIER,
		Databank.RESERVED,
		Databank.USER
	};
	private ParameterEnumerationArrayAdapter<Databank> mDatabankArrayAdapter;

    // The buttons that invoke actions
	private Button mReadActionButton;
	private Button mWriteActionButton;
	private Button mClearActionButton;

	//Create model class derived from ModelBase
	private ReadWriteModel mModel;

	// The text-based parameters
	private EditText mTargetTagEditText;
	private EditText mWordAddressEditText;
	private EditText mWordCountEditText;
	private EditText mDataEditText;

	private TextView mResultTextView;
	private ScrollView mResultScrollView;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read_write);
    	
        // The SeekBar provides an integer value for the antenna power
        mPowerLevelTextView = (TextView)findViewById(R.id.powerTextView);
        mPowerSeekBar = (SeekBar)findViewById(R.id.powerSeekBar);
        mPowerSeekBar.setOnSeekBarChangeListener(mPowerSeekBarListener);
        // Set the seek bar to cover the range of the power settings - 
        mPowerSeekBar.setMax(AntennaParameters.MaximumCarrierPower - AntennaParameters.MinimumCarrierPower);
    	mPowerLevel = AntennaParameters.MaximumCarrierPower;
        mPowerSeekBar.setProgress(mPowerLevel - AntennaParameters.MinimumCarrierPower);

        // Set up the spinner with the memory bank selections
        mDatabankArrayAdapter = new ParameterEnumerationArrayAdapter<Databank>(this, android.R.layout.simple_spinner_item, mDatabanks);
    	// Find and set up the sessions spinner
        Spinner spinner = (Spinner) findViewById(R.id.bankSpinner);
        mDatabankArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(mDatabankArrayAdapter);
        spinner.setOnItemSelectedListener(mBankSelectedListener);
        spinner.setSelection(mDatabanks.length - 1);

        
        // Set up the action buttons
    	mReadActionButton = (Button)findViewById(R.id.readButton);
    	mReadActionButton.setOnClickListener(mAction1ButtonListener);

    	mWriteActionButton = (Button)findViewById(R.id.writeButton);
    	mWriteActionButton.setOnClickListener(mAction2ButtonListener);

    	mClearActionButton = (Button)findViewById(R.id.clearButton);
    	mClearActionButton.setOnClickListener(mAction3ButtonListener);

    	// Set up the target EPC EditText
    	mTargetTagEditText = (EditText)findViewById(R.id.targetTagEditText);
		mTargetTagEditText.addTextChangedListener(mTargetTagEditTextChangedListener);

		mWordAddressEditText = (EditText)findViewById(R.id.wordAddressEditText);
		mWordAddressEditText.addTextChangedListener(mWordAddressEditTextChangedListener);

		mWordCountEditText = (EditText)findViewById(R.id.wordCountEditText);
		mWordCountEditText.addTextChangedListener(mWordCountEditTextChangedListener);

		mDataEditText = (EditText)findViewById(R.id.dataEditText);
		mDataEditText.addTextChangedListener(mDataEditTextChangedListener);

		// Set up the results area
		mResultTextView = (TextView)findViewById(R.id.resultTextView);
		mResultScrollView = (ScrollView)findViewById(R.id.resultScrollView);

		mSpAntennas = (Spinner) findViewById(R.id.sp_read_antenna);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpAntennas.setAdapter(adapter);
		
        //
		// An AsciiCommander has been created by the base class
		//
    	AsciiCommander commander = getCommander();

		// Add the LoggerResponder - this simply echoes all lines received from the reader to the log
        // and passes the line onto the next responder
        // This is added first so that no other responder can consume received lines before they are logged.
        commander.addResponder(new LoggerResponder());

        // Add a synchronous responder to handle synchronous commands
        commander.addSynchronousResponder();

        //TODO: Create a (custom) model and configure its commander and handler
        mModel = new ReadWriteModel();
        mModel.setCommander(getCommander());
        mModel.setHandler(mGenericModelHandler);

        // Use the model's values for the offset and length
        // Display the initial values
        int offset = mModel.getReadCommand().getOffset();
        int length = mModel.getReadCommand().getLength();
        mWordAddressEditText.setText(String.format("%d", offset));
        mWordCountEditText.setText(String.format("%d", length));

		try {
			findAutomatic();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



    //----------------------------------------------------------------------------------------------
	// Pause & Resume life cycle
	//----------------------------------------------------------------------------------------------

    @Override
    public synchronized void onPause() {
        super.onPause();

        // Register to receive notifications from the AsciiCommander
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public synchronized void onResume()
    {
    	super.onResume();

        // Register to receive notifications from the AsciiCommander
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
        	      new IntentFilter(AsciiCommander.STATE_CHANGED_NOTIFICATION));

        displayReaderState();
        UpdateUI();
    }


    //----------------------------------------------------------------------------------------------
	// Menu
	//----------------------------------------------------------------------------------------------

	private MenuItem mReconnectMenuItem;
	private MenuItem mConnectMenuItem;
	private MenuItem mDisconnectMenuItem;
	private MenuItem mResetMenuItem;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.reader_menu, menu);

		mResetMenuItem = menu.findItem(R.id.reset_reader_menu_item);
		mReconnectMenuItem = menu.findItem(R.id.reconnect_reader_menu_item);
		mConnectMenuItem = menu.findItem(R.id.insecure_connect_reader_menu_item);
		mDisconnectMenuItem= menu.findItem(R.id.disconnect_reader_menu_item);
		return true;
	}


	/**
	 * Prepare the menu options
	 */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

    	boolean isConnected = getCommander().isConnected();
    	mResetMenuItem.setEnabled(isConnected);
    	mDisconnectMenuItem.setEnabled(isConnected);

    	mReconnectMenuItem.setEnabled(!isConnected);
    	mConnectMenuItem.setEnabled(!isConnected);
    	
    	return super.onPrepareOptionsMenu(menu);
    }
    
	/**
	 * Respond to menu item selections
	 */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

        case R.id.reconnect_reader_menu_item:
            Toast.makeText(this.getApplicationContext(), "Reconectando...", Toast.LENGTH_LONG).show();
        	reconnectDevice();
        	UpdateUI();
        	return true;

        case R.id.insecure_connect_reader_menu_item:
            // Choose a device and connect to it
        	selectDevice();
            return true;

        case R.id.disconnect_reader_menu_item:
            Toast.makeText(this.getApplicationContext(), "Desconectando...", Toast.LENGTH_SHORT).show();
        	disconnectDevice();
        	displayReaderState();
        	return true;

        case R.id.reset_reader_menu_item:
        	resetReader();
        	UpdateUI();
        	return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //----------------------------------------------------------------------------------------------
	// Model notifications
	//----------------------------------------------------------------------------------------------

    private final WeakHandler<ReadWriteActivity> mGenericModelHandler = new WeakHandler<ReadWriteActivity>(this) {

		@Override
		public void handleMessage(Message msg, ReadWriteActivity thisActivity) {
			try {
				switch (msg.what) {
				case ModelBase.BUSY_STATE_CHANGED_NOTIFICATION:
					if( mModel.error() != null ) {
						mResultTextView.append("\n Task failed:\n" + mModel.error().getMessage() + "\n\n");
						mResultScrollView.post(new Runnable() { public void run() { mResultScrollView.fullScroll(View.FOCUS_DOWN); } });
					}
					UpdateUI();
					break;

				case ModelBase.MESSAGE_NOTIFICATION:
					String message = (String)msg.obj;
//					String[] data = message.split("\n");
					mResultTextView.append(message);
//					mResultTextView.append(data[1] + "\n");
					mResultScrollView.post(new Runnable() { public void run() { mResultScrollView.fullScroll(View.FOCUS_DOWN); } });
					break;

					case 99:
						String espacio = (String)msg.obj + "\n";
						mResultTextView.append(espacio);
						mResultScrollView.post(new Runnable() { public void run() { mResultScrollView.fullScroll(View.FOCUS_DOWN); } });
						break;
					
				default:
					break;
				}
			} catch (Exception e) {
			}
			
		}
	};

	
    //----------------------------------------------------------------------------------------------
	// UI state and display update
	//----------------------------------------------------------------------------------------------

    private void displayReaderState()
    {
		String connectionMsg = "Lector: " + (getCommander().isConnected() ? getCommander().getConnectedDeviceName() : "Desconectado");
		setTitle(connectionMsg);
    }
	
    
    //
    // Set the state for the UI controls
    //
    private void UpdateUI()
    {
    	boolean isConnected = getCommander().isConnected();
    	boolean canIssueCommand = isConnected & !mModel.isBusy();
    	mReadActionButton.setEnabled(canIssueCommand);
    	// Only enable the write button when there is at least a partial EPC
    	mWriteActionButton.setEnabled(canIssueCommand && mTargetTagEditText.getText().length() != 0);
    }

	
    //----------------------------------------------------------------------------------------------
	// AsciiCommander message handling
	//----------------------------------------------------------------------------------------------

    //
    // Handle the messages broadcast from the AsciiCommander
    //
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		if (D) { Log.d(getClass().getName(), "AsciiCommander state changed - isConnected: " + getCommander().isConnected()); }
    		
    		String connectionStateMsg = intent.getStringExtra(AsciiCommander.REASON_KEY);
            Toast.makeText(context, connectionStateMsg, Toast.LENGTH_LONG).show();

            displayReaderState();

            UpdateUI();
    	}
    };

    //----------------------------------------------------------------------------------------------
	// Reader reset
	//----------------------------------------------------------------------------------------------

    //
    // Handle reset controls
    //
    private void resetReader()
    {
		try {
			// Reset the reader
			FactoryDefaultsCommand fdCommand = FactoryDefaultsCommand.synchronousCommand();
			getCommander().executeCommand(fdCommand);
			String msg = "Reinicio " + (fdCommand.isSuccessful() ? "exitoso" : "fallido");
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
			
			UpdateUI();

		} catch (Exception e) {
			e.printStackTrace();
		}
    }


	//----------------------------------------------------------------------------------------------
	// Power seek bar
	//----------------------------------------------------------------------------------------------

    //
    // Handle events from the power level seek bar. Update the mPowerLevel member variable for use in other actions
    //
    private OnSeekBarChangeListener mPowerSeekBarListener = new OnSeekBarChangeListener() {
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// Nothing to do here
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {

			// Update the reader's setting only after the user has finished changing the value
			updatePowerSetting(AntennaParameters.MinimumCarrierPower + seekBar.getProgress());
			mModel.getReadCommand().setOutputPower(mPowerLevel);
			mModel.getWriteCommand().setOutputPower(mPowerLevel);
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			updatePowerSetting(AntennaParameters.MinimumCarrierPower + progress);
		}
	};

	private void updatePowerSetting(int level)	{
		mPowerLevel = level;
		mPowerLevelTextView.setText( mPowerLevel + " dBm");
	}

	
	//----------------------------------------------------------------------------------------------
	// Handler for changes in databank
	//----------------------------------------------------------------------------------------------

    private AdapterView.OnItemSelectedListener mBankSelectedListener =
			new AdapterView.OnItemSelectedListener()
    {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			Databank targetBank = (Databank)parent.getItemAtPosition(pos);
			if( mModel.getReadCommand() != null ) {
				mModel.getReadCommand().setBank(targetBank);
			}
			if( mModel.getWriteCommand() != null ) {
				mModel.getWriteCommand().setBank(targetBank);
			}

		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
    };


	//----------------------------------------------------------------------------------------------
	// Handlers for the action buttons - invoke the currently selected actions
	//----------------------------------------------------------------------------------------------

    private OnClickListener mAction1ButtonListener = new OnClickListener() {
    	public void onClick(View v) {
			mResultTextView.setText("");
    		mModel.read();
			getCommander().send(".bl");
    	}
    };
	
    private OnClickListener mAction2ButtonListener = new OnClickListener() {
    	public void onClick(View v) {
    		mModel.write();
    	}
    };

    private OnClickListener mAction3ButtonListener = new OnClickListener() {
    	public void onClick(View v) {
			mResultTextView.setText("");
			getCommander().send(".bl");
    	}
    };

    
	//----------------------------------------------------------------------------------------------
	// Handler for 
	//----------------------------------------------------------------------------------------------

    private TextWatcher mTargetTagEditTextChangedListener = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		
		@Override
		public void afterTextChanged(Editable s) {
			String value = s.toString();
			if( mModel.getReadCommand() != null ) {
				mModel.getReadCommand().setSelectData(value);
			}
			if( mModel.getWriteCommand() != null ) {
				mModel.getWriteCommand().setSelectData(value);
			}
			UpdateUI();
		}
	};


    private TextWatcher mWordAddressEditTextChangedListener = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		
		@Override
		public void afterTextChanged(Editable s) {
			int value = 0;
			try {
				value = Integer.parseInt(s.toString());

				if( mModel.getReadCommand() != null ) {
					mModel.getReadCommand().setOffset(value);
				}
				if( mModel.getWriteCommand() != null ) {
					mModel.getWriteCommand().setOffset(value);
				}
			} catch (Exception e) {
			}
			UpdateUI();
		}
	};


    private TextWatcher mWordCountEditTextChangedListener = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		
		@Override
		public void afterTextChanged(Editable s) {
			int value = 0;
			try {
				value = Integer.parseInt(s.toString());

				if( mModel.getReadCommand() != null ) {
					mModel.getReadCommand().setLength(value);
				}
				if( mModel.getWriteCommand() != null ) {
					mModel.getWriteCommand().setLength(value);
				}
			} catch (Exception e) {
			}
			UpdateUI();
		}
	};


    private TextWatcher mDataEditTextChangedListener = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		
		@Override
		public void afterTextChanged(Editable s) {
			String value = s.toString();
			if( mModel.getWriteCommand() != null ) {
				byte[] data = null;
				try {
					data = HexEncoding.stringToBytes(value);
					mModel.getWriteCommand().setData(data);
				} catch (Exception e) {
					// Ignore if invalid
				}
			}
			UpdateUI();
		}
	};


	private void clearData(){
		if (null != mResultTextView){
			ReadWriteActivity.this.mResultTextView.setText("");
		}
	}


//	@Override
//	public void onStart() {
//		super.onStart();
//		battery();
//	}

//	public void battery(){
//		new Handler().postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				BatteryStatusCommand battery = BatteryStatusCommand.synchronousCommand();
//				if (null != battery){
//					Log.i("battery", "Battery level : " + battery.getBatteryLevel() +
//							"\nCharge status : " + battery.getChargeStatus());
//				} else {
//					Log.i("battery", "Battery is null");
//				}
//				battery();
//			}
//		}, 1000);
//	}



}
