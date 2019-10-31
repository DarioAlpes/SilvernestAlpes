package smartobjects.com.smobapp.battery;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

/**
 * Created by Andres Rubiano on 26/11/2015.
 */
public class UtilsBattery {
    private static UtilsBattery ourInstance = new UtilsBattery();

    public static UtilsBattery getInstance() {
        return ourInstance;
    }

    private UtilsBattery() {
    }

    //Devuelve el porcentaje de la batería del dispositivo. -1 en caso de error.
    private int getLevelBattery(Context mContext) throws Exception {
        if (mContext != null) {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            if (ifilter != null) {
                Intent batteryStatus = mContext.registerReceiver(null, ifilter);
                if (batteryStatus != null) {
                    return batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                }
            }
        }
        return -1;
    }
}
