package smartobjects.com.smobapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import smartobjects.com.smobapp.R;
import smartobjects.com.smobapp.utils.BaseFragment;


/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentProgressBar extends BaseFragment {

    public FragmentProgressBar() {
    }

    public static FragmentProgressBar newInstance (Bundle arguments){
        FragmentProgressBar fragment = new FragmentProgressBar();
        if(arguments != null) {
            fragment.setArguments(arguments);
        }
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //setContentView(R.layout.fragment_historial);
        //Colocamos que la orientación por defecto que sea horizontal
        View v = inflater.inflate(R.layout.fragment_progress_bar, container, false);
        return v;
    }

}
