package com.eniac.optimalist.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.eniac.optimalist.R;
import com.eniac.optimalist.database.DBHelper;
import com.eniac.optimalist.database.model.Market;
import com.eniac.optimalist.database.model.ShoppingList;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    PlaceAutocompleteFragment placeAutoComplete;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MarketTest", "test");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        db = DBHelper.getInstance(this);
        placeAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("Granted","Permission is granted");
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("Granted","Permission is granted");
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        mMap.setMyLocationEnabled(true);

        placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.d("Maps", "Place selected: " + place.getName());
                mMap.addMarker(new MarkerOptions()
                        .position(place.getLatLng())
                        .title(place.getName().toString())
                );
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 16.0f));
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        Log.d("MarkerClick", marker.getTitle());
                        showMarketDialog(false, null, 0, marker.getTitle(), marker.getPosition().latitude, marker.getPosition().longitude);
                        return true;
                    }
                });
            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });
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
            Intent data = new Intent();
            data.setData(Uri.parse("" + id));
            setResult(1, data);
            finish();
        }
    }

    /**
     * Updating shopping list in db and updating
     * item in the list by its position
     */
    private void updateMarket(String title, int position) {

    }

    private void showMarketDialog(final boolean shouldUpdate, final Market market, final int position, final String defaultName, final double lat, final double lng) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);
        View view = layoutInflaterAndroid.inflate(R.layout.add_market_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this);
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
                    Toast.makeText(getApplicationContext(), "Market adını giriniz!", Toast.LENGTH_SHORT).show();
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
}
