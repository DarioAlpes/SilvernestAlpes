package smartobjects.com.smobapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.commands.FactoryDefaultsCommand;
import com.uk.tsl.rfid.asciiprotocol.parameters.AntennaParameters;
import com.uk.tsl.rfid.asciiprotocol.responders.LoggerResponder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import smartobjects.com.smobapp.adapters.AdapterEPCItem;
import smartobjects.com.smobapp.adapters.AdapterSKUItemDetail;
import smartobjects.com.smobapp.adapters.AdapterSkuItem;
import smartobjects.com.smobapp.bluetoothTsl.ModelBase;
import smartobjects.com.smobapp.bluetoothTsl.ReadWriteModel;
import smartobjects.com.smobapp.bluetoothTsl.TSLBluetoothDeviceActivity;
import smartobjects.com.smobapp.bluetoothTsl.WeakHandler;
import smartobjects.com.smobapp.customViews.CustomDialogSku;
import smartobjects.com.smobapp.objects.ObjectItem;
import smartobjects.com.smobapp.utils.BaseActivity;

public class ActivityAuditoria extends TSLBluetoothDeviceActivity {

    public static boolean conciliado = false;

    private ReadWriteModel mModel;
    private MenuItem mReconnectMenuItem;
    private MenuItem mConnectMenuItem;
    private MenuItem mDisconnectMenuItem;
    private MenuItem mResetMenuItem;
    private TextView mTvResumen = null;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auditoria);

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setLogo(R.mipmap.ic_launcher);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTvResumen = (TextView) findViewById(R.id.tv_resumen);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        if (tab.getPosition() == 1) {
                            getFragmentItemsDetails().updateInterface();
                        } else if (tab.getPosition() == 0) {
                            getFragmentItemsMaster().updateInterface();
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                        if (tab.getPosition() == 1) {
                            getFragmentItemsDetails().updateInterface();
                        } else if (tab.getPosition() == 0) {
                            getFragmentItemsMaster().updateInterface();
                        }
                    }
                }
        );

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
//                    if (conciliado){
//                        changeActivityForward(FragmentResumenAuditoria.class);
//                    } else {
//                        changeActivityForward(FragmentConciliar.class);
//                    }
                    mModel.read(null, getFragmentItemsMaster(), getFragmentItemsDetails());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            updateResumen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FragmentItemsMaster getFragmentItemsMaster() {
        return FragmentItemsMaster.newInstance();
    }

    public FragmentItemsDetails getFragmentItemsDetails() {
        return FragmentItemsDetails.newInstance();
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a FragmentItemsMaster (defined as a static inner class below).
            Fragment fragment = null;
            if (position == 0){
//                fragment = FragmentItemsMaster.newInstance(position + 1);
//                fragment = getFragmentItemsMaster();
                fragment = FragmentItemsDetails.newInstance();

            } else {
//                fragment = FragmentItemsDetails.newInstance(position + 1);
                fragment = FragmentItemsDetails.newInstance();
            }
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SKU";
                case 1:
                    return "EPC";
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class FragmentItemsMaster extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private RecyclerView recycler;
        private RecyclerView.LayoutManager lManager;
        private RecyclerView.Adapter adapter;

        List<ObjectItem> itemsBySku = new ArrayList<>();
        List<ObjectItem> listaLimpia = new ArrayList<>();

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
//        public static FragmentItemsMaster newInstance(int sectionNumber) {
        public static FragmentItemsMaster newInstance() {
            FragmentItemsMaster fragment = new FragmentItemsMaster();
            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public FragmentItemsMaster() {
        }

        @Override
        public void onResume() {
            super.onResume();
            if (null != adapter){
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_adapter_item_sku, container, false);

            // Obtener el Recycler
            recycler = (RecyclerView) rootView.findViewById(R.id.rc_adapter_sku);
            recycler.setHasFixedSize(true);

            // Usar un administrador para LinearLayout
            lManager = new LinearLayoutManager(getActivity());
            recycler.setLayoutManager(lManager);

            // Crear un nuevo adaptador
            adapter = new AdapterSkuItem(getContext(), getItemsBySku());
            recycler.setAdapter(adapter);
            recycler.addOnItemTouchListener(
                    new AdapterSKUItemDetail(getActivity(), new AdapterSKUItemDetail.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            ObjectItem item = (ObjectItem) ObjectItem.getmListaItem().get(position);
                            CustomDialogSku skuDialog = new CustomDialogSku(getActivity(), item);
                            skuDialog.show();
                        }
                    })
            );

            return rootView;
        }

        private List<ObjectItem> getItemsBySku() {
            List<ObjectItem> item = ObjectItem.getmListaItem();
            listaLimpia.clear();

            //Forma número 1 (Uso de Maps).
            Map<String, ObjectItem> mapPersonas = new HashMap<String, ObjectItem>(ObjectItem.getmListaItem().size());

            //Aquí está la magia
            for(ObjectItem p : item) {
                mapPersonas.put(p.getSKU(), p);
            }

            for(Map.Entry<String, ObjectItem> p : mapPersonas.entrySet()) {
                listaLimpia.add(p.getValue());
            }

            return listaLimpia;
        }

        public void updateInterface(){
            //Validamos que el adapter ya exista.
            if (null != adapter){
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        List<ObjectItem> lista = getItemsBySku();
                        adapter.notifyDataSetChanged();
//                        recycler.setAdapter(null);
//                        adapter = new AdapterSkuItem(getContext(), getItemsBySku());
//                        recycler.setAdapter(adapter);
                    }
                });
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class FragmentItemsDetails extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private RecyclerView recycler;
        private RecyclerView.LayoutManager lManager;
        public static RecyclerView.Adapter adapter;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
