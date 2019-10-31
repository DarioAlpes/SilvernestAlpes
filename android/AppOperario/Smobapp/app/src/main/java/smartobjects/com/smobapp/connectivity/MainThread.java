package smartobjects.com.smobapp.connectivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;

import com.google.gson.Gson;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import smartobjects.com.smobapp.ActivityConsultarProducto;
import smartobjects.com.smobapp.ActivityDescargaItem;
import smartobjects.com.smobapp.ActivityModulos;
import smartobjects.com.smobapp.ActivityProductDetail;
import smartobjects.com.smobapp.ActivityUbicacion;
import smartobjects.com.smobapp.R;
import smartobjects.com.smobapp.adapters.AdapterListaItem;
import smartobjects.com.smobapp.adapters.AdapterListaProducto;
import smartobjects.com.smobapp.fragments.FragmentListProducto;
import smartobjects.com.smobapp.objects.ObjectItem;
import smartobjects.com.smobapp.objects.ObjectProducto;
import smartobjects.com.smobapp.objects.ObjectUbicacion;
import smartobjects.com.smobapp.objects.ObjectUsuario;
import smartobjects.com.smobapp.utils.BaseActivity;
import smartobjects.com.smobapp.utils.GlobalClass;
import smartobjects.com.smobapp.utils.UtilsConstants;

/**
 * Created by Andres Rubiano on 11/11/2015.
 * Hilo principal que se encarga de hacer las descargas asincronas del servidor.
 */
public class MainThread extends AsyncTask<String, String, Bundle> {

    //Elemento para mostrar en pantalla.
    private ProgressDialog pDialog = null;
    //Clase que se encarga de descargar la información del servidor.
    private HttpUtils httppostaux;
    //Tendrá el contexto de la clase que está instanciando
    private Context mContext = null;
    //Título a mostrar
    private String mTitulo;
    private BaseActivity mActivity;
    private ActivityUbicacion mActivityUbicacion;
    private ActivityDescargaItem mActivityDescargarItem;
    private ListView mListView;
    private Bundle mBundle = null;
    private ActivityProductDetail mActivityProductDetail;
    private int mLayout;

    public MainThread(BaseActivity activity, String titulo) {
        this.mActivity = activity;
        this.mTitulo = titulo;
        httppostaux = HttpUtils.newInstance();
        pDialog = new ProgressDialog(activity);
    }

    public MainThread(BaseActivity activity, String titulo, ListView listView) {
        this.mActivity = activity;
        this.mTitulo = titulo;
        httppostaux = HttpUtils.newInstance();
        pDialog = new ProgressDialog(activity);
        this.mListView = listView;
    }

    public MainThread(ActivityProductDetail activity, String titulo) {
        this.mActivity = activity;
        this.mTitulo = titulo;
        httppostaux = HttpUtils.newInstance();
        pDialog = new ProgressDialog(activity);
        this.mActivityProductDetail = activity;
    }

    public MainThread(ActivityConsultarProducto activity, String titulo, int frameLayout) {
        this.mActivity = activity;
        this.mTitulo = titulo;
        this.mLayout = frameLayout;
        httppostaux = HttpUtils.newInstance();
        pDialog = new ProgressDialog(activity);
    }

    public MainThread(ActivityUbicacion activity, String titulo) {
        this.mActivity = activity;
        this.mTitulo = titulo;
        this.mActivityUbicacion = activity;
        httppostaux = HttpUtils.newInstance();
        pDialog = new ProgressDialog(activity);
    }

    public MainThread(ActivityDescargaItem activity) {
        this.mActivity = activity;
        this.mActivityDescargarItem = activity;
        httppostaux = HttpUtils.newInstance();
    }

    protected void onPreExecute() {
        if (null != pDialog) {
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setMessage(mTitulo);
            pDialog.setCancelable(true);
            pDialog.show();
        }
    }

