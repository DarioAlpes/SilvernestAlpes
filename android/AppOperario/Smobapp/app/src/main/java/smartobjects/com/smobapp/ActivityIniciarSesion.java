package smartobjects.com.smobapp;

import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import smartobjects.com.smobapp.connectivity.MainThread;
import smartobjects.com.smobapp.utils.BaseActivity;
import smartobjects.com.smobapp.utils.UtilsConstants;

/**
 * Actividad que se encarga de hacer un registro de LogIn con el usuario. Se usa la librería ButterKnife
 * para hacer inyecciones de código de la vista, además de usar la librería Saripaar que permite la validación
 * de campos que no se encuentren nulos.
 * La clase <<MainThread>> es la encargada de realizar la descarga y validación del servidor.
 */
public class ActivityIniciarSesion extends BaseActivity implements Validator.ValidationListener {
    //Obtenemos el switch que permite mostrar la contraseña
    @Bind(R.id.switch_iniciar_sesion_mostrar) Switch mSwitchShowPassword;
    //Hilo principal que se encargara de la descarga y lectura de los servicios web.
    private MainThread mMainThread = null;
    //Campo de texto que tendra el usuario escrito. Campo no puede ser nulo.
    @NotEmpty(messageResId = R.string.error_field_mandatory) @Bind(R.id.et_iniciar_sesion_usuario) EditText mEtUser;
    //Campo de texto que contendrá la contraseña del usuario. El campo no puede ser nulo.
    @NotEmpty(messageResId = R.string.error_field_mandatory) @Bind(R.id.et_iniciar_sesion_contrasena) EditText mEtPassword;
    //Clase que permitirá la validación de los campos.
    Validator mValidator = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_sesion);

        //Obtenemos los componentes con los que vamos a interactuar
        getComponents();
    }

    /**
     * Método que se encarga de obtener los componentes de la vista que vamos a usar y de
     * colocar los listener respectivos.
     *
     * @throws NullPointerException : Capturamos el error si algún componente es null.
     */
    private void getComponents() throws NullPointerException {
        //Le permitimos a ButterKnife que pueda hacer las inyecciones de las View.
        ButterKnife.bind(this);
        //Validamos que exista en memoria el componente y le asignamos a una función
        if (isNotNull(mSwitchShowPassword)){
            mSwitchShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mostrarDatosSwitch(isChecked);
                }
            });
        } else {
            msgLogError(getString(R.string.log_iniciar_sesion_error_switch_contrasena));
        }
        //Instanciamos la clase Validator de Saripaar para que valide los campos de texto.
        mValidator = new Validator(this);
        mValidator.setValidationListener(this);
    }

    /**
     * Método que se encarga de mostrar la información que se encuentra oculta en el campo de contraseña
     * @param mostrar : Estado booleano del componente para mostrar o no, la información
     * @throws NullPointerException : El switch no puede ser nulo.
     */
    private void mostrarDatosSwitch(boolean mostrar) throws NullPointerException {
        //Si el parámetro es verdadero, mostramos la información
        if (mostrar){
            mEtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            return;
        }
        //Si no es verdadero, vuelve a ocultar la información.
        mEtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        mEtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    /**
     * Método que se encarga de hacer toda la gestión para iniciar sesión
     *
     * @throws Exception
     */
    private void gestionIniciarSesion() throws Exception {
        if (isInternetConnection()) {
            mMainThread = new MainThread(ActivityIniciarSesion.this, UtilsConstants.PROGRESS_DIALOG.AUTHENTICATE_USER);
            mMainThread.execute(UtilsConstants.USUARIO.JSON_KEY,
                    mEtUser.getText().toString(),
                    mEtPassword.getText().toString());
        } else {
            showAlertMessage(UtilsConstants.GENERAL.LOG_ERROR, getString(R.string.error_internet));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            try {
                changeActivityBackward();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onValidationSucceeded() {
        try {
            gestionIniciarSesion();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                try {
                    showAlertMessage("Annotation", message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @OnClick(R.id.btn_login_iniciar)
    public void clickIniciarSesion(View v) {
        mValidator.validate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEtUser.setText("andres.rubiano");
        mEtPassword.setText("admin");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isNotNull(mMainThread)) {
            mMainThread.cancel(true);
        }
    }
}
