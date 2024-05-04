package com.ataraxia.pawahara.utils;

import static android.content.ContentValues.TAG;
import static android.content.Context.ACTIVITY_SERVICE;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ImageButton;

import com.ataraxia.pawahara.R;
import com.ataraxia.pawahara.helper.RingdroidEditActivity;
import com.ataraxia.pawahara.service.IncomingCallReceiver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CommonUtils {
    private IncomingCallReceiver incomingCallReceiver;

    public static void startNewActivityAndFinishCurrent(Activity currentActivity, Class<?> targetActivity) {
        Intent intent = new Intent(currentActivity, targetActivity);
        currentActivity.startActivity(intent);
        currentActivity.finish();
    }
    public static String getDateString(Date date) {
        Date record_date_time = date;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = dateFormat.format(record_date_time);
        return formattedDate;
    }

    public static String getTimeString(Date date) {
        Date record_date_time = date;
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String formattedDate = dateFormat.format(record_date_time);
        return formattedDate;
    }

    public static String getDateTimeString(Date date) {
        Date record_date_time = date;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedDate = dateFormat.format(record_date_time);
        return formattedDate;
    }


    public static String getPlanName(Context context, int index){
        switch (index){
            case 0:
                String plan0 = "15 "+context.getResources().getString(R.string.min)+context.getResources().getString(R.string.plan);
                return plan0;
            case 1:
                String plan1 ="1 "+context.getResources().getString(R.string.hour_plan) ;
                return plan1;
            case 2:
                String plan2 ="2 "+context.getResources().getString(R.string.hour_plan) ;
                return plan2;
            case 3:
                String plan3 ="3 "+context.getResources().getString(R.string.hour_plan) ;
                return plan3;
            default:
                return "";
        }
    }

    public static int getPlanTime(Context context,int index){
        switch (index){
            case 1:
                return 60;
            case 2:
                return 120;
            case 3:
                return 180;
            default:
                return 0;
        }
    }

    public static int getPlanMaxSize(Context context,int statusCode) {
        switch (statusCode) {
            case 15:
                return 20 * 10;
            case 60:
                return 130 * 10;
            case 120:
                return 250 * 10;
            case 180:
                return 360 * 10;
            // Add more cases as needed
            default:
                return 0;
        }
    }
    public static void checkMemory(Activity activity, String name) {
        ActivityManager activityManager = (ActivityManager) activity.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        Log.d(TAG, "onCreate: memoryInfo "+name+" "+ memoryInfo.totalMem);
        Log.d(TAG, "onCreate: memoryInfo "+name+" "+ memoryInfo.availMem);

        Log.d(TAG, "onCreate: memoryInfo "+name+" "+ memoryInfo.threshold);
    }
    public static int getTimeLimit(int statusCode) {
        switch (statusCode) {
            case 0:
                return 15;
            case 1:
                return 60;
            case 2:
                return 60*2;
            case 3:
                return 60*3;
            // Add more cases as needed
            default:
                return 0;
        }
    }
    public static long convertTimeToMillis(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

        long totalMillis = hours * 3600000L + minutes * 60000L + seconds * 1000L;
        return totalMillis;
    }

//    private void triggerphonecallListener() {
//        Log.d(TAG, "triggerphonecallListener: in phone call");
//        registerReceiver(incomingCallReceiver, new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED));
//        incomingCallReceiver = new IncomingCallReceiver();
//        incomingCallReceiver.setOnIncomingCallListener(RingdroidEditActivity.this);
//    }
//
//
//    // Call this method when you want to unregister the receiver
//    private void stopPhoneCallListener() {
//        if (incomingCallReceiver != null) {
//            try {
//                unregisterReceiver(incomingCallReceiver);
//            } catch (IllegalArgumentException e) {
//                // Handle exception if the receiver is not registered
//                e.printStackTrace();
//            }
//        }
//    }


    public static int getPlanTime(int index){
        switch (index){
            case 1:
                return 60;
            case 2:
                return 120;
            case 3:
                return 180;
            default:
                return 0;
        }
    }
    public static  void elevateButton(ImageButton button) {
        ObjectAnimator elevateAnimator = ObjectAnimator.ofFloat(button, "translationZ", 16f);
        elevateAnimator.setDuration(1000);
        elevateAnimator.start();
    }

    public static void lowerButton(ImageButton button) {
        ObjectAnimator lowerAnimator = ObjectAnimator.ofFloat(button, "translationZ", 0f);
        lowerAnimator.setDuration(1000);
        lowerAnimator.start();
    }

}

