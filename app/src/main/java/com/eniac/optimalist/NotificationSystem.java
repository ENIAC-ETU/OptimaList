package com.eniac.optimalist;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.eniac.optimalist.activities.ItemActivity;


public class NotificationSystem {
    private static NotificationSystem notifyManager;
    private NotificationSystem(){

        if (notifyManager != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public synchronized static NotificationSystem getInstance(){
        if (notifyManager == null){ //if there is no instance available... create new one
            notifyManager = new NotificationSystem();
        }

        return notifyManager;
    }

    public void setNotification(Context context, String notificationTitle, String notificationMessage, int notificationRequestCode) {
        String temp=notificationMessage;
        Intent     intent= new Intent(context, ItemActivity.class);

            temp=temp.replace("ShopList:","");
            temp=temp.substring(0,temp.indexOf(":"));
            String replacable="ShopList:"+temp+":";
            notificationMessage=notificationMessage.substring(notificationMessage.indexOf(replacable)+replacable.length(),notificationMessage.length());
            intent.putExtra("id",Long.parseLong(temp));

        PendingIntent contentIntent = PendingIntent.getActivity(context, notificationRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context,MainActivity.CHANNEL_1_ID)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle(notificationTitle)
                        .setColor(101)
                        .setContentText(notificationMessage)
                        .addAction( R.drawable.common_google_signin_btn_icon_dark,"Go To Shopping List", contentIntent)
                ;

        builder.setContentIntent(contentIntent);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
        Log.i("MyLocation:", "notification sended");

    }
}
