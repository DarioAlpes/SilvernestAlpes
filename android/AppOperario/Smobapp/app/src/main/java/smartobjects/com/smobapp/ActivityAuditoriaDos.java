package smartobjects.com.smobapp;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.commands.FactoryDefaultsCommand;
import com.uk.tsl.rfid.asciiprotocol.responders.LoggerResponder;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import smartobjects.com.smobapp.bluetoothTsl.ModelBase;
import smartobjects.com.smobapp.bluetoothTsl.ReadWriteModel;
import smartobjects.com.smobapp.bluetoothTsl.TSLBluetoothDeviceActivity;
import smartobjects.com.smobapp.bluetoothTsl.WeakHandler;
import smartobjects.com.smobapp.fragments.FragmentItemMaster;
import smartobjects.com.smobapp.fragments.FragmentProgressBar;
import smartobjects.com.smobapp.objects.ObjectItem;
import smartobjects.com.smobapp.objects.Test;
import smartobjects.com.smobapp.utils.UtilsConstants;
import smartobjects.com.smobapp.views.textView.TextProgressBar;

/**
 * Actividad que se encarga de realizar la Auditoría a unos Items que vienen por parámetro.
 * La actividad hereda de <<TSLBluetoothDeviceActivity>> que es la clase encargada de hacer
 * la conexión con el bluetooth para hacer la conexión con la pistola. Además, de implementar
 * las <<OnCheckListener>> de FragmentResumenAuditoria que nos devuelve un valor booleano para
 * saber si ha sido aceptado el mensaje de que la auditoría fue revisada por este usuario, y
 * <<OnHeadlineSelectedListener>> de AsciiCommander para saber cuando la pistola ha sidp
 * presionado el trigger, y así recibir los EPC leídos en esa búsqueda.
 * <p/>
 * Un parámetro importante que proviene del Bundle, es la lista de todos los items. La llave
 * para poder obtenerlo, es <<UtilsContants.ITEM.BUNDLE_ITEM>>.
 */
