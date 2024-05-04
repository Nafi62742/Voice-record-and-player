package com.ataraxia.pawahara.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ataraxia.pawahara.utils.NotificationUtils;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean pushEnable = intent.getBooleanExtra("is_push_enable", true);
        boolean schedule_enable = intent.getBooleanExtra("schedule_enable_code", true);
        Log.d("schedule_enable", "onReceive: "+schedule_enable);

        boolean isAppInForeground = ((Entireapp) context.getApplicationContext()).isAppInForeground();
        boolean isAppInBackground = ((Entireapp) context.getApplicationContext()).isAppInBacground();
if(schedule_enable){

    long alarm_endtime_code=intent.getLongExtra("endtime_code",0);
    Intent alarm_intent = new Intent("ALARM_EVENT");
    alarm_intent.putExtra("EXTRA_KEY", alarm_endtime_code);

    if(!isAppInForeground ||!pushEnable){

        NotificationUtils.toggleNotificationChannel(context);
        if(isAppInForeground && !pushEnable){
            LocalBroadcastManager.getInstance(context).sendBroadcast(alarm_intent);
        }
    }else{
        LocalBroadcastManager.getInstance(context).sendBroadcast(alarm_intent);
        // LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("ALARM_EVENT"));
    }
//        if(isAppInBackground){
//            LocalBroadcastManager.getInstance(context).sendBroadcast(alarm_intent);
//        }
}

        scheduleNextAlarm(context, intent);



    }

    private void scheduleNextAlarm(Context context,Intent intent) {
        boolean schedule_enable = intent.getBooleanExtra("schedule_enable_code", true);
        boolean pushEnable = intent.getBooleanExtra("is_push_enable", true);
        String request_code=intent.getStringExtra("request_code");
        int alarm_hour_code=intent.getIntExtra("alarm_hour_code",0);
        int alarm_min_code=intent.getIntExtra("alarm_min_code",0);
        long alarm_endtime_code=intent.getLongExtra("endtime_code",0);

        long nextAlarmTimeMillis = calculateNextAlarmTime(alarm_hour_code,alarm_min_code);
        Log.d("7 days alarm", "scheduleNextAlarm: "+nextAlarmTimeMillis);

        // Set the next alarm using setExactAndAllowWhileIdle
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = getPendingIntent(context,request_code,alarm_hour_code,alarm_min_code,pushEnable,alarm_endtime_code,schedule_enable);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarmTimeMillis, pendingIntent);
        } else {
            // For versions prior to Marshmallow, use setExact
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextAlarmTimeMillis, pendingIntent);
        }
    }

    private long calculateNextAlarmTime(int hour,int min) {
        // Customize this method based on your logic for determining the time for the next week
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 7); // Add 7 days for next week
        calendar.set(Calendar.HOUR_OF_DAY, hour); // Set the hour
        calendar.set(Calendar.MINUTE, min); // Set the minute
        calendar.set(Calendar.SECOND, 0); // Set the second

        return calendar.getTimeInMillis();
    }

    private PendingIntent getPendingIntent(Context context,String requestcode,int hour,int min,Boolean pushenable,long alarm_endtime_code,Boolean scheduleenable) {
        // Create and return a PendingIntent for the BroadcastReceiver
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("request_code", requestcode);
        intent.putExtra("alarm_hour_code",hour );
        intent.putExtra("alarm_min_code", min);
        intent.putExtra("is_push_enable",pushenable );
        intent.putExtra("schedule_enable_code",scheduleenable );
        intent.putExtra("endtime_code",alarm_endtime_code);
        return PendingIntent.getBroadcast(context, Integer.parseInt(requestcode), intent, PendingIntent.FLAG_MUTABLE);
    }


}
