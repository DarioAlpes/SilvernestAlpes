//----------------------------------------------------------------------------------------------
// Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved.
//----------------------------------------------------------------------------------------------

package co.smartobjects.visitscreator;

import co.smartobjects.visitscreator.entities.Location;
import co.smartobjects.visitscreator.entities.Sensor;
import co.smartobjects.visitscreator.utils.TSLBluetoothDeviceActivity;
import co.smartobjects.visitscreator.utils.ModelBase;
import co.smartobjects.visitscreator.utils.WeakHandler;
import co.smartobjects.visitscreator.utils.services.RESTReader;
import co.smartobjects.visitscreator.utils.services.RESTResultProcessor;

import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.commands.FactoryDefaultsCommand;
import com.uk.tsl.rfid.asciiprotocol.enumerations.Databank;
import com.uk.tsl.rfid.asciiprotocol.enumerations.EnumerationBase;
import com.uk.tsl.rfid.asciiprotocol.parameters.AntennaParameters;
import com.uk.tsl.rfid.asciiprotocol.responders.LoggerResponder;

import android.os.Bundle;
import android.os.Message;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

@SuppressWarnings("ALL")
public class VisitsCreatorActivity extends TSLBluetoothDeviceActivity{

    private List<Location> locations;
    private Hashtable<Long, Sensor> sensorsByIdLocation;

    // Debug control
    private static final boolean D = BuildConfig.DEBUG;

    // The text view to display the RF Output Power used in RFID commands
    private TextView mPowerLevelTextView;
    // The seek bar used to adjust the RF Output Power for RFID commands
    private SeekBar mPowerSeekBar;
    // The current setting of the power level
    private int mPowerLevel = AntennaParameters.MaximumCarrierPower;

    private static final long ID_CLIENT = 18;

    // Custom adapter for the Ascii command enumerated parameter values to display the description rather than the toString() value
    public class ParameterEnumerationArrayAdapter<T extends EnumerationBase > extends ArrayAdapter<T> {
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

    // The buttons that invoke actions
    private Button mReadActionButton;
    private Button mClearActionButton;

    //Create model class derived from ModelBase
    private VisitsCreatorModel mModel;

    private TextView mResultTextView;
    private ScrollView mResultScrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorsByIdLocation = new Hashtable<>();
        setContentView(R.layout.activity_visits_creator);


        // The SeekBar provides an integer value for the antenna power
        mPowerLevelTextView = (TextView)findViewById(R.id.powerTextView);
        mPowerSeekBar = (SeekBar)findViewById(R.id.powerSeekBar);
        mPowerSeekBar.setOnSeekBarChangeListener(mPowerSeekBarListener);
        // Set the seek bar to cover the range of the power settings -
        mPowerSeekBar.setMax(AntennaParameters.MaximumCarrierPower - AntennaParameters.MinimumCarrierPower);
        mPowerLevel = AntennaParameters.MaximumCarrierPower;
        mPowerSeekBar.setProgress(mPowerLevel - AntennaParameters.MinimumCarrierPower);

        // Set up the action buttons
        mReadActionButton = (Button)findViewById(R.id.readButton);
        mReadActionButton.setOnClickListener(mAction1ButtonListener);

        mClearActionButton = (Button)findViewById(R.id.clearButton);
        mClearActionButton.setOnClickListener(mAction3ButtonListener);

        // Set up the results area
        mResultTextView = (TextView)findViewById(R.id.resultTextView);
        mResultScrollView = (ScrollView)findViewById(R.id.resultScrollView);

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

        mModel = new VisitsCreatorModel();
        mModel.setCommander(getCommander());
        mModel.setHandler(mGenericModelHandler);
        mModel.getReadCommand().setBank(Databank.ELECTRONIC_PRODUCT_CODE);
        mModel.getReadCommand().setOffset(2);
        mModel.getReadCommand().setLength(6);
        mModel.getReadCommand().setSelectData(String.format("%012x", ID_CLIENT));
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
        this.locations = null;
        this.sensorsByIdLocation = null;
        new RESTReader(new VisitsGetter(this))
                .execute("https://smartobjectssas.appspot.com/clients/" + ID_CLIENT + "/locations/");

