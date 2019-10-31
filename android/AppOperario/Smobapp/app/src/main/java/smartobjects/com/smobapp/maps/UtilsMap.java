package smartobjects.com.smobapp.maps;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by Andres Rubiano on 03/12/2015.
 */
public class UtilsMap {
    private static UtilsMap ourInstance = new UtilsMap();

    public static UtilsMap getInstance() {
        return ourInstance;
    }

    private UtilsMap() {
    }

    //Método que se encarga de abrir Google Maps, y buscar la ubicación en el mapa, adicionandole
    //un market ene l punto con un texto.
    public boolean showPositionInMap(Context context, String title, double latitud, double longitud) {
//        37.7749 + "," + -122.4194
        if (context != null) {
            String strUri = "http://maps.google.com/maps?q=loc:" + latitud + "," + longitud + " (" + title + ")";
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
            context.startActivity(intent);
            return true;
        }
        return false;
    }

}
