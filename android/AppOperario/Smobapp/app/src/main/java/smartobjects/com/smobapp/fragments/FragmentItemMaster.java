package smartobjects.com.smobapp.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import smartobjects.com.smobapp.R;
import smartobjects.com.smobapp.customViews.CustomDialogSku;
import smartobjects.com.smobapp.objects.ObjectItem;
import smartobjects.com.smobapp.sounds.UtilsSound;
import smartobjects.com.smobapp.utils.UtilsConstants;

/**
 * Created by Andres Rubiano on 02/03/2016.
 * <p/>
 * Fragment que se encarga de colocar la información más relevante de los items a buscar
 * Se recibe por parámetro, la información del Bundle que deberá ser parseada para poder ser usada
 */
public class FragmentItemMaster extends Fragment {

    private int ChildClickStatus = -1;
    private int ParentClickStatus = -1;
    //Adapter que se asocia a la lista expandible
    MyExpandableListAdapter mAdapter;
    //Lista limpia de la información a mostrar.
    ArrayList<ObjectItem> listaLimpia = new ArrayList<>();
    //Lista que recibimos del Bundle.
    ArrayList<ObjectItem> mListaItems = new ArrayList<>();
    //Lista expandible que mostrará la información de los EPC
    private ExpandableListView mElvSku;


    //Usamos el patrón Singleton para la instancia del Fragment
    public static FragmentItemMaster newInstance(Bundle bundle) {
        FragmentItemMaster fragment = new FragmentItemMaster();
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    public FragmentItemMaster() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_adapter_item_sku, container, false);

        mElvSku = (ExpandableListView) rootView.findViewById(R.id.elv_lista_sku);

        mElvSku.setGroupIndicator(null);
        mElvSku.setDividerHeight(1);
        Bundle bundle = getArguments();
        //Creating static data in arraylist
        ArrayList<ObjectItem> listaItems = bundle.getParcelableArrayList(UtilsConstants.ITEM.BUNDLE_ITEM);
        ArrayList<ObjectItem> dummyList = getItemsBySku(listaItems);
        // Adding ArrayList data to ExpandableListView values
        loadHosts(dummyList);
        return rootView;
    }


