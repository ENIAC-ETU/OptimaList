package com.eniac.optimalist.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.eniac.optimalist.R;
import com.eniac.optimalist.database.DBHelper;
import com.eniac.optimalist.database.model.ItemList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class RecommAdapter extends RecyclerView.Adapter<RecommAdapter.MyViewHolder> {

    private Context context;
    private List<Long> itemsList;
    private List<Integer> dateList;
    private HashMap<Long,Integer> mapList;
    private DBHelper db;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView items;
        public TextView timestamp;


        public MyViewHolder(View view) {
            super(view);
            items = view.findViewById(R.id.rec_items);
            timestamp = view.findViewById(R.id.timestamp);
            db=DBHelper.getInstance(view.getContext());
        }
    }

    public RecommAdapter(Context context, HashMap<Long,Integer> p1) {
        this.context = context;
        dateList = new ArrayList<Integer>(p1.values());
        itemsList = new ArrayList<Long>(p1.keySet());

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recomm_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Integer holdDate = dateList.get(position);
        ItemList holdItem=db.getItemList(itemsList.get(position));


        Calendar c=Calendar.getInstance();
        int a=c.get(Calendar.DAY_OF_YEAR);
        holder.items.setText(holdItem.getTitle() + " Öneri Tarihi:"+(holdDate-a)+" gün sonra");
        /*c.set(Calendar.YEAR, c.get(Calendar.YEAR));
        c.set(Calendar.MONTH, Calendar.JANUARY);
        c.set(Calendar.DATE, 1);*/
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.DATE, holdDate-a);
        Date date = c.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String strDate = dateFormat.format(date);
        holder.timestamp.setText(strDate);
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

}
