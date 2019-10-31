package smartobjects.com.smobapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import smartobjects.com.smobapp.connectivity.MainThread;
import smartobjects.com.smobapp.objects.ObjectItem;
import smartobjects.com.smobapp.objects.ObjectProducto;
import smartobjects.com.smobapp.utils.BaseActivity;
import smartobjects.com.smobapp.utils.UtilsConstants;

public class ActivityListarItem extends BaseActivity
        implements AdapterView.OnItemClickListener{

    private MainThread mHilo;
    ListView mListProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_item);

        mListProduct = (ListView) findViewById(R.id.lv_item_list);
        mListProduct.setOnItemClickListener(this);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        mHilo = new MainThread(this,
                UtilsConstants.PROGRESS_DIALOG.DOWNLOAD_ITEMS,
                mListProduct);
        mHilo.execute(UtilsConstants.ITEM.JSON_LIST_ITEM);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mHilo)
            mHilo.cancel(true);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            ObjectItem item = ObjectItem.getItem(position);
            Bundle bundle = new Bundle();
            Intent intent = new Intent(ActivityListarItem.this, ActivityProductDetail.class);
            intent.putExtra(UtilsConstants.ITEM.BUNDLE_SKU, item.getSKU());
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
