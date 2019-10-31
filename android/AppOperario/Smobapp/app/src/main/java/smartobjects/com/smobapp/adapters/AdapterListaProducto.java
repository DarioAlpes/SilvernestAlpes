package smartobjects.com.smobapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import smartobjects.com.smobapp.R;
import smartobjects.com.smobapp.objects.ObjectProducto;

/**
 * Created by Andres Rubiano on 23/10/2015.
 */
public class AdapterListaProducto extends ArrayAdapter<ObjectProducto> {

    private ArrayList<ObjectProducto> historial = null;
    private ViewHolder holder = null;
    private Context mContext;

    public AdapterListaProducto(Context mContext,
                                int p,
                                ArrayList<ObjectProducto> historial){
        super(mContext, p, historial);
        this.historial = new ArrayList<ObjectProducto>();
        this.historial = historial;
        this.mContext = mContext;
    }

    private class ViewHolder{
        private TextView tvSku = null, tvId = null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        holder = null;

        if(convertView == null){
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.two_row_layout, null);

            holder = new ViewHolder();

            holder.tvId  = (TextView) convertView.findViewById(R.id.tv_second_row);
            holder.tvSku = (TextView) convertView.findViewById(R.id.tv_first_row);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ObjectProducto historial = this.historial.get(position);
        holder = (ViewHolder) convertView.getTag();

        holder.tvSku.setText("SKU : " + historial.getSKU());
        holder.tvId.setText( "Id  : " + historial.getId());

        //this.notifyDataSetChanged();

        return convertView;
    }


}