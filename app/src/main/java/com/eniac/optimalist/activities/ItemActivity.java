package com.eniac.optimalist.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.eniac.optimalist.MainActivity;
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

    public static DBHelper db;


    private ItemListAdapter itemListAdapter;
    private List<ItemList> itemLists = new ArrayList<>();

    private RecyclerView recyclerView;
    private TextView noItemListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item2);
        db = ShoppingListFragment.db;
        recyclerView = findViewById(R.id.recycler_view);
        noItemListView = findViewById(R.id.empty_item_lists_view);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddItemDialog();
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
        CharSequence colors[] = new CharSequence[]{"Hatırlatıcı ekle", "Sil"};

        AlertDialog.Builder builder = new AlertDialog.Builder(ItemActivity.this);
        builder.setTitle("Bir seçenek seçiniz");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {

                }
                else {
                    deleteItem(position);
                }

            }
        });
        builder.show();
    }

    private void deleteItem(int position) {
        // deleting the shopping list from db
        db.deleteItem(itemLists.get(position));

        // removing the shopping list from the list
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



}
