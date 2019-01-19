package com.eniac.optimalist.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.eniac.optimalist.R;
import com.eniac.optimalist.database.model.Market;
import com.eniac.optimalist.database.model.ShoppingList;

public class MarketAdapter extends RecyclerView.Adapter<MarketAdapter.MyViewHolder> {

    private Context context;
    private List<Market> markets;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView market;
        public TextView dot;
        public TextView timestamp;

        public MyViewHolder(View view) {
            super(view);
            market = view.findViewById(R.id.market);
            dot = view.findViewById(R.id.dot);
            timestamp = view.findViewById(R.id.timestamp);
        }
    }

    public MarketAdapter(Context context, List<Market> markets) {
        this.context = context;
        this.markets = markets;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.market_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Market market = markets.get(position);

        holder.market.setText(market.getTitle());

        // Displaying dot from HTML character code
        holder.dot.setText(Html.fromHtml("&#8226;"));

        // Formatting and displaying timestamp
        holder.timestamp.setText(formatDate(market.getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return markets.size();
    }

    /**
     * Formatting timestamp to `MMM d` format
     * Input: 2018-02-21 00:15:42
     * Output: Feb 21
     */
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
