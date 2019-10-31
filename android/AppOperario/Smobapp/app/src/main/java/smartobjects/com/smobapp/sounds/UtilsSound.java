package smartobjects.com.smobapp.sounds;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;

import smartobjects.com.smobapp.R;

/**
 * Created by Andres Rubiano on 12/01/2016.
 */
public class UtilsSound {
    private static UtilsSound ourInstance = new UtilsSound();

    public static UtilsSound getInstance() {
        return ourInstance;
    }

    private UtilsSound() {
    }

    public void soundError(Context context) {
        playMusic(context, R.raw.error);
    }

    public synchronized void soundOk(final Context context) {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                playMusic(context, R.raw.ok);
            }
        });
    }

    public void soundOkDos(Context context) {
        playMusic(context, R.raw.ok_dos);
    }

    public void soundTimeOutMin(Context context) {
        playMusic(context, R.raw.time_out_min);
    }

    public void soundTimeOutMax(Context context) {
        playMusic(context, R.raw.time_out_max);
    }

    private void playMusic(Context context, int music){
        if (null !=  context) {
            MediaPlayer mp = MediaPlayer.create(context, music);
            mp.start();
        }
    }
}
