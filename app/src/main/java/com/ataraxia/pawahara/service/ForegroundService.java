package com.ataraxia.pawahara.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.ataraxia.pawahara.MainActivity;
import com.ataraxia.pawahara.R;

public class ForegroundService extends Service {
    private static final int FOREGROUND_SERVICE_ID = 1;
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    @Override
    public void onCreate() {
        super.onCreate();
        // Start your background tasks, such as updating the AlarmManager here.

        // Create a notification for the foreground service.
//        Notification notification = createNotification();
//        startForeground(FOREGROUND_SERVICE_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class); // Replace with your main activity class
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.recording))
                .setSmallIcon(R.drawable.heart_icon) // Replace with your notification icon
                .setContentIntent(pendingIntent)
                .build();
//        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setSmallIcon(android.R.color.transparent) // Use a transparent icon or your app's icon
//                .setContentTitle("") // Empty content title
//                .setContentText("") // Empty content text
//                .setVisibility(NotificationCompat.VISIBILITY_SECRET) // Make it less visible
//                .build();

        startForeground(1, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop your background tasks and any ongoing work.
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Foreground Service Channel";
            String description = "This is the channel for the Foreground Service";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
