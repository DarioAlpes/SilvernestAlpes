package smartobjects.com.smobapp.utils;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import smartobjects.com.smobapp.R;
import smartobjects.com.smobapp.sounds.UtilsSound;
import smartobjects.com.smobapp.vibrate.UtilsVibrate;

/**
 * Created by Andres Rubiano on 14/10/2015.
 */
public class BaseFragment extends Fragment {

    protected void msgLogError(CharSequence error){
        Log.e(UtilsConstants.GENERAL.LOG_ERROR, error.toString());
    }

    protected void showAlertMessage(CharSequence titulo, CharSequence mensaje) throws  Exception{
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(titulo);
        alertDialog.setMessage(mensaje);
        alertDialog.setIcon(R.mipmap.ic_launcher);

        alertDialog.setPositiveButton(R.string.st_general_aceptar,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialog.show();
    }

    protected void showAlertMessageTwoOptions(String title, String mensaje){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(title);
        alertDialog.setMessage(mensaje);
        alertDialog.setIcon(R.mipmap.ic_launcher);
        EditText mEditText = new EditText(getActivity());
        mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        alertDialog.setView(mEditText);

        alertDialog.setPositiveButton(R.string.st_general_aceptar,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        alertDialog.setNegativeButton(R.string.st_general_cancelar, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    protected void showAlertMessageTwoOptions(final CharSequence title, final CharSequence mensaje,
                                              DialogInterface.OnClickListener viewActionPositive,
                                              DialogInterface.OnClickListener viewActionNegative){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(title);
        alertDialog.setMessage(mensaje);
        alertDialog.setIcon(R.mipmap.ic_launcher);
        EditText mEditText = new EditText(getActivity());
        alertDialog.setView(mEditText);

        alertDialog.setPositiveButton(R.string.st_general_aceptar, viewActionPositive);

        alertDialog.setNegativeButton(R.string.st_general_cancelar, viewActionNegative);

        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    protected DialogInterface.OnClickListener closeAlertDialog() throws Exception{

        return new android.content.DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        };
    }

    protected void changeActivityForward(Class classToGo, Bundle bundle) throws Exception{
        if(classToGo != null){
            Intent mIntent = new Intent(getActivity(), classToGo);
            if (bundle != null) {
                mIntent.putExtras(bundle);
            }
            ActivityOptions opts = ActivityOptions.makeCustomAnimation(
                    getActivity(), R.anim.entrada_derecha, R.anim.salida_izquierda);
            this.startActivity(mIntent, opts.toBundle());
        }
    }

    //Permite colocar la información del parámetro en la vista.
    protected  void setInfoView (View view, CharSequence info) throws Exception {
        if (view instanceof TextView){
            ((TextView) view).setText(info);
        }
    }

    /**
     * Método que retorna la fechaSorteo actual.
     */
    protected String getCurrentDate() throws InterruptedException {
        final Calendar c = Calendar.getInstance();
        Date fecha = c.getTime();
        return new SimpleDateFormat(UtilsConstants.FECHA.SIMPLE_DATE_FORMAT).format(fecha);
    }

    protected void saveDataInPreferences() throws Exception {
    }

    protected synchronized void vibrar() throws Exception {
        UtilsVibrate vibrate = UtilsVibrate.getInstance();
        vibrate.vibratePhone(getActivity());
    }

    protected synchronized void soundError() throws Exception {
        UtilsSound sound = UtilsSound.getInstance();
        sound.soundError(getActivity());
    }

    protected void soundOk() throws Exception {
        UtilsSound sound = UtilsSound.getInstance();
        sound.soundOk(getActivity());
    }

    protected synchronized void soundOkDos() throws Exception {
        UtilsSound sound = UtilsSound.getInstance();
        sound.soundOkDos(getActivity());
    }

    protected synchronized void soundTimeOutMin() throws Exception {
        UtilsSound sound = UtilsSound.getInstance();
        sound.soundTimeOutMin(getActivity());
    }

    protected synchronized void soundTimeOutMax() throws Exception {
        UtilsSound sound = UtilsSound.getInstance();
        sound.soundTimeOutMax(getActivity());
    }

    protected synchronized void showSnackbarAlert (String text) throws Exception{
        Snackbar.make(getActivity().findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    protected synchronized void hideKeyboard(EditText mEditText) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    public boolean isNotNull(Object object) {
        if (null != object) {
            return true;
        }
        return false;
    }
}
