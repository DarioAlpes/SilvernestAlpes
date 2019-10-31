package smartobjects.com.smobapp;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import smartobjects.com.smobapp.adapters.AdapterMenu;
import smartobjects.com.smobapp.objects.ObjectMenu;
import smartobjects.com.smobapp.objects.ObjectUbicacion;
import smartobjects.com.smobapp.sharedPreferences.UtilsSharedPreferences;
import smartobjects.com.smobapp.utils.BaseActivity;

public class ActivityModulos extends BaseActivity
        implements AdapterView.OnItemClickListener, View.OnClickListener{

    private ObjectMenu itemDetallado;
    public static final String EXTRA_PARAM_ID = "com.herprogramacion.coches2015.extra.ID";
    public static final String VIEW_NAME_HEADER_IMAGE = "imagen_compartida";
    private GridView gridView;
    private AdapterMenu adaptador;
    //TextView que mostrará la usuario una señal en caso que se haya empezado una auditoría
    private TextView mTvAlertaAuditoria;

    @Override
    protected void onStart() {
        super.onStart();
        ActivityAuditoria.conciliado = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modulos);
//        ItemFragment mFragment = new ItemFragment();
//        getFragmentManager().beginTransaction()
//                .replace(R.id.fl_activity_modulos, mFragment).commit();

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

//        usarToolbar();

        // Obtener el coche con el identificador establecido en la actividad principal
        itemDetallado = ObjectMenu.getItem(getIntent().getIntExtra(EXTRA_PARAM_ID, 0));
        gridView = (GridView) findViewById(R.id.grid);
        adaptador = new AdapterMenu(this);
        gridView.setAdapter(adaptador);
        gridView.setOnItemClickListener(this);

        mTvAlertaAuditoria = (TextView) findViewById(R.id.tv_alert_auditoria);
        mTvAlertaAuditoria.setOnClickListener(this);

        showView(mTvAlertaAuditoria);
    }

//    private void usarToolbar() {
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//    }

//    @Override
    public void onFragmentInteraction(String id) {
        switch (id){
            case "1":
                try {
                    changeActivityForward(ActivityDevice.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case "2":
                try {
                    changeActivityForward(ActivityUbicacion.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case "3" :
                try {
                    changeActivityForward(ReadWriteActivity.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            try {
                cerrar();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        try {
            showSnackbarAlert("Ha iniciado sesión como " + getSessionName());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ObjectMenu item = (ObjectMenu) parent.getItemAtPosition(position);
        onFragmentInteraction(String.valueOf(position + 1));


//        Intent intent = new Intent(this, ActividadDetalle.class);
//        intent.putExtra(ActividadDetalle.EXTRA_PARAM_ID, item.getId());

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//
//            ActivityOptionsCompat activityOptions =
//                    ActivityOptionsCompat.makeSceneTransitionAnimation(
//                            this,
//                            new Pair<View, String>(view.findViewById(R.id.imagen_coche),
//                                    ActividadDetalle.VIEW_NAME_HEADER_IMAGE)
//                    );
//
//            ActivityCompat.startActivity(this, intent, activityOptions.toBundle());
//        } else
//            startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_alert_auditoria){
            try {
                UtilsSharedPreferences preferences = UtilsSharedPreferences.newInstance(this);
                boolean valor = preferences.getPreference("data", true);
                if (valor){
                    showAlertMessage("Data", "Existe auditoría");
                } else {
                    showAlertMessage("Data", "No Existe auditoría");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
