package smartobjects.com.smobapp.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import smartobjects.com.smobapp.R;
import smartobjects.com.smobapp.objects.ObjectItem;

/**
 * Created by Andres Rubiano on 18/02/2016.
 */
public class AdapterEPCItem extends RecyclerView.Adapter<AdapterEPCItem.AnimeViewHolder>
                        implements View.OnClickListener{
    private List<ObjectItem> items;
    private View.OnClickListener listener;

    public static class AnimeViewHolder extends RecyclerView.ViewHolder {
        // Campos respectivos de un item
        public TextView sku;
//        public TextView epc;
        public TextView estatus;
        public CardView cardView;

        public AnimeViewHolder(View v) {
            super(v);
            sku      = (TextView) v.findViewById(R.id.tv_adapter_item_sku);
//            epc      = (TextView) v.findViewById(R.id.tv_adapter_item_epc);
            estatus  = (TextView) v.findViewById(R.id.tv_adapter_item_estatus);
        }
    }

    public AdapterEPCItem(List<ObjectItem> items) {
        this.items = items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public AnimeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_items_epc, viewGroup, false);
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
        viewHolder.sku.setText(items.get(i).getSKU());
//        viewHolder.epc.setText(items.get(i).getEpc());
        String estatus = ObjectItem.getEstatus(items.get(i).getEstatus());
        viewHolder.estatus.setText(estatus);
//        changeCardView(viewHolder.cardView, items.get(i).getEstatus());
    }

    private void changeCardView(CardView cardView, int estatus) {
        if (null != cardView){
            switch (estatus){
                case ObjectItem.ESTADO_ESPERANDO :
//                        cardView.setCardBackgroundColor(R.color.red_color_dark);
                    break;

                case ObjectItem.ESTADO_ENCONTRADO :
                        cardView.setCardBackgroundColor(R.color.red_color_dark);
//                        cardView.setCardBackgroundColor(R.color.green_color);
                    break;
            }
        }
    }
}

