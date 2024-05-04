package com.ataraxia.pawahara.ui.home;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ataraxia.pawahara.MainActivity;
import com.ataraxia.pawahara.model.Schedule_Pojos;
import com.ataraxia.pawahara.model.ToggleState;
import com.ataraxia.pawahara.utils.Popuputils;
import com.ataraxia.pawahara.utils.PreferenceUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class HomeViewModel extends ViewModel {
    private final Map<String, Timer> timers = new HashMap<>();

    private MutableLiveData<ToggleState> toggleSwitchState = new MutableLiveData<>(new ToggleState(false));
    private MutableLiveData<String> currenttime_txt = new MutableLiveData<>("Initial Text");
    private MutableLiveData<String> timer_current_txt = new MutableLiveData<>("Initial Text");
    private final MutableLiveData<Boolean> isWithinSchedule = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isstartrecord = new MutableLiveData<>();

    private final MutableLiveData<Boolean> scheduletimes = new MutableLiveData<>();

    public HomeViewModel() {
        scheduletimes.setValue(false);

    }

    public LiveData<String> getcurrenttime() {
        return currenttime_txt;
    }
    public LiveData<ToggleState> getToggleSwitchState() {
        return toggleSwitchState;
    }

    public void setToggleSwitchEnabled(boolean isEnabled) {
        toggleSwitchState.setValue(new ToggleState(isEnabled));
    }
    public void setscheduletimes(boolean scheduled) {
        Log.d("fetch post", "setscheduletimes: ");
        scheduletimes.postValue(scheduled);
    }
    public void setstartrecord(boolean scheduled) {
        Log.d("fetch post", "setscheduletimes: ");
        isstartrecord.setValue(scheduled);
    }  public void setwithingschedule(boolean scheduled) {

        Log.d("fetch post", "setscheduletimes: ");
        isWithinSchedule.setValue(scheduled);
    }
    public LiveData<Boolean> getIsWithinSchedule() {
        return isWithinSchedule;
    }
    public LiveData<Boolean> getisstartrecord() {
        return isstartrecord;
    }
    public LiveData<Boolean> getscheduletimes() {
        return scheduletimes;
    }

    public void update_crnt_time(String newText) {
        currenttime_txt.setValue(newText);
    }


    public void update_crnt_time2(String newText) {
        timer_current_txt.setValue(newText);
    }
    public  void check_schedule(Context context){
        String currentLanguage = Locale.getDefault().getLanguage();
        // Get the current day of the week and time
        final SimpleDateFormat dayFormat = new SimpleDateFormat("E", Locale.getDefault());
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        int day = calendar.get(Calendar.DAY_OF_WEEK);

        Log.d(ContentValues.TAG, "check_schedule1: "+day);
        String currentTime = timeFormat.format(new Date());

        // Get the list of schedules from SharedPreferences
        PreferenceUtils pref = new PreferenceUtils();
        List<Schedule_Pojos> scheduleListpreff = pref.getScheduleListFromSharedPreferences(context);

        // Iterate over the schedules
        for (Schedule_Pojos schedule : scheduleListpreff) {
            Log.d("schedule 1", "check_schedule: "+schedule.getSchedule_days()+day);

            if (schedule.getSchedule_days().contains(day)) {


                // Parse the start and end times of the schedule
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

                try {
                    Date scheduleStartTime = sdf.parse(schedule.getStart_time());
                    Date scheduleEndTime = sdf.parse(schedule.getEnd_time());
                    Date currentTimeDate = sdf.parse(currentTime);

                    // Check if the current time is within the schedule's start and end times
                    if (currentTimeDate != null && scheduleStartTime != null && scheduleEndTime != null) {
                        if (currentTimeDate.equals(scheduleStartTime)  && schedule.getSchedule_switch()|| (currentTimeDate.after(scheduleStartTime) && currentTimeDate.before(scheduleEndTime)&& schedule.getSchedule_switch())) {

                            scheduleTask(String.valueOf(getscheduletime_in_mlls(scheduleEndTime,day).getTimeInMillis()),getscheduletime_in_mlls(scheduleEndTime,day).getTimeInMillis(),context);
                            isWithinSchedule.setValue(true);
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    // Handle exception
                }
            }
        }


    }
    public String requestcodebuilder(int day,String endhour,String endmin){
        String requestCode = day + "" + endhour + "" + endmin;

        return requestCode;
    }
   public  Calendar getscheduletime_in_mlls( Date schedule_end_time,int day){
       SimpleDateFormat outputFormat = new SimpleDateFormat("HH", Locale.getDefault());
       SimpleDateFormat outputFormat2 = new SimpleDateFormat("mm", Locale.getDefault());
       int formattedTime = Integer.parseInt(outputFormat.format(schedule_end_time));
       Log.d("checking endtime", "check_schedule: endtime "+formattedTime);
       int formattedTime2 = Integer.parseInt(outputFormat2.format(schedule_end_time));
       Calendar schedulfor=Popuputils.getNextAlarmTime(day,formattedTime,formattedTime2);
       return schedulfor;
   }
    public void scheduleTask(String timerId,long targetTimeMillis,Context context) {
        Timer timer = new Timer();
        MainActivity mainActivity = (MainActivity) context;
        long delay = targetTimeMillis - System.currentTimeMillis();
        Log.d("_schedule", "scheduleTask 0: " + delay);
        if (delay > 0) {
            // The target time is in the past, adjust the delay to a non-negative value
            //delay = 0;

            Log.d("_schedule", "scheduleTask 1: " + delay);

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.d("_schedule", "run: live action"+timer);
                    //setscheduletimes(true);
                    mainActivity.stop_record_switch();
                    // Notify observers about the change in toggleSwitch state

                }
            }, delay);
            timers.put(timerId, timer);
        }
        Log.d("inputted timer", "scheduleTask: "+timers);
    }

    public void cancelTimer(String timerId) {
        Log.d("all timers 0", "cancelTimer: "+timerId);
        Log.d("all timers 1", "cancelTimer: "+timers);
        Timer timer = timers.get(timerId);
        Log.d("all timers 1.1", "cancelTimer: "+timers);
        Log.d("all timers 2", "cancelTimer: "+timer);
        if (timer != null) {
            timer.cancel();
            timers.remove(timerId);
        }
    }


}