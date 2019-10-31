package smartobjects.com.smobapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import smartobjects.com.smobapp.R;
import smartobjects.com.smobapp.objects.ObjectMenu;

/**
 * Created by Andres Rubiano on 16/02/2016.
 */
public class AdapterMenu extends BaseAdapter {
    private Context context;

    public AdapterMenu(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return ObjectMenu.ITEMS.length;
    }

    @Override
    public ObjectMenu getItem(int position) {
        return ObjectMenu.ITEMS[position];
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getmId();
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.grid_menu, viewGroup, false);
        }

        TextView nombreCoche = (TextView) view.findViewById(R.id.nombre_menu);

        final ObjectMenu item = getItem(position);
        nombreCoche.setText(item.getmNombre());

        return view;
    }

}
