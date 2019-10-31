package smartobjects.com.smobapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.List;

import smartobjects.com.smobapp.R;
import smartobjects.com.smobapp.objects.ObjectItem;

/**
 * Created by Andres Rubiano on 18/02/2016.
 */
public class AdapterSKUItemDetail extends RecyclerView.Adapter<AdapterSKUItemDetail.AnimeViewHolder>
                        implements RecyclerView.OnItemTouchListener{

    private List<ObjectItem> items;
    private OnItemClickListener mListener;

    GestureDetector mGestureDetector;

    public AdapterSKUItemDetail() {
    }

    public AdapterSKUItemDetail(Context context, OnItemClickListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }



    public static class AnimeViewHolder extends RecyclerView.ViewHolder {
        // Campos respectivos de un item
        public TextView sku;
        public TextView epc;
        public TextView estatus;



        public AnimeViewHolder(View v) {
            super(v);
            sku    = (TextView) v.findViewById(R.id.tv_adapter_sku_detail_sku);
            epc    = (TextView) v.findViewById(R.id.tv_adapter_sku_detail_epc);
            estatus = (TextView) v.findViewById(R.id.tv_adapter_sku_detail_estatus);
        }
    }

    public AdapterSKUItemDetail(List<ObjectItem> items) {
        this.items = items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public AnimeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_items_sku_details, viewGroup, false);
        return new AnimeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final AnimeViewHolder viewHolder, final int i) {
        viewHolder.sku.setText(items.get(i).getSKU());
        viewHolder.epc.setText(items.get(i).getEpc());
        String estatus = ObjectItem.getEstatus(items.get(i).getEstatus());
        viewHolder.estatus.setText(estatus);
    }

}

