package com.eniac.optimalist.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.eniac.optimalist.NotificationSystem;

public class AlertReminder extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationSystem n =NotificationSystem.getInstance();
        Bundle extras=intent.getExtras();
        String remTitle=extras.getString("send_rem_title");
        n.setNotification(context,"Bir hatırlatıcınız var",remTitle+" Hatırlatıcısının zamanı geldi :)",1);
    }
}