//    private ArrayList<ObjectItem> buildDummyData() {
//        // Creating ArrayList of type parent class to store parent class objects
//        return getItemsBySku();
//    }


    private void loadHosts(final ArrayList<ObjectItem> newParents) {
        if (newParents == null)
            return;

        // Check for ExpandableListAdapter object
        if (mAdapter == null) {
            //Create ExpandableListAdapter Object
            mAdapter = new MyExpandableListAdapter(newParents, FragmentItemMaster.this.getActivity());

            // Set Adapter to ExpandableList Adapter
//            mElvSku.setListAdapter(mAdapter);
            mElvSku.setAdapter(mAdapter);
        } else {
            // Refresh ExpandableListView data
//            ((MyExpandableListAdapter) getExpandableListAdapter()).notifyDataSetChanged();
//            ((MyExpandableListAdapter) mElvSku.getExpandableListAdapter()).notifyDataSetChanged();
//            mAdapter.notifyDataSetChanged();
        }
    }

    private ArrayList<ObjectItem> getItemsBySku(ArrayList<ObjectItem> item) {
//        List<ObjectItem> item = ObjectItem.getmListaItem();
        listaLimpia.clear();

        //Forma número 1 (Uso de Maps).
        Map<String, ObjectItem> mapPersonas = new HashMap<String, ObjectItem>(ObjectItem.getmListaItem().size());

        //Aquí está la magia
        for (ObjectItem p : item) {
            mapPersonas.put(p.getSKU(), p);
        }

        for (Map.Entry<String, ObjectItem> p : mapPersonas.entrySet()) {
            listaLimpia.add(p.getValue());
        }

        HashMap<String, Integer> resumenEncontrados = new HashMap<String, Integer>();
        String[] listaSku = new String[listaLimpia.size()];
        for (int lista = 0; lista < listaLimpia.size(); lista++) {
            listaSku[lista] = listaLimpia.get(lista).getSKU();
        }

        for (String sku : listaSku) {
            int cantidad = 0;
            for (ObjectItem itemUsado : item) {
                if (sku.equals(itemUsado.getSKU())) {
                    if (itemUsado.getEstatus() == ObjectItem.ESTADO_ENCONTRADO) {
                        cantidad++;
                    }
                }
            }
            resumenEncontrados.put(sku, cantidad);
        }

        for (Map.Entry<String, Integer> dato : resumenEncontrados.entrySet()) {
            for (ObjectItem ultimoItem : listaLimpia) {
                if (dato.getKey().equals(ultimoItem.getSKU())) {
                    ultimoItem.setItemEncontrados(dato.getValue());
                }
            }
        }

        ArrayList<ObjectItem> listaOrdenados = new ArrayList<>();
        for (ObjectItem itemPendientes : listaLimpia) {
            int encontrados = itemPendientes.getItemEncontrados();
            int esperados = itemPendientes.getItemEsperados();
            if (encontrados < esperados) {
                listaOrdenados.add(itemPendientes);
            }
        }

        for (ObjectItem itemPendientes : listaLimpia) {
            int encontrados = itemPendientes.getItemEncontrados();
            int esperados = itemPendientes.getItemEsperados();
            if (encontrados == esperados) {
                listaOrdenados.add(itemPendientes);
            }
        }
        return listaOrdenados;
    }

    private class MyExpandableListAdapter extends BaseExpandableListAdapter {
        private ArrayList<ObjectItem> mListaItems = null;

        private Activity mActivity;

        public MyExpandableListAdapter(ArrayList<ObjectItem> lista, Activity activity) {
            // Create Layout Inflator
            this.mActivity = activity;
            this.mListaItems = lista;
        }


        // This Function used to inflate parent rows view
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parentView) {
            ObjectItem parent = mListaItems.get(groupPosition);
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(this.mActivity.LAYOUT_INFLATER_SERVICE);
            // Inflate grouprow.xml file for parent rows
            convertView = vi.inflate(R.layout.adapter_items_epc, parentView, false);

            // Get grouprow.xml file elements and set values
            ((TextView) convertView.findViewById(R.id.tv_adapter_item_sku)).setText(parent.getSKU());
//            ((TextView) convertView.findViewById(R.id.tv_adapter_item_estatus)).setText(parent.getSKU());
            int itemEncontrados = parent.getItemEncontrados();
            int itemEsperados = parent.getItemEsperados();
            ((TextView) convertView.findViewById(R.id.tv_adapter_item_estatus)).setText(String.valueOf(itemEncontrados) +
                    "/" + String.valueOf(itemEsperados));

            ImageView ivCheck = (ImageView) convertView.findViewById(R.id.iv_adapter_item_check);
            if (itemEncontrados == itemEsperados) {
                ivCheck.setVisibility(View.VISIBLE);
                if (!parent.isSkuCompleto()) {
//                    UtilsSound classSound = UtilsSound.getInstance();
//                    classSound.soundOk(getActivity());
                    ObjectItem.setSkuCompleto(parent);
                }
            }
            return convertView;
        }


        // This Function used to inflate child rows view
        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parentView) {
//            ObjectItem parent = mListaItems.get(groupPosition);
            ObjectItem parent = mListaItems.get(groupPosition);
            final ObjectItem parentClickListener = parent;
//            ObjectItem child = parent.getChildren().get(childPosition);
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(this.mActivity.LAYOUT_INFLATER_SERVICE);
            // Inflate childrow.xml file for child rows
            convertView = vi.inflate(R.layout.adapter_items_sku, parentView, false);

            // Get childrow.xml file elements and set values
            ((TextView) convertView.findViewById(R.id.tv_adapter_sku)).setText(parent.getSKU());
            ((TextView) convertView.findViewById(R.id.tv_adapter_talla)).setText(parent.getTalla());
            ((TextView) convertView.findViewById(R.id.tv_adapter_nombre)).setText(parent.getNombre());
            ImageView image = (ImageView) convertView.findViewById(R.id.iv_adapter_item_sku);
            if (null != image) {
                Picasso.with(this.mActivity).load(parent.getRutaImagen()).into(image);
            }
            RelativeLayout container = (RelativeLayout) convertView.findViewById(R.id.rl_adapter_items_sku);
            if (null != container) {
                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        ObjectItem item = (ObjectItem) parentClickListener;
                        CustomDialogSku skuDialog = new CustomDialogSku(getActivity(), parentClickListener);
                        skuDialog.show();
                    }
                });
            }

            return convertView;
        }


        @Override
        public Object getChild(int groupPosition, int childPosition) {
            //Log.i("Childs", groupPosition+"=  getChild =="+childPosition);
            return mListaItems.get(groupPosition).getChildren().get(childPosition);
        }

        //Call when child row clicked
        @Override
        public long getChildId(int groupPosition, int childPosition) {
            /****** When Child row clicked then this function call *******/

            //Log.i("Noise", "parent == "+groupPosition+"=  child : =="+childPosition);
            if (ChildClickStatus != childPosition) {
                ChildClickStatus = childPosition;
            }

            return childPosition;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }


        @Override
        public Object getGroup(int groupPosition) {
            return mListaItems.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return mListaItems.size();
        }

        //Call when parent row clicked
        @Override
        public long getGroupId(int groupPosition) {
//            Log.i("Parent", groupPosition+"=  getGroupId "+ParentClickStatus);

            if (groupPosition == 2 && ParentClickStatus != groupPosition) {

                //Alert to user
//                Toast.makeText(getApplicationContext(), "Parent :"+groupPosition ,
//                        Toast.LENGTH_LONG).show();
            }

            ParentClickStatus = groupPosition;
            if (ParentClickStatus == 0)
                ParentClickStatus = -1;

            return groupPosition;
        }

        @Override
        public void notifyDataSetChanged() {
            // Refresh List rows
            super.notifyDataSetChanged();
        }

        @Override
        public boolean isEmpty() {
            return ((mListaItems == null) || mListaItems.isEmpty());
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }
    }
}

