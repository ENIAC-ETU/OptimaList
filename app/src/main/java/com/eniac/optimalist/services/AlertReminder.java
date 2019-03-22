package com.eniac.optimalist.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.eniac.optimalist.MainActivity;
import com.eniac.optimalist.NotificationSystem;
import com.eniac.optimalist.database.DBHelper;
import com.eniac.optimalist.database.model.ReminderModel;

import java.util.Calendar;
import java.util.Date;

public class AlertReminder extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationSystem n =NotificationSystem.getInstance();
        Bundle extras=intent.getExtras();
        String remTitle=extras.getString("send_rem_title");
        Long shopId=extras.getLong("send_shop_id");
        Long remId=extras.getLong("send_rem_id");
        SharedPreferences reminder = context.getSharedPreferences("recom_reminder", 0);
        Long redid=reminder.getLong("recom_reminder",-1);
        if (remId==redid && remId!=-1){
            replaceEveryDayReminder(context,remId);
        }
        n.setNotification(context,"Bir hatırlatıcınız var","ShopList:"+shopId+":"+remTitle+" Hatırlatıcısının zamanı geldi :)",1);
    }

    private void replaceEveryDayReminder(Context e,long id) {
        DBHelper db=DBHelper.getInstance(e);
        ReminderModel temp=db.getReminder(id);
        Calendar c=Calendar.getInstance();
        c.set(Calendar.DAY_OF_YEAR,c.get(Calendar.DAY_OF_YEAR)+1);
        c.set(Calendar.SECOND,0);
        Date date = c.getTime();
        String strDate=c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH)+" "+c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE)+":00";
         Log.d("MyLocation","date:"+strDate);
        temp.setReminder_time(strDate);
        db.updateReminder(temp);

    }
}
