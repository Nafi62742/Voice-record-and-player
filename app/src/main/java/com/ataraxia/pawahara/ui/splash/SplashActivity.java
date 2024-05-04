package com.ataraxia.pawahara.ui.splash;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ataraxia.pawahara.BuildConfig;
import com.ataraxia.pawahara.MainActivity;
import com.ataraxia.pawahara.R;
import com.ataraxia.pawahara.databinding.ActivitySplashBinding;
import com.ataraxia.pawahara.subscription.SubscriptionManager;
import com.ataraxia.pawahara.ui.thankyou.ThankyouActivity;
import com.ataraxia.pawahara.utils.CommonUtils;
import com.ataraxia.pawahara.utils.PreferenceUtils;

public class SplashActivity extends AppCompatActivity {
    ActivitySplashBinding activitySplashBinding;
    int appState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activitySplashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        appState = PreferenceUtils.getState(this);
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;

        Log.d(TAG, "versionCode: " + versionCode);
        Log.d(TAG, "versionName: " + versionName);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (appState >= 0) {
                    SubscriptionManager subscriptionManager = new SubscriptionManager(SplashActivity.this);
                    subscriptionManager.checkSubscription();
                    navigateToMainActivity();
                } else {
                    navigateToPlanActiivity();
                }
            }
        }, 2000);

    }

    private void navigateToMainActivity() {
        CommonUtils.startNewActivityAndFinishCurrent(this, MainActivity.class);
    }

    private void navigateToPlanActiivity() {
        CommonUtils.startNewActivityAndFinishCurrent(this, ThankyouActivity.class);
    }
}