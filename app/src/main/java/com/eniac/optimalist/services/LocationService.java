package com.eniac.optimalist.services;


import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.eniac.optimalist.NotificationSystem;
import com.eniac.optimalist.database.DBHelper;
import com.eniac.optimalist.database.model.Market;
import com.eniac.optimalist.database.model.ReminderModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationService extends Service
{
    private static final String TAG = "MyLocation";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 100;
    private static final float LOCATION_DISTANCE = 0;
    private final IBinder mBinder = new LocalBinder();
    private DBHelper db;
    private static List<Market> marketList=new ArrayList<>();
    private final static  Location mLastLocation=new Location("gps");
    private static Market lastClosest =null;
    private boolean changed=true;
    final Handler handler = new Handler();
    private static final double  distanceLimit=1000;
    private static NotificationSystem notify= NotificationSystem.getInstance();
    public static  Location getLastLocation(){ //get the last location
        if (mLastLocation!=null)
        return mLastLocation;
        return null;
    }

    public class LocalBinder extends Binder {
        public LocationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationService.this;
        }
    }
    private class LocationListener implements android.location.LocationListener
    {

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);

        }

        @Override
        public void onLocationChanged(Location location) {
            double mLatitude = location.getLatitude();
            double mLongitude = location.getLongitude();

            if (mLastLocation != null && mLastLocation.getLatitude() != mLatitude && mLastLocation.getLongitude() != mLongitude) {
                String p = getCompleteAddressString(mLatitude, mLongitude);
                if(mLastLocation!=null)
                Log.e(TAG, "LastLocation:"  + getCompleteAddressString(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                mLastLocation.set(location);

                Log.e(TAG, "onLocation:"  + p);


            }

            if (lastClosest != null && changed!=false) {
                calculateDistance(location, lastClosest);

            }
            else
                Log.d(TAG,"tempisnull");

        }
        private boolean calculateDistance(Location A,Market lastClosest){
            Location B=new Location("new");
            B.setLatitude(lastClosest.getLat());
            B.setLongitude(lastClosest.getLng());
            float distance=A.distanceTo(B);
            ReminderModel m=db.getMarketSpecificReminder(lastClosest);
            if (distance <distanceLimit && m!=null){
                notify.setNotification(getApplicationContext(), "Reminder:"+m.getTitle(),  "ShopList:"+db.getShoppingList(m.get_shopping_list_id()).getTitle()+" Uzaklık:"+(int)distance+"metre", 1);
                changed=false;
                return true;
            }
            return false;
        }
        public  String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
            String strAdd = "";
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
                if (addresses != null) {
                    Address returnedAddress;
                    try {
                         returnedAddress = addresses.get(0);
                    }catch (Exception e){
                        return null;
                    }
                    StringBuilder strReturnedAddress = new StringBuilder("");

                    for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                        strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                    }
                    strAdd = strReturnedAddress.toString();
                    Log.w("My  location address", strReturnedAddress.toString());
                } else {
                    Log.w("My  location address", "No Address returned!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.w("My  location address", "Cannot get Address!");
            }
            return strAdd;
        }
        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    public static Market orderMarketList() {
        double min=1000;
        int index=-1;
        Location newLocation=new Location("newlocation");
        Log.d(TAG,"orderMarketList");
        for (int i=0;i<marketList.size();i++){
            newLocation.setLatitude(marketList.get(i).getLat());
            newLocation.setLongitude(marketList.get(i).getLng());
            double tempMin=mLastLocation.distanceTo(newLocation);
            if (min>tempMin && tempMin<distanceLimit){
                    min=tempMin;
                    index=i;
            }
        }
        if (index!=-1){
            return marketList.get(index);
        }
        return null;
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        db = DBHelper.getInstance(getApplicationContext());
        onCreate();
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        final int delay = 60000; //milliseconds

        handler.postDelayed(new Runnable(){
            public void run(){
                marketList = db.getAllMarkets();
                Market templastClosest =orderMarketList();
                if (lastClosest!=null && templastClosest!=null && templastClosest.getTitle().equals(lastClosest.getTitle())){
                    Log.d(TAG,"didn't changed");
                }else{
                    lastClosest=templastClosest;
                    Log.d(TAG,"did changed");
                    changed=true;
                }
                handler.postDelayed(this, delay);
            }
        }, delay);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }
    public void triggerLocationUpdate(){
        Location loc = new Location("gps");
        loc.setLatitude(13.32); // just mock values
        loc.setLongitude(13.32);
        mLocationManager.setTestProviderLocation("gps", loc);
    }
    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        handler.removeMessages(0);
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listeners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}