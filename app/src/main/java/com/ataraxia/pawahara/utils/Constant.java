package com.ataraxia.pawahara.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.ataraxia.pawahara.R;

import java.util.Calendar;
import java.util.HashMap;

public class Constant {
    Context context;
    public static int freePlanType = 0;
    public static int freePlanPrice = 0;
    public static int freePlanTime = 15;
    public static int PlanType1Hour = 1;
    public static int PlanPrice1Hour = 130;
    public static int PlanTime1Hour = 60;


    public static int PlanType2Hour = 2;
    public static int PlanPrice2Hour = 260;
    public static int PlanTime2Hour = 120;
    public static int PlanType3Hour = 3;
    public static int PlanPrice3Hour = 360;
    public static int PlanTime3Hour = 180;


    public static int DEBOUNCE_INTERVAL = 1000;
    public static String GoogleLink = "https://www.google.com";
    public static String CancelSubLink = "https://www.google.com";

    public static HashMap<Integer, String> getDayMapping(Context context) {
        HashMap<Integer, String> dayMapping = new HashMap<>();

//    HashMap<Integer, String> dayMapping = new HashMap<Integer, String>() {{
        dayMapping.put(Calendar.MONDAY, context.getResources().getString(R.string.mo));
        dayMapping.put(Calendar.TUESDAY, context.getResources().getString(R.string.tu));
        dayMapping.put(Calendar.WEDNESDAY, context.getResources().getString(R.string.we));
        dayMapping.put(Calendar.THURSDAY, context.getResources().getString(R.string.th));
        dayMapping.put(Calendar.FRIDAY, context.getResources().getString(R.string.fr));
        dayMapping.put(Calendar.SATURDAY, context.getResources().getString(R.string.sa));
        dayMapping.put(Calendar.SUNDAY, context.getResources().getString(R.string.su));
//    }};
        return dayMapping;
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

}
