package com.eniac.optimalist.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.eniac.optimalist.MainActivity;
import com.eniac.optimalist.R;
import com.eniac.optimalist.database.DBHelper;
import com.eniac.optimalist.database.model.Market;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationService extends Service
{
    private static final String TAG = "MyLocation";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 100;
    private static final float LOCATION_DISTANCE = 0;
    private DBHelper db;
    private static List<Market> marketList=new ArrayList<>();
    public static Location mLastLocation;
    private static Market lastClosest =null;
    private boolean changed=true;
    private static final double  distanceLimit=1000;
    private class LocationListener implements android.location.LocationListener
    {

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);

            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            double mLatitude = location.getLatitude();
            double mLongitude = location.getLongitude();

            if (mLastLocation != null && mLastLocation.getLatitude() != mLatitude && mLastLocation.getLongitude() != mLongitude) {
                String p = getCompleteAddressString(mLatitude, mLongitude);

                Log.e(TAG, "onLocation:"  + p);
                mLastLocation.set(location);
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
            if (distance <distanceLimit){
                setNotification(getApplicationContext(), "You are close to ...", "You are close to "+lastClosest.getTitle(), 1);
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
                    Address returnedAddress = addresses.get(0);
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
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        db = DBHelper.getInstance(getApplicationContext());
        onCreate();
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        final Handler handler = new Handler();
        final int delay = 60000; //milliseconds

        handler.postDelayed(new Runnable(){
            public void run(){
                //do something
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
    public void setNotification(Context context, String notificationTitle, String notificationMessage, int notificationRequestCode) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context,MainActivity.CHANNEL_1_ID)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle(notificationTitle)
                        .setColor(101)
                        .setContentText(notificationMessage);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, notificationRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
        Log.i(TAG, "notification sended");

    }
    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
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