package com.eniac.optimalist.fragments;

import android.app.Activity;
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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.eniac.optimalist.R;
import com.eniac.optimalist.adapters.MarketAdapter;
import com.eniac.optimalist.database.DBHelper;
import com.eniac.optimalist.database.model.Market;
import com.eniac.optimalist.utils.DividerItemDecoration;
import com.eniac.optimalist.utils.RecyclerTouchListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.ArrayList;
import java.util.List;

public class MarketFragment extends Fragment {

    private DBHelper db;
    private MarketAdapter marketAdapter;
    private List<Market> markets = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView noMarketView;
    private final int PLACE_PICKER_REQUEST = 1;
    PlacePicker.IntentBuilder builder;

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
        builder = new PlacePicker.IntentBuilder();

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivityForResult(new Intent(getContext(), MapsActivity.class), RESULT_OK);
                try {
                    startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
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

    /**
     * Inserting new shopping list in db
     * and refreshing the list
     */
    private void createMarket(String title, double lat, double lng) {
        // inserting shopping list in db and getting
        // newly inserted shopping list id
        long id = db.insertMarket(title, lat, lng);

        if (id > 0) {
            // get the newly inserted shopping list from db
            Market m = db.getMarket(id);

            if (m != null) {
                // adding new shopping list to array list at 0 position
                markets.add(0, m);

                // refreshing the list
                marketAdapter.notifyDataSetChanged();

                toggleEmptyMarkets();
                Toast.makeText(getContext(), "Market eklendi", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Updating shopping list in db and updating
     * item in the list by its position
     */
    private void updateMarket(String title, int position) {

    }

    private void showMarketDialog(final boolean shouldUpdate, final Market market, final int position, final String defaultName, final double lat, final double lng) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getContext());
        View view = layoutInflaterAndroid.inflate(R.layout.add_market_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(getContext());
        alertDialogBuilderUserInput.setView(view);

        final EditText inputMarket = view.findViewById(R.id.market);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!shouldUpdate ? "Yeni Market" : "Market Adını Değiştir");

        /*if (shouldUpdate && market != null) {
            inputMarket.setText(market.getTitle());
        }*/
        inputMarket.setText(defaultName);
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
                if (TextUtils.isEmpty(inputMarket.getText().toString())) {
                    Toast.makeText(getContext(), "Market adını giriniz!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }

                // check if user updating note
                if (shouldUpdate && market != null) {
                    // update note by it's id
                    updateMarket(inputMarket.getText().toString(), position);
                } else {
                    // create new note
                    createMarket(inputMarket.getText().toString(), lat, lng);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode){
                case PLACE_PICKER_REQUEST:
                    Place place = PlacePicker.getPlace(getContext(), data);
                    showMarketDialog(false, null, 0, place.getName().toString(), place.getLatLng().latitude, place.getLatLng().longitude);
            }
        }
    }
}