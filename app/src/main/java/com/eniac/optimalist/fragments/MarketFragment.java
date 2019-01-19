package com.eniac.optimalist.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eniac.optimalist.R;
import com.eniac.optimalist.activities.MapsActivity;
import com.eniac.optimalist.adapters.MarketAdapter;
import com.eniac.optimalist.database.DBHelper;
import com.eniac.optimalist.database.model.Market;
import com.eniac.optimalist.utils.DividerItemDecoration;
import com.eniac.optimalist.utils.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.List;

public class MarketFragment extends Fragment {

    private DBHelper db;
    private MarketAdapter marketAdapter;
    private List<Market> markets = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView noMarketView;
    final int RESULT_OK = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_markets, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Marketlerim");

        db = DBHelper.getInstance(getContext());
        recyclerView = view.findViewById(R.id.recycler_view);
        noMarketView = view.findViewById(R.id.empty_market_view);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showMarketDialog(false, null, -1);
                startActivityForResult(new Intent(getContext(), MapsActivity.class), RESULT_OK);
            }
        });

        markets.addAll(db.getAllMarkets());

        marketAdapter = new MarketAdapter(getActivity(), markets);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(marketAdapter);

        toggleEmptyMarkets();

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
    private void deleteMarket(int position) {
        // deleting the shopping list from db
        db.deleteMarket(markets.get(position));

        // removing the shopping list from the list
        markets.remove(position);
        marketAdapter.notifyItemRemoved(position);

        toggleEmptyMarkets();
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
                    //showMarketDialog(true, markets.get(position), position);
                }
                else {
                    deleteMarket(position);
                }
            }
        });
        builder.show();
    }

    /**
     * Toggling list and empty shopping lists view
     */
    private void toggleEmptyMarkets() {
        if (db.getMarketsCount() > 0) {
            noMarketView.setVisibility(View.GONE);
        } else {
            noMarketView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_OK && resultCode == RESULT_OK) {
            String returnedResult = data.getDataString();

            if (returnedResult != null && !returnedResult.equals("")) {
                // get the newly inserted shopping list from db
                Market m = db.getMarket(Integer.parseInt(returnedResult));

                if (m != null) {
                    // adding new shopping list to array list at 0 position
                    markets.add(0, m);

                    // refreshing the list
                    marketAdapter.notifyDataSetChanged();

                    toggleEmptyMarkets();
                }
            }
        }
    }
}