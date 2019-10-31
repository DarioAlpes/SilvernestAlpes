package smartobjects.com.smobapp;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import smartobjects.com.smobapp.adapters.AdapterConciliar;
import smartobjects.com.smobapp.adapters.AdapterSkuItem;
import smartobjects.com.smobapp.objects.ObjectItem;
import smartobjects.com.smobapp.utils.BaseFragment;
import smartobjects.com.smobapp.utils.UtilsConstants;

public class FragmentConciliar extends Fragment {

    private RecyclerView recycler;
    private RecyclerView.LayoutManager lManager;
    public static RecyclerView.Adapter adapter;
    //Lista que recibimos del Bundle.
    ArrayList<ObjectItem> mListaItems = new ArrayList<>();

    public static FragmentConciliar newInstance(Bundle bundle) {
        FragmentConciliar fragment = new FragmentConciliar();
        if (null != bundle) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    public FragmentConciliar() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.activity_concicliar, container, false);

        recycler = (RecyclerView) rootView.findViewById(R.id.rc_activity_conciliar);
        recycler.setHasFixedSize(true);

        // Usar un administrador para LinearLayout
        lManager = new LinearLayoutManager(getActivity());
        recycler.setLayoutManager(lManager);
        Bundle bundle = getArguments();
        ArrayList<ObjectItem> listaItems = bundle.getParcelableArrayList(UtilsConstants.ITEM.BUNDLE_ITEM);
        // Crear un nuevo adaptador
//        adapter = new AdapterConciliar(ObjectItem.getmListaItemEsperado());
        adapter = new AdapterConciliar(listaItems);
        recycler.setAdapter(adapter);

//        ButterKnife.bind(getActivity());
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
//        updateInterface();
    }

    public void updateInterface(){
        //Validamos que el adapter ya exista.
        if (null != adapter){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//                    List<ObjectItem> lista = getItemsBySku();
//                    adapter.notifyDataSetChanged();
//                    recycler.setAdapter(null);
//                    List<ObjectItem> mListaItem = new ArrayList<>();
//                    adapter = new AdapterConciliar(ObjectItem.getmListaItemEsperado());
//                    recycler.setAdapter(adapter);
//                    updateInterface();
                }
            }, 1000);
        }
    }

}
