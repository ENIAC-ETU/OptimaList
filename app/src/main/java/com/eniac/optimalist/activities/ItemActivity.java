package com.eniac.optimalist.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.eniac.optimalist.adapters.MarketAdapter;
import com.eniac.optimalist.database.model.Market;
import com.getbase.floatingactionbutton.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.eniac.optimalist.R;
import com.eniac.optimalist.adapters.ItemListAdapter;
import com.eniac.optimalist.database.DBHelper;
import com.eniac.optimalist.database.model.ItemList;
import com.eniac.optimalist.fragments.ShoppingListFragment;
import com.eniac.optimalist.utils.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class ItemActivity extends AppCompatActivity {

    private DBHelper db;
    private ItemListAdapter itemListAdapter;
    private List<ItemList> itemLists = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView noItemListView;
    private List<Market> markets = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item2);
        db = DBHelper.getInstance(this);
        recyclerView = findViewById(R.id.recycler_view);
        noItemListView = findViewById(R.id.empty_item_lists_view);

        markets.addAll(db.getAllMarkets());

        FloatingActionButton fab_add_item = (FloatingActionButton) findViewById(R.id.fab_add_item);
        fab_add_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddItemDialog();
            }
        });

        FloatingActionButton fab_select_market = (FloatingActionButton) findViewById(R.id.fab_select_market);
        fab_select_market.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectMarketDialog();
            }
        });

        //itemLists.addAll(db.getAllItemLists());
        itemLists.addAll(db.getCurrentItems(ShoppingListFragment.currentPositionId));
        itemListAdapter = new ItemListAdapter(this, itemLists);


        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(itemListAdapter);


        toggleEmptyItemLists();




    }

    //-------------------------------------------------------------------------------------------------------------


    private void showAddItemDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.add_item_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(ItemActivity.this);
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


    private void addItemToShoppingList(String item){


        long id = db.insertItemList(item, ShoppingListFragment.currentPositionId);

        ItemList l = db.getItemList(id);

        if (l != null) {

            itemLists.add(0, l);

            itemListAdapter.notifyDataSetChanged();

            toggleEmptyItemLists();

        }

    }

    private void toggleEmptyItemLists() {
        if (db.getItemListsCount() > 0) {
            noItemListView.setVisibility(View.GONE);
        } else {
            noItemListView.setVisibility(View.VISIBLE);
        }
    }

    private void showSelectMarketDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.select_market_dialog, null);

        Spinner spinner = (Spinner) view.findViewById(R.id.markets_spinner);
        ArrayAdapter<Market> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, markets);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this);
        alertDialogBuilderUserInput.setView(view);

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

            }
        });
    }

}