//        public static FragmentItemsDetails newInstance(int sectionNumber) {
        public static FragmentItemsDetails newInstance() {
            FragmentItemsDetails fragment = new FragmentItemsDetails();
            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public FragmentItemsDetails() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_adapter_item_epc, container, false);

// Obtener el Recycler
            recycler = (RecyclerView) rootView.findViewById(R.id.rc_adapter_epc);
            recycler.setHasFixedSize(true);

// Usar un administrador para LinearLayout
            lManager = new LinearLayoutManager(getActivity());
            recycler.setLayoutManager(lManager);

// Crear un nuevo adaptador
            adapter = new AdapterEPCItem(ObjectItem.getmListaItem());
            recycler.setAdapter(adapter);
            return rootView;
        }

        /**
         * Permite actualizar los componentes de la interfaz
         */
        public void updateInterface(){
            //Validamos que el adapter ya exista.
            if (null != adapter){
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
//                        adapter.notifyDataSetChanged();
//                        ArrayList<ObjectItem> data = new ArrayList<>();
//                        data.add(new ObjectItem("2097935529", "Jean",     "30",  9, 0,  "https://s3-eu-west-1.amazonaws.com/iouproyect/photos/items/a1I20000000cYb9EAEphoto.jpg",              1, "E2801160600002050790B859"));
//                        adapter = new AdapterEPCItem(data);
//                        adapter = new AdapterEPCItem(ObjectItem.getmListaItem());
//                        recycler.setAdapter(adapter);

                    }
                });
            }

        }

        @Override
        public void onResume() {
            super.onResume();
//            Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    ArrayList<ObjectItem> data = new ArrayList<>();
//                    data.add(new ObjectItem("2097935529", "Jean",     "30",  9, 0,  "https://s3-eu-west-1.amazonaws.com/iouproyect/photos/items/a1I20000000cYb9EAEphoto.jpg",              1, "E2801160600002050790B859"));
//                    adapter = new AdapterEPCItem(data);
//                    recycler.setAdapter(adapter);
//                }
//            }, 2000);da
        }
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

    private void displayReaderState()
    {
        String connectionMsg = "Reader: " + (getCommander().isConnected() ? getCommander().getConnectedDeviceName() : "Disconnected");
        setTitle(connectionMsg);
    }

    private void UpdateUI()
    {
        boolean isConnected = getCommander().isConnected();
        boolean canIssueCommand = isConnected & !mModel.isBusy();
//        mReadActionButton.setEnabled(canIssueCommand);
        // Only enable the write button when there is at least a partial EPC
//        mWriteActionButton.setEnabled(canIssueCommand && mTargetTagEditText.getText().length() != 0);
    }

    private final WeakHandler<ActivityAuditoria> mGenericModelHandler = new WeakHandler<ActivityAuditoria>(this) {

        @Override
        public void handleMessage(Message msg, ActivityAuditoria thisActivity) {
            try {
                switch (msg.what) {
                    case ModelBase.BUSY_STATE_CHANGED_NOTIFICATION:
                        if( mModel.error() != null ) {
//                            mResultTextView.append("\n Task failed:\n" + mModel.error().getMessage() + "\n\n");
//                            mResultScrollView.post(new Runnable() { public void run() { mResultScrollView.fullScroll(View.FOCUS_DOWN); } });

                        }
                        UpdateUI();
                        break;

                    case ModelBase.MESSAGE_NOTIFICATION:
                        String message = (String)msg.obj;
//                        mResultTextView.append(message);
//                        mResultScrollView.post(new Runnable() { public void run() { mResultScrollView.fullScroll(View.FOCUS_DOWN); } });
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
            }

        }
    };

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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        boolean isConnected = getCommander().isConnected();
        mResetMenuItem.setEnabled(isConnected);
        mDisconnectMenuItem.setEnabled(isConnected);

        mReconnectMenuItem.setEnabled(!isConnected);
        mConnectMenuItem.setEnabled(!isConnected);

        return super.onPrepareOptionsMenu(menu);
    }

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

    public synchronized void updateResumen() throws Exception {
        if (isNotNull(mTvResumen)){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        int encontrados = ObjectItem.getTamanioItemsEncontrados();
                        int tamanioTotal = ObjectItem.getmListaItem().size();
                        StringBuilder resumen = new StringBuilder()
                                .append(String.valueOf(encontrados))
                                .append("/")
                                .append(String.valueOf(tamanioTotal));
                        mTvResumen.setText(resumen);
                        updateResumen();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 1000);
        }
    }
}
