package smartobjects.com.smobapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import smartobjects.com.smobapp.connectivity.MainThread;
import smartobjects.com.smobapp.utils.BaseActivity;
import smartobjects.com.smobapp.utils.UtilsConstants;

public class ActivityItemDetail extends BaseActivity {

    @Bind(R.id.tv_det_pro_id)
    TextView mEtProductoId;

    @Bind(R.id.tv_pro_det_epc)
    TextView mEtProductoEpc;

    @Bind(R.id.tv_pro_det_estado)
    TextView mEtProductoEstado;

    @Bind(R.id.tv_pro_det_sku)
    TextView mEtProductoSku;

    @Bind(R.id.tv_pro_det_ubicacion)
    TextView mEtProductoUbicacion;

    private MainThread mHilo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        ButterKnife.bind(this);

        try {
            getData(getIntent().getExtras());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getData(Bundle extras) throws Exception {
        if (null != extras){
            int id = extras.getInt(UtilsConstants.PRODUCTO.BUNDLE_ID);
            String sku = extras.getString(UtilsConstants.PRODUCTO.BUNDLE_SKU);
            setData(String.valueOf(id), sku, "", "", "");

            mHilo = new MainThread(this, UtilsConstants.PROGRESS_DIALOG.DOWNLOAD_PRODUCTS_DETAIL);
            mHilo.execute(UtilsConstants.PRODUCTO.JSON_DETAIL_PRODUCT, String.valueOf(id), sku);
        }
    }

    public void setData(String id, String sku, String epc, String estado, String ubicacion) throws NullPointerException {
        if (isNotNull(mEtProductoId)){
            mEtProductoId.setText(id);
        }
        if (isNotNull(mEtProductoSku)){
            mEtProductoSku.setText(sku);
        }
        if (isNotNull(mEtProductoEpc)){
            mEtProductoEpc.setText(epc);
        }
        if (isNotNull(mEtProductoEstado)){
            mEtProductoEstado.setText(estado);
        }
        if (isNotNull(mEtProductoUbicacion)){
            mEtProductoUbicacion.setText(ubicacion);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHilo.cancel(true);
    }
}