public class ActivityAuditoriaDos extends TSLBluetoothDeviceActivity
        implements FragmentResumenAuditoria.OnCheckListener, AsciiCommander.OnHeadlineSelectedListener {
    //Variable que nos permite saber si la persona tiene activado el check de conciliado al aprobar la auditoría
    public static boolean conciliado = false;
    //Clase modelo que nos permite hacer las gestiones con la pistola
    private ReadWriteModel mModel;
    //Item para reconectar la pistola
    private MenuItem mReconnectMenuItem;
    //Item para conectar la pistola
    private MenuItem mConnectMenuItem;
    //Item para desconectar la pistola
    private MenuItem mDisconnectMenuItem;
    //Item para reiniciar la pistola
    private MenuItem mResetMenuItem;
    //Textview para mostrar el resumen de la información
    @Bind(R.id.tv_resumen_dos)
    TextView mTvResumen;
    //TextView para mostrar la cantidad de items encontrados
    @Bind(R.id.tv_encontrado_esperado)
    TextView mTvTipoOpcion;
    //Variable para saber si se está mostrando el Fragment de items Esperados
    private boolean mostrandoEsperado = true;
    //Botón que permite buscar automáticamente la información de los items cercanos
    @Bind(R.id.tb_buscar_automatico)
    ToggleButton mTbBuscar;
    //Variable para saber si se puede mostrar el fragment de aprobar
    private boolean mAprobarDisponible = false;
    //Variable que dice si se ha aceptado la la auditoría realizada
    private boolean mResumenAprobado = true;
    //Imagen que nos permite seguir a la siguiente ventana
    @Bind(R.id.ib_auditoria_siguiente)
    ImageView ivSiguiente;
    //Botón que nos permite conciliar
    @Bind(R.id.btn_conciliar)
    Button mBtnConciliar;
    //Botón que nos permite hacer inventario
    @Bind(R.id.btn_inventario)
    Button mBtnInventario;
    //Botón que nos va a permitir el proceso de aprobar
    @Bind(R.id.btn_aprobar)
    Button mBtnAprobar;
    //ProgressBar que nos permite ver el proceso actua de los items
    @Bind(R.id.pb)
    TextProgressBar pb;
    //Lista que se recibe a través del Bundle que contiene todos los items a buscar en la auditoría
    ArrayList<ObjectItem> mListaItemAuditoria = null;
    private boolean mActivatedThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auditoria_dos);
        //Colocamos un icono de espera para que el usuario sepa que está cargando.
        frameWaiting();
        //Permitimos al ButterKnife a hacer las inyecciones de las Views.
        ButterKnife.bind(this);
        try {
            //Inicializamos la acción para poder encontrar el bluetooth
            bluetoothInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mActivatedThread = true;
        //Método que busca la información enviada por el Bundle.
        getBundleInfo();
//        mTvResumen = (TextView) findViewById(R.id.tv_resumen_dos);
//        mTvTipoOpcion = (TextView) findViewById(R.id.tv_encontrado_esperado);
//        mTbBuscar = (ToggleButton) findViewById(R.id.tb_buscar_automatico);

    }

    /**
     * Método que se encarga de recibir la información del Bundle
     */
    private void getBundleInfo() {
        Bundle bundle = getIntent().getExtras();
        mListaItemAuditoria = bundle.getParcelableArrayList(UtilsConstants.ITEM.BUNDLE_ITEM);
        //Si la informaicón de la lista contiene datos, entonces, mostramos la información.
        if (mListaItemAuditoria.size() > 0)
            new Thread(myThread).start();
    }

    /**
     * Devolvemos la lista de los items que vamos a usar.
     */
    private synchronized ArrayList<ObjectItem> getListaItems() throws NullPointerException, Exception {
        return mListaItemAuditoria;
    }

    /**
     * Método que se encarga de devolver la cantidad de items que ya han sido encontrados.
     */
    public synchronized ArrayList<ObjectItem> getListaItemEncontrados() throws Exception {
        //Creamos una nueva lista
        ArrayList<ObjectItem> itemEncontrados = new ArrayList<>();
        //Recorremos la lista actual donde se tiene la información
        for (ObjectItem item : getListaItems()) {
            //Si el estatus de la lista actual, su estado es encotnrado, entonces, lo agregamos a la lista.
            if (item.getEstatus() == ObjectItem.ESTADO_ENCONTRADO) {
                itemEncontrados.add(item);
            }
        }
        //Retornamos la lista.
        return itemEncontrados;
    }

    //Método que se encarga de devolver al cantidad de items encontrados.
    public synchronized int getTamanioItemsEncontrados() throws Exception {
        return getListaItemEncontrados().size();
    }

    public synchronized ArrayList<ObjectItem> getmListaItemEsperado() {
        ArrayList<ObjectItem> itemEncontrados = new ArrayList<>();

        try {
            for (ObjectItem item : getListaItems()) {
                if (item.getEstatus() == ObjectItem.ESTADO_ESPERANDO) {
                    itemEncontrados.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemEncontrados;
    }

    private Runnable myThread = new Runnable() {
        @Override
        public void run() {
            try {
                //Cambiamos el tamaño total a buscar
                pb.setMax(getListaItems().size());
                //Si la lista es inferior a la cantidad total, se ejecuta un hilo hasta que termine de encontrar los items.
                while ((mActivatedThread) && (getTamanioItemsEncontrados() < getListaItems().size())) {
                    try {
                        //Actualizamos el proceso, dependiendo de la información
                        pb.setProgress(getTamanioItemsEncontrados());
                        Thread.sleep(1000);
                    } catch (Throwable t) {
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Método que se encarga
     *
     * @throws Exception
     */
    private void bluetoothInit() throws Exception {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //
                // An AsciiCommander has been created by the base class
                //
                AsciiCommander commander = getCommander();

                // Add the LoggerResponder - this simply echoes all lines received from the reader to the log
                // and passes the line onto the next responder
                // This is added first so that no other responder can consume received lines before they are logged.
                commander.addResponder(new LoggerResponder());
//
//        // Add a synchronous responder to handle synchronous commands
                commander.addSynchronousResponder();
//
//        //Agregado prueba
                mModel = new ReadWriteModel();
                mModel.setCommander(getCommander());
                mModel.setHandler(mGenericModelHandler);
            }
        }, 1000);
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            findAutomatic();
            realm();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.tb_buscar_automatico)
    public void onClickToggleButtonAutomatico() {
        findAutomatic();
    }

    private synchronized void findAutomatic() {
        if (null != mTbBuscar) {
            if (mTbBuscar.isChecked()) {
                mTbBuscar.setBackground(ContextCompat.getDrawable(ActivityAuditoriaDos.this, R.drawable.press_button));
                try {
                    mModel.read(ActivityAuditoriaDos.this, getListaItems());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mostrandoEsperado) {
                    changeForInventario();
                } else {
                    changeForConciliar();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            findAutomatic();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 500);
            } else {
                mTbBuscar.setBackground(ContextCompat.getDrawable(ActivityAuditoriaDos.this, R.drawable.unpress_button));
            }
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();

        // Register to receive notifications from the AsciiCommander
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        if (isNotNull(mModel)) {
            mModel.clearListItemsFounds();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Register to receive notifications from the AsciiCommander
                LocalBroadcastManager.getInstance(ActivityAuditoriaDos.this).registerReceiver(mMessageReceiver,
                        new IntentFilter(AsciiCommander.STATE_CHANGED_NOTIFICATION));

                displayReaderState();
                UpdateUI();

                try {
                    updateResumen();
                    changeTextType(mTvTipoOpcion, "Inventario");
                    Bundle bundle = new Bundle();
                    bundle.putInt("tipo", ObjectItem.ESTADO_ESPERANDO);
                    changeFragment(FragmentItemMaster.newInstance(getBundleListaItem()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1500);
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

    private void displayReaderState() {
        String connectionMsg = "Lector: " + (getCommander().isConnected() ? getCommander().getConnectedDeviceName() : "Desconectado");
        setTitle(connectionMsg);
    }

    private void UpdateUI() {
        boolean isConnected = getCommander().isConnected();
        boolean canIssueCommand = isConnected & !mModel.isBusy();
//        mReadActionButton.setEnabled(canIssueCommand);
        // Only enable the write button when there is at least a partial EPC
//        mWriteActionButton.setEnabled(canIssueCommand && mTargetTagEditText.getText().length() != 0);
    }

    private final WeakHandler<ActivityAuditoriaDos> mGenericModelHandler = new WeakHandler<ActivityAuditoriaDos>(this) {

        @Override
        public void handleMessage(Message msg, ActivityAuditoriaDos thisActivity) {
            try {
                switch (msg.what) {
                    case ModelBase.BUSY_STATE_CHANGED_NOTIFICATION:
                        if (mModel.error() != null) {
//                            mResultTextView.append("\n Task failed:\n" + mModel.error().getMessage() + "\n\n");
//                            mResultScrollView.post(new Runnable() { public void run() { mResultScrollView.fullScroll(View.FOCUS_DOWN); } });

                        }
                        UpdateUI();
                        break;

                    case ModelBase.MESSAGE_NOTIFICATION:
                        String message = (String) msg.obj;
//                        mResultTextView.append(message);
//                        mResultScrollView.post(new Runnable() { public void run() { mResultScrollView.fullScroll(View.FOCUS_DOWN); } });
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
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
        mDisconnectMenuItem = menu.findItem(R.id.disconnect_reader_menu_item);
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

    private void resetReader() {
        try {
            // Reset the reader
            FactoryDefaultsCommand fdCommand = FactoryDefaultsCommand.synchronousCommand();
            getCommander().executeCommand(fdCommand);
            String msg = "Reset " + (fdCommand.isSuccessful() ? "¡Exitoso!" : "¡Fallido!");
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

            UpdateUI();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void updateResumen() throws Exception {
        if (isNotNull(mTvResumen)) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
//                        int encontrados = ObjectItem.getTamanioItemsEncontrados();
                        int encontrados = getTamanioItemsEncontrados();
//                        int tamanioTotal = ObjectItem.getmListaItem().size();
                        int tamanioTotal = getListaItems().size();
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

    /**
     * Método que permite envía la señal para colocar un Fragment de espera para que el usuario conozca
     * que se está cargando la información
     */
    private void frameWaiting() {
        changeFragment(FragmentProgressBar.newInstance(null));
    }

    @OnClick(R.id.btn_conciliar)
    public void onClickConciliar() {
        mostrandoEsperado = false;
        changeTextType(mTvTipoOpcion, "Conciliar");
        changeForConciliar();
        mTbBuscar.setChecked(false);
        mTbBuscar.setVisibility(View.INVISIBLE);
        ivSiguiente.setVisibility(View.INVISIBLE);
//        ivSiguiente.setVisibility(View.VISIBLE);
        changeBackgroundButton(mBtnConciliar, R.drawable.estilo_boton_activo);
        changeBackgroundButton(mBtnInventario, R.drawable.estilo_boton_disponible);
        changeBackgroundButton(mBtnAprobar, R.drawable.estilo_boton_disponible);
        if (!mAprobarDisponible) mAprobarDisponible = true;
    }

    @OnClick(R.id.btn_inventario)
    public void onClickInventario() {
        mostrandoEsperado = true;
        changeTextType(mTvTipoOpcion, "Inventario");
        changeForInventario();
        mTbBuscar.setVisibility(View.VISIBLE);
        ivSiguiente.setVisibility(View.INVISIBLE);
        changeBackgroundButton(mBtnConciliar, R.drawable.estilo_boton_disponible);
        changeBackgroundButton(mBtnInventario, R.drawable.estilo_boton_activo);
        if (mAprobarDisponible) {
            changeBackgroundButton(mBtnAprobar, R.drawable.estilo_boton_disponible);
        }
    }

    @OnClick(R.id.btn_aprobar)
    public void onClickAprobar() {
        if (mAprobarDisponible) {
            changeBackgroundButton(mBtnConciliar, R.drawable.estilo_boton_disponible);
            changeBackgroundButton(mBtnInventario, R.drawable.estilo_boton_disponible);
            changeBackgroundButton(mBtnAprobar, R.drawable.estilo_boton_activo);
            mostrandoEsperado = true;
            mTbBuscar.setVisibility(View.INVISIBLE);
            ivSiguiente.setVisibility(View.VISIBLE);
            try {
                changeFragment(FragmentResumenAuditoria.newInstance(getBundleListaItem()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            changeTextType(mTvTipoOpcion, "Aprobación");
        } else {
            try {
                showAlertMessage("Aprobar", "Se deben hacer todos los pasos en orden (Conciliar)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Bundle que se encarga de devolver la información de la lista para realizar la auditoría
     *
     * @return
     * @throws Exception
     */
    public Bundle getBundleListaItem() throws Exception {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(UtilsConstants.ITEM.BUNDLE_ITEM, getListaItems());
        return bundle;
    }

    private synchronized void changeForInventario() {
        Bundle bundle = new Bundle();
        bundle.putInt("tipo", ObjectItem.ESTADO_ESPERANDO);
//        changeFragment(FragmentItemEsperado.newInstance(bundle));
        try {
            changeFragment(FragmentItemMaster.newInstance(getBundleListaItem()));
        } catch (Exception e) {
            e.printStackTrace();
        }
//        textview.setBackground(ContextCompat.getDrawable(mContext, R.drawable.estilo_edit_text));
    }

    private synchronized void changeForConciliar() {
//        Handler handlerDos = new Handler();
//        handlerDos.postDelayed(new Runnable() {
//            @Override
//            public void run() {
        try {
            Bundle bundle = new Bundle();
            bundle.putInt("tipo", ObjectItem.ESTADO_ENCONTRADO);
            bundle.putParcelableArrayList(UtilsConstants.ITEM.BUNDLE_ITEM, getmListaItemEsperado());
            try {
                changeFragment(FragmentConciliar.newInstance(bundle));
            } catch (Exception e) {
                e.printStackTrace();
            }
            AsciiCommander commander = getCommander();
            if (isNotNull(commander) && commander.isConnected()) {
                commander.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//            }
//        }, 2000);
    }

    private synchronized void changeFragment(final Fragment fragment) {
//        ActivityAuditoriaDos.this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//        Handler handler =  new Handler();
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
        try {
            ActivityAuditoriaDos.this.getFragmentManager().beginTransaction()
                    .replace(R.id.fl_auditoria_dos, fragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
//            }
//        });
//            }
//        });
    }

    private void changeTextType(final TextView view, final String info) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (isNotNull(view) && info.length() > 0) {
                    view.setText(info);
                }
            }
        });
    }

    /**
     * Método que se encarga de realizar la acción del botón Siguiente
     *
     * @param v : Vista a la cual se le asigna la acción.
     */
    @OnClick(R.id.ib_auditoria_siguiente)
    public void clickSeguiInventario(View v) {
        try {
            //Debemos preguntar si el usuario ha aceptado los datos.
            if (mResumenAprobado) {
                //Validamos la conexión a internet.
                if (!isInternetConnection()) {
                    showAlertMessageWithCallback("Auditoría", "En el momento, no cuenta con internet, pero la aplicación a través de un servicio, enviará la información una vez que se tenga conexión a internet.", R.mipmap.ic_launcher);
                } else {
                    showAlertMessageWithCallback("Auditoría", "La auditoría se ha enviado correctamente.", R.mipmap.ic_launcher);
                }
            } else {
                showAlertMessage("Aprobar", "Para continuar, debe aceptar que la información escrita, es correcta.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método que se encarga de tener el estado para saber si el usuario ha aceptado la auditoría
     *
     * @param state : Estado actual del CheckBox
     */
    @Override
    public void changeAprobar(boolean state) {
        mResumenAprobado = state;
    }

    @Override
    public synchronized void onArticleSelected() {
        try {
//            AsciiCommander commander = getCommander();
//            if (commander != null) {
//                if (commander.isConnected() && commander.getConnectionState().equals(AsciiCommander.ConnectionState.CONNECTED)) {
                    if (mostrandoEsperado) {
//                        if(!mModel.isBusy()){
                            try{
                                mModel.read(ActivityAuditoriaDos.this, getListaItems());
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                            changeForInventario();
//                        }
                    } else {
//                    changeForConciliar();
//                    }
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mActivatedThread = false;
    }

    private void realm() {
        super.onStart();
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ObjectItem> data = queryToRealm(realm, ObjectItem.class);
        if (isNotNull(data) && data.size() > 0){
            for (ObjectItem prueba : data){
                msgLogInfo("Edad : " + prueba.toString());
            }
        }
        closeRealmConnection(realm);
    }


}
