package smartobjects.com.smobapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import smartobjects.com.smobapp.connectivity.MainThread;
import smartobjects.com.smobapp.objects.ObjectProducto;
import smartobjects.com.smobapp.utils.BaseActivity;
import smartobjects.com.smobapp.utils.UtilsConstants;

public class ActivityListProduct extends BaseActivity
        implements AdapterView.OnItemClickListener{

    private List<String> listValues;
    private MainThread mHilo;
    ListView mListProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        mListProduct = (ListView) findViewById(R.id.lv_product_list);
        mListProduct.setOnItemClickListener(this);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        mHilo = new MainThread(this,
                                    UtilsConstants.PROGRESS_DIALOG.DOWNLOAD_PRODUCTS,
                                    mListProduct);
        mHilo.execute(UtilsConstants.PRODUCTO.JSON_LIST_PRODUCT);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        try {
            ObjectProducto producto = ObjectProducto.getProducto(position);
            Bundle bundle = new Bundle();
            Intent intent = new Intent(ActivityListProduct.this, ActivityProductDetail.class);
            intent.putExtra(UtilsConstants.PRODUCTO.BUNDLE_ID, producto.getId());
            intent.putExtra(UtilsConstants.PRODUCTO.BUNDLE_SKU, producto.getSKU());
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mHilo)
            mHilo.cancel(true);
    }

}
