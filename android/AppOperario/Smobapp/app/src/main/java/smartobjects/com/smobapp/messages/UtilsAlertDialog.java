package smartobjects.com.smobapp.messages;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import smartobjects.com.smobapp.R;
import smartobjects.com.smobapp.utils.UtilsConstants;

/**
 * Created by Andres Rubiano on 11/11/2015.
 */
public class UtilsAlertDialog {

    public static UtilsAlertDialog mAlertDialog = null;

    public UtilsAlertDialog () { }

    public static UtilsAlertDialog newInstance() {
        if (mAlertDialog == null) {
            mAlertDialog = new UtilsAlertDialog();
        }
        return mAlertDialog;
    }

    public void showAlertWithOneButton(Context context, CharSequence title, CharSequence message)
                                        throws NullPointerException {
        if (context != null) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle(title);
            alertDialog.setMessage(message);
            alertDialog.setIcon(R.mipmap.ic_launcher);

            alertDialog.setPositiveButton(R.string.st_general_aceptar,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            alertDialog.show();
        } else {
            Log.e(UtilsConstants.GENERAL.LOG_ERROR, "Contexto nulo al intentar mostrar un " +
                    "Alert Dialog.");
        }
    }

    public void showAlertMessageTwoOptions(Context context, final CharSequence title, final CharSequence mensaje,
                                           DialogInterface.OnClickListener viewActionPositive){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(mensaje);
        alertDialog.setIcon(R.mipmap.ic_launcher);

        alertDialog.setPositiveButton(R.string.st_general_aceptar, viewActionPositive);

        AlertDialog alert = alertDialog.create();
        alert.show();
    }
}
