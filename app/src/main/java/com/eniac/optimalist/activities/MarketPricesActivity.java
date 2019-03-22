package com.eniac.optimalist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.eniac.optimalist.R;
import com.eniac.optimalist.activities.ImageActivity;
import com.eniac.optimalist.adapters.ItemListAdapter;
import com.eniac.optimalist.adapters.ItemPriceAdapter;
import com.eniac.optimalist.database.DBHelper;
import com.eniac.optimalist.database.model.ItemList;
import com.eniac.optimalist.database.model.Market;
import com.eniac.optimalist.database.model.ReminderModel;
import com.eniac.optimalist.database.model.ShoppingList;
import com.eniac.optimalist.utils.DividerItemDecoration;
import com.eniac.optimalist.utils.RecyclerTouchListener;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MarketPricesActivity extends AppCompatActivity {

    public static DBHelper db;
    private ItemPriceAdapter itemListAdapter;
    private List<ShoppingList> shoppingLists = new ArrayList<>();
    private List<ReminderModel> reminders = new ArrayList<>();
    private List<Market> markets = new ArrayList<>();
    private List<ItemList> items =new ArrayList<>();
    private List<ItemList> differentItems=new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView noMarketPricesListView;

    public static long currentPositionId;
    public static String currentShoppingListTitle;

    @Override
    public void onCreate( @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_market_prices);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        toolbar.setTitle("Diğer Marketlerdeki Fiyatlar");

        db = DBHelper.getInstance(getApplicationContext());
        recyclerView = findViewById(R.id.recycler_view_market_prices);
        noMarketPricesListView = findViewById(R.id.empty_market_prices_lists_view);

        Intent intent =getIntent();
        Bundle b = intent.getExtras();

        if(b!=null)
            currentPositionId=b.getLong("id");


        reminders.addAll(db.getAllReminders());
        markets.addAll(db.getAllMarkets());
        shoppingLists.addAll(db.getAllShoppingLists());
        items.addAll(db.getAllItemLists());

        ItemList currentItem;

        for(ItemList item: items){
            currentItem=db.getItemList(currentPositionId);
            if(item.getTitle().equals(currentItem.getTitle())&&currentItem.getShoppingListId()!=item.getShoppingListId())
                differentItems.add(item);
        }

        Log.d("--------------------",""+currentPositionId+"  "+differentItems.size());

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL, 16));

        itemListAdapter=new ItemPriceAdapter(this, differentItems);

        recyclerView.setAdapter(itemListAdapter);

        toggleEmptyPricesLists();

        /*
         * On long press on RecyclerView item, open alert dialog
         * with options to choose
         * Edit and Delete
         * */
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(),
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    private void toggleEmptyPricesLists() {


        if (differentItems.size() > 0) {
            noMarketPricesListView.setVisibility(View.GONE);
        } else {
            noMarketPricesListView.setVisibility(View.VISIBLE);
        }
    }

}
