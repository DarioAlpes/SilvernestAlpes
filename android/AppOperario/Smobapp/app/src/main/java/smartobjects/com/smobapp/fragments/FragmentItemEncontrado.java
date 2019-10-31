package smartobjects.com.smobapp.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import smartobjects.com.smobapp.R;
import smartobjects.com.smobapp.adapters.AdapterEPCItem;
import smartobjects.com.smobapp.objects.ObjectItem;

public class FragmentItemEncontrado extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private RecyclerView recycler;
    private RecyclerView.LayoutManager lManager;
    public static RecyclerView.Adapter adapter;

    public static FragmentItemEncontrado newInstance(Bundle bundle) {
        FragmentItemEncontrado fragment = new FragmentItemEncontrado();
        if (null != bundle) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    public FragmentItemEncontrado() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_adapter_item_epc, container, false);

// Obtener el Recycler
        recycler = (RecyclerView) rootView.findViewById(R.id.rc_adapter_epc);
        recycler.setHasFixedSize(true);

// Usar un administrador para LinearLayout
        lManager = new LinearLayoutManager(getActivity());
        recycler.setLayoutManager(lManager);

        // Crear un nuevo adaptador
        adapter = new AdapterEPCItem(ObjectItem.getmListaItemEncontrados());

        recycler.setAdapter(adapter);
        return rootView;
    }

    /**
     * Permite actualizar los componentes de la interfaz
     */
    public synchronized void updateInterface() {
        //Validamos que el adapter ya exista.
        if (null != adapter) {
            Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (null != adapter) {
//                        adapter.notifyDataSetChanged();
                        ArrayList<ObjectItem> data = new ArrayList<>();
                        adapter = new AdapterEPCItem(ObjectItem.getmListaItemEncontrados());
                        recycler.setAdapter(adapter);
//                        updateInterface();
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
            updateInterface();
//            Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    ArrayList<ObjectItem> data = new ArrayList<>();
//                    data.add(new ObjectItem("2097935529", "Jean",     "30",  9, 0,  "https://s3-eu-west-1.amazonaws.com/iouproyect/photos/items/a1I20000000cYb9EAEphoto.jpg",              1, "E2801160600002050790B859"));
//                    adapter = new AdapterEPCItem(data);
//                    recycler.setAdapter(adapter);
//                }
//            }, 2000);da
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }

}
