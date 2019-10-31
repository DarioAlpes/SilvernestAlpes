package smartobjects.com.smobapp.customViews;

import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import smartobjects.com.smobapp.ActivityAuditoria;
import smartobjects.com.smobapp.R;
import smartobjects.com.smobapp.adapters.AdapterSKUItemDetail;
import smartobjects.com.smobapp.objects.ObjectItem;

/**
 * Created by Andres Rubiano on 30/10/2015.
 */
public class CustomDialogSku extends Dialog implements View.OnClickListener
{
    private Button okButton = null;
    private TextView infoText=null, confirmBody=null;
    private int errorMessage=0;
    private Activity activity;

    private RecyclerView recycler;
    private RecyclerView.LayoutManager lManager;
    private ObjectItem mObject;

    public CustomDialogSku(ActivityAuditoria context, ObjectItem item) {
        super(context);
        this.mObject = item;
        initData(context);
    }

    public CustomDialogSku(Activity context, ObjectItem item) {
        super(context);
        this.mObject = item;
        initData(context);
    }

    private void initData(Activity context) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_adapter_item_sku_details);
        this.activity = context;

        recycler = (RecyclerView) findViewById(R.id.rc_adapter_sku_details);
        recycler.setHasFixedSize(true);

        // Usar un administrador para LinearLayout
        lManager = new LinearLayoutManager(activity);
        recycler.setLayoutManager(lManager);

        // Crear un nuevo adaptador
        List<ObjectItem> array = new ArrayList<>();
        for (ObjectItem items : ObjectItem.getmListaItem()){
            if (items.getSKU().equals(this.mObject.getSKU())){
                array.add(items);
            }
        }

        List<ObjectItem> listaFinal = new ArrayList<>();
        for(ObjectItem itemFinal : array){
            if (itemFinal.getEstatus() ==  ObjectItem.ESTADO_ESPERANDO){
                listaFinal.add(itemFinal);
            }
        }

        for(ObjectItem itemFinal : array){
            if (itemFinal.getEstatus() ==  ObjectItem.ESTADO_ENCONTRADO){
                listaFinal.add(itemFinal);
            }
        }

        final RecyclerView.Adapter adapter = new AdapterSKUItemDetail(listaFinal);
        recycler.setAdapter(adapter);
    }

    public void onClick(View v)
    {
        dismiss();
    }
}