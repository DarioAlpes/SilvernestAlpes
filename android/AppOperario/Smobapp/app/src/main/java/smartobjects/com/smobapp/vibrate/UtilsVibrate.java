package smartobjects.com.smobapp.vibrate;

import android.content.Context;
import android.os.Vibrator;

/**
 * Created by Andres Rubiano on 11/11/2015.
 */
public class UtilsVibrate {
    private static UtilsVibrate ourInstance = new UtilsVibrate();
    private final int TIME_VIBRATION = 300;

    public static UtilsVibrate getInstance() {
        return ourInstance;
    }

    private UtilsVibrate() {
    }

    public void vibratePhone(Context context) {
        if (context != null) {
            Vibrator vibrator =(Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(TIME_VIBRATION);
        }
    }
}
