package com.eniac.optimalist;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


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
        Log.i("MyLocation:", "notification sended");

    }
}
