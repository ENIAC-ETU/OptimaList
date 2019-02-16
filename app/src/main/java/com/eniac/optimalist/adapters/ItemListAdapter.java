package com.eniac.optimalist.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eniac.optimalist.R;
import com.eniac.optimalist.database.model.ItemList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.MyViewHolder> {

    private Context context;
    private List<ItemList> itemLists;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView itemList;
        public TextView dot;
        public TextView timestamp;

        public MyViewHolder(View view) {
            super(view);
            itemList = view.findViewById(R.id.item_list);
            dot = view.findViewById(R.id.dot);
            timestamp = view.findViewById(R.id.timestamp);
        }
    }


    public ItemListAdapter(Context context, List<ItemList> itemLists) {
        this.context = context;
        this.itemLists = itemLists;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ItemList itemList = itemLists.get(position);

        holder.itemList.setText(itemList.getAmount()+" Adet "+itemList.getTitle()+" Fiyat: "+itemList.getPrice());

        // Displaying dot from HTML character code
        holder.dot.setText(Html.fromHtml("&#8226;"));

        // Formatting and displaying timestamp
        holder.timestamp.setText(formatDate(itemList.getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return itemLists.size();
    }


    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = fmt.parse(dateStr);
            SimpleDateFormat fmtOut = new SimpleDateFormat("d MMM yyyy - HH:mm:ss");
            return fmtOut.format(date);
        } catch (ParseException e) {

        }

        return "";
    }
}
