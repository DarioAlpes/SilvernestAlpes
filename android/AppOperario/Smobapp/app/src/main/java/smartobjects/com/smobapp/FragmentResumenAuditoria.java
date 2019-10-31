package smartobjects.com.smobapp;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import smartobjects.com.smobapp.adapters.AdapterConciliar;
import smartobjects.com.smobapp.objects.ObjectItem;
import smartobjects.com.smobapp.utils.BaseActivity;
import smartobjects.com.smobapp.utils.BaseFragment;
import smartobjects.com.smobapp.utils.UtilsConstants;

public class FragmentResumenAuditoria extends BaseFragment {

    OnCheckListener mCallback;

    @Bind(R.id.tv_resumen_auditoria_esperados)
    TextView mTvEsperados = null;

    @Bind(R.id.tv_resumen_auditoria_daniados)
    TextView mTvDaniados = null;

    @Bind(R.id.tv_resumen_auditoria_encontrados)
    TextView mTvEncontrados = null;

    @Bind(R.id.tv_resumen_auditoria_perdidos)
    TextView mTvPerdidos = null;

    @Bind(R.id.cb_resumen_aprobar)
    CheckBox mCbAprobar = null;

    ArrayList<ObjectItem> mListaItems = new ArrayList<>();


    public FragmentResumenAuditoria() {
    }

    public static FragmentResumenAuditoria newInstance(Bundle bundle) {
        FragmentResumenAuditoria fragment = new FragmentResumenAuditoria();
        if (null != bundle){
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    public interface OnCheckListener{
        public void changeAprobar(boolean state);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnCheckListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.activity_resumen_auditoria, container, false);

        ButterKnife.bind(this, rootView);

        Bundle bundle = getArguments();
        mListaItems = bundle.getParcelableArrayList(UtilsConstants.ITEM.BUNDLE_ITEM);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                putInfo();
            }
        });
    }

    public void putInfo() {
        infoEncontrados();
        infoEsperados();
        infoPerdidos();
        infoDaniados();
    }

    private void infoDaniados() {
        if (isNotNull(mTvDaniados)){
            try {
                mTvDaniados.setText(String.valueOf(getTamanioTipo(ObjectItem.ESTADO_TAG_DAÑADO)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void infoPerdidos() {
        if (isNotNull(mTvPerdidos)){
            try {
                mTvPerdidos.setText(String.valueOf(getmListaItemPerdidos()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void infoEsperados() {
        if (isNotNull(mTvEsperados)){
            mTvEsperados.setText(String.valueOf(FragmentResumenAuditoria.this.mListaItems.size()));
        }
    }

    private void infoEncontrados() {
        if (isNotNull(mTvEncontrados)){
            try {
                mTvEncontrados.setText(String.valueOf(getTamanioTipo(ObjectItem.ESTADO_ENCONTRADO)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Método que se encarga de cambiar el estado del checkBox seleccionado.
     * @param checked : Estado del Checkbox seleccionado.
     */
    @OnCheckedChanged(R.id.cb_resumen_aprobar)
    public void onChecked(boolean checked) {
        //Llama al método de la interfaz para comunicarse con la actividad.
        mCallback.changeAprobar(checked);
    }


    @Override
    public void onDestroyView(){
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public int getTamanioTipo(int tipo) throws Exception {
        int tamanio = 0;
        if (isNotNull(mListaItems) && mListaItems.size() > 0){
            ArrayList<ObjectItem> itemEncontrados = new ArrayList<>();

            for (ObjectItem item : mListaItems){
                if (item.getEstatus() == tipo){
                    itemEncontrados.add(item);
                }
            }
            tamanio = itemEncontrados.size();
        }
        return tamanio;
    }

    public synchronized int getmListaItemPerdidos() {
//        if (mListaItem.size() == 0) {
//            fillListaItem();
//        }
        ArrayList<ObjectItem> itemEncontrados = new ArrayList<>();

        for (ObjectItem item : this.mListaItems){
            if (item.getEstatus() == ObjectItem.ESTADO_NO_ESTA || item.getEstatus() == ObjectItem.ESTADO_ESPERANDO){
                itemEncontrados.add(item);
            }
        }
        return itemEncontrados.size();
    }

}
