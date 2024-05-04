package com.ataraxia.pawahara.utils;


import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;


import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.ataraxia.pawahara.service.Entireapp;
import com.ataraxia.pawahara.subscription.Subs;
import com.ataraxia.pawahara.subscription.SubscriptionManager;
import com.ataraxia.pawahara.ui.home.HomeViewModel;
import com.ataraxia.pawahara.ui.splash.SplashActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.ataraxia.pawahara.MainActivity;

import com.ataraxia.pawahara.R;
import com.ataraxia.pawahara.adapter.ScheduleRecyclerViewAdapter;

import com.ataraxia.pawahara.databinding.AddingPopupLayoutBinding;
import com.ataraxia.pawahara.databinding.CommonPopupLayoutBinding;
import com.ataraxia.pawahara.databinding.PlanDetailsPopupLayoutBinding;
import com.ataraxia.pawahara.databinding.PlayTrimPopupLayoutBinding;

import com.ataraxia.pawahara.helper.CustomDialog;
import com.ataraxia.pawahara.helper.CustomDialogBtn;
import com.ataraxia.pawahara.helper.RingdroidEditActivity;
import com.ataraxia.pawahara.helper.ScheduleDeleteDialog;
import com.ataraxia.pawahara.model.Plan_pojos;
import com.ataraxia.pawahara.model.Records_pojos;
import com.ataraxia.pawahara.model.Schedule_Pojos;
import com.ataraxia.pawahara.service.AlarmReceiver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class Popuputils {
    public static void showCustomDialog(Context context, String message) {
        CustomDialog customDialog = new CustomDialog(context, message);
        customDialog.show();
    }

    public static void showCustomDialogWithBtn(Context context, String message, String btnText, CustomDialogBtn.OnOkButtonClickListener callback) {
        CustomDialogBtn customDialogBtn = new CustomDialogBtn(context, message, btnText);

        // Set the listener for the "OK" button click
        customDialogBtn.setOnOkButtonClickListener(callback);

        customDialogBtn.show();
    }

    private static class ReadFileTask extends AsyncTask<String, Void, SpannableString> {

        private TextView description;

        ReadFileTask(TextView description) {
            this.description = description;
        }

        @Override
        protected SpannableString doInBackground(String... params) {
            StringBuilder stringBuilder = new StringBuilder();
            try {
                AssetManager assetManager = description.getContext().getAssets();
                InputStream inputStream = assetManager.open(params[0]);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }

                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Create a SpannableString with a ClickableSpan for the link
            SpannableString spannableString = new SpannableString(stringBuilder.toString());
            String linkText = "https://pawa-amu.com/pp/";
            int start = stringBuilder.indexOf(linkText);
            int end = start + linkText.length();

            if (start != -1) {
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        // Handle the click event, for example, open a web page
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkText));
                        description.getContext().startActivity(intent);
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(true);
                        // You can customize other text appearance properties here
                    }
                };

                spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            return spannableString;
        }

        @Override
        protected void onPostExecute(SpannableString result) {
            // Set the SpannableString to the TextView
            description.setText(result);
            // Make the links clickable
            description.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }


    public static void showCommonPopup(Context context, String title, String message, String buttonText, View.OnClickListener onButtonClick) {
        CommonPopupLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.common_popup_layout, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(binding.getRoot());
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.show();

        TextView popupText = binding.popupText;
        TextView description = binding.description;
        ImageView closeButton = binding.cancelIcon;
        CardView customButton = binding.customButton;
        TextView customText = binding.buttonText;

        popupText.setText(title);
        if (message.equals("Terms")) {
            String language = context.getResources().getConfiguration().locale.getLanguage();
            String textSource = language.equals("ja") ? "terms_ja.txt" : "terms_en.txt";

            new ReadFileTask(description).execute(textSource);
        } else if (message.equals("Policy")) {
            String language = context.getResources().getConfiguration().locale.getLanguage();
            String textSource = language.equals("ja") ? "policy_ja.txt" : "policy_en.txt";

            new ReadFileTask(description).execute(textSource);

        } else {
            description.setText(message);
        }


        if (buttonText.equals("")) {
            customButton.setVisibility(View.INVISIBLE);
        } else {
            customText.setText(buttonText);
        }
        Log.d("showCommonPopup: ", onButtonClick.toString());
        customButton.setOnClickListener(onButtonClick);

        closeButton.setOnClickListener(v -> {
            alertDialog.cancel();
        });
    }


    public static void showPlanDetailsPopup(Context context, View.OnClickListener onButtonClick, Plan_pojos plan, Activity activity) {


        PlanDetailsPopupLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.plan_details_popup_layout, null, false);
        TextView popuptime = binding.planPopupTimeTV;
        TextView popup_plan_name = binding.planPopupPlannameTV;
        TextView popup_plan_price = binding.planPopupPriceTV;
        TextView popup_plan_size = binding.planPopupSizeTV;
        ImageView closeButton = binding.cancelIcon;
        CardView popup_decide_btn = binding.planPopupBTN;

        int timeInHours = plan.getminuates();

        if (timeInHours >= 60) {
            timeInHours /= 60;
        }
        String planExtension = "";
        String time = String.valueOf(timeInHours);
        if (timeInHours == 15) {
            planExtension = context.getString(R.string.min);
        } else {
            planExtension = context.getString(R.string.hour_plan);
        }

        int planSize = CommonUtils.getPlanMaxSize(context, plan.getminuates());
        String size = planSize + context.getResources().getString(R.string.mb);
        popup_plan_size.setText(size);

        String suffixTime = context.getString(R.string.master_plan_approx);
        String planTime = suffixTime + time + " " + planExtension;

        popuptime.setText(planTime);
        popup_plan_name.setText(plan.getName());
        String price = String.valueOf(plan.getPrice());

        String planPrice = price + " " + (context.getString(R.string.plan_price_extension));
        if (timeInHours == 15) {
            planPrice = context.getResources().getString(R.string.free);
        }
        popup_plan_price.setText(planPrice);


        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setView(binding.getRoot());
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.show();

        popup_decide_btn.setOnClickListener(v -> {
            // Check the condition before navigating to MainActivity
            int planType = plan.getplantype();

//            if (planType >= 1) {
//                PreferenceUtils.saveState(context, planType);
//                Log.d(TAG, "showPlanDetailsPopup: planType " + planType);
//                if (!(context instanceof MainActivity)) {
////                    CommonUtils.startNewActivityAndFinishCurrent((Activity) context, MainActivity.class);
//                    SubscriptionManager subscriptionManager = new SubscriptionManager(context);
//                    subscriptionManager.subscriptionTrigger(activity, planType);
//                } else {
////                    CommonUtils.startNewActivityAndFinishCurrent((Activity) context, SplashActivity.class);
//                    SubscriptionManager subscriptionManager = new SubscriptionManager(context);
//                    subscriptionManager.subscriptionTrigger(activity, planType);
//                }
//                alertDialog.cancel();
//            } else
                if (planType >= 0) {

                PreferenceUtils.saveState(context, planType);
                Log.d(TAG, "showPlanDetailsPopup: planType " + planType);
                if (!(context instanceof MainActivity)) {
                    CommonUtils.startNewActivityAndFinishCurrent((Activity) context, MainActivity.class);
//                    SubscriptionManager subscriptionManager = new SubscriptionManager(context);
//                    subscriptionManager.subscriptionTrigger(activity,planType);
                } else {
                    CommonUtils.startNewActivityAndFinishCurrent((Activity) context, SplashActivity.class);
//                    SubscriptionManager subscriptionManager = new SubscriptionManager(context);
//                    subscriptionManager.subscriptionTrigger(activity,planType);
                }
                alertDialog.cancel();
            }
        });

        closeButton.setOnClickListener(v -> {
            alertDialog.cancel();

        });
    }

    public static void showAddingDataPopup(Context context, int position, List<Schedule_Pojos> scheduleList, ScheduleRecyclerViewAdapter adapter, Schedule_Pojos schedule_pojos) {
        AddingPopupLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.adding_popup_layout, null, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        int s_hour = 12;
        int s_min = 0;
        int e_hour = 12;
        int e_min = 0;
        boolean newData;

        HomeViewModel homeViewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(HomeViewModel.class);


        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        if (schedule_pojos.getSchedule_name() != null) {
            String[] parts = schedule_pojos.getStart_time().split(":");
            String[] parte = schedule_pojos.getEnd_time().split(":");
            if (parts.length == 2) {
                try {
                    s_hour = Integer.parseInt(parts[0]);
                    s_min = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    // Handle parsing error

                }
            } else {
                // Handle invalid time string
                System.err.println("Invalid time string: ");
            }
            if (parte.length == 2) {
                try {
                    e_hour = Integer.parseInt(parte[0]);
                    e_min = Integer.parseInt(parte[1]);
                } catch (NumberFormatException e) {
                    // Handle parsing error

                }
            } else {
                // Handle invalid time string
                System.err.println("Invalid time string: ");
            }
        }

        MaterialTimePicker.Builder picker_builder1 = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(s_hour)
                .setMinute(s_min)
                .setTitleText(R.string.select_schedule_time)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK);
        MaterialTimePicker.Builder picker_builder2 = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(e_hour)
                .setMinute(e_min)
                .setTitleText(R.string.select_schedule_time)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK);

        MaterialTimePicker timePicker1 = picker_builder1.build();
        MaterialTimePicker timePicker2 = picker_builder2.build();

        Intent intent = new Intent(context, AlarmReceiver.class);

        // Access views using Data Binding
        ImageView cancelIcon = binding.cancelIcon;
        TextView cancelSchedule = binding.cancelSchedule;
        CardView saveButton = binding.saveScheduleBtn;
        PreferenceUtils pref = new PreferenceUtils();
        EditText nameText = binding.outlinedTextField;
        TextView startTime = binding.startTime;
        TextView endTime = binding.endTime;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch toggleSwitch = binding.notificationSwitch;
        toggleSwitch.setChecked(true);
        ChipGroup chipGroup = binding.weekDays; // Replace with your actual ChipGroup ID
        List<Integer> checkedChips = new ArrayList<>();
