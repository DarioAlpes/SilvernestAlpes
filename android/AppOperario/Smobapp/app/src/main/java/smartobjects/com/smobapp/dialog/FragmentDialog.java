package smartobjects.com.smobapp.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import smartobjects.com.smobapp.R;

/**
 * Created by Andres Rubiano on 15/12/2015.
  Clase que permite mostrar una pantalla al usuario final.
  Se debe llamar de la siguiente manera:
           FragmentDialog dialog = new FragmentDialog();
            FragmentManager fragmentManager = getSupportFragmentManager();
            dialog.show(fragmentManager, "tagPersonalizado");
 */
public class FragmentDialog extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.fragment_progress_bar,container);
        return view;
    }
}
