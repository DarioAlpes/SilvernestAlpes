package smartobjects.com.smobapp.imei;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

/**
 * Created by Andres Rubiano on 26/11/2015.
 */
public class UtilsImei {
    private static UtilsImei ourInstance = new UtilsImei();

    public static UtilsImei getInstance() {
        return ourInstance;
    }

    private UtilsImei() {
    }

    //Obtiene el IMEI del dispositivo
    public String getImeiDevice (Context context) throws Exception {
        if (context != null) {
            TelephonyManager tm =(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                return tm.getDeviceId();
            }
        }
        return "";
    }
}