//        TextInputLayout outlinedTextField = binding.outlinedTextField;

        final AtomicBoolean istimepickeropen = new AtomicBoolean(false);
        startTime.setOnClickListener(new View.OnClickListener() {
            long DEBOUNCE_INTERVAL = 1000; // Set the debounce interval in milliseconds
            long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                startTime.setError(null);

                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime > DEBOUNCE_INTERVAL) {
                    istimepickeropen.set(true);
                    timePicker1.show(((FragmentActivity) context).getSupportFragmentManager(), "androidknowledge");
                    lastClickTime = currentTime;
                }

                // Use context and getSupportFragmentManager
                timePicker1.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    boolean positiveButtonClicked = false;

                    @Override
                    public void onClick(View v) {
                        if (!positiveButtonClicked) {

                            int hour = timePicker1.getHour();
                            int minute = timePicker1.getMinute();
                            String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                            startTime.setText(selectedTime);

                            calendar.set(Calendar.HOUR_OF_DAY, timePicker1.getHour());
                            calendar.set(Calendar.MINUTE, timePicker1.getMinute());
                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MILLISECOND, 0);
                            timePicker1.dismiss();
                        }
                    }
                });
            }
        });
        endTime.setOnClickListener(new View.OnClickListener() {
            long DEBOUNCE_INTERVAL = 1000; // Set the debounce interval in milliseconds
            long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                endTime.setError(null);

                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime > DEBOUNCE_INTERVAL) {
                    timePicker2.show(((FragmentActivity) context).getSupportFragmentManager(), "androidknowledge");// Use context and getSupportFragmentManager

                    lastClickTime = currentTime;
                }

                timePicker2.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    boolean popUpTrack = false;

                    @Override
                    public void onClick(View v) {
                        Log.d("checkMulti", "onClick: " + popUpTrack);
                        int hour = timePicker2.getHour();
                        int minute = timePicker2.getMinute();

                        if (!popUpTrack) { // Check if the pop-up has been shown already
                            if (calendar.get(Calendar.HOUR_OF_DAY) < hour) {
                                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                                endTime.setText(selectedTime);

                                timePicker2.dismiss();
                            } else if (calendar.get(Calendar.HOUR_OF_DAY) == hour) {
                                if (calendar.get(Calendar.MINUTE) <= minute) {
                                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                                    endTime.setText(selectedTime);

                                    timePicker2.dismiss();
                                } else {
                                    Popuputils.showCustomDialog(v.getContext(), v.getContext().getResources().getString(R.string.sorry_wrong_minute));
                                    popUpTrack = true; // Set the flag to true after showing the pop-up
                                }
                            } else {
                                Log.d("checkMulti1", "onClick: ");
                                Popuputils.showCustomDialog(v.getContext(), v.getContext().getResources().getString(R.string.sorry_wrong_hour));
                                popUpTrack = true; // Set the flag to true after showing the pop-up
                            }
                        }
                    }
                });
            }
        });

        HashMap<Integer, Integer> dayMapping = new HashMap<Integer, Integer>() {{
            put(R.id.chipMonday, Calendar.MONDAY);
            put(R.id.chipTuesday, Calendar.TUESDAY);
            put(R.id.chipWednesday, Calendar.WEDNESDAY);
            put(R.id.chipThursday, Calendar.THURSDAY);
            put(R.id.chipFriday, Calendar.FRIDAY);
            put(R.id.chipSaturday, Calendar.SATURDAY);
            put(R.id.chipSunday, Calendar.SUNDAY);
        }};

        builder.setView(binding.getRoot());
        if (schedule_pojos.getSchedule_name() == null) {
            newData = false;
            schedule_pojos.setSchedule_push_switch(true);
            schedule_pojos.setSchedule_switch(true);
            cancelSchedule.setVisibility(View.GONE);
        } else {
            newData = true;

//            Toast.makeText(context, String.valueOf(position), Toast.LENGTH_SHORT).show();
            assert nameText != null;
//            outlinedTextField.setHint("");
            nameText.setText(String.valueOf(schedule_pojos.getSchedule_name()));
            startTime.setText(String.valueOf(schedule_pojos.getStart_time()));
            endTime.setText(String.valueOf(schedule_pojos.getEnd_time()));
            List<Integer> scheduleDays = schedule_pojos.getSchedule_days();


            toggleSwitch.setChecked(schedule_pojos.getSchedule_push_switch());

            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                View child = chipGroup.getChildAt(i);
                if (child instanceof Chip) {
                    Chip chip = (Chip) child;
//                    String chipText = chip.getText().toString();

                    boolean isSelected = scheduleDays.contains(dayMapping.get(chip.getId()));
                    if (isSelected) {
                        String fullDayName = String.valueOf(dayMapping.get(chip.getId()));
                        chip.setChipStrokeColor(ColorStateList.valueOf(Color.RED));
                        chip.setTextColor(ColorStateList.valueOf(Color.RED));
                        checkedChips.add(Integer.valueOf(fullDayName));  // Assuming you want to add chipText instead of fullDayName
                    }
                    // Set the checked state of the chip based on whether it's in the scheduleDays list
                    chip.setChecked(isSelected);
                }
            }

        }
        builder.setCancelable(false);

        // Create the AlertDialog and set properties
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        int finalS_hour2 = s_hour;
        int finalS_min2 = s_min;
        boolean finalNewData = newData;
        saveButton.setOnClickListener(new View.OnClickListener() {
            long DEBOUNCE_INTERVAL = 1000; // Set the debounce interval in milliseconds
            long lastClickTime = 0;

            @SuppressLint("ScheduleExactAlarm")
            @Override
            public void onClick(View v) {

//                Toast.makeText(context, startTime.getText(), Toast.LENGTH_SHORT).show();

                // Validate start time
                if (TextUtils.equals(startTime.getText(), context.getString(R.string.default_time))) {
//                    startTime.setError("Start time is required");
                    Popuputils.showCustomDialog(context, context.getString(R.string.start_time_is_required));

                    return;
                }

                // Validate end time
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

                try {
                    // Parse start time
                    Date startTimeDate = timeFormat.parse(startTime.getText().toString());

                    // Parse end time
                    Date endTimeDate = timeFormat.parse(endTime.getText().toString());

                    // Check if start time is less than end time
                    if (startTimeDate != null && endTimeDate != null && startTimeDate.before(endTimeDate)) {
                        // Valid case
                    } else if (endTimeDate.before(startTimeDate)) {
                        Popuputils.showCustomDialog(context, context.getString(R.string.need_proper_end_time));
                        return;
                    } else {
                        // Invalid case
                        // Show an error message or handle the invalid scenario
//                        endTime.setError("Proper end time is required");
                        Popuputils.showCustomDialog(context, context.getString(R.string.end_time_is_required));
                        return;
                    }

                } catch (ParseException e) {
                    // Handle parsing exception, e.g., invalid time format
                    e.printStackTrace();
                }
                if (checkedChips.isEmpty()) {
                    Popuputils.showCustomDialog(context, context.getString(R.string.no_days_selected));
                    return;
                }
                if (TextUtils.isEmpty(nameText.getText())) {
//                    nameText.setError("Schedule name is required");
                    Popuputils.showCustomDialog(context, context.getString(R.string.schedule_name_is_required));

                    return;
                }


                // Validate at least one day is selected
                String startTimeStr = startTime.getText().toString();
                String endTimeStr = endTime.getText().toString();
                try {
                    Date startTimeDate = sdf.parse(startTimeStr);
                    Date endTimeDate = sdf.parse(endTimeStr);

                    if (endTimeDate.after(startTimeDate)) {

                        if (schedule_pojos.getSchedule_name() == null) {
                            if (!finalNewData) {
                                String scheduleName = nameText.getText().toString();
                                if (doesScheduleExist(context, scheduleName)) {
                                    // Schedule with the same name already exists, handle accordingly
                                    Popuputils.showCustomDialog(context, context.getString(R.string.schedule_with_the_same_name_already_exists));
                                    return;
                                }
                            } else {
                                String scheduleName = nameText.getText().toString();
                                if (doesScheduleExist(context, scheduleName) && scheduleName != schedule_pojos.getSchedule_name()) {
                                    // Schedule with the same name already exists, handle accordingly
                                    Popuputils.showCustomDialog(context, context.getString(R.string.schedule_with_the_same_name_already_exists));
                                    return;
                                }
                            }
                            if (!checkedChips.isEmpty()) {
                                assert nameText != null;
                                Collections.sort(checkedChips);

                                scheduleList.add(new Schedule_Pojos(true, nameText.getText().toString(),
//                                Arrays.asList(cleanedString),
                                        new ArrayList<>(checkedChips)
                                        , startTime.getText().toString(), endTime.getText().toString(), schedule_pojos.getSchedule_push_switch()));
//add to shared preference
                                List<Schedule_Pojos> scheduleListpreff = pref.getScheduleListFromSharedPreferences(context);
                                Schedule_Pojos newSchedule = new Schedule_Pojos(
                                        true,
                                        nameText.getText().toString(),
                                        new ArrayList<>(checkedChips), startTime.getText().toString(),
                                        endTime.getText().toString(),
                                        schedule_pojos.getSchedule_push_switch()
                                );
                                scheduleListpreff.add(newSchedule);

                                String[] timeParts = endTime.getText().toString().split(":");
                                int hours = Integer.parseInt(timeParts[0]);
                                int minutes = Integer.parseInt(timeParts[1]);
                                intent.putExtra("is_push_enable", schedule_pojos.getSchedule_push_switch());
                                pref.saveScheduleListToSharedPreferences(context, scheduleListpreff);

//                        set notifier

                                for (int i = 0; i < checkedChips.size(); i++) {

                                    Log.d("daystring", "onClick: " + checkedChips);
                                    int dayOfWeek = checkedChips.get(i);


                                    Log.d("daymapper 1", "onClick: 12h " + dayOfWeek + calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE));

                                    Calendar nextAlarmTime = getNextAlarmTime(dayOfWeek, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
                                    Calendar endAlarmTime = getNextAlarmTime(dayOfWeek, hours, minutes);

                                    String requestCode = dayOfWeek + "" + calendar.get(Calendar.HOUR_OF_DAY) + "" + calendar.get(Calendar.MINUTE);
                                    intent.putExtra("endtime_code", endAlarmTime.getTimeInMillis());
                                    intent.putExtra("request_code", requestCode);
                                    intent.putExtra("is_push_enable", true);
                                    intent.putExtra("alarm_hour_code", calendar.get(Calendar.HOUR_OF_DAY));
                                    intent.putExtra("alarm_min_code", calendar.get(Calendar.MINUTE));
                                    intent.putExtra("schedule_enable_code", true);
                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(requestCode), intent, PendingIntent.FLAG_MUTABLE);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        if (!schedule_pojos.getSchedule_push_switch()) {
                                            intent.putExtra("is_push_enable", false);

                                        }

                                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarmTime.getTimeInMillis(), pendingIntent);

                                        Log.d("set timer", "onClick: " + nextAlarmTime.getTimeInMillis());


                                    }
                                }


                                adapter.notifyDataSetChanged();

                                checkedChips.clear();
                                alertDialog.dismiss();
                            } else {

                            }
                        } else {
                            if (!checkedChips.isEmpty()) {
                                // Notify the adapter of the updated item
                                Collections.sort(checkedChips);
                                scheduleList.set(position,
                                        new Schedule_Pojos(schedule_pojos.getSchedule_switch(), nameText.getText().toString(),
                                                new ArrayList<>(checkedChips),
                                                startTime.getText().toString(), endTime.getText().toString(), schedule_pojos.getSchedule_push_switch()));

                                Log.d("alarmpush", "onClick: " + schedule_pojos.getSchedule_push_switch());
                                Log.d("alarmpush switch", "onClick: " + schedule_pojos.getSchedule_switch());
                                intent.putExtra("is_push_enable", schedule_pojos.getSchedule_push_switch());
                                intent.putExtra("schedule_enable_code", schedule_pojos.getSchedule_switch());
                                String[] timeParts = endTime.getText().toString().split(":");
                                int hours = Integer.parseInt(timeParts[0]);
                                int minutes = Integer.parseInt(timeParts[1]);


                                for (int i = 0; i < checkedChips.size(); i++) {
                                    int dayOfWeek = checkedChips.get(i);
                                    Calendar nextAlarmTime;
                                    Calendar endAlarmTime;
                                    String requestCode;
                                    PendingIntent pendingIntent;

                                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    try {
                                        Date scheduleEndTime = sdf.parse(schedule_pojos.getEnd_time());
                                        String endtime_inmlls = String.valueOf(homeViewModel.getscheduletime_in_mlls(scheduleEndTime, dayOfWeek).getTimeInMillis());
                                        homeViewModel.cancelTimer(endtime_inmlls);
                                    } catch (ParseException e) {
                                        throw new RuntimeException(e);
                                    }

                                    if (istimepickeropen.get()) {
                                        Log.d("checked request code", "request code: time picker " + timePicker1.getHour());
                                        Log.d("daymapper", "onC12hlick:  " + dayOfWeek + timePicker1.getHour() + timePicker1.getMinute());

                                        nextAlarmTime = getNextAlarmTime(dayOfWeek, timePicker1.getHour(), timePicker1.getMinute());
                                        endAlarmTime = getNextAlarmTime(dayOfWeek, hours, minutes);
                                        Log.d(TAG, "onClick check timer: " + endAlarmTime.getTimeInMillis());

                                        homeViewModel.scheduleTask(String.valueOf(endAlarmTime.getTimeInMillis()), endAlarmTime.getTimeInMillis(), context);
                                        requestCode = dayOfWeek + "" + finalS_hour2 + "" + finalS_min2;
                                        pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(requestCode), intent, PendingIntent.FLAG_MUTABLE);
                                        alarmManager.cancel(pendingIntent);
                                        requestCode = dayOfWeek + "" + timePicker1.getHour() + "" + timePicker1.getMinute();
                                        intent.putExtra("endtime_code", endAlarmTime.getTimeInMillis());
                                        intent.putExtra("request_code", requestCode);
                                        intent.putExtra("alarm_hour_code", timePicker1.getHour());
                                        intent.putExtra("alarm_min_code", timePicker1.getMinute());

                                        pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(requestCode), intent, PendingIntent.FLAG_MUTABLE);


                                    } else {
                                        nextAlarmTime = getNextAlarmTime(dayOfWeek, finalS_hour2, finalS_min2);
                                        endAlarmTime = getNextAlarmTime(dayOfWeek, hours, minutes);
                                        homeViewModel.scheduleTask(String.valueOf(endAlarmTime.getTimeInMillis()), endAlarmTime.getTimeInMillis(), context);
                                        requestCode = dayOfWeek + "" + finalS_hour2 + "" + finalS_min2;
                                        intent.putExtra("request_code", requestCode);
                                        intent.putExtra("endtime_code", endAlarmTime.getTimeInMillis());
                                        intent.putExtra("alarm_hour_code", finalS_hour2);
                                        intent.putExtra("alarm_min_code", finalS_min2);
                                        pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(requestCode), intent, PendingIntent.FLAG_MUTABLE);
                                        alarmManager.cancel(pendingIntent);
                                    }

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        if (!schedule_pojos.getSchedule_push_switch()) {
                                            intent.putExtra("is_push_enable", false);
                                        }
                                        Log.d("checking", "onClick: " + "one time");
                                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarmTime.getTimeInMillis(), pendingIntent);

                                    }

                                }

                                adapter.notifyDataSetChanged();


                                pref.saveScheduleListToSharedPreferences(context, scheduleList);


