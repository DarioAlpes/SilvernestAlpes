package smartobjects.com.smobapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import smartobjects.com.smobapp.R;
import smartobjects.com.smobapp.objects.ObjectItem;

/**
 * Created by Andres Rubiano on 23/10/2015.
 */
public class AdapterListaItem extends ArrayAdapter<ObjectItem> {

    private ArrayList<ObjectItem> historial = null;
    private ViewHolder holder = null;
    private Context mContext;

    public AdapterListaItem(Context mContext,
                            int p,
                            ArrayList<ObjectItem> historial){
        super(mContext, p, historial);
        this.historial = new ArrayList<ObjectItem>();
        this.historial = historial;
        this.mContext = mContext;
    }

    private class ViewHolder{
        private TextView tvId = null, tvSku = null, tvUbicacion = null, tvEpc = null, tvEstado =  null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        holder = null;

        if(convertView == null){
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.five_row_layout, null);

            holder = new ViewHolder();

            holder.tvId  = (TextView) convertView.findViewById(R.id.tv_second_row);
            holder.tvSku = (TextView) convertView.findViewById(R.id.tv_first_row);
            holder.tvEpc  = (TextView) convertView.findViewById(R.id.tv_third_row);
            holder.tvUbicacion = (TextView) convertView.findViewById(R.id.tv_fourth_row);
            holder.tvEstado  = (TextView) convertView.findViewById(R.id.tv_fifth_row);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ObjectItem historial = this.historial.get(position);
        holder = (ViewHolder) convertView.getTag();

        holder.tvSku.setText("SKU : " + historial.getSKU());
        holder.tvId.setText( "Id  : " + historial.getId());
        holder.tvUbicacion.setText("Ubicación : " + historial.getUbicacion());
        holder.tvEpc.setText( "EPC : " + historial.getEpc());
        holder.tvEstado.setText("Estado : " + historial.getEstado());

        return convertView;
    }


}