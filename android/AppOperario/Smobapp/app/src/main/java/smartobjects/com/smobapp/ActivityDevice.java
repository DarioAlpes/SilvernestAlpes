package smartobjects.com.smobapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.commands.FactoryDefaultsCommand;
import com.uk.tsl.rfid.asciiprotocol.responders.LoggerResponder;

import smartobjects.com.smobapp.bluetoothTsl.ModelBase;
import smartobjects.com.smobapp.bluetoothTsl.ReadWriteModel;
import smartobjects.com.smobapp.bluetoothTsl.TSLBluetoothDeviceActivity;
import smartobjects.com.smobapp.bluetoothTsl.WeakHandler;

public class ActivityDevice extends TSLBluetoothDeviceActivity implements View.OnClickListener{

    private ReadWriteModel mModel;
    private MenuItem mReconnectMenuItem;
    private MenuItem mConnectMenuItem;
    private MenuItem mDisconnectMenuItem;
    private MenuItem mResetMenuItem;

    private TextView mResultTextView;
    private ScrollView mResultScrollView;

    private Button mReadActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        mReadActionButton = (Button)findViewById(R.id.btn_inventario);
        mReadActionButton.setOnClickListener(this);

        //
        // An AsciiCommander has been created by the base class
        //
        AsciiCommander commander = getCommander();
//
//        // Add the LoggerResponder - this simply echoes all lines received from the reader to the log
//        // and passes the line onto the next responder
//        // This is added first so that no other responder can consume received lines before they are logged.
        commander.addResponder(new LoggerResponder());
//
//        // Add a synchronous responder to handle synchronous commands
        commander.addSynchronousResponder();
//
//        //Agregado prueba
        mModel = new ReadWriteModel();
        mModel.setCommander(getCommander());
        mModel.setHandler(mGenericModelHandler);

        mResultTextView = (TextView)findViewById(R.id.resultTextView);
        mResultScrollView = (ScrollView)findViewById(R.id.resultScrollView);
//
//        // Use the model's values for the offset and length
//        // Display the initial values
//        int offset = mModel.getReadCommand().getOffset();
//        int length = mModel.getReadCommand().getLength();
//        mWordAddressEditText.setText(String.format("%d", offset));
//        mWordCountEditText.setText(String.format("%d", length));
    }

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

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String connectionStateMsg = intent.getStringExtra(AsciiCommander.REASON_KEY);
            Toast.makeText(context, connectionStateMsg, Toast.LENGTH_LONG).show();

            displayReaderState();

            UpdateUI();
        }
    };
//
    private void displayReaderState()
    {
        String connectionMsg = "Reader: " + (getCommander().isConnected() ? getCommander().getConnectedDeviceName() : "Disconnected");
        setTitle(connectionMsg);
    }
//
    private void UpdateUI()
    {
        boolean isConnected = getCommander().isConnected();
        boolean canIssueCommand = isConnected & !mModel.isBusy();
//        mReadActionButton.setEnabled(canIssueCommand);
        // Only enable the write button when there is at least a partial EPC
//        mWriteActionButton.setEnabled(canIssueCommand && mTargetTagEditText.getText().length() != 0);
    }
//
    private final WeakHandler<ActivityDevice> mGenericModelHandler = new WeakHandler<ActivityDevice>(this) {

        @Override
        public void handleMessage(Message msg, ActivityDevice thisActivity) {
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
//
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
//
//    /**
//     * Prepare the menu options
//     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        boolean isConnected = getCommander().isConnected();
        mResetMenuItem.setEnabled(isConnected);
        mDisconnectMenuItem.setEnabled(isConnected);

        mReconnectMenuItem.setEnabled(!isConnected);
        mConnectMenuItem.setEnabled(!isConnected);

        return super.onPrepareOptionsMenu(menu);
    }
//
//    /**
//     * Respond to menu item selections
//     */
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

            case android.R.id.home : {
                try {
                    cerrar();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }
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

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_inventario:
                    mModel.read(mResultTextView, null, null);
                break;
        }
    }
}
