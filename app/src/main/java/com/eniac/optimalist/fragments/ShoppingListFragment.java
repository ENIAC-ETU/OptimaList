package com.eniac.optimalist.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

//import android.support.design.widget.FloatingActionButton;
import com.eniac.optimalist.activities.ImageActivity;
import com.getbase.floatingactionbutton.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.eniac.optimalist.MainActivity;
import com.eniac.optimalist.R;
import com.eniac.optimalist.activities.ItemActivity;
import com.eniac.optimalist.adapters.ReminderAdapter;
import com.eniac.optimalist.adapters.ShoppingListAdapter;
import com.eniac.optimalist.database.DBHelper;
import com.eniac.optimalist.database.model.Market;
import com.eniac.optimalist.database.model.ReminderModel;
import com.eniac.optimalist.database.model.ShoppingList;
import com.eniac.optimalist.utils.DividerItemDecoration;
import com.eniac.optimalist.utils.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ShoppingListFragment extends Fragment implements DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener {

    public static DBHelper db;
    private ShoppingListAdapter shoppingListAdapter;
    private ReminderAdapter reminderAdapter;
    private List<ShoppingList> shoppingLists = new ArrayList<>();
    private List<ReminderModel> reminders = new ArrayList<>();
    private List<Market> markets = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView noShoppingListView;
    public static long currentPositionId;
    public static String currentShoppingListTitle;

    int day, month, year, hour, minute;
    int dayFinal, monthFinal, yearFinal,hourFinal, minuteFinal;

CheckBox location_box;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_shopping_lists, container, false);
        location_box = (CheckBox) root.findViewById(R.id.checkbox);
        checkLocationBox();
        return root;
    }
    private void checkLocationBox(){
        Log.d("MyLocation:",(location_box==null)+"");
        location_box.setChecked(true);
        location_box.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).changeLocationServiceStatus();
                if(location_box.isChecked()){
                    Log.d("MyLocation:","Checked");
                }else{
                    Log.d("MyLocation:","Un-Checked");
                }
            }
        });
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Alışveriş Listelerim");

        db = DBHelper.getInstance(getContext());
        recyclerView = view.findViewById(R.id.recycler_view);
        noShoppingListView = view.findViewById(R.id.empty_shopping_lists_view);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showShoppingListDialog(false, null, -1);
            }
        });


        FloatingActionButton select_image = (FloatingActionButton) view.findViewById(R.id.select_image);
        select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ImageActivity.class);
                startActivityForResult(intent,1313);
            }
        });

        reminders.addAll(db.getAllReminders());
        markets.addAll(db.getAllMarkets());
        shoppingLists.addAll(db.getAllShoppingLists());

        shoppingListAdapter = new ShoppingListAdapter(getActivity(), shoppingLists);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(shoppingListAdapter);
        reminderAdapter = new ReminderAdapter(getActivity(), reminders);


        toggleEmptyShoppingLists();

        /*
         * On long press on RecyclerView item, open alert dialog
         * with options to choose
         * Edit and Delete
         * */
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(),
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                ShoppingList l = shoppingLists.get(position);
                currentPositionId = l.getId();
                currentShoppingListTitle = l.getTitle();
                showItems();
            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == 1313) {
            ShoppingList list=db.getShoppingList((long)resultCode);
            shoppingLists.add(0,list);
            Toast.makeText(getContext(),list.getTitle()+" Listesi Oluşturuldu",Toast.LENGTH_SHORT).show();
            shoppingListAdapter.notifyDataSetChanged();
        }

    }


    public void showItems(){

        Intent intent = new Intent(getContext(), ItemActivity.class);
        intent.putExtra("id",currentPositionId);
        startActivity(intent);
    }

    /**
     * Inserting new shopping list in db
     * and refreshing the list
     */
    private void createShoppingList(String title) {
        // inserting shopping list in db and getting
        // newly inserted shopping list id
        long id = db.insertShoppingList(title);

        // get the newly inserted shopping list from db
        ShoppingList l = db.getShoppingList(id);

        if (l != null) {
            // adding new shopping list to array list at 0 position
            shoppingLists.add(0, l);

            // refreshing the list
            shoppingListAdapter.notifyDataSetChanged();

            toggleEmptyShoppingLists();
        }
    }

    /**
     * Updating shopping list in db and updating
     * item in the list by its position
     */
    private void updateShoppingList(String title, int position) {
        ShoppingList l = shoppingLists.get(position);
        // updating shopping list title
        l.setTitle(title);

        // updating note in db
        db.updateShoppingList(l);

        // refreshing the list
        shoppingLists.set(position, l);
        shoppingListAdapter.notifyItemChanged(position);

        toggleEmptyShoppingLists();
    }

    /**
     * Deleting shopping list from SQLite and removing the
     * item from the list by its position
     */
    private void deleteShoppingList(int position) {
        // deleting the shopping list from db
        db.deleteShoppingList(shoppingLists.get(position));

        // removing the shopping list from the list
        shoppingLists.remove(position);
        shoppingListAdapter.notifyItemRemoved(position);

        toggleEmptyShoppingLists();
    }

    /**
     * Opens dialog with Edit - Delete options
     * Edit - 0
     * Delete - 0
     */
    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Düzenle", "Hatırlatıcı ekle", "Sil"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Bir seçenek seçiniz");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showShoppingListDialog(true, shoppingLists.get(position), position);
                }
                else if (which == 1){
                    showReminderDialog(position);
                }
                else {
                    deleteShoppingList(position);
                }
            }
        });
        builder.show();
    }

    private void showShoppingListDialog(final boolean shouldUpdate, final ShoppingList shoppingList, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getContext());
        View view = layoutInflaterAndroid.inflate(R.layout.add_shopping_list_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(getContext());
        alertDialogBuilderUserInput.setView(view);

        final EditText inputShoppingList = view.findViewById(R.id.shopping_list);
        //final EditText inputMarketId = view.findViewById(R.id.market_id);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!shouldUpdate ? getString(R.string.lbl_new_shopping_list_title) : getString(R.string.lbl_edit_shopping_list_title));

        if (shouldUpdate && shoppingList != null) {
            inputShoppingList.setText(shoppingList.getTitle());
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(shouldUpdate ? "güncelle" : "kaydet", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

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
                // Show toast message when no text is entered
                if (TextUtils.isEmpty(inputShoppingList.getText().toString())) {
                    Toast.makeText(getContext(), "Alışveriş listesi adını giriniz!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }

                // check if user updating note
                if (shouldUpdate && shoppingList != null) {
                    // update note by it's id
                    updateShoppingList(inputShoppingList.getText().toString(), position);
                } else {
                    // create new note
                    createShoppingList(inputShoppingList.getText().toString());
                }
            }
        });
    }

    /**
     * Toggling list and empty shopping lists view
     */
    private void toggleEmptyShoppingLists() {
        if (db.getShoppingListsCount() > 0) {
            noShoppingListView.setVisibility(View.GONE);
        } else {
            noShoppingListView.setVisibility(View.VISIBLE);
        }
    }

    private void addItemToShoppingList(String item){}

    /**
     * Shows alert dialog with EditText options to enter / edit
     * a shopping list.
     * when shouldUpdate=true, it automatically displays old shopping list and changes the
     * button text to UPDATE
     */
    private void showAddItemDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getContext());
        View view = layoutInflaterAndroid.inflate(R.layout.add_item_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(getContext());
        alertDialogBuilderUserInput.setView(view);

        final EditText inputItemName = view.findViewById(R.id.add_item);
        TextView dialogTitle = view.findViewById(R.id.add_item_dialog_title);
        dialogTitle.setText(getString(R.string.new_item));

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("kaydet", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

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
                alertDialog.dismiss();

                addItemToShoppingList(inputItemName.getText().toString());
            }
        });
    }

    private void showReminderDialog(final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getActivity().getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.add_reminder_from_shopping_list, null);

        Button datePickerButton = (Button) view.findViewById(R.id.datetime_picker_button2);
        datePickerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Calendar c = Calendar.getInstance();
                year=c.get(Calendar.YEAR);
                month=c.get(Calendar.MONTH);
                day =c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog.OnDateSetListener listener=ShoppingListFragment.this;
                Context context=getContext();

                DatePickerDialog datePickerDialog = new DatePickerDialog(context,listener,year,month,day);
                datePickerDialog.show();
            }
        });

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
                        createReminder(title.getText().toString(),shoppingLists.get(position).getId(),((Market)spinner2.getSelectedItem()).getId(),
                                yearFinal+"-"+monthFinal+"-"+dayFinal+" "+hourFinal+":"+minuteFinal+":00");
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
                    createReminder(title.getText().toString(),shoppingLists.get(position).getId(),((Market)spinner2.getSelectedItem()).getId(),
                            yearFinal+"-"+monthFinal+"-"+dayFinal+" "+hourFinal+":"+minuteFinal+":00");
                alertDialog.dismiss();

            }
        });
    }

    private void createReminder(String title, long shopping_list_id, long market_id, String reminderTime) {
        // inserting reminder for shopping list in db and getting
        // newly inserted reminder id
        long id = db.insertReminder(title, shopping_list_id, market_id, reminderTime);

        // get the newly inserted reminder from db
        ReminderModel r= db.getReminder(id);

        if (r != null) {
            // adding new reminder to array list at 0 position
            reminders.add(0, r);

            // refreshing the list
            reminderAdapter.notifyDataSetChanged();
        }
    }

    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        yearFinal=year;
        monthFinal=month+1;
        dayFinal=dayOfMonth;

        Calendar c =Calendar.getInstance();
        hour=c.get(Calendar.HOUR_OF_DAY);
        minute=c.get(Calendar.MINUTE);

        Context context=getContext();
        TimePickerDialog.OnTimeSetListener listener=ShoppingListFragment.this;

        TimePickerDialog timePickerDialog = new TimePickerDialog(context,listener,
                hour,minute,android.text.format.DateFormat.is24HourFormat(getContext()));
        timePickerDialog.show();

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        hourFinal=hourOfDay;
        minuteFinal=minute;
    }

}