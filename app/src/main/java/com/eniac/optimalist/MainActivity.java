package com.eniac.optimalist;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.text.format.DateFormat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;

import com.eniac.optimalist.database.DBHelper;
import com.eniac.optimalist.database.model.ReminderModel;
import com.eniac.optimalist.database.model.ShoppingList;
import com.eniac.optimalist.fragments.MarketFragment;
import com.eniac.optimalist.fragments.RecommFragment;
import com.eniac.optimalist.fragments.ReminderFragment;
import com.eniac.optimalist.fragments.SettingFragment;
import com.eniac.optimalist.fragments.ShoppingListFragment;
import com.eniac.optimalist.services.AlertReminder;
import com.eniac.optimalist.services.LocationService;
import com.eniac.optimalist.services.RecommendationService;
import com.google.gson.Gson;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final String CHANNEL_1_ID = "Channel 1";
    LocationService mService;
    NotificationManagerCompat notificationManager;
    DBHelper db;
    private DrawerLayout drawer;
    private boolean serviceStatus=true;
    public static Intent locationIntent;
    public static RecommendationService p1;
    boolean mBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            MainActivity.this.mService = (LocationService) binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isLocationEnabled()) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        db = DBHelper.getInstance(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);


            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        addNotification();
        notificationManager = NotificationManagerCompat.from(this);
        locationIntent=new Intent(this,LocationService.class);
        bindService(locationIntent, mConnection, Context.BIND_AUTO_CREATE);
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("Granted","Permission is granted");
        }
        else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("Granted","Permission is granted");
        }
        else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
            SharedPreferences settings = getApplicationContext().getSharedPreferences("settings", 0);
            serviceStatus=settings.getBoolean("backgroundswitch",true);
            p1=RecommendationService.getInstance(getApplicationContext());
            p1.createReminderFromRecom();
            Log.d("MyLocation","p1:"+p1);
//set variables of 'myObject', etc.

            if (serviceStatus) {
            startService(locationIntent);
            Log.d("MyLocation:","hey");
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, new ShoppingListFragment());
        ft.commit();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addNotification() {
        // Builds your notification

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Channel 1");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }

    public void sendOnChannel(View v, String tempTitle,String tempMessage) {
        String title = tempTitle;
        String message = tempMessage;

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        notificationManager.notify(1, notification);
    }
    public void changeLocationServiceStatus(){
        if (serviceStatus){
            serviceStatus=false;
            stopService(locationIntent);
        }else{
            serviceStatus=true;
            startService(locationIntent);
        }
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (id == R.id.nav_shopping_lists) {
            ft.replace(R.id.content_frame, new ShoppingListFragment());
        } else if (id == R.id.nav_markets) {
            //startActivity(new Intent(this, MapsActivity.class));
            ft.replace(R.id.content_frame, new MarketFragment());
        } else if (id == R.id.nav_reminders) {
            ft.replace(R.id.content_frame, new ReminderFragment());
        } else if (id == R.id.nav_manage) {
            ft.replace(R.id.content_frame, new SettingFragment());
        } else if (id == R.id.nav_recommendation) {
            Log.d("MyLocation","aa"+((HashMap<Long,Integer>)db.getHashMap("key1")));
            Log.d("MyLocation","bb"+p1.getItemDate());

            ft.replace(R.id.content_frame, new RecommFragment());

        }

        ft.commit();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public boolean isLocationEnabled() {
        int locationMode = 0;
        String locationProviders;

        try {
            locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return locationMode != Settings.Secure.LOCATION_MODE_OFF;
    }
    public void updateAlarms(){
            Log.e("MainActivity update","Update Alarms called");
        AlarmManager alarmManager =(AlarmManager)getSystemService(Context.ALARM_SERVICE);
        List<ReminderModel> reminders = db.getAllReminders();
        Intent intent =new Intent(getApplicationContext(),AlertReminder.class);
        for(ReminderModel rem:reminders){
            try {
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = fmt.parse(rem.getReminder_time());
                Calendar c=Calendar.getInstance();
                c.setTime(date);
                if(date.before(Calendar.getInstance().getTime()))
                    continue;
                intent.putExtra("send_rem_title",rem.getTitle());
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                SimpleDateFormat fmtOut = new SimpleDateFormat("d MMM yyyy - HH:mm:ss");
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),pendingIntent);
                Log.e("Reminder time",fmtOut.format(date));
            } catch (ParseException e) {

            }
        }
    }

}