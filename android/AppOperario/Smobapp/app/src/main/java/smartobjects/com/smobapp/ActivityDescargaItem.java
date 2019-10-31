package smartobjects.com.smobapp;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import smartobjects.com.smobapp.connectivity.MainThread;
import smartobjects.com.smobapp.date.UtilsDate;
import smartobjects.com.smobapp.fragments.FragmentProgressBar;
import smartobjects.com.smobapp.fragments.FragmentWithoutInternet;
import smartobjects.com.smobapp.objects.ObjectItem;
import smartobjects.com.smobapp.objects.ObjectUbicacion;
import smartobjects.com.smobapp.objects.Test;
import smartobjects.com.smobapp.utils.BaseActivity;
import smartobjects.com.smobapp.utils.UtilsConstants;

/**
 * Actividad que se encarga de descargar la cantidad de items para hacer Auditoría.
 * Se recibe por el Bundle, la posición a la cual, se le va a hacer la auditoría.
 * El objecto Bundle, recibirá el objeto con la llave <<UtilsConstants.UBICACION.BUNDLE_UBICACION_FINAL>>.
 */
public class ActivityDescargaItem extends BaseActivity {
    //Botón que permite iniciar la auditoría
    @Bind(R.id.btn_descarga_item_iniciar) Button mBtnIniciar = null;
    //Frame Layout que coloca la información en pantalla.
    @Bind(R.id.fl_descargar_item_espera) FrameLayout mFlDescargar;
    //TextView que tendrá la información de la última fecha con actualización.
    @Bind(R.id.tv_descarga_item_actualizacion) TextView mTvFechaActualizacion;
    //TextView que mostrará la información del Local al cual se le hará la auditoría
    @Bind(R.id.tv_descarga_item_local) TextView mTvLocal;
    //Hilo que permite  la descarga de la información.
    private MainThread mHilo = null;
    //Objeto de la ubicación que debemos recibir por parámetros para saber a que local, se le va a hacer la Auditoría
    private ObjectUbicacion mUbicacionFinal = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descarga_item);
        //Hacemos el SetuP de la toolbar.
        setUpToolbar(R.id.toolbar_conciliar);
        //Permitimos el Injection View.
        ButterKnife.bind(ActivityDescargaItem.this);
        //Método que se encarga de mostrar/ocultar botones
        init();
    }

    /**
     * Método que se encarga de colocar la configuración de los botones.
     */
    private void init() {
        if (isNotNull(mBtnIniciar)){
            mBtnIniciar.setEnabled(false);
//            hideView(mBtnIniciar);
        }
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = getIntent().getExtras();
                ObjectUbicacion ubicacion = bundle.getParcelable(UtilsConstants.UBICACION.BUNDLE_UBICACION_FINAL);
                if (isNotNull(ubicacion)){
                    mUbicacionFinal = ubicacion;
                    if (isNotNull(mTvLocal)){
                        mTvLocal.setText("Local : " + mUbicacionFinal.getNombre());
                    }
                }
            }
        });
    }

    @OnClick(R.id.btn_descarga_item_iniciar)
    public void onClickIniciar(){
        if (ActivityDescargaItem.this != null){
            try {
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(UtilsConstants.ITEM.BUNDLE_ITEM, ObjectItem.getmListaItem());
                changeActivityForward(ActivityAuditoriaDos.class, bundle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Método que se encarga de hacer la gestion al presionar el botón de descarga.
     * Si no se encuentra con conexión a internet, se cambiará el FrameLayout para que el
     * usuario conozca la raiz del problema.
     */
    @OnClick(R.id.btn_descarga_item_descargar)
    public void onClickDescargar(){
        try {
            if (isInternetConnection()){
                changeFrameLayoutInfo();
                mHilo =  new MainThread(ActivityDescargaItem.this);
                mHilo.execute(UtilsConstants.ITEM.JSON_LIST_ITEM);
            } else {
                ActivityDescargaItem.this.getFragmentManager().beginTransaction()
                        .replace(R.id.fl_descargar_item_espera, new FragmentWithoutInternet().newInstance(null)).commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeFrameLayoutInfo() throws NullPointerException{
        ActivityDescargaItem.this.getFragmentManager().beginTransaction()
                .replace(R.id.fl_descargar_item_espera, new FragmentProgressBar().newInstance(null)).commit();
    }

    public void activateButton() throws NullPointerException {
        if (isNotNull(mBtnIniciar)){
            changeBackgroundButton(mBtnIniciar, R.drawable.estilo_boton_disponible);
        }
        if (isNotNull(mFlDescargar)){
            mFlDescargar.setVisibility(View.GONE);
        }

        UtilsDate fecha = UtilsDate.getInstance();
        if (isNotNull(fecha) && isNotNull(mTvFechaActualizacion)){
            mTvFechaActualizacion.setText("Última actualización : " + fecha.getDate());
        }

        if (isNotNull(mBtnIniciar)){
            mBtnIniciar.setEnabled(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isNotNull(mHilo)){
            mHilo.cancel(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ObjectItem> data = queryToRealm(realm, ObjectItem.class);
        if (isNotNull(data) && data.size() > 0){
            for (ObjectItem prueba : data){
                msgLogInfo("Edad : " + prueba.toString());
            }
        }

        Realm realm2 = Realm.getDefaultInstance();
        realm2.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                ObjectItem test = realm.where(ObjectItem.class).equalTo("EPC", "E28011606000020507906AE8").findFirst();
                if (isNotNull(test)){
                    test.setEstatus(ObjectItem.ESTADO_ENCONTRADO);
                }
            }
        });

        Realm realm3 = Realm.getDefaultInstance();
        RealmResults<ObjectItem> data2 = queryToRealm(realm, ObjectItem.class);
        if (isNotNull(data) && data.size() > 0){
            for (ObjectItem prueba : data){
                msgLogInfo("Edad : " + prueba.toString());
            }
        }
    }
}