//                        checkedChips.clear();
                                alertDialog.dismiss();
                            } else {

                            }

                        }
                    } else {
                        Popuputils.showCustomDialog(context, context.getResources().getString(R.string.wrong_time_input));
//                Toast.makeText(context, "time wrongly inputed", Toast.LENGTH_SHORT).show();
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                long currentTime = System.currentTimeMillis();
                lastClickTime = currentTime;
            }

        });

        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
//                String chipText = chip.getText().toString();
//                String fullDayName = dayMapping.get(chip.getId());
                int dayNumber = dayMapping.get(chip.getId());


                chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            chip.setChipStrokeColor(ColorStateList.valueOf(Color.RED));
                            chip.setTextColor(ColorStateList.valueOf(Color.RED));

                            checkedChips.add(dayNumber);
                        } else {
                            chip.setChipStrokeColor(ColorStateList.valueOf(Color.GRAY));
                            chip.setTextColor(ColorStateList.valueOf(Color.GRAY));
                            checkedChips.remove((Integer) dayNumber);
                        }
                    }
                });
            }
        }
        alertDialog.show();
        toggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                schedule_pojos.setSchedule_push_switch(isChecked);

//                Toast.makeText(context, "notification "+isChecked, Toast.LENGTH_SHORT).show();
            }
        });
        // Set a click listener for the cancel icon
        cancelIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkedChips.clear();
                alertDialog.dismiss();
            }
        });
        int finalS_hour = s_hour;
        int finalS_min = s_min;
        int finalE_hour = e_hour;
        cancelSchedule.setOnClickListener(new View.OnClickListener() {
            long DEBOUNCE_INTERVAL = 1000; // Set the debounce interval in milliseconds
            long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime > DEBOUNCE_INTERVAL) {
                    if (schedule_pojos.getSchedule_name() == null) {
                        schedule_pojos.setSchedule_push_switch(true);
                        schedule_pojos.setSchedule_switch(false);
                        checkedChips.clear();
                        alertDialog.dismiss();

                    } else {
                        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
                            public void handleMessage(Message response) {

                                List<Integer> scheduleDays = schedule_pojos.getSchedule_days();
                                String end_time = schedule_pojos.getEnd_time();


                                Log.d("_cancel", "handleMessage: cancel" + scheduleList.get(position).getStart_time());
                                for (int i = 0; i < scheduleDays.size(); i++) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    try {
                                        Date scheduleEndTime = sdf.parse(end_time);


                                        int dayOfWeek = scheduleDays.get(i);
                                        String endtime_inmlls = String.valueOf(homeViewModel.getscheduletime_in_mlls(scheduleEndTime, dayOfWeek).getTimeInMillis());
                                        Log.d(TAG, "handleMessage: before endtime" + endtime_inmlls);
                                        homeViewModel.cancelTimer(endtime_inmlls);

                                        String requestCode = dayOfWeek + "" + finalS_hour2 + "" + finalS_min;
                                        Log.d("cancelation", "onClick: " + schedule_pojos.getEnd_time());
                                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(requestCode), intent, PendingIntent.FLAG_IMMUTABLE);

                                        alarmManager.cancel(pendingIntent);
                                    } catch (ParseException e) {
                                        throw new RuntimeException(e);
                                    }
                                    //WorkManager.getInstance(context).cancelAllWorkByTag("recurring_work_tag" + requestCode);
                                }
                                if (position >= 0 && position < scheduleList.size()) {
                                    scheduleList.remove(position);
                                    System.out.println("Element at position " + position + " removed successfully.");
                                } else {
                                    System.out.println("Invalid position: " + position);
                                }
                                pref.saveScheduleListToSharedPreferences(context, scheduleList);
                                adapter.notifyDataSetChanged();
                                checkedChips.clear();
                                alertDialog.dismiss();
                            }
                        };
                        Message message = Message.obtain(handler);
                        ScheduleDeleteDialog scheduleDeleteDialog = new ScheduleDeleteDialog(v.getContext(), message
                                , context.getResources().getString(R.string.delete_question), context.getResources().getString(R.string.delete));

