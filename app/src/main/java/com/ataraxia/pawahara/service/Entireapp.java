package com.ataraxia.pawahara.service;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

public class Entireapp extends Application {
//    private static Boolean visibleActivityCount = true;
    private boolean isAppInForeground = false;
    private boolean isAppInBacground = false;
    @Override
    public void onCreate() {
        super.onCreate();

        // Set the default night mode to MODE_NIGHT_NO to turn off dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

    }

    public boolean isAppInForeground() {
        return isAppInForeground;
    }
    public boolean isAppInBacground() {
        return isAppInBacground;
    }
    public void setAppInForeground(boolean isAppInForeground) {
        this.isAppInForeground = isAppInForeground;
    }
    public void setAppInBackground(boolean isAppInBacground) {
        this.isAppInBacground = isAppInBacground;
    }
}
