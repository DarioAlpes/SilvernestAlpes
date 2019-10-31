package smartobjects.com.smobapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import smartobjects.com.smobapp.R;
import smartobjects.com.smobapp.objects.ObjectItem;

/**
 * Created by Andres Rubiano on 18/02/2016.
 */
public class AdapterSkuItem extends RecyclerView.Adapter<AdapterSkuItem.AnimeViewHolder>
                        implements View.OnClickListener{
    private List<ObjectItem> items;
    private View.OnClickListener listener;
    private Context mContext;
    public static final int THREE = 3;
    public static final int FOUR  = 4;

    public static class AnimeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // Campos respectivos de un item
        public TextView sku;
        public TextView nombre;
        public TextView talla;
        public TextView cantidadItems;
        public ImageView imagenSku;
//        public ImageView imagenEstatus;



        public AnimeViewHolder(View v) {
            super(v);
            sku    = (TextView) v.findViewById(R.id.tv_adapter_sku);
            nombre = (TextView) v.findViewById(R.id.tv_adapter_nombre);
            talla  = (TextView) v.findViewById(R.id.tv_adapter_talla);
//            cantidadItems = (TextView) v.findViewById(R.id.tv_adapter_conteo);

//            imagenEstatus = (ImageView) v.findViewById(R.id.iv_adapter_item_estatus);
            imagenSku     = (ImageView) v.findViewById(R.id.iv_adapter_item_sku);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
//            Log.d("adapter", "onClick " + getPosition() + " " + mItem);
        }
    }

    public AdapterSkuItem(Context context, List<ObjectItem> items) {
        this.items = items;
        this.mContext = context;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public AnimeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_items_sku, viewGroup, false);
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
    public void onBindViewHolder(final AnimeViewHolder viewHolder, final int i) {
//        viewHolder.imagenSku.setImageURI(items.get(i).getRutaImagen());
        viewHolder.sku.setText(items.get(i).getSKU());
        viewHolder.nombre.setText("Tipo : " + items.get(i).getNombre());
        viewHolder.talla.setText("Talla : " + items.get(i).getTalla());
        int itemEncontrados = items.get(i).getItemEncontrados();
        int itemEsperados = items.get(i).getItemEsperados();
        viewHolder.cantidadItems.setText(String.valueOf(itemEncontrados) +
                "/" + String.valueOf(itemEsperados));
//        if (itemEncontrados == itemEsperados) {
//            viewHolder.imagenEstatus.setImageResource(android.R.drawable.checkbox_on_background);
//        }
        String urlImagen = items.get(i).getRutaImagen();
        Picasso.with(this.mContext).load(urlImagen).into(viewHolder.imagenSku);
    }

}