    @Override
    protected void onPostExecute(Bundle bundle) {
        // Si se ha ejecutado bien las peticiones a la base de datos
        if (bundle != null) {
            switch (bundle.getString(UtilsConstants.URL.PARAMETER_ACTIVIDAD_PROVENIENTE)) {
                case UtilsConstants.USUARIO.JSON_KEY:
                    try {
                        postValidateUser(bundle);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case UtilsConstants.PRODUCTO.JSON_LIST_PRODUCT:
                    try {
                        postValidateListProduct(bundle);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case UtilsConstants.PRODUCTO.JSON_PRODUCT_BY_SKU:
                    try {
                        postProductBySku(bundle);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case UtilsConstants.ITEM.JSON_LIST_ITEM:
                    try {
                        postValidateListItem(bundle);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case UtilsConstants.UBICACION.JSON_LIST_UBICACIONES :
                    try {
                        postValidateListUbicaciones(bundle);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }

        closeProgressDialog();
    }
    //Método que se encarga de validar si la información se ha descargado correctamente del servidor.
    private void postValidateListUbicaciones(Bundle bundle) throws Exception{
        if (null != bundle) {
            //VAlidamos que si el Bundle contiene información, buscamos el campo PARAMETER_RESUMEN
            //que nos dirá el resultado de la operación.
            String resumen = bundle.getString(UtilsConstants.URL.PARAMETER_RESUMEN);
            if (resumen.equals(UtilsConstants.URL.PARAMETER_RESUMEN_OK)) {
                okListUbicaciones(bundle);
            } else if (resumen.equals(UtilsConstants.URL.PARAMETER_RESUMEN_ERROR)) {
//                errorAuthenticateUser(bundle.getString(UtilsConstants.URL.PARAMETER_MENSAJE, ""));
            }
        }
    }

    private void okListUbicaciones(Bundle bundle) {
        List<ObjectUbicacion> mListaUbicacion = bundle.getParcelableArrayList(UtilsConstants.UBICACION.ARRAY_UBICACIONES);
//        List<ObjectUbicacion> mListaUbicacion = null;
        if (null != mActivityUbicacion){
            try {
                mActivityUbicacion.setInfo(mListaUbicacion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void postValidateListItem(Bundle bundle) throws Exception{
        if (null != bundle) {
            //VAlidamos que si el Bundle contiene información, buscamos el campo PARAMETER_RESUMEN
            //que nos dirá el resultado de la operación.
            String resumen = bundle.getString(UtilsConstants.URL.PARAMETER_RESUMEN);
            if (resumen.equals(UtilsConstants.URL.PARAMETER_RESUMEN_OK)) {
                okListItem(bundle);
            } else if (resumen.equals(UtilsConstants.URL.PARAMETER_RESUMEN_ERROR)) {
                errorAuthenticateUser(bundle.getString(UtilsConstants.URL.PARAMETER_MENSAJE, ""));
            }
        }
    }

    private void okListItem(Bundle bundle) throws Exception{
        ArrayList<ObjectItem> mListaItem = bundle.getParcelableArrayList(UtilsConstants.ITEM.BUNDLE_LIST_ITEM);
//        ObjectItem.setItems(mListaItem);
        if (null != mActivityDescargarItem){
            mActivityDescargarItem.activateButton();
        }
//        AdapterListaItem myAdapter = new AdapterListaItem(this.mActivity,
//                R.layout.five_row_layout,
//                mListaItem);
//        this.mListView.setAdapter(myAdapter);
    }

    private void postProductBySku(Bundle bundle) throws Exception {
        if (null != bundle) {
            //VAlidamos que si el Bundle contiene información, buscamos el campo PARAMETER_RESUMEN
            //que nos dirá el resultado de la operación.
            String resumen = bundle.getString(UtilsConstants.URL.PARAMETER_RESUMEN);
            if (resumen.equals(UtilsConstants.URL.PARAMETER_RESUMEN_OK)) {
                okProductBySku(bundle);
            } else if (resumen.equals(UtilsConstants.URL.PARAMETER_RESUMEN_ERROR)) {
                errorAuthenticateUser(bundle.getString(UtilsConstants.URL.PARAMETER_MENSAJE, ""));
            }
        }
    }

    private void okProductBySku(Bundle bundle) throws Exception {
        if (null != bundle){
            ObjectProducto producto = bundle.getParcelable(UtilsConstants.PRODUCTO.BUNDLE_PRODUCT);
            ObjectProducto.setProducto(producto);
            FragmentListProducto fragmentProducto = new FragmentListProducto();
            mActivity.getFragmentManager().beginTransaction()
                    .replace(mLayout, fragmentProducto).commit();
        }
    }

    private void postValidateListProduct(Bundle bundle) throws Exception {
        if (null != bundle) {
            //VAlidamos que si el Bundle contiene información, buscamos el campo PARAMETER_RESUMEN
            //que nos dirá el resultado de la operación.
            String resumen = bundle.getString(UtilsConstants.URL.PARAMETER_RESUMEN);
            if (resumen.equals(UtilsConstants.URL.PARAMETER_RESUMEN_OK)) {
                okListProduct(bundle);
            } else if (resumen.equals(UtilsConstants.URL.PARAMETER_RESUMEN_ERROR)) {
                errorAuthenticateUser(bundle.getString(UtilsConstants.URL.PARAMETER_MENSAJE, ""));
            }
        }
    }

    private void okListProduct(Bundle bundle) throws Exception {
        ArrayList<ObjectProducto> mListaProducto = bundle.getParcelableArrayList(UtilsConstants.PRODUCTO.BUNDLE_LIST_PRODUCT);
        ObjectProducto.setProductos(mListaProducto);
        AdapterListaProducto myAdapter = new AdapterListaProducto(this.mActivity,
                R.layout.two_row_layout,
                mListaProducto);
        this.mListView.setAdapter(myAdapter);
    }

    private void closeProgressDialog() {
        if (null != this.pDialog) {
            if (this.pDialog.isShowing()) {
                this.pDialog.dismiss();
            }
        }
    }

    /**
     * Método que se encarga de hacer la validación de la información que contiene el bundle.
     *
     * @param bundle
     * @throws Exception
     */
    private void postValidateUser(Bundle bundle) throws Exception {
        if (null != bundle) {
            //VAlidamos que si el Bundle contiene información, buscamos el campo PARAMETER_RESUMEN
            //que nos dirá el resultado de la operación.
            String resumen = bundle.getString(UtilsConstants.URL.PARAMETER_RESUMEN);
            if (resumen.equals(UtilsConstants.URL.PARAMETER_RESUMEN_OK)) {
                okAuthenticateUser(bundle);
            } else if (resumen.equals(UtilsConstants.URL.PARAMETER_RESUMEN_ERROR)) {
                errorAuthenticateUser(bundle.getString(UtilsConstants.JSON.JSON_MENSAJE, ""));
            }
        }
    }

    private void errorAuthenticateUser(String mensaje) throws Exception {
        if (!this.isCancelled()) {
            this.mActivity.showAlertMessage(UtilsConstants.GENERAL.LOG_ERROR,
                    mensaje);
        }
    }

    /**
     * Método que se encarga de dirigir a la pantalla de inicio de sesión.
     *
     * @param bundle
     * @throws Exception
     */
    private void okAuthenticateUser(Bundle bundle) throws Exception {
        //Si inicia sesión correctamente, va a la pantalla de módulos.
        final GlobalClass globalVariable = (GlobalClass) this.mActivity.getApplicationContext();
        ObjectUsuario usuario = bundle.getParcelable(UtilsConstants.USUARIO.USER_PARCELABLE);
        if (null != usuario) {
            globalVariable.usuario = usuario.getUsername();
            globalVariable.token = usuario.getToken();
            this.mActivity.changeActivityForward(ActivityModulos.class, bundle);
            this.mActivity.finish();
        }
    }

    // Acciones a realizar mientas se está descargando los datos de servidor.
    // args[0] = Tipo para descargar
    @Override
    protected Bundle doInBackground(String... args) {
        //args[0] : Actividad proveniente
        String actividadProveniente = args[0];
        if (actividadProveniente.length() > 0) {
            switch (actividadProveniente) {
                case UtilsConstants.USUARIO.JSON_KEY:
                    //Validamos el usuario y enviamos el usuario y la contraseña
                    try {
                        return validateUser(actividadProveniente, args[1], args[2]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case UtilsConstants.PRODUCTO.JSON_LIST_PRODUCT:
                    //Validamos el usuario y enviamos el usuario y la contraseña
                    try {
                        return getListProduct(actividadProveniente);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case UtilsConstants.PRODUCTO.JSON_DETAIL_PRODUCT:
                    //Validamos el usuario y enviamos el usuario y la contraseña
                    try {
                        return getDetailProduct(actividadProveniente, args[1], args[2]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case UtilsConstants.PRODUCTO.JSON_PRODUCT_BY_SKU:
                    //Validamos el usuario y enviamos el usuario y la contraseña
                    try {
                        return getProductBySku(actividadProveniente, args[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case UtilsConstants.ITEM.JSON_LIST_ITEM:
                    //Validamos el usuario y enviamos el usuario y la contraseña
                    try {
                        return getListItem(actividadProveniente);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case UtilsConstants.UBICACION.JSON_LIST_UBICACIONES:
                    //Validamos el usuario y enviamos el usuario y la contraseña
                    try {
                        return getListUbicacion(actividadProveniente);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

            }
        } else {
            if (null != this.mActivity) {
                this.mActivity.msgLogError("Error al enviar la actividad provenienete en el en el Main Thread.");
            }
        }
        return null;
    }

    private Bundle getListUbicacion(String actividadProveniente) throws Exception{
        Bundle bundle = new Bundle();
        try {
            //Preguntamos si el hilo no ha sido detenido.
            if (!this.isCancelled()) {
                //Clase que se encarga de tener las url de los servicios web.
                UtilsConnectionServer connection = UtilsConnectionServer.getInstance((mContext));
                if (null != connection) {
                    //Obtenemos la url del servicio.
                    String url = connection.getUrlUbicaciones();
                    if (url.length() > 0) {
                        //Con una instancia de la clase GSON para genrar el JSON
                        Gson gson = new Gson();
                        //Parseamos el objeto con la librería.
                            if (null != gson) {
                                final GlobalClass globalClass = (GlobalClass) this.mActivity.getApplicationContext();
                                //OBtenemos todos los datos del servidor.
                                //Obtenemos respuesta del servidor.
                                JSONObject objectUser = descargarJsonObject(url, "", globalClass.getToken());
                                if (objectUser != null){
                                    bundle.putString(UtilsConstants.URL.PARAMETER_ACTIVIDAD_PROVENIENTE, actividadProveniente);
                                    if (null != objectUser && objectUser.length() > 0) {
                                        int success = Integer.parseInt(objectUser.getString(UtilsConstants.JSON.JSON_SUCCESS));
                                        if (success == 1) {
                                            JSONArray ubicaciones = objectUser.getJSONArray("valor");
                                            int tamanio = ubicaciones.length();
                                            if (tamanio > 0){
                                                ArrayList<ObjectUbicacion> listaUbicaciones = new ArrayList<>();
                                                //Creamos una instancia para insertar la información en la base de datos.
//                                                Realm realm = Realm.getInstance(this.mActivity);
                                                for(int i=0; i < tamanio; i++){
                                                    JSONObject json = (JSONObject) ubicaciones.get(i);
//                                                    realm.beginTransaction();
//                                                    ObjectUbicacion user = realm.createObject(ObjectUbicacion.class);
//                                                    user.setId(json.getInt("id"));
//                                                    user.setNombre(json.getString("nombre"));
//                                                    user.setEpc(json.getString("EPC"));
//                                                    user.setTipo(json.getString("tipo"));
//                                                    user.setLlave(json.getString("llave"));
//                                                    realm.commitTransaction();
                                                    listaUbicaciones.add(gson.fromJson(String.valueOf(json), ObjectUbicacion.class));
                                                }
                                                bundle.putParcelableArrayList(UtilsConstants.UBICACION.ARRAY_UBICACIONES, listaUbicaciones);
                                            }
                                            bundle.putString(UtilsConstants.URL.PARAMETER_RESUMEN, UtilsConstants.URL.PARAMETER_RESUMEN_OK);
                                        } else {
                                            bundle.putString(UtilsConstants.URL.PARAMETER_RESUMEN, UtilsConstants.URL.PARAMETER_RESUMEN_ERROR);
                                            bundle.putString(UtilsConstants.JSON.JSON_MENSAJE, objectUser.getString(UtilsConstants.JSON.JSON_MENSAJE));
                                        }
                                    } else {
                                        mActivity.msgLogError("Problema al parsear el JSON proveniente del servidor.");
                                    }
                                } else {
                                    mActivity.msgLogError("No llegó información del servidor.");
                                }
                            } else {
                                mActivity.msgLogError("Clase Gson es null.");
                            }
                    } else {
                        mActivity.msgLogError("La URL debe tener una url válida.");
                    }
                } else {
                    mActivity.msgLogError("Clase de constantes de URL no encontrada.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bundle == null ? getBundleError(actividadProveniente) : bundle;
    }

    /**
     * Método que se encarga de crear un nuevo objeto para la Ubicación
     * @param id : Id del objeto
     * @param nombre : Nombre de la ubicación
     * @param epc : Epc de la ubicación
     * @param tipo : Tipo al cual pertenece
     * @param llave : Llave del objeto
     */
    private ObjectUbicacion createNewUbicacion(int id, String nombre, String epc, String tipo, String llave){
        return new ObjectUbicacion(id, nombre, epc, tipo, llave);
    }

    private Bundle getListItem(String actividad) throws Exception {
        Bundle bundle = new Bundle();
        try {
            //Preugntamos si el hilo no ha sido detenido.
            if (!this.isCancelled()) {
                //Clase que se encarga de tener las url de los servicios web.
                UtilsConnectionServer connection = UtilsConnectionServer.getInstance((mActivity));
                if (null != connection) {
                    //Obtenemos la url del servicio.
                     String url = connection.getUrlItemList();
                    if (url.length() > 0) {
                        //OBtenemos el TOken de la persona que inició sesión
                        final GlobalClass globalClass = (GlobalClass) this.mActivity.getApplicationContext();
                        Gson gson = new Gson();
                        //OBtenemos todos los datos del servidor.
                        JSONObject objectUser = descargarJsonObject(url, "", globalClass.getToken());
                        if (objectUser != null) {
                            bundle.putString(UtilsConstants.URL.PARAMETER_ACTIVIDAD_PROVENIENTE, actividad);
                            int success = Integer.parseInt(objectUser.getString(UtilsConstants.JSON.JSON_SUCCESS));
                            if (success == 1) {
                                JSONArray listaItems = objectUser.getJSONArray("valor");
                                int tamanio = listaItems.length();
                                if (tamanio > 0) {
                                    ArrayList<ObjectItem> items = new ArrayList<>();
                                    for (int item = 0; item < tamanio - 1; item++) {
                                        JSONObject json = (JSONObject) listaItems.get(item);
                                        items.add(gson.fromJson(String.valueOf(json), ObjectItem .class));
                                    }
                                    bundle.putParcelableArrayList(UtilsConstants.ITEM.BUNDLE_LIST_ITEM, items);
                                }
                                bundle.putString(UtilsConstants.URL.PARAMETER_RESUMEN, UtilsConstants.URL.PARAMETER_RESUMEN_OK);
                            } else {
                                bundle.putString(UtilsConstants.URL.PARAMETER_RESUMEN, UtilsConstants.URL.PARAMETER_RESUMEN_ERROR);
                                bundle.putString(UtilsConstants.JSON.JSON_MENSAJE, objectUser.getString(UtilsConstants.JSON.JSON_MENSAJE));
                                mActivity.msgLogError("La información del servidor, no ha devuelto nada.");
                            }
                        }
                    } else {
                        mActivity.msgLogError("La URL debe tener una url válida.");
                    }
                } else {
                    mActivity.msgLogError("Clase de constantes de URL no encontrada.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bundle == null ? getBundleError(actividad) : bundle;
    }

    private Bundle getProductBySku(String actividad, String sku) throws Exception {
        Bundle bundle = null;
//        try {
//            //Preugntamos si el hilo no ha sido detenido.
//            if (!this.isCancelled()) {
//                //Clase que se encarga de tener las url de los servicios web.
//                UtilsConnectionServer connection = UtilsConnectionServer.getInstance((mContext));
//                if (null != connection) {
//                    //Obtenemos la url del servicio.
//                    String url = connection.getUrlProductBySku();
//                    if (url.length() > 0) {
//                        //Con una instancia de la clase GSON para genrar el JSON
//                        Gson gson = new Gson();
//                        //Creamos un objeto con los parámetros enviados.
//                        ObjectProducto producto = new ObjectProducto(sku);
//                        //Parseamos el objeto con la librería.
//                        if (null != producto) {
//                            if (null != gson) {
//                                //Creamos el JSON
//                                String json = gson.toJson(producto);
//                                final GlobalClass global = (GlobalClass) mActivity.getApplicationContext();
//                                //Obtenemos respuesta del servidor.
//                                String datos = descargarJsonObject(url, json, global.getToken());
//                                if (datos != null) {
//                                    JSONObject objectUser = new JSONObject(datos);
//                                    if (null != objectUser && objectUser.length() > 0) {
//                                        ObjectProducto objectProducto = (gson.fromJson(String.valueOf(objectUser), ObjectProducto.class));
//                                        bundle = new Bundle();
//                                        bundle.putString(UtilsConstants.URL.PARAMETER_ACTIVIDAD_PROVENIENTE, actividad);
//                                        bundle.putString(UtilsConstants.URL.PARAMETER_RESUMEN, UtilsConstants.URL.PARAMETER_RESUMEN_OK);
//                                        bundle.putParcelable(UtilsConstants.PRODUCTO.BUNDLE_PRODUCT, objectProducto);
//                                    } else {
//                                        mActivity.msgLogError("Problema al parsear el JSON proveniente del servidor.");
//                                    }
//                                } else {
//                                    mActivity.msgLogError("La información del servidor, no ha devuelto nada.");
//                                }
//                            } else {
//                                mActivity.msgLogError("Clase Gson es null.");
//                            }
//                        } else {
//                            mActivity.msgLogError("ObjectUsuario es null.");
//                        }
//                    } else {
//                        mActivity.msgLogError("La URL debe tener una url válida.");
//                    }
//                } else {
//                    mActivity.msgLogError("Clase de constantes de URL no encontrada.");
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return bundle == null ? getBundleError(actividad) : bundle;
    }

    private Bundle getDetailProduct(String actividad, String id, String sku) throws Exception {
        Bundle bundle = null;
//        try {
//            //Preugntamos si el hilo no ha sido detenido.
//            if (!this.isCancelled()) {
//                //Clase que se encarga de tener las url de los servicios web.
//                UtilsConnectionServer connection = UtilsConnectionServer.getInstance((mContext));
//                if (null != connection) {
//                    //Obtenemos la url del servicio.
//                    String url = connection.getUrlProductDetail();
//                    if (url.length() > 0) {
//                        //Con una instancia de la clase GSON para genrar el JSON
//                        Gson gson = new Gson();
//                        //Creamos un objeto con los parámetros enviados.
//                        ObjectProducto producto = new ObjectProducto(Integer.parseInt(id), sku);
//                        //Parseamos el objeto con la librería.
//                        if (null != producto) {
//                            if (null != gson) {
//                                //Creamos el JSON
//                                String json = gson.toJson(producto);
//                                final GlobalClass global = (GlobalClass) mActivity.getApplicationContext();
//                                //Obtenemos respuesta del servidor.
//                                String datos = descargarJsonObject(url, json, global.getToken());
//                                if (datos != null) {
//                                    JSONObject objectUser = new JSONObject(datos);
//                                    if (null != objectUser && objectUser.length() > 0) {
//                                        bundle = new Bundle();
//                                        bundle.putString(UtilsConstants.URL.PARAMETER_ACTIVIDAD_PROVENIENTE, actividad);
//                                        bundle.putString(UtilsConstants.URL.PARAMETER_RESUMEN, UtilsConstants.URL.PARAMETER_RESUMEN_OK);
//                                        bundle.putParcelable(UtilsConstants.USUARIO.USER_PARCELABLE, producto);
//                                    } else {
//                                        mActivity.msgLogError("Problema al parsear el JSON proveniente del servidor.");
//                                    }
//                                } else {
//                                    mActivity.msgLogError("La información del servidor, no ha devuelto nada.");
//                                }
//                            } else {
//                                mActivity.msgLogError("Clase Gson es null.");
//                            }
//                        } else {
//                            mActivity.msgLogError("ObjectUsuario es null.");
//                        }
//                    } else {
//                        mActivity.msgLogError("La URL debe tener una url válida.");
//                    }
//                } else {
//                    mActivity.msgLogError("Clase de constantes de URL no encontrada.");
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
        return bundle == null ? getBundleError(actividad) : bundle;
    }

    private Bundle getListProduct(String actividad) throws Exception {
        Bundle bundle = null;
        try {
            //Preugntamos si el hilo no ha sido detenido.
            if (!this.isCancelled()) {
                //Clase que se encarga de tener las url de los servicios web.
                UtilsConnectionServer connection = UtilsConnectionServer.getInstance((mActivity));
                if (null != connection) {
                    //Obtenemos la url del servicio.
                    String url = connection.getUrlProductList();
                    if (url.length() > 0) {
                        //OBtenemos el TOken de la persona que inició sesión
                        final GlobalClass globalClass = (GlobalClass) this.mActivity.getApplicationContext();
                        //OBtenemos todos los datos del servidor.
                        JSONArray datos = descargarGetJsonObject(url, globalClass.getToken());
                        if (datos != null) {
                            int tamanio = datos.length();
                            Gson gson = new Gson();
                            ArrayList<ObjectProducto> listaProductos = new ArrayList<>();
                            for (int producto = 0; producto < tamanio - 1; producto++) {
                                JSONObject json = (JSONObject) datos.get(producto);
                                listaProductos.add(gson.fromJson(String.valueOf(json), ObjectProducto.class));
                            }
                            if(listaProductos.size() > 0){
                                bundle = new Bundle();
                                bundle.putString(UtilsConstants.URL.PARAMETER_ACTIVIDAD_PROVENIENTE, actividad);
                                bundle.putString(UtilsConstants.URL.PARAMETER_RESUMEN, UtilsConstants.URL.PARAMETER_RESUMEN_OK);
                                bundle.putParcelableArrayList(UtilsConstants.PRODUCTO.BUNDLE_LIST_PRODUCT, listaProductos);
                                return bundle;
                            } else {
                                this.mActivity.msgLogError("NO hay datos en la lista de productos.");
                            }
                        } else {
                            mActivity.msgLogError("La información del servidor, no ha devuelto nada.");
                        }
                    } else {
                        mActivity.msgLogError("La URL debe tener una url válida.");
                    }
                } else {
                    mActivity.msgLogError("Clase de constantes de URL no encontrada.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bundle == null ? getBundleError(actividad) : bundle;
    }

    /**
     * Método que se encarga de validar en el servidor si está haciendo bien la autenticación
     *
     * @param user
     * @param password
     * @throws Exception
     */
    private Bundle validateUser(String actividad, String user, String password) throws Exception {
        Bundle bundle = new Bundle();
        try {
            //Preguntamos si el hilo no ha sido detenido.
            if (!this.isCancelled()) {
                //Clase que se encarga de tener las url de los servicios web.
                UtilsConnectionServer connection = UtilsConnectionServer.getInstance((mContext));
                if (null != connection) {
                    //Obtenemos la url del servicio.
                    String url = connection.getUrlAuthenticateUser();
                    if (url.length() > 0) {
                        //Con una instancia de la clase GSON para genrar el JSON
                        Gson gson = new Gson();
                        //Creamos un objeto con los parámetros enviados.
                        ObjectUsuario usuario = new ObjectUsuario(user, password);
                        //Parseamos el objeto con la librería.
                        if (null != usuario) {
                            if (null != gson) {
                                //Creamos el JSON
                                String json = gson.toJson(usuario);
                                //Obtenemos respuesta del servidor.
                                String datos = descargarJsonObject(url, json);
                                if (datos != null) {
                                    JSONObject objectUser = new JSONObject(datos);
                                    bundle.putString(UtilsConstants.URL.PARAMETER_ACTIVIDAD_PROVENIENTE, actividad);
                                    if (null != objectUser && objectUser.length() > 0) {
                                        int success = Integer.parseInt(objectUser.getString(UtilsConstants.JSON.JSON_SUCCESS));
                                        if (success == 1) {
                                            usuario.setToken(objectUser.getString(UtilsConstants.JSON.JSON_VALOR));
                                            bundle.putString(UtilsConstants.URL.PARAMETER_RESUMEN, UtilsConstants.URL.PARAMETER_RESUMEN_OK);
                                            bundle.putParcelable(UtilsConstants.USUARIO.USER_PARCELABLE, usuario);
                                        } else {
                                            bundle.putString(UtilsConstants.URL.PARAMETER_RESUMEN, UtilsConstants.URL.PARAMETER_RESUMEN_ERROR);
                                            bundle.putString(UtilsConstants.JSON.JSON_MENSAJE, objectUser.getString(UtilsConstants.JSON.JSON_MENSAJE));
                                        }
                                    } else {
                                        mActivity.msgLogError("Problema al parsear el JSON proveniente del servidor.");
                                    }
                                } else {
                                    mActivity.msgLogError("La información del servidor, no ha devuelto nada.");
                                }
                            } else {
                                mActivity.msgLogError("Clase Gson es null.");
                            }
                        } else {
                            mActivity.msgLogError("ObjectUsuario es null.");
                        }
                    } else {
                        mActivity.msgLogError("La URL debe tener una url válida.");
                    }
                } else {
                    mActivity.msgLogError("Clase de constantes de URL no encontrada.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bundle == null ? getBundleError(actividad) : bundle;
    }

    //Método que se encarga de devolver un Bundle que contiene información errónea.
    private Bundle getBundleError(String actividadProveniente) {
        Bundle bundle = new Bundle();
        bundle.putString(UtilsConstants.URL.PARAMETER_ACTIVIDAD_PROVENIENTE, actividadProveniente);
        bundle.putString(UtilsConstants.URL.PARAMETER_MENSAJE, String.valueOf("Se ha presentado un error en el servidor. Por favor, vuelva a intentarlo."));
        bundle.putString(UtilsConstants.URL.PARAMETER_RESUMEN, UtilsConstants.URL.PARAMETER_RESUMEN_ERROR);
        return bundle;
    }

    private String descargarJsonObject(String url, String json) {
        String respuesta = "";
        try {
            HttpResponse response = JSONParser.makeRequest(url, json);
            respuesta = getResponseBody(response);
        } catch (Exception ie) {
        }
        return respuesta;
    }

    private JSONObject descargarJsonObject(String url, String json, String authorization) {
        String respuesta = "";
        try {
            return JSONParser.makeHttpRequest(url, "GET", null, authorization);
//            respuesta = getResponseBody(null);
        } catch (Exception ie) {
            ie.printStackTrace();
        }
        return null;
    }

    private JSONArray descargarGetJsonObject(String url, String authorization) {
        String respuesta = "";
        try {
            JSONArray array = JSONParser.getJSONFromUrl(url, authorization);
            return array;
        } catch (Exception ie) {
        }
        return null;
    }

    public static String getResponseBody(HttpResponse response) {
        String response_text = null;
        HttpEntity entity = null;
        try {
            entity = response.getEntity();
            response_text = _getResponseBody(entity);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            if (entity != null) {

                try {
                    entity.consumeContent();
                } catch (IOException e1) {
                }
            }
        }
        return response_text;
    }


    public static String _getResponseBody(final HttpEntity entity)
            throws IOException, ParseException {

        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        }

        InputStream instream = entity.getContent();

        if (instream == null) {
            return "";
        }

        if (entity.getContentLength() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(

                    "HTTP entity too large to be buffered in memory");
        }

        String charset = getContentCharSet(entity);

        if (charset == null) {

            charset = HTTP.DEFAULT_CONTENT_CHARSET;

        }

        Reader reader = new InputStreamReader(instream, charset);

        StringBuilder buffer = new StringBuilder();

        try {

            char[] tmp = new char[1024];

            int l;

            while ((l = reader.read(tmp)) != -1) {

                buffer.append(tmp, 0, l);

            }

        } finally {

            reader.close();

        }

        return buffer.toString();

    }

    public static String getContentCharSet(final HttpEntity entity)
            throws ParseException {

        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        }

        String charset = null;

        if (entity.getContentType() != null) {

            HeaderElement values[] = entity.getContentType().getElements();

            if (values.length > 0) {

                NameValuePair param = values[0].getParameterByName("charset");

                if (param != null) {

                    charset = param.getValue();

                }

            }

        }

        return charset;

    }

}

