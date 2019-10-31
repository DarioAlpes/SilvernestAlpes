package smartobjects.com.smobapp.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import smartobjects.com.smobapp.ActivityAuditoria;
import smartobjects.com.smobapp.R;
import smartobjects.com.smobapp.objects.ObjectItem;

/**
 * Created by Andres Rubiano on 18/02/2016.
 */
public class AdapterConciliar extends RecyclerView.Adapter<AdapterConciliar.AnimeViewHolder>
        implements View.OnClickListener {
    private List<ObjectItem> items;
    private View.OnClickListener listener;

    public static class AnimeViewHolder extends RecyclerView.ViewHolder {
        // Campos respectivos de un item
//        public TextView sku;
        public TextView epc;
        public Switch mSwAccion;


        public AnimeViewHolder(View v) {
            super(v);
//            sku    = (TextView) v.findViewById(R.id.tv_apater_item_sku);
            epc = (TextView) v.findViewById(R.id.tv_apater_item_epc);
            mSwAccion = (Switch) v.findViewById(R.id.sw_apater_item_conciliar);
        }
    }

    public AdapterConciliar(List<ObjectItem> items) {
        this.items = items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public AnimeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_items_conciliar, viewGroup, false);
        v.setOnClickListener(this);
        return new AnimeViewHolder(v);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        if (listener != null)
            listener.onClick(view);
    }

    @Override
    public void onBindViewHolder(AnimeViewHolder viewHolder, int i) {
//        viewHolder.sku.setText(items.get(i).getSKU());
        viewHolder.epc.setText(items.get(i).getEpc());
        viewHolder.mSwAccion.setChecked(items.get(i).getEstatus() == ObjectItem.ESTADO_TAG_DAÑADO ? true : false);
        final int pos = i;
        final ObjectItem item = items.get(pos);
        viewHolder.mSwAccion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    if (null != this) {
                        if (isChecked){
                            ObjectItem.changeEstatusItem(item, pos, ObjectItem.ESTADO_TAG_DAÑADO);
                        } else {
                            ObjectItem.changeEstatusItem(item, pos, ObjectItem.ESTADO_NO_ESTA);
                        }
                    }
                }
            }
        });
    }
}

