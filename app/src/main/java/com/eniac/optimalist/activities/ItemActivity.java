package com.eniac.optimalist.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.eniac.optimalist.adapters.MarketAdapter;
import com.eniac.optimalist.database.model.Market;
import com.getbase.floatingactionbutton.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.eniac.optimalist.database.model.ShoppingList;
import com.eniac.optimalist.fragments.ShoppingListFragment;
import com.eniac.optimalist.utils.DividerItemDecoration;
import com.eniac.optimalist.utils.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.List;

public class ItemActivity extends AppCompatActivity {

    private DBHelper db;
    private ItemListAdapter itemListAdapter;
    private List<ItemList> itemLists = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView noItemListView;
    private List<Market> markets = new ArrayList<>();
    private long shop_id=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_item2);
        db = DBHelper.getInstance(this);
        recyclerView = findViewById(R.id.recycler_view);
        noItemListView = findViewById(R.id.empty_item_lists_view);

        markets.addAll(db.getAllMarkets());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);


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


        FloatingActionButton fab_image = (FloatingActionButton) findViewById(R.id.fab_image);
        fab_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ItemActivity.this, ImageActivity.class);
                startActivity(intent);
            }
        });

        //itemLists.addAll(db.getAllItemLists());
        Intent intent = getIntent();
        long value=intent.getLongExtra("id",0);
        Bundle b = intent.getExtras();
        long j=0;
        if (b!=null) {
            j = (Long) b.get("id");
            shop_id=j;
            Log.d("MyLocation:a","id:"+j);
            itemLists.addAll(db.getCurrentItems(j));
        }
        itemListAdapter = new ItemListAdapter(this, itemLists);
        toolbar.setTitle(db.getShoppingList(j).getTitle());
        setSupportActionBar(toolbar);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(itemListAdapter);


        toggleEmptyItemLists();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(ItemActivity.this,
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                showActionsDialog(position);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));



    }

    //-------------------------------------------------------------------------------------------------------------
    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Hatırlatıcı ekle", "Ürünü Sil", "Tüm alışveriş listelerinden ürünü sil", "test"};

        AlertDialog.Builder builder = new AlertDialog.Builder(ItemActivity.this);
        builder.setTitle("Bir seçenek seçiniz");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {

                }
                else if (which == 1) {
                    deleteItem(position);
                }
                else if (which == 2 ){
                    deleteItemFromAllShoppingLists(position);
                }else
                    createShoppingListFromOCR("asd",null);

            }
        });
        builder.show();
    }

    private void deleteItem(int position) {

        db.deleteItem(itemLists.get(position));

        itemLists.remove(position);
        itemListAdapter.notifyItemRemoved(position);

        toggleEmptyItemLists();
    }


    private void deleteItemFromAllShoppingLists(int position) {


        List<ItemList> allItems;
        allItems = db.getAllItemLists();

        String current_item_title = itemLists.get(position).getTitle();



        int i = 0;
        while(allItems.size()>i) {
            ItemList item = allItems.get(i);

            if (item.getTitle().equalsIgnoreCase(current_item_title)){
                db.deleteItem(item);
            }
            i++;

        }




        itemLists.remove(position);
        itemListAdapter.notifyItemRemoved(position);
        toggleEmptyItemLists();
    }


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


        long id = db.insertItemList(item, shop_id);

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

    public ShoppingList createShoppingListFromOCR(String name, String[] OCR){// position of this method will be changed after ocr implementation
        //creates a new shopping list
        //parameters can be changed in future

        String[] items={"misir", "sut", "un", "elma", "peynir"};
        Double[] prices={9.90,4.75,15.50,7.89,4.85};

        // inserting shopping list in db and getting
        // newly inserted shopping list id
        long id = db.insertShoppingList(name);

        // get the newly inserted shopping list from db
        ShoppingList l = db.getShoppingList(id);

        for(int i=0;i<items.length;i++) {
            long itemId =db.insertItemList(items[i], ShoppingListFragment.currentPositionId);
            ItemList item =db.getItemList(itemId);
            itemLists.add(0, item);

            itemListAdapter.notifyDataSetChanged();

            toggleEmptyItemLists();
        }

        return l;
    }

}