        new RESTReader(new SensorsGetter(this))
                .execute("https://smartobjectssas.appspot.com/clients/" + ID_CLIENT + "/static-sensors/");
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
                Toast.makeText(this.getApplicationContext(), "Reconnecting...", Toast.LENGTH_LONG).show();
                reconnectDevice();
                UpdateUI();
                return true;

            case R.id.insecure_connect_reader_menu_item:
                // Choose a device and connect to it
                selectDevice();
                return true;

            case R.id.disconnect_reader_menu_item:
                Toast.makeText(this.getApplicationContext(), "Disconnecting...", Toast.LENGTH_SHORT).show();
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

    private final WeakHandler<VisitsCreatorActivity> mGenericModelHandler = new WeakHandler<VisitsCreatorActivity>(this) {

        @Override
        public void handleMessage(Message msg, VisitsCreatorActivity thisActivity) {
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
                        mResultTextView.append(message);
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
        String connectionMsg = "Reader: " + (getCommander().isConnected() ? getCommander().getConnectedDeviceName() : "Disconnected");
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
            String msg = "Reset " + (fdCommand.isSuccessful() ? "succeeded" : "failed");
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
    // Handlers for the action buttons - invoke the currently selected actions
    //----------------------------------------------------------------------------------------------

    private OnClickListener mAction1ButtonListener = new OnClickListener() {
        public void onClick(View v) {
            mModel.read();
        }
    };


    private OnClickListener mAction3ButtonListener = new OnClickListener() {
        public void onClick(View v) {
            mResultTextView.setText("");
        }
    };

    private AdapterView.OnItemSelectedListener spinnerSelectionListener=new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Location selectedLocation = (Location)parent.getItemAtPosition(position);
            if(selectedLocation != null && sensorsByIdLocation != null) {
                mModel.setLocation(selectedLocation, sensorsByIdLocation.get(selectedLocation.getId()));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private synchronized void setLocations(List<Location> locations) {
        this.locations = locations;
        if(this.sensorsByIdLocation != null){
            initializeLocationsSpiner();
        }
    }


    private synchronized void setSensors(List<Sensor> sensors) {
        sensorsByIdLocation = new Hashtable<>();
        for(Sensor sensor: sensors){
            sensorsByIdLocation.put(sensor.getIdLocation(), sensor);
        }

        if(this.locations != null){
            initializeLocationsSpiner();
        }
    }

    private void initializeLocationsSpiner() {
        filterLocations();        
        Spinner locationsSpinner = (Spinner) findViewById(R.id.locations_spinner);
        locationsSpinner.setOnItemSelectedListener(spinnerSelectionListener);
        ArrayAdapter<Location> adapter = new ArrayAdapter<>(this,
                R.layout.spinner_item_layout, locations);
        assert locationsSpinner != null;
        locationsSpinner.setAdapter(adapter);

    }

    private void filterLocations() {
        List<Location> locationsFiltered = new ArrayList<>();
        for(Location location:locations) {
            if(sensorsByIdLocation.containsKey(location.getId())){
                locationsFiltered.add(location);
            }
        }
        this.locations = locationsFiltered;
    }

    private class VisitsGetter implements RESTResultProcessor {

        public VisitsCreatorActivity visitsActivity;

        public VisitsGetter(VisitsCreatorActivity visitsActivity) {
            this.visitsActivity = visitsActivity;
        }

        @Override
        public void processResult(String url, String result) {
            List<Location> locations = Location.listFromJSONString(result);
            if(locations!=null) {
                this.visitsActivity.setLocations(locations);
            }
        }
    }

    private class SensorsGetter implements RESTResultProcessor {

        public VisitsCreatorActivity visitsActivity;

        public SensorsGetter(VisitsCreatorActivity visitsActivity) {
            this.visitsActivity = visitsActivity;
        }

        @Override
        public void processResult(String url, String result) {
            List<Sensor> sensors = Sensor.listFromJSONString(result);
            if(sensors!=null) {
                this.visitsActivity.setSensors(sensors);
            }
        }
    }
}
