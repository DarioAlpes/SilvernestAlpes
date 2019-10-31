package smartobjects.com.smobapp;

import android.os.Bundle;
import android.os.Handler;

import smartobjects.com.smobapp.utils.BaseActivity;

/**
 * Clase que se encarga de cargar una imagen mientras que en un hilo
 * diferente al de la interfaz, se encarga de descargar algunos parámetros
 * que vienen del servidor.
 */
public class ActivitySplashScreen extends BaseActivity {
    //Cantida de tiempo que mostrará el ProgressBar mientras carga alguna información por defecto.
    private final long SPLASH_SCREEN_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            //Ocultamos el homeBar
            ocultarVentana();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);
        //Asignamos la vista a la actividad
        setContentView(R.layout.splash_screen);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            //Método que creará el hilo de espera
            changeActividad();
        } catch (Exception e) {
            //Mostramos un mensaje de error en el Log para ayudar al developer a solucionar el error.
            msgLogError(getString(R.string.log_splash_screen_error_cambiar_actividad));
            e.printStackTrace();
        }
    }

    /**
     * Método que se encarga de de cambiar la actividad actual. Mientras se espera cierto tiempo, la
     * idea es reemplazar el Delay por alguna actividad.
     * @throws Exception : En caso de que se genere algun error desconocido.
     */
    private void changeActividad() throws Exception{
        //Creamos un nuevo handler.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    //Método que se encarga de hacer la gestión del cambio de actividad.
                    changeActivityForward(ActivityIniciarSesion.class);
                    //Finalizamos la actividad actual.
                    ActivitySplashScreen.this.finalizar();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, SPLASH_SCREEN_DELAY);
    }

}
