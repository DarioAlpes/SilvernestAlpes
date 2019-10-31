package smartobjects.com.smobapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import io.realm.Realm;
import smartobjects.com.smobapp.connectivity.MainThread;
import smartobjects.com.smobapp.objects.ObjectItem;
import smartobjects.com.smobapp.objects.ObjectUbicacion;
import smartobjects.com.smobapp.objects.Test;
import smartobjects.com.smobapp.utils.BaseActivity;
import smartobjects.com.smobapp.utils.UtilsConstants;

/**
 * Actividad que se encarga de descargar los datos de Ubicación para un usuario que ha hecho LogIn.
 * Por cada inicio de sesión, se asigna un token y el usuario, que podrás ser accedidos desde la
 * clse global que tendrá la aplicación.
 */
public class ActivityUbicacion extends BaseActivity {
    //Spinner para mostrar los países
    @Bind(R.id.sp_ubicacion_paises) Spinner mSpCountry = null;
    //Spinner para mostrar las regiones
    @Bind(R.id.sp_ubicacion_region) Spinner mSpRegion = null;
    //Spinner para mostrar las ciudades
    @Bind(R.id.sp_ubicacion_ciudad) Spinner mSpCity = null;
    //Spinner para mostrar las zonas
    @Bind(R.id.sp_ubicacion_zona)   Spinner mSpZona = null;
    //Spinner para mostrar los locales
    @Bind(R.id.sp_ubicacion_local)  Spinner mSpLocal = null;
    //Spinner para mostrar las áreas
    @Bind(R.id.sp_ubicacion_area)   Spinner mSpArea = null;
    //Spinner para mostrar los contenedores
    @Bind(R.id.sp_ubicacion_contenedor) Spinner mSpContenedor = null;
    //Botón para buscar la siguiente actividad.
    @Bind(R.id.btn_ubicacion_siguiente) Button mBtnNext = null;
    //Hilo que se encarga de descargar la información del servicio.
    private MainThread mHilo = null;
    private List<ObjectUbicacion> mListaUbicaciones = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubicacion);
        //Activamos el Injection Views
        ButterKnife.bind(ActivityUbicacion.this);
        //Configuramos la toolbar.
        setUpToolbar(R.id.toolbar_conciliar);
        mListaUbicaciones = new ArrayList<>();
        try {
//            addInfo();
//            putPosition();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void putPosition() {
        if (isNotNull(mSpCountry)){
            mSpCountry.setSelection(1);
        }
        if (isNotNull(mSpRegion)){
            mSpRegion.setSelection(1);
        }
        if (isNotNull(mSpCity)){
            mSpCity.setSelection(1);
        }
    }

    private void addInfo() throws Exception {
        addInfoPaises();
        addInfoRegion();
        addInfoCiudad();
        addInfoZona();
        addInfoLocal();
        addInfoArea();
        addInfoContenedor();
    }

    private void addInfoPaises(){
        if (null != mSpCountry){
            setSampleArrayAdapter(mSpCountry, getDataPaises());
        }
    }

    private void addInfoRegion(){
        if (null != mSpRegion){
            setSampleArrayAdapter(mSpRegion, getDataRegion());
        }
    }

    private void addInfoCiudad(){
        if (null != mSpCity){
            setSampleArrayAdapter(mSpCity, getDataCiudad());
        }
    }

    private void addInfoZona(){
        if (null != mSpZona){
            setSampleArrayAdapter(mSpZona);
        }
    }

    private void addInfoLocal(){
        if (null != mSpLocal){
            setSampleArrayAdapter(mSpLocal);
        }
    }

    private void addInfoArea(){
        if (null != mSpArea){
            setSampleArrayAdapter(mSpArea);
        }
    }

    private void addInfoContenedor(){
        if (null != mSpContenedor){
            setSampleArrayAdapter(mSpContenedor);
        }
    }

    @OnItemSelected(R.id.sp_ubicacion_paises)
    void OnItemSelectedPaises(int position){
        updateTypeButton();
    }

    @OnItemSelected(R.id.sp_ubicacion_region)
    void OnItemSelectedRegion(int position){
        updateTypeButton();
    }

    @OnItemSelected(R.id.sp_ubicacion_ciudad)
    void OnItemSelectedCiudad(int position){
        updateTypeButton();
    }

    @OnItemSelected(R.id.sp_ubicacion_local)
    void OnItemSelectedLocal(int position){
        updateTypeButton();
    }

    private void setSampleArrayAdapter(Spinner spinner){
        if (isNotNull(spinner)){
            spinner.setAdapter(getSampleArrayAdapter(null));
        }
    }

    private void setSampleArrayAdapter(Spinner spinner, List<String> data){
        if (isNotNull(spinner)){
            spinner.setAdapter(getSampleArrayAdapter(data));
        }
    }

    private ArrayAdapter<String> getSampleArrayAdapter(List<String> data){
        List<String> list = new ArrayList<String>();
        if (isNotNull(data)){
            list.addAll(data);
        } else {
            list.add("Por favor, seleccione una opción.");
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,list);

        dataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);

        return dataAdapter;
    }

    private List<String> getDataPaises(){
        List<String> data = new ArrayList<String>();
        data.add("Por favor, seleccione una opción.");
        data.add("Colombia");
        return data;
    }

    private List<String> getDataRegion(){
        List<String> data = new ArrayList<String>();
        data.add("Por favor, seleccione una opción.");
        data.add("Cundinamarca");
        return data;
    }

    private List<String> getDataCiudad(){
        List<String> data = new ArrayList<String>();
        data.add("Por favor, seleccione una opción.");
        data.add("Bogotá");
        return data;
    }

    private boolean getPositionSpinner(Spinner spinner){
        if (isNotNull(spinner)){
            if (spinner.getSelectedItemPosition() != 0){
                return true;
            }
        }
        return false;
    }

    @OnClick(R.id.btn_ubicacion_siguiente)
    public void onClickUbicacionSiguiente(){
        String mensaje = "";
        try {
            if (getPositionSpinner(mSpCountry)){
                if (getPositionSpinner(mSpRegion)){
                    if (getPositionSpinner(mSpCity)){
                        if (getPositionSpinner(mSpLocal)) {
                            String localPosition = mSpLocal.getSelectedItem().toString();
                            ObjectUbicacion ubicacionFinal = null;
                            for (ObjectUbicacion ubicacion : mListaUbicaciones){
                               if (ubicacion.getNombre().equals(localPosition)){
                                   ubicacionFinal = ubicacion;
                                   break;
                               }
                            }
                            if (isNotNull(ubicacionFinal)){
                                Bundle bundle = new Bundle();
                                bundle.putParcelable(UtilsConstants.UBICACION.BUNDLE_UBICACION_FINAL, ubicacionFinal);
                                changeActivityForward(ActivityDescargaItem.class, bundle);
                            }
                        } else {
                            mensaje = "Por favor, seleccione un local.";
                        }
                    } else {
                        mensaje = "Por favor, seleccione una ciudad.";
                    }
                } else {
                    mensaje = "Por favor, seleccione una región.";
                }
            } else {
                mensaje = "Por favor, seleccione un País.";
            }
            if (mensaje.length() > 0){
                showAlertMessage("Error", mensaje);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Descargamos la información del servidor.
//        Realm realm = Realm.getInstance(ActivityUbicacion.this);
//        RealmQuery<ObjectUbicacion> query = realm.where(ObjectUbicacion.class);

// Add query conditions:
//        query.equalTo("tipo", "PAIS");
//        query.or().equalTo("name", "Peter");

// Execute the query:
//        RealmResults<ObjectUbicacion> result1 = query.findAll();

// Or alternatively do the same all at once (the "Fluent interface"):
//        RealmResults<ObjectUbicacion> result2 = realm.where(ObjectUbicacion.class)
//                .equalTo("name", "John")
//                .or()
//                .equalTo("name", "Peter")
//                .findAll();

        downloadInfo();
    }

    /**
     * Se encarga de descargar la información de la ubicación
     * @throws NullPointerException :
     */
    private void downloadInfo() throws NullPointerException{
        try {
            //Validamos que tengamos conexión a internet.
            if(isInternetConnection()){
                mHilo = new MainThread(this, "Descargando ubicaciones");
                mHilo.execute(UtilsConstants.UBICACION.JSON_LIST_UBICACIONES);
            } else {
                //Mostramos un mensaje de alerta indicando que no hay internet.
                showAlertMessage(UtilsConstants.GENERAL.LOG_ERROR, getString(R.string.error_internet));
                //Cerramos la actividad
//                ActivityUbicacion.this.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Se encarga de colocar la información que se ha descargado del servidor.
    public void setInfo(List<ObjectUbicacion> ubicaciones) throws Exception {
        ActivityUbicacion.this.mListaUbicaciones.addAll(ubicaciones);
        if (isNotNull(ubicaciones) && ubicaciones.size() > 0){
            List<String> paises = new ArrayList<>();
            paises.add(getDefaultValue());
            List<String> regiones = new ArrayList<>();
            regiones.add(getDefaultValue());
            List<String> ciudad = new ArrayList<>();
            ciudad.add(getDefaultValue());
            List<String> local = new ArrayList<>();
            local.add(getDefaultValue());
            for(ObjectUbicacion ubicacion : ubicaciones){
                if (ubicacion.getTipo().equals("PAIS")){
                    paises.add(ubicacion.getNombre());
                }
                if (ubicacion.getTipo().equals("REGION")){
                    regiones.add(ubicacion.getNombre());
                }
                if (ubicacion.getTipo().equals("CIUDAD")){
                    ciudad.add(ubicacion.getNombre());
                }
                if (ubicacion.getTipo().equals("LOCAL")){
                    local.add(ubicacion.getNombre());
                }
            }
            if (paises.size() > 0){
//                Collections.sort(paises);
                setDataOnSpinner(mSpCountry, paises);
                mSpCountry.setSelection(1);
            }
            if (regiones.size() > 0){
//                Collections.sort(regiones);
                setDataOnSpinner(mSpRegion, regiones);
                mSpRegion.setSelection(15);
            }
            if (ciudad.size() > 0){
//                Collections.sort(ciudad);
                setDataOnSpinner(mSpCity, ciudad);
                mSpCity.setSelection(5);
            }
            if (local.size() > 0){
//                Collections.sort(local);
                setDataOnSpinner(mSpLocal, local);
                mSpLocal.setSelection(1);
            }
        }
        updateTypeButton();
    }

    private String getDefaultValue() {
        return "Por favor, seleccione un campo.";
    }

    /**
     * Método que se encarga de validar si se han seleccionado todos los campos. Si es así,
     * cambia el estado del botón.
     */
    private void updateTypeButton() {
        if (isAllSpinnerSelected()){
            if (isNotNull(mBtnNext)){
                changeBackgroundButton(mBtnNext, R.drawable.estilo_boton_activo);
            }
        } else {
            changeBackgroundButton(mBtnNext, R.drawable.estilo_boton_no_disponible);
        }
    }

    /**
     * Método que se encarga de validar si se ha seleccionado algún campo de los Spinner.
     * @return True:False dependiendo si se ha seleccionado algún campo.
     */
    private boolean isAllSpinnerSelected() {
        if (isNotNull(mSpCountry) && getPositionSpinner(mSpCountry)){
            if (isNotNull(mSpRegion) && getPositionSpinner(mSpRegion)){
                if (isNotNull(mSpCity) && getPositionSpinner(mSpCity)){
                    if (isNotNull(mSpLocal) && getPositionSpinner(mSpLocal)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void setDataOnSpinner(Spinner spinner, List<String> data){
        if (isNotNull(spinner) && isNotNull(data)){
            setSampleArrayAdapter(spinner, data);
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
//        Test test = new Test(40, "Ubicacion");
        ObjectItem  item = new ObjectItem("1502242198", "Pantalón", "28", 25, 0, "http://www.ripley.cl/wcsstore/ripleycl_CAT_AS/Attachment/WOP/D124/2000313943382/2000313943382_2.jpg",  1, "E28011606000020507906AE8");
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(item);
        realm.commitTransaction();
        realm.close();
    }
}
