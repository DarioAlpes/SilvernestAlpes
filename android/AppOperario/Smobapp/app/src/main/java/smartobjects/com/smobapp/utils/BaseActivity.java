package smartobjects.com.smobapp.utils;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Slide;
import android.transition.Transition;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import smartobjects.com.smobapp.ActivityIniciarSesion;
import smartobjects.com.smobapp.ActivityModulos;
import smartobjects.com.smobapp.R;
import smartobjects.com.smobapp.objects.ObjectItem;
import smartobjects.com.smobapp.objects.Test;
import smartobjects.com.smobapp.sharedPreferences.UtilsSharedPreferences;

/**
 * Created by Andres Rubiano on 07/10/2015.
 */
public class BaseActivity extends AppCompatActivity {

    protected Class mClassBackward = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            ocultarVentana();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);
        try {
            //Siempre validamos que la persona haya iniciado sesión
            //if (!isSignIn()) {
            //Si no se ha iniciado sesión, lo enviará a la venta para hacerlo.
            //showAlertMessage(UtilsConstants.GENERAL.LOG_ERROR, "Por favor, inicie sesión.");
            //changeActivityBackward(ActivityIniciarSesion.class);
            //}
        } catch (Exception e) {
            msgLogError("Error al cambiar la orientación del dispositivo.");
            e.printStackTrace();
        }
    }

    //Método que se encaga de colocar la orientación del dispositivo
    private void setOrientation() throws Exception {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            try {
                doBackLifeCycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    // Esconde la barra de las notificaciones
    protected synchronized void hideWindowContainer() throws Exception {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void msgLogError(String error) {
        Log.e(UtilsConstants.GENERAL.LOG_ERROR, error);
    }

    protected void msgLogInfo(String info) {
        Log.i(UtilsConstants.GENERAL.LOG_INFO, info);
    }

    protected void hideView(View v) {
        v.setVisibility(View.GONE);
    }

    protected void showView(View v) {
        v.setVisibility(View.VISIBLE);
    }

    protected boolean isInternetConnection() throws Exception {
        ConnectivityManager cm = (ConnectivityManager) BaseActivity.this.getSystemService(
                Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork != null && wifiNetwork.isConnected()) {
            return true;
        }

        NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileNetwork != null && mobileNetwork.isConnected()) {
            return true;
        }

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        }

        return false;
    }

    public synchronized void showAlertMessage(String titulo, String mensaje) throws Exception {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BaseActivity.this);
        alertDialog.setTitle(titulo);
        alertDialog.setMessage(mensaje);
        alertDialog.setIcon(R.mipmap.ic_launcher);

        alertDialog.setPositiveButton(R.string.st_general_aceptar,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialog.show();
    }

    protected void showAlertMessageTwoOptions(String title, String mensaje) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BaseActivity.this);
        alertDialog.setTitle(title);
        alertDialog.setMessage(mensaje);
        alertDialog.setIcon(R.mipmap.ic_launcher);

        alertDialog.setPositiveButton(R.string.st_general_aceptar,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        alertDialog.setNegativeButton(R.string.st_general_cancelar, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    protected void showAlertMessageWithCallback(String title, String message, int icon) throws Exception {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BaseActivity.this);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        if (isNotNull(icon)) {
            alertDialog.setIcon(icon);
        }

        alertDialog.setPositiveButton("Aceptar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
//                            changeActivityBackward(ActivityModulos.class);
                            changeActivityForward(ActivityModulos.class);
//                            finalizar();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

//        alertDialog.setNegativeButton("Cancelar", null);

        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    protected void showAlertMessageWithCallback(String title, String message, DialogInterface.OnClickListener listener) throws Exception {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BaseActivity.this);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setIcon(R.mipmap.ic_launcher);

        alertDialog.setPositiveButton("Aceptar", listener);

//        alertDialog.setNegativeButton("Cancelar", null);

        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    protected boolean isSignIn() throws Exception {
        final GlobalClass globalVariable = (GlobalClass) BaseActivity.this.getApplicationContext();
//        String usuario = globalVariable.usuario;
//        if (!usuario.equals("")) {
//            return true;
//        }
        return false;
    }

    protected void startSession(String usuario) throws Exception {
        final GlobalClass globalVariable = (GlobalClass) BaseActivity.this.getApplicationContext();
//              globalVariable.usuario = usuario;
    }

    protected void closeSession() throws Exception {
        final GlobalClass globalVariable = (GlobalClass) BaseActivity.this.getApplicationContext();
        globalVariable.usuario = "";
        globalVariable.token = "";
    }

    protected String getSessionName() throws Exception {
        final GlobalClass globalVariable = (GlobalClass) BaseActivity.this.getApplicationContext();
        return globalVariable.usuario;
    }

    protected boolean isFieldEmpty(EditText editText) {
        return ((editText == null)) || ((editText.getText().toString().length() == 0));
    }

    protected void changeActivityForward(Class classToGo) throws Exception {
        changeActivityForward(classToGo, new HashMap<String, String>());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void changeActivityForward(Class classToGo, HashMap<String, String> map) throws Exception {
        if (classToGo != null) {
            Intent mIntent = new Intent(BaseActivity.this, classToGo);
            if (map != null) {
                for (Map.Entry entry : map.entrySet()) {
                    String key = (String) entry.getKey();
                    String value = (String) entry.getValue();
                    mIntent.putExtra(key, value);
                }
            }
            Transition exitTrans = new Explode();
            getWindow().setExitTransition(exitTrans);

            Transition reenterTrans = new Slide();
            getWindow().setReenterTransition(reenterTrans);

//            Transition enterTrans = new Explode();
//            getWindow().setEnterTransition(enterTrans);
//
//            Transition returnTrans = new Slide();
//            getWindow().setReturnTransition(returnTrans);

//            ActivityOptions opts = ActivityOptions.makeCustomAnimation(
//                    BaseActivity.this, R.anim.entrada_derecha, R.anim.salida_izquierda);
            ActivityOptions opts = ActivityOptions.makeSceneTransitionAnimation(BaseActivity.this);
            BaseActivity.this.startActivity(mIntent, opts.toBundle());
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void changeActivityBackward(Class classToGo) throws Exception {
        Intent mIntent = new Intent(BaseActivity.this, classToGo)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Transition enterTrans = new Explode();
        getWindow().setEnterTransition(enterTrans);

        Transition returnTrans = new Slide();
        getWindow().setReturnTransition(returnTrans);

//        ActivityOptions opts = ActivityOptions.makeSceneTransitionAnimation(BaseActivity.this);
//        BaseActivity.this.startActivity(mIntent, opts.toBundle());
//        BaseActivity.this.startActivity(mIntent);
        BaseActivity.this.finish();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void changeActivityBackward() throws Exception {
        Transition enterTrans = new Explode();
        getWindow().setEnterTransition(enterTrans);

        Transition returnTrans = new Slide();
        getWindow().setReturnTransition(returnTrans);

//        ActivityOptions opts = ActivityOptions.makeSceneTransitionAnimation(BaseActivity.this);
//        BaseActivity.this.startActivity(mIntent, opts.toBundle());
//        BaseActivity.this.startActivity(mIntent);
        BaseActivity.this.finish();
    }

    protected void finalizar() {
        this.finish();
    }

    public void cambiarFragment(Fragment mFragment, int layout) throws Exception {
        if (isNotNull(mFragment) && isNotNull(layout)) {
            getFragmentManager().beginTransaction().replace(layout, mFragment).commit();
        }
    }

    //Método que se encarga de ir a la última página ingresada en caso de error.
    protected void goLastPage(int page) {
        switch (page) {
            case UtilsConstants.LAST_SCREEN.ATENDER_HOME:
                try {
//                    changeActivityForward(ActivityAtenderProfile.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    protected synchronized void hideKeyboard(EditText mEditText) {
        InputMethodManager imm = (InputMethodManager) BaseActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    // Mostrar el teclado
    protected void showKeyboard(EditText mEditText) {
        InputMethodManager imm = (InputMethodManager) BaseActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInputFromInputMethod(mEditText.getWindowToken(), 0);
    }

    protected void cerrar() throws Exception {
        showAlertMessageWithCallback(getString(R.string.close_session_title),
                getSessionName() + ", " +
                        getString(R.string.close_session_message),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            changeActivityForward(ActivityIniciarSesion.class);
                            finalizar();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    protected void cerrarSesionJugador() throws Exception {
        showAlertMessageWithCallback(getString(R.string.close_session_title),
                getSessionName() + ", " +
                        getString(R.string.close_session_message),
                R.mipmap.ic_launcher);
    }

    protected synchronized void hideActionBar() throws Exception {
        ActionBar mActionBar = getActionBar();
        if (mActionBar != null)
            mActionBar.hide();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void changeActivityForward(Class classToGo, Bundle bundle) throws Exception {
        if (classToGo != null) {
            Intent mIntent = new Intent(BaseActivity.this, classToGo);
            mIntent.putExtras(bundle);

            Transition exitTrans = new Explode();
            getWindow().setExitTransition(exitTrans);

            Transition reenterTrans = new Slide();
            getWindow().setReenterTransition(reenterTrans);

//            Transition enterTrans = new Explode();
//            getWindow().setEnterTransition(enterTrans);
//
//            Transition returnTrans = new Slide();
//            getWindow().setReturnTransition(returnTrans);

            ActivityOptions opts = ActivityOptions.makeSceneTransitionAnimation(BaseActivity.this);
//            ActivityOptions opts = ActivityOptions.makeCustomAnimation(
//                    BaseActivity.this, R.anim.entrada_derecha, R.anim.salida_izquierda);
            this.startActivity(mIntent, opts.toBundle());
        }
    }

    // Método que retorna la fechaSorteo actual.
    protected String getCurrentDate() throws InterruptedException {
        final Calendar c = Calendar.getInstance();
        Date fecha = c.getTime();
        return new SimpleDateFormat(UtilsConstants.FECHA.SIMPLE_DATE_FORMAT).format(fecha);
    }

    //Almacena la información de la última pantalla ingresada en las preferences
    protected void saveLastPage(int page) throws NullPointerException {
        UtilsSharedPreferences preferences = UtilsSharedPreferences.newInstance(
                BaseActivity.this);
        try {
//            preferences.savePreference(UtilsConstants.SHARED_PREFERENCES.LAST_PAGE_ID,
//                                        page);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        msgLogInfo(UtilsConstants.SHARED_PREFERENCES.PREFERENCES_INFO + " " + page);
    }

    //Obtiene la información de la última ventana ingresada.
    protected Bundle getPreferencesData(CharSequence pageId) throws NullPointerException, IllegalStateException {
        Bundle bundle = null;
        UtilsSharedPreferences preferences = UtilsSharedPreferences.newInstance(
                BaseActivity.this);
        Object page = null;
        try {
            page = preferences.getPreference(pageId);
            if (page != null) {
                UtilsTypeClass typeClass = UtilsTypeClass.newInstance();
                int lastPage = (int) page;
                if (typeClass.isInteger(lastPage)) {
                    if (lastPage > 0) {
                        bundle = new Bundle();
                        bundle.putInt(String.valueOf(pageId), lastPage);
                        //msgLogInfo(UtilsConstants.SHARED_PREFERENCES.PREFERENCES_INFO + " " + lastPage);
                    }
                } else {
                    //TODO mostrar un mensaje que el tipo de la última página, es diferente al esperado
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bundle;
    }

    protected synchronized void showSnackbarAlert(final String text) throws Exception {
        if (text.length() > 0) {
            Handler handler = getNewHandler();
            if (isNotNull(handler)) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
            }
        } else {
            msgLogError("El texto a mostrar en el Snakbar, está vacío.");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                doBackLifeCycle();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onKeyDown(keyCode, event);
    }

    //Método que se encarga de obtener la actividad anterior y dirigirla hacia esa pantalla,
    //en caso de presionar el botón "back" o en el menú lateral.
    private void doBackLifeCycle() throws NullPointerException, Exception {
        if (mClassBackward != null) {
            changeActivityBackward(mClassBackward);
        } else {
            msgLogError("NO se ha estipulado la actividad con la que se debe de volver.");
            throw new NullPointerException("NO se ha estipulado la actividad con la que se debe de volver.");
        }
    }

    //Método que permite verificar si el usuario o el atender, ha iniciado sesiónz
    protected boolean validarLogIn() throws Exception {
        boolean iniciadoSesion = isSignIn();
        if (!iniciadoSesion) {
//            changeActivityForward(ActivityIniciarSesion.class);
        }
        return iniciadoSesion;
    }


    public AlertDialog.Builder getAlertDialog(CharSequence title, CharSequence mensaje) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(title);
        alertDialog.setMessage(mensaje);
        alertDialog.setIcon(R.mipmap.ic_launcher);
        return alertDialog;
    }

    public boolean isNotNull(Object object) {
        if (null != object) {
            return true;
        }
        return false;
    }

    //Oculta la ventana superior del dispositivo.
    protected synchronized void ocultarVentana() throws Exception {
        try {
            hideWindowContainer();
            hideActionBar();
        } catch (Exception e) {
            msgLogError("Error al intentar ocultar la ventana contenedora.");
            e.printStackTrace();
        }
    }

    protected synchronized Handler getNewHandler() throws Exception {
        return new Handler();
    }

    /**
     * Permite colocar una toolbar en el proyecto.
     */
    protected void setUpToolbar(int idToolbar) {
        Toolbar toolbar = (Toolbar) findViewById(idToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setLogo(R.mipmap.ic_launcher);
    }

    /**
     * Método que se encarga de cambiar el background de un botón
     *
     * @throws NullPointerException
     */
    protected void changeBackgroundButton(final Button button, final int style) throws NullPointerException {
        //Validamos que el botón no sea null.
        if (isNotNull(button)) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    button.setBackground(ContextCompat.getDrawable(BaseActivity.this, style));
                }
            });
        } else {
            msgLogError("Problema al cambiar el background del botón debido a que es Null.");
        }
    }

    protected void closeRealmConnection(Realm realm) {
        if (isNotNull(realm)){
            realm.getDefaultInstance().close();
        }
    }

    protected void addObjectToRealm(Realm realm, Object object) {
        if (isNotNull(realm) && isNotNull(object)){
            if (object instanceof Test) {
                Test test = (Test) object;
                realm.copyToRealmOrUpdate(test);
            }
        }
    }

    protected RealmResults<ObjectItem> queryToRealm(Realm realm, Class classToFind){
        // Build the query looking at all users:
        RealmQuery<ObjectItem> query = realm.where(classToFind);

        // Add query conditions:
//        query.equalTo("name", "John");
//        query.or().equalTo("name", "Peter");

        // Execute the query:
        RealmResults<ObjectItem> result1 = query.findAll();
        return result1;
    }

    /**
     * Permite saber si una auditoría ha sido inicializada para permitirle al usuario que pueda continuarla
     * @param textView: TextView que le mostrará al usuario una etiqueta para que pueda continuar con la auditoría
     * @throws Exception
     */
    protected void isAuditInit(TextView textView) throws Exception {
        if (isNotNull(textView)){
            //Obtenemos la clase que nos ayuda a obtener los campos que se encuentran en las preferencias.
            UtilsSharedPreferences preferencias = UtilsSharedPreferences.newInstance(this);
            if (isNotNull(preferencias)){
                if (preferencias.getPreference(UtilsConstants.AUDITORIA.AUDITORIA_INIT, true)){
                    showView(textView);
                    msgLogInfo("Auditoría disponible");
                } else {
                    msgLogInfo("No hay una auditoria iniciada.");
                }
            } else {
                msgLogError("No se pudo obtener la clase de preferencias.");
            }
        } else {
            msgLogError("FrameLayout no se ha obtenido correctamente.");
        }
    }
}