// Show the ScheduleDeleteDialog
                        scheduleDeleteDialog.show();
                    }
                    lastClickTime = currentTime;
                }
            }
        });
    }


    public static void showPlayTrimPopup(Context context, List<Records_pojos> recordLists, MainActivity mainActivity) {
        PlayTrimPopupLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.play_trim_popup_layout, null, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        ImageView closeButton = binding.cancelIcon;
        TextView cancel_trim = binding.cancelTrim;
        EditText date = binding.mainDate;
        TextView start_end_datetime = binding.startEndDatetime;
        EditText cuttingHour = binding.cuttingStartTime;
        EditText cuttingminute = binding.cuttingMin;
        boolean mWasGetContentIntent = false;

        builder.setCancelable(false);

        ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getResources().getString(R.string.processing));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        String directoryName = "/" + context.getResources().getString(R.string.harassment_amulet)
                + "/" + context.getResources().getString(R.string.myrecordingdirectory);
        String tempDirectoryName = "/" + context.getResources().getString(R.string.harassment_amulet)
                + "/" + context.getResources().getString(R.string.outputtempdirectory);
        SimpleDateFormat timeHour = new SimpleDateFormat("HH", Locale.getDefault());
        SimpleDateFormat timeMinute = new SimpleDateFormat("mm", Locale.getDefault());
        String directoryToUse = tempDirectoryName;
        builder.setView(binding.getRoot());
        CardView next_btn = binding.nextBtn;
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.show();
        String outputFileName = "TempOutput.wav";
        String outputFileName1 = "TempOutput1.wav";
        File audioDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File dir2 = new File(audioDir, tempDirectoryName);

