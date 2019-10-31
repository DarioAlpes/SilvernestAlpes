package smartobjects.com.smobapp.connectivity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import smartobjects.com.smobapp.sharedPreferences.UtilsSharedPreferences;
import smartobjects.com.smobapp.utils.UtilsConstants;

/**
 * Created by Andres Rubiano on 17/11/2015.
 */
public class UtilsConnectionServer {

    private Context context = null;
    private static UtilsSharedPreferences preferences = null;

    private static UtilsConnectionServer ourInstance = new UtilsConnectionServer();

    public static UtilsConnectionServer getInstance(Context context) {
        if (context != null) {
            preferences = UtilsSharedPreferences.newInstance((Activity) context);
        }
        return ourInstance;
    }

    private UtilsConnectionServer() {
    }

    //Devuelve la ruta de la carpeta
    private String getRoute() throws Exception {
        return UtilsConstants.URL.URL_BASE;
    }

    /**
     * Método que devuelve la URL para autenticar usuario
     * @return
     * @throws Exception
     */
    public String getUrlAuthenticateUser() throws Exception {
        return getRoute() + UtilsConstants.URL.USER_AUTHENTICATE;
    }

    /**
     * Método que devuelve la URL para listar los productos disponibles.
     * @return
     * @throws Exception
     */
    public String getUrlProductList() throws Exception {
        return getRoute() + UtilsConstants.URL.PRODUCT_LIST;
    }

    public String getUrlProductDetail() throws Exception {
        return getRoute() + UtilsConstants.URL.PRODUCT_DETAIL;
    }

    public String getUrlProductBySku() throws Exception {
        return getRoute() + UtilsConstants.URL.PRODUCT_BY_SKU;
    }

    public String getUrlItemList() throws Exception {
        return getRoute() + UtilsConstants.URL.ITEM_LIST;
    }

    /**
     * Método que devuelve la URL para obtener las ubicaciones.
     * @return
     * @throws Exception
     */
    public String getUrlUbicaciones() throws Exception {
        return getRoute() + UtilsConstants.URL.UBICACION_LISTS;
    }

}
