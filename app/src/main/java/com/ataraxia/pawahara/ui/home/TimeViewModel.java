package com.ataraxia.pawahara.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class TimeViewModel extends ViewModel {

    private final MutableLiveData<String> currentTime = new MutableLiveData<>();
    private final MutableLiveData<String> currentDate = new MutableLiveData<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault()); // Use HH for 24-hour format
    SimpleDateFormat timer_dateFormat = new SimpleDateFormat("yyyy-MM-dd E", Locale.getDefault()); // Use HH for 24-hour format
    private Timer timer;

    public TimeViewModel() {
        // Calculate the initial delay to sync with the next minute change
        Calendar calendar = Calendar.getInstance();
        long currentTimeMillis = System.currentTimeMillis();
        calendar.setTimeInMillis(currentTimeMillis);
        int seconds = calendar.get(Calendar.SECOND);
        long initialDelay = 60000 - (seconds * 1000);
        String time = timeFormat.format(new Date());
        String date = timer_dateFormat.format(new Date());
        currentDate.postValue(date);
        currentTime.postValue(time);
        // Update the time every minute
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String time = timeFormat.format(new Date());
                String date = timer_dateFormat.format(new Date());
                currentTime.postValue(time);
                currentDate.postValue(date);
            }
        }, initialDelay, 60000); // Start with the initial delay and then repeat every minute
    }

    public LiveData<String> getCurrentTime() {
        return currentTime;
    }
    public LiveData<String> getCurrentDate() {
        return currentDate;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
