package com.ataraxia.pawahara.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.ataraxia.pawahara.MainActivity;
import com.ataraxia.pawahara.R;
import com.ataraxia.pawahara.service.Entireapp;


public class NotificationUtils {

    public static final String CHANNEL_ID = "my_channel_id"; // Define it as public static
    private static final String CHANNEL_NAME = "My Channel";


    public static void createNotificationChannel(Context context) {

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("Notification Chnnel", "createNotificationChannel: ");
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public static void toggleNotificationChannel(Context context) {

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setAction("NOTIFICATION_TAPPED");
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE // Use FLAG_IMMUTABLE
        );

        // Create and configure the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationUtils.CHANNEL_ID)
                .setSmallIcon(R.drawable.heart_icon)
                .setContentTitle(context.getResources().getString(R.string.powerharras_text1)+" "+context.getResources().getString(R.string.powerharras_text2))
                .setContentText(context.getResources().getString(R.string.please_open_the_app_to_start_recording))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        // Create a unique notification ID
        int notificationId = (int) System.currentTimeMillis();

        // Set notification style for heads-up display
        builder.setDefaults(Notification.DEFAULT_ALL);

        // Show the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, builder.build());
    }


}
