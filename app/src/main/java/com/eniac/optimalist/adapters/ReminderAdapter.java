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
import com.eniac.optimalist.database.model.ReminderModel;
import com.eniac.optimalist.database.model.ShoppingList;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.MyViewHolder> {

    private Context context;
    private List<ReminderModel> reminders;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView reminder;
        public TextView dot;
        public TextView timestamp;
        public TextView reminderTime;

        public MyViewHolder(View view) {
            super(view);
            reminder = view.findViewById(R.id.reminder);
            dot = view.findViewById(R.id.dot);
            timestamp = view.findViewById(R.id.timestamp);
            reminderTime = view.findViewById(R.id.reminderTimeStamp);
        }
    }

    public ReminderAdapter(Context context, List<ReminderModel> reminders) {
        this.context = context;
        this.reminders = reminders;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View reminderView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reminder_row, parent, false);

        return new MyViewHolder(reminderView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ReminderModel reminder = reminders.get(position);

        holder.reminder.setText(reminder.getTitle());

        // Displaying dot from HTML character code
        holder.dot.setText(Html.fromHtml("&#8226;"));

        // Formatting and displaying timestamp
        holder.timestamp.setText(formatDate(reminder.getCreatedAt()));

        holder.reminderTime.setText(formatDate(reminder.getReminder_time()));
    }

    @Override
    public int getItemCount() {
        return reminders.size();
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
