package com.eniac.optimalist.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eniac.optimalist.R;
import com.eniac.optimalist.database.DBHelper;
import com.eniac.optimalist.database.model.ItemList;
import com.eniac.optimalist.database.model.Market;
import com.eniac.optimalist.database.model.ShoppingList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ItemPriceAdapter extends RecyclerView.Adapter<ItemPriceAdapter.MyViewHolder> {

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


    public ItemPriceAdapter(Context context, List<ItemList> itemLists) {
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
        DBHelper db=DBHelper.getInstance(context);
        if (db.getShoppingList(itemList.getShoppingListId()).getTitle().equals("Recommended")){
            ItemList e=db.lowestMarketAndPrice(itemList);
            if (db.getShoppingList(e.getShoppingListId())!=null && db.getMarket(db.getShoppingList(e.getShoppingListId()).getMarketId())!=null) {
                holder.itemList.setText(itemList.getTitle() + " Fiyat: " + e.getPrice() + " En Uygun Market: " + db.getMarket(db.getShoppingList(e.getShoppingListId()).getMarketId()).getTitle());
            }else {
                holder.itemList.setText(itemList.getTitle() + " Fiyat: " + e.getPrice() + " En Uygun Market: " + "Yok");
            }
        }
        else {
            Market market = db.getMarket((long)db.getShoppingList((long)itemList.getShoppingListId()).getMarketId());
            if (market != null) {
                if (itemList.getPrice() > 0) {

                    holder.itemList.setText(itemList.getTitle() + " ürünü bu markette " + itemList.getPrice() + " TL : " + market.getTitle());
                } else {
                    holder.itemList.setText(itemList.getTitle() + " ürünü için bu markette fiyat girilmemiş: " + market.getTitle());
                }
            }
        }

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
