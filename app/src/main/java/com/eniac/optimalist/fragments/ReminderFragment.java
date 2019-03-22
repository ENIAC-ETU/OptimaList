package com.eniac.optimalist.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.eniac.optimalist.MainActivity;
import com.eniac.optimalist.R;
import com.eniac.optimalist.database.DBHelper;
import com.eniac.optimalist.database.model.Market;
import com.eniac.optimalist.database.model.ReminderModel;
import com.eniac.optimalist.database.model.ShoppingList;
import com.eniac.optimalist.utils.DividerItemDecoration;
import com.eniac.optimalist.utils.RecyclerTouchListener;
import com.eniac.optimalist.adapters.ReminderAdapter;


import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReminderFragment extends Fragment implements DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener {

    private DBHelper db;
    private ReminderAdapter reminderAdapter;
    private List<ReminderModel> reminders = new ArrayList<>();
    private List<Market> markets = new ArrayList<>();
    private List<ShoppingList> shopping_lists = new ArrayList<>();

    int day, month, year, hour, minute;
    int dayFinal, monthFinal, yearFinal,hourFinal, minuteFinal;
    boolean timePicked=false ;

    private RecyclerView recyclerView;
    private TextView noReminderView;
    final int RESULT_OK = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminders, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Hatırlatıcılar");

        db = DBHelper.getInstance(getContext());
        recyclerView = view.findViewById(R.id.recycler_view);
        noReminderView = view.findViewById(R.id.empty_reminder_view);

        FloatingActionButton fab_create_reminder = (FloatingActionButton) getActivity().findViewById(R.id.fab_create_reminder);
        fab_create_reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showReminderDialog(false,0);
            }
        });

        reminders.addAll(db.getAllReminders());
        markets.addAll(db.getAllMarkets());
        shopping_lists.addAll(db.getAllShoppingLists());

        reminderAdapter = new ReminderAdapter(getActivity(), reminders);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(reminderAdapter);

        toggleEmptyReminders();
        /*
         * On long press on RecyclerView item, open alert dialog
         * with options to choose
         * Edit and Delete
         * */
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(),
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));
    }

    /**
     * Deleting shopping list from SQLite and removing the
     * item from the list by its position
     */
    private void deleteReminder(int position) {
        SharedPreferences reminder = getContext().getSharedPreferences("recom_reminder", 0);
        Long redid=reminder.getLong("recom_reminder",-1);
        if (reminders.get(position).getId()== redid && redid!=-1){
            SharedPreferences rec_settings = getContext().getSharedPreferences("rec_settings", 0);
            SharedPreferences.Editor edit1=rec_settings.edit();
            edit1.putBoolean("recommendSwitch",false);
            edit1.commit();
            SharedPreferences.Editor edit2=reminder.edit();
            edit2.putLong("recom_reminder",-1);
            edit2.commit();
        }
        // deleting the reminder from db
        db.deleteReminder(reminders.get(position));

        // removing the shopping list from the list
        reminders.remove(position);
        reminderAdapter.notifyItemRemoved(position);

        toggleEmptyReminders();
    }

    private void createReminder(String title, long shopping_list_id, long market_id,String reminderTime) {
        // inserting reminder for shopping list in db and getting
        // newly inserted reminder id
        long id = db.insertReminder(title, shopping_list_id, market_id,reminderTime);

        ((MainActivity)getActivity()).updateAlarms();
        // get the newly inserted reminder from db
        ReminderModel r= db.getReminder(id);

        if (r != null) {
            // adding new reminder to array list at 0 position
            reminders.add(0, r);

            // refreshing the list
            reminderAdapter.notifyDataSetChanged();

            toggleEmptyReminders();
        }
    }

    private void updateReminder(String title, long shopping_list_id, long market_id, int position,String reminderTime) {
        ReminderModel r = reminders.get(position);
        // updating reminder title
        r.setTitle(title);
        r.setMarketId(market_id);
        r.setShoppingListId(shopping_list_id);
        r.setReminder_time(reminderTime);
        ((MainActivity)getActivity()).updateAlarms();

        // updating note in db
        db.updateReminder(r);


        // refreshing the list
        reminders.set(position, r);
        reminderAdapter.notifyItemChanged(position);

        toggleEmptyReminders();
    }

    /**
     * Opens dialog with Edit - Delete options
     * Edit - 0
     * Delete - 0
     */
    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Düzenle", "Sil"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Bir seçenek seçiniz");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showReminderDialog(true, position);
                }
                else {
                    deleteReminder (position);
                }
            }
        });
        builder.show();
    }

    private void showReminderDialog(final boolean shouldUpdate, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getActivity().getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.add_reminder_dialog, null);

        Button datePickerButton = (Button) view.findViewById(R.id.datetime_picker_button1);
        datePickerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                timePicked=false;
                Calendar c = Calendar.getInstance();
                year=c.get(Calendar.YEAR);
                month=c.get(Calendar.MONTH);
                day =c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog.OnDateSetListener listener=ReminderFragment.this;
                Context context=getContext();

                DatePickerDialog datePickerDialog = new DatePickerDialog(context,listener,year,month,day);
                datePickerDialog.show();
            }
        });

        final Spinner spinner1 = (Spinner) view.findViewById(R.id.rem_shopping_list_spinner);
        ArrayAdapter<ShoppingList> dataAdapter1 = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_item, shopping_lists);
        dataAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(dataAdapter1);

        final Spinner spinner2 = (Spinner) view.findViewById(R.id.rem_markets_spinner);
        ArrayAdapter<Market> dataAdapter2 = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_item, markets);
        dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(dataAdapter2);


        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this.getActivity());
        alertDialogBuilderUserInput.setView(view);

        final EditText title = view.findViewById(R.id.reminder);

        TextView dialogTitle = view.findViewById(R.id.rem_dialog_title);
        dialogTitle.setText(getContext().getString(R.string.reminder));

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("kaydet", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        createReminder(title.getText().toString(),((ShoppingList)spinner1.getSelectedItem()).getId(),((Market)spinner2.getSelectedItem()).getId(),"");
                    }
                })
                .setNegativeButton("iptal",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shouldUpdate)
                    updateReminder(title.getText().toString(),((ShoppingList)spinner1.getSelectedItem()).getId(),((Market)spinner2.getSelectedItem()).getId(),position,
                            yearFinal+"-"+monthFinal+"-"+dayFinal+" "+hourFinal+":"+minuteFinal+":00");
                else
                    createReminder(title.getText().toString(),((ShoppingList)spinner1.getSelectedItem()).getId(),((Market)spinner2.getSelectedItem()).getId(),
                            yearFinal+"-"+monthFinal+"-"+dayFinal+" "+hourFinal+":"+minuteFinal+":00");
                alertDialog.dismiss();

            }
        });
    }

    /**
     * Toggling list and empty reminder view
     */
    private void toggleEmptyReminders() {
        if (db.getRemindersCount() > 0) {
            noReminderView.setVisibility(View.GONE);
        } else {
            noReminderView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_OK && resultCode == RESULT_OK) {
            String returnedResult = data.getDataString();

            if (returnedResult != null && !returnedResult.equals("")) {
                // get the newly inserted reminder from db
                ReminderModel r = db.getReminder(Integer.parseInt(returnedResult));

                if (r != null) {
                    // adding new reminder to array list at 0 position
                    reminders.add(0, r);

                    // refreshing the list
                    reminderAdapter.notifyDataSetChanged();

                    toggleEmptyReminders();
                }
            }
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        yearFinal=year;
        monthFinal=month+1;
        dayFinal=dayOfMonth;

        Calendar c =Calendar.getInstance();
        hour=c.get(Calendar.HOUR_OF_DAY);
        minute=c.get(Calendar.MINUTE);

        Context context=getContext();
        TimePickerDialog.OnTimeSetListener listener=ReminderFragment.this;

        TimePickerDialog timePickerDialog = new TimePickerDialog(context,listener,
                hour,minute,android.text.format.DateFormat.is24HourFormat(getContext()));
        timePickerDialog.show();

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        hourFinal=hourOfDay;
        minuteFinal=minute;

        timePicked=true;
    }
}