// Make sure the directory exists; if not, create it along with parent directories
        if (!dir2.exists()) {
            dir2.mkdirs(); // Use mkdirs() to create parent directories if needed
        }
        File outputFileWithName = new File(dir2, outputFileName);
        File outputFileWithName1 = new File(dir2, outputFileName1);

        String outputFilePath2 = outputFileWithName.getAbsolutePath();
        String outputFilePath3 = outputFileWithName1.getAbsolutePath();
        Log.d(TAG, "onCreate file path: " + outputFilePath2);

        if (dir2.exists()) {
            // Loop through the files and delete them
            for (File outputFileCheck : new File[]{outputFileWithName, outputFileWithName1}) {
                if (outputFileCheck.exists()) {
                    boolean deleted = outputFileCheck.delete();
                    if (deleted) {
                        // File deleted successfully
                        Log.d("FileDeleted", "File deleted: " + outputFileCheck.getAbsolutePath());
                    } else {
                        // Failed to delete the file
                        Log.e("FileDeleteError", "Failed to delete file: " + outputFileCheck.getAbsolutePath());
                    }
                } else {
                    // File does not exist
                    Log.d("FileNotFound", "File not found: " + outputFileCheck.getAbsolutePath());
                }
            }
        } else {
            // Directory does not exist
            Log.e("DirectoryNotFound", "Directory not found: " + dir2.getAbsolutePath());
        }


        if (!recordLists.isEmpty()) {
            //initialize output file name and path


            Records_pojos firstRecord = recordLists.get(0);
            Records_pojos lastRecord = recordLists.get(recordLists.size() - 1);
            String duration = firstRecord.getRecord_duration();
            long startTimeMillis = firstRecord.getRecord_date_time().getTime();
            int firstdurationInSeconds = Integer.parseInt(firstRecord.getRecord_duration());

            long firstdurationInmilis = firstdurationInSeconds * 1000L;
            long firstTimeMillis = firstRecord.getRecord_date_time().getTime() - firstdurationInmilis;
            // Format the start time
            Date startDateTime = new Date(firstTimeMillis);
            //processing start Date
            String firstRecordDate = CommonUtils.getDateTimeString(startDateTime);

            // Assuming the date property in Records_pojos is named 'date'
            String lastRecordDate = CommonUtils.getDateTimeString(lastRecord.getRecord_date_time());
            String DateTimeDuration = firstRecordDate + " - " + lastRecordDate;

            //processing start Time
            int seconds = Integer.parseInt(duration);
            // Convert seconds to milliseconds
            long durationInMillis = seconds * 1000L;
            // Calculate the start time by subtracting the duration from the end time
//            long firstTimeMillis = firstRecord.getRecord_date_time().getTime();
            long endTimeMillis = firstRecord.getRecord_date_time().getTime() - durationInMillis;
            // Format the start time
            Date startTime = new Date(endTimeMillis);
//            Toast.makeText(context, startTime.toString(), Toast.LENGTH_SHORT).show();
//            String formattedStartHour = timeHour.format(startTime);
            String formattedStartHour = String.valueOf(startTime.getHours());
            String formattedStartMinute = String.valueOf(startTime.getMinutes());
//            Toast.makeText(context, startTime.toString(), Toast.LENGTH_SHORT).show();
            //setting data
            date.setText(firstRecordDate);
            cuttingHour.setText(formattedStartHour);
            cuttingminute.setText(formattedStartMinute);
            start_end_datetime.setText(DateTimeDuration);
        } else {
            alertDialog.cancel();
        }

        //popup setup


        //button to go to Audio player
        next_btn.setOnClickListener(

                new View.OnClickListener() {
                    long DEBOUNCE_INTERVAL = 1000; // Set the debounce interval in milliseconds
                    long lastClickTime = 0;

                    @Override
                    public void onClick(View v) {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastClickTime > DEBOUNCE_INTERVAL) {
                            if (TextUtils.isEmpty(date.getText())) {
                                Popuputils.showCustomDialog(v.getContext(), context.getResources().getString(R.string.date_is_required));
//                date.setError("Date is required");
                                return;
                            }
                            String inputDate = date.getText().toString();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                            try {
                                // Attempt to parse the input date
                                Date parsedDate = dateFormat.parse(inputDate);

                                // Parsing successful, it's a valid date
                            } catch (ParseException e) {
                                // Parsing failed, it's not a valid date
                                Popuputils.showCustomDialog(v.getContext(), context.getResources().getString(R.string.invalid_date));
                                return;
                                // date.setError("Invalid Date");
                            }
                            if (TextUtils.isEmpty(cuttingHour.getText())) {
                                Popuputils.showCustomDialog(v.getContext(), context.getResources().getString(R.string.hour_is_required));
                                return;
                            }
                            if (TextUtils.isEmpty(cuttingminute.getText())) {
                                Popuputils.showCustomDialog(v.getContext(), context.getResources().getString(R.string.minute_is_required));
                                return;
                            }
                            // turn off recording
                            boolean isToggleSwitchChecked = mainActivity.isToggleSwitchChecked();
                            if (isToggleSwitchChecked) {
                                mainActivity.setToggleSwitchChecked(!mainActivity.isToggleSwitchChecked());
                            }

                            //processing data to combine audio files
                            if (!recordLists.isEmpty()) {
                                progressDialog.show();
                                List<String> dateList = new ArrayList<>();
                                List<String> timeList = new ArrayList<>();
                                List<Integer> durationList = new ArrayList<>();


                                File directory = new File(audioDir, directoryName);
//                dir = new File(audioDir, directoryName);
                                File[] files = directory.listFiles();

                                String cuttingTime = cuttingHour.getText().toString() + ":" + cuttingminute.getText().toString();
                                String cuttingDate = date.getText().toString();
                                String cuttingDateTimeString = cuttingDate + " " + cuttingTime + ":00";
                                // Assuming the date and time format in cuttingDate and cuttingTime are known
                                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                Date cuttingDateTime;
                                try {
                                    cuttingDateTime = dateTimeFormat.parse(cuttingDateTimeString);
//                    Toast.makeText(context, cuttingDateTime.toString(), Toast.LENGTH_SHORT).show();

                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }


                                if (files != null && files.length > 0) {
                                    List<String> cmdList = new ArrayList<>();
                                    int length = 0;
                                    int duration = 0;
                                    int maxDuration = 60 * 60;
                                    boolean recordsAdded = false; // Flag to track whether any records were added

                                    for (int i = 0; i < recordLists.size(); i++) {

                                        //initializing variables
                                        Records_pojos record = recordLists.get(i);
//                        String recordStartTimeString = getTimeString(record.getRecord_date_time());
                                        String startTime = CommonUtils.getTimeString(record.getRecord_date_time());


                                        int durationInSeconds = Integer.parseInt(record.getRecord_duration());

                                        long durationInMillis = durationInSeconds * 1000L;
                                        // Calculate the start time by subtracting the duration from the end time
                                        long startTimeMillis = record.getRecord_date_time().getTime();
                                        long endTimeMillis = startTimeMillis - durationInMillis;
                                        // Format the start time
                                        Date startDateTime = new Date(endTimeMillis);
                                        Log.d("Time tracking", startDateTime.toString());


                                        //checking condition
                                        if (startDateTime.compareTo(cuttingDateTime) >= 0) {
                                            File fileToRead = new File(record.getRecord_file());

                                            if (fileToRead.canRead()) {
                                                Log.e("File Reading", "We can read the file: " + fileToRead.getAbsolutePath());
                                                // Example: FileReader, BufferedReader, etc.
                                            } else {
                                                Log.e("File Reading", "Cannot read the file: " + fileToRead.getAbsolutePath());

                                                break;
                                                // File cannot be read, handle accordingly
                                            }


                                            if (duration + durationInSeconds <= maxDuration) {
                                                cmdList.add("-i");

                                                cmdList.add(record.getRecord_file());
                                                //to pass data to list
                                                dateList.add(CommonUtils.getDateString(record.getRecord_date_time()));
                                                timeList.add(startTime);
                                                durationList.add(duration);
                                                //track process
                                                duration += durationInSeconds;
                                                recordsAdded = true;
                                                length++;
                                            } else {
                                                break;
                                            }
                                            Log.d("Time tracking", String.valueOf(duration));

                                        }

                                    }
                                    if (recordsAdded) {
                                        // Add filter_complex part
                                        cmdList.add("-filter_complex");
                                        StringBuilder filterComplex = new StringBuilder();
                                        for (int i = 0; i < length; i++) {
                                            filterComplex.append("[").append(i).append(":0]");
                                        }
                                        filterComplex.append("concat=n=").append(length).append(":v=0:a=1[out]");
                                        cmdList.add(filterComplex.toString());
                                        // Add map part
                                        cmdList.add("-map");
                                        cmdList.add("[out]");
                                        // Add output file path
                                        cmdList.add(outputFilePath3);
                                        // Convert the list to an array
                                        String[] cmdArray = cmdList.toArray(new String[0]);
                                        // Now you can use cmdArray in your FFmpeg execution
                                        Log.d("cmdString2", Arrays.toString(cmdArray));
                                        //MergeRecord(cmdArray);

                                        MergeRecord(cmdArray, new MergeRecordCallback() {


                                            @Override
                                            public void onMergeComplete(String[] cmd, int returnCode) {
                                                if (returnCode == Config.RETURN_CODE_SUCCESS) {
                                                    // Merge successful, proceed with compression
                                                    compressAudio(outputFilePath3, outputFilePath2, new CompressAudioCallback() {
                                                        @Override
                                                        public void onCompressionComplete(boolean success) {
                                                            if (success) {
                                                                File fileToDelete = new File(outputFilePath3);
                                                                Log.d(TAG, "onCompressionComplete: fileToDeleteAfter " + fileToDelete.getAbsolutePath());
                                                                RecordsFetcher.deleteFile(fileToDelete);
                                                                // Compression successful
                                                                Log.d("FFmpeg", "Audio compression successful");
                                                                progressDialog.dismiss();
                                                                alertDialog.cancel();
                                                                Intent intent = new Intent(context, RingdroidEditActivity.class);
                                                                intent.putExtra("was_get_content_intent", mWasGetContentIntent);
                                                                intent.putExtra("filename", outputFileName);
                                                                intent.putExtra("directory", directoryToUse);
                                                                intent.setClassName("com.ataraxia.pawahara", "com.ataraxia.pawahara.helper.RingdroidEditActivity");
                                                                ((Activity) context).startActivityForResult(intent, 1);
//                                                                mainActivity.finish();

                                                            } else {
                                                                // Compression failed
                                                                Log.e("FFmpeg", "Audio compression failed. Check logs for details.");
                                                                progressDialog.dismiss();
                                                                // Show error message
//                                                                showErrorMessage("Audio compression failed. Check logs for details.");
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    // Merge failed
                                                    Log.e("FFmpeg", "Audio merge failed. Check logs for details. Return code: " + returnCode);
                                                    progressDialog.dismiss();
                                                    // Show error message
//                                                    showErrorMessage("Audio merge failed. Check logs for details.");
                                                }
                                            }
                                        });
                                        Log.d("cmdString3", cmdArray.toString());
                                    } else {
                                        Popuputils.showCustomDialog(v.getContext(), v.getContext().getString(R.string.no_data_to_trim));

                                        progressDialog.dismiss();
//                    Log.d("hello1", "No valid audio records found in the directory.");


                                        // Show a prompt or take appropriate action for an invalid start time
                                    }
                                } else {
                                    progressDialog.dismiss();
                                    Log.d("hello1", "No audio files found in the directory.");
                                }

                            }
                            lastClickTime = currentTime;
                        }
                    }
                });


        closeButton.setOnClickListener(v -> {
            alertDialog.cancel();

        });
        cancel_trim.setOnClickListener(v -> {
            alertDialog.cancel();
        });
    }


    public static Calendar getNextAlarmTime(int dayOfWeek, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Get the current day of the week and the difference
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int daysUntilNextDayOfWeek = (dayOfWeek - currentDayOfWeek + 7) % 7;

        // If it's today and the time has already passed, we want to start from next week
        if (daysUntilNextDayOfWeek == 0 && calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            daysUntilNextDayOfWeek = 7;
        }

        // Add the number of days until the next day of the week
        calendar.add(Calendar.DAY_OF_YEAR, daysUntilNextDayOfWeek);

        return calendar;
    }


    public interface CompressAudioCallback {
        void onCompressionComplete(boolean success);
    }

    private static void compressAudio(String inputFilePath, String outputFilePath, CompressAudioCallback callback) {
        // Example command to compress audio with a lower bitrate
        String[] cmd = {
                "-i", inputFilePath,
                "-b:a", "32k",    // Adjust the bitrate as needed
                "-ar", "22050",   // Adjust the sample rate as needed
                "-ac", "1",       // Set to 1 for mono audio, 2 for stereo
                "-compression_level", "8",  // Adjust the compression level
                "-aq", "2",       // Adjust the audio quality
                outputFilePath
        };

        FFmpeg.executeAsync(cmd, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (callback != null) {
                    callback.onCompressionComplete(returnCode == Config.RETURN_CODE_SUCCESS);
                }
            }
        });
    }

    private static void MergeRecord(String[] cmd, MergeRecordCallback callback) {
        FFmpeg.executeAsync(cmd, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (callback != null) {
                    callback.onMergeComplete(cmd, returnCode);
                }
            }
        });
    }

    public interface MergeRecordCallback {
        void onMergeComplete(String[] cmdArray, int returnCode);
    }

    public static boolean doesScheduleExist(Context context, String scheduleName) {
        PreferenceUtils pref = new PreferenceUtils();
        List<Schedule_Pojos> scheduleList = pref.getScheduleListFromSharedPreferences(context);

        for (Schedule_Pojos schedule : scheduleList) {
            if (schedule.getSchedule_name().equals(scheduleName)) {
                // Schedule with the same name already exists
                return true;
            }
        }

        // No schedule with the same name found
        return false;
    }


    private static void editschedule(Context context, int dayOfWeek, int starthour, int start_min, int endhour, int end_min) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        Calendar nextAlarmTime;
        Calendar endAlarmTime;
        String requestCode;
        PendingIntent pendingIntent;
        Intent intent = new Intent(context, AlarmReceiver.class);

        nextAlarmTime = getNextAlarmTime(dayOfWeek, starthour, start_min);
        endAlarmTime = getNextAlarmTime(dayOfWeek, endhour, end_min);
        requestCode = dayOfWeek + "" + starthour + "" + start_min;
        intent.putExtra("request_code", requestCode);
        intent.putExtra("endtime_code", endAlarmTime.getTimeInMillis());
        intent.putExtra("alarm_hour_code", starthour);
        intent.putExtra("alarm_min_code", start_min);
        pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(requestCode), intent, PendingIntent.FLAG_MUTABLE);
        alarmManager.cancel(pendingIntent);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            intent.putExtra("is_push_enable", false);

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarmTime.getTimeInMillis(), pendingIntent);


        }

    }


}
