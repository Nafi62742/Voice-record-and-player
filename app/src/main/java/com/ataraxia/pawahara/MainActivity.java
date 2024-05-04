package com.ataraxia.pawahara;

import static com.google.ads.AdRequest.LOGTAG;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ataraxia.pawahara.helper.ScheduleDeleteDialog;
import com.ataraxia.pawahara.service.Entireapp;
import com.ataraxia.pawahara.utils.CommonUtils;
import com.ataraxia.pawahara.utils.Constant;
import com.ataraxia.pawahara.utils.RecordsFetcher;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.ataraxia.pawahara.databinding.ActivityMainBinding;
import com.ataraxia.pawahara.helper.CustomDialogBtn;
import com.ataraxia.pawahara.service.AudioRecord;
import com.ataraxia.pawahara.service.ForegroundService;
import com.ataraxia.pawahara.service.IncomingCallReceiver;
import com.ataraxia.pawahara.ui.home.HomeViewModel;
import com.ataraxia.pawahara.utils.NotificationUtils;
import com.ataraxia.pawahara.utils.Popuputils;
import com.ataraxia.pawahara.utils.PreferenceUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements IncomingCallReceiver.OnIncomingCallListener  {

    private ActivityMainBinding binding;
    private AudioRecord audioRecord;
    private HomeViewModel viewModel;
    private  TextView currenttime;
    private  TextView switchStatus;
    private  Boolean is_recording=false;
    private  TextView title_black;
    private  TextView title_brown;
    private  BottomNavigationView navView;
    private  TextView howToUse;
    private ImageView howToUseIcon;
    private ImageView backToPrevious;
    private ImageView AppIcon;
    private AdView adView;
    private Boolean  isshow_popup =false;
    private LinearLayout recorderBar;
    private Switch toggleSwitch;

    boolean seeking_bool;

    int planType;
    String directoryName;

    private RecordsFetcher recordsFetcher;
    private  Boolean Ispaused =false;
    private static final String TAG = "MainActivity";
    private IncomingCallReceiver incomingCallReceiver;

    public boolean isToggleSwitchChecked() {
        return toggleSwitch.isChecked();
    }

    public void backToSettingUi() {
        AppIcon.setVisibility(View.GONE);
        title_brown.setVisibility(View.GONE);
        recorderBar.setVisibility(View.GONE);
        navView.setVisibility(View.GONE);
        title_black.setText(getResources().getString(R.string.settings));
        backToPrevious.setVisibility(View.VISIBLE);
//                            backToPrevious.setOnClickListener();
    }

    public void setToggleSwitchChecked(boolean checked) {
        toggleSwitch.setChecked(checked);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        CommonUtils.checkMemory(this, "MainActivity");

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        NotificationUtils.createNotificationChannel(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        View root = binding.getRoot();
        adView=binding.adView;
        directoryName = "/"+getResources().getString(R.string.harassment_amulet)
                +"/"+getResources().getString(R.string.myrecordingdirectory);



        viewModel.check_schedule(this);
        toggleSwitch = findViewById(R.id.toggle_switch);
        currenttime=root.findViewById(R.id.rec_start_timeTV);
        switchStatus=root.findViewById(R.id.toogleTV);
        howToUse=root.findViewById(R.id.how_to_use);
        howToUseIcon=root.findViewById(R.id.how_to_use_icon);
        howToUse.setOnClickListener(mHowToUseLink);
        howToUseIcon.setOnClickListener(mHowToUseLink);
        title_black =binding.topBarTitleBlk;
        title_brown =binding.topBarTitleBrn;
        backToPrevious = binding.backToPrevImg;
        AppIcon=binding.imageTool;
        recorderBar = binding.recorderBar;
        seeking_bool = PreferenceUtils.getBool(this);
        handleIntent(getIntent());
        triggerphonecallListener();
        recordsFetcher = new RecordsFetcher();


        ((Entireapp) getApplication()).setAppInForeground(true);
        audioRecord=new AudioRecord(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        // Handle the broadcast
                        long endtimer = intent.getLongExtra("EXTRA_KEY", -1);
                        Log.d(TAG, "onReceive: for handled value"+endtimer);
                        start_record_switch();
                        viewModel.scheduleTask(String.valueOf(endtimer),endtimer,MainActivity.this);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
//                                viewModel.scheduleTask(String.valueOf(endtimer),endtimer);
//                                start_record_switch();
                            }
                        }, 3000);

                    }
                },
                new IntentFilter("ALARM_EVENT")
        );

        checkAvailableSpace();
//        Intent serviceIntent = new Intent(this, ForegroundService.class);
//        ContextCompat.startForegroundService(this, serviceIntent);

        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        int wednesday = Calendar.WEDNESDAY;
        int daysUntilNextWednesday = (Calendar.SATURDAY - today + wednesday) % 7;
        if (daysUntilNextWednesday == 0) { // If today is Wednesday, check if the time has passed
            if (calendar.get(Calendar.HOUR_OF_DAY) > 15 ||
                    (calendar.get(Calendar.HOUR_OF_DAY) == 15 && calendar.get(Calendar.MINUTE) >= 21)) {
                daysUntilNextWednesday = 7; // Set for next week
            }
        }


        calendar.add(Calendar.DAY_OF_YEAR, daysUntilNextWednesday);
        calendar.set(Calendar.HOUR_OF_DAY, 15);
        calendar.set(Calendar.MINUTE, 25);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);




//        isAccessibilityEnabled();
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                AdRequest adRequest = new AdRequest.Builder().build();

                if (PreferenceUtils.getState(MainActivity.this)==0){
                    adView.loadAd(adRequest);
                    adView.setVisibility(View.VISIBLE);
                }

            }
        });




        navView = binding.navView;
        String recordTotalDuration =PreferenceUtils.getDuration(this);

        long timeInSeconds = convertTimeToSeconds(recordTotalDuration);

        int recordTotalDurationSeconds =(int) timeInSeconds;
        planType =CommonUtils.getTimeLimit(PreferenceUtils.getState(this));

        int PlanTime = planType*60;
        promptToDisablePowerSavingMode(this);
        CompoundButton.OnCheckedChangeListener toggleListener = new CompoundButton.OnCheckedChangeListener() {
            long DEBOUNCE_INTERVAL = 1000; // Set the debounce interval in milliseconds
            long lastClickTime = 0;




            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean previousState = !isChecked;
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastClickTime > DEBOUNCE_INTERVAL) {
                        lastClickTime = currentTime;
                if (recordTotalDurationSeconds>PlanTime) {
//                    Toast.makeText(MainActivity.this, "Plan limit exceeded", Toast.LENGTH_SHORT).show();
                }
                else if (recordTotalDurationSeconds>(PlanTime*0.75)) {
//                    Toast.makeText(MainActivity.this, "75% of the recording quota filled", Toast.LENGTH_SHORT).show();
                }else if (recordTotalDurationSeconds>(PlanTime*0.5)) {
//                    Toast.makeText(MainActivity.this, "50% of the recording quota filled", Toast.LENGTH_SHORT).show();
                }else {

                }

                if (isChecked) {
                    //viewModel.setToggleSwitchEnabled(true);
                    viewModel.setstartrecord(true);
                    currenttime.setVisibility(View.VISIBLE);
                    switchStatus.setVisibility(View.VISIBLE);

                    switchStatus.setText(getResources().getString(R.string.on));
                    Log.d(TAG, "onCheckedChanged: recorrrrd"+is_recording);
                    if (audioRecord != null && !is_recording) {
                        triggerphonecallListener();
                        is_recording=true;
                        Log.d(TAG, "onCheckedChanged: recording");
                        record_audio();
                    }
                }
                else {
                    viewModel.setstartrecord(false);
                  viewModel.setToggleSwitchEnabled(false);
                    is_recording=false;
                    currenttime.setVisibility(View.GONE);
                    switchStatus.setVisibility(View.GONE);
                    stopPhoneCallListener();
                    try {
                        NavController navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment_activity_main);
                        NavDestination currentDestination = navController.getCurrentDestination();
                        if (currentDestination != null) {
                            int currentFragmentId = currentDestination.getId();

                            // Now you can check the ID of the current fragment
                            if (currentFragmentId == R.id.navigation_playlist) {
                                // The NavController is currently on the "navigation_playlist" fragment
                                navController.navigate(R.id.navigation_playlist);
                            }
                        }
                    }catch (Exception e){

                    }
                    if (audioRecord != null) {
                        Log.d(TAG, "onCheckedChanged: stopping");
                        audioRecord.stopRecording();
                        int planTime = CommonUtils.getTimeLimit(PreferenceUtils.getState(buttonView.getContext()))*60*1000;
                        Log.d(TAG, "onCheckedChanged: planTime " + planTime);
                        recordsFetcher.fetchMyRecords(buttonView.getContext(), directoryName, planTime);
                    }
                }
            }
                    else{
                        buttonView.setChecked(previousState);
                    }
            }
        };
        toggleSwitch.setOnCheckedChangeListener(toggleListener);
        viewModel.getscheduletimes().observe(this, scheduletimes -> {
            Log.d(TAG, "onCreate: from vm"+scheduletimes);
            //scheduleTask(scheduletimes);//13:27
            if(scheduletimes){

                stop_record_switch();
            }
                });

        viewModel.getIsWithinSchedule().observe(this, isWithinSchedule -> {
           if (isWithinSchedule) {
//                toggleSwitch.setOnCheckedChangeListener(null);
//                toggleSwitch.setChecked(true);
//                triggerphonecallListener();
//                toggleSwitch.setOnCheckedChangeListener(toggleListener);
//                is_recording=true;
//                if (audioRecord != null ) {
//                    Log.d(TAG, "onCreate: record start");
//
//                    record_audio();
//                }
               Log.d(TAG, " ");
               start_record_switch();
            } else {
               Log.d(TAG, "onCreate: stop");
//                toggleSwitch.setOnCheckedChangeListener(null);
//                toggleSwitch.setChecked(false);
//                is_recording=false;
//                toggleSwitch.setOnCheckedChangeListener(toggleListener);
//                // Stop recording if it's currently recording
//                if (audioRecord != null ) {
//                    Log.d(TAG, "onCreate: stopping");
//                    audioRecord.stopRecording();
//                }
               stop_record_switch();

            }

        });
        int savedIndex = PreferenceUtils.getSavedIndex(this);



        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_playlist, R.id.navigation_setting)
                .build();



        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        backToPrevious.setOnClickListener(mBackToSettings);
//        if(title_black.getText().equals(getResources().getString(R.string.settings))){
        title_black.setOnClickListener(mBackToSettings);
//        }
        NavigationUI.setupWithNavController(binding.navView, navController);
        binding.navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int selectedItemId = item.getItemId();

                // Save the selected index
                PreferenceUtils.saveSelectedIndex(MainActivity.this,selectedItemId);

                // Handle navigation item selection based on the selectedItemId

                navController.navigate(selectedItemId);
                defaultTopBar();

                return true;
            }
        });

    }

    private void start_record_switch() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!toggleSwitch.isChecked()){
                    toggleSwitch.setChecked(true);
                    //viewModel.setToggleSwitchEnabled(true);

                }
            }
        });

    }


    private View.OnClickListener mBackToSettings = new View.OnClickListener() {
        long DEBOUNCE_INTERVAL = 1000; // Set the debounce interval in milliseconds
        long lastClickTime = 0;
        public void onClick(View sender) {
            // Open Google link on click
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime > DEBOUNCE_INTERVAL) {
                NavController navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment_activity_main);
                NavDestination currentDestination = navController.getCurrentDestination();
                if (currentDestination != null) {
                    int currentFragmentId = currentDestination.getId();


                    if (currentFragmentId == R.id.plan_change_fragment) {
                        defaultTopBar();
                        navController.navigate(R.id.action_plan_change_fragment_to_navigation_setting);
                    }
                }
            }
        }
    };

    private long convertTimeToSeconds(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

        return hours * 3600 + minutes * 60 + seconds;
    }
    private View.OnClickListener mHowToUseLink = new View.OnClickListener() {
        long DEBOUNCE_INTERVAL = 1000; // Set the debounce interval in milliseconds
        long lastClickTime = 0;
        public void onClick(View sender) {
            // Open Google link on click
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime > DEBOUNCE_INTERVAL) {
                String googleLink = Constant.GoogleLink;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(googleLink));
                startActivity(intent);
            }
        }
    };
    private  void record_audio(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDateTime = dateFormat.format(new Date());
        String currentLanguage = Locale.getDefault().getLanguage();
        Log.d(TAG, "record_audio:  record startttt");
        // Show a toast with the current time and date
        audioRecord.createfile_start();
        String scheduleDaysText;
        if ("ja".equals(currentLanguage)) {
            // Japanese localization
            scheduleDaysText = currentDateTime +" "+ getString(R.string.starts_from);
        } else {
            // Default to English if the language is not Japanese
            scheduleDaysText = getResources().getString(R.string.starts_from) +" "+currentDateTime;
        }
        viewModel.update_crnt_time(scheduleDaysText);
        viewModel.getcurrenttime().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String newText) {
                // Update the other text view with the new text
                currenttime.setText(newText);
                switchStatus.setVisibility(View.VISIBLE);
                switchStatus.setText(getResources().getString(R.string.on));
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopForegroundService();
        ((Entireapp) getApplication()).setAppInBackground(false);
        ((Entireapp) getApplication()).setAppInForeground(false);
        // Check if recording is in progress and stop it
        if (audioRecord != null) {
            Log.d(TAG, "onDestroy: stopping");
            audioRecord.stopRecording();
            int planTime = CommonUtils.getTimeLimit(PreferenceUtils.getState(this))*60*1000;
            Log.d(TAG, "onCheckedChanged: planTime " + planTime);
            recordsFetcher.fetchMyRecords(this, directoryName, planTime);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        ((Entireapp) getApplication()).setAppInForeground(false);
        ((Entireapp) getApplication()).setAppInBackground(true);
        Log.d(TAG, "onPause: created 0"+!((Entireapp) getApplication()).isAppInForeground());
            if(is_recording){
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (is_recording ) {
                        startForegroundService();
                    }
                }, 1000);
            }






        // Add any additional logic you want to perform when the activity goes into the background
    }
    private void startForegroundService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            startForegroundService(serviceIntent);
        }
    }

    private  void triggerphonecallListener(){
        Log.d(TAG, "triggerphonecallListener: in phone call");
        registerReceiver(incomingCallReceiver, new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED));
        incomingCallReceiver = new IncomingCallReceiver();
        incomingCallReceiver.setOnIncomingCallListener(MainActivity.this);
    }

    private void stopForegroundService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopForegroundService();
        if(!is_recording){
            viewModel.check_schedule(this);
        }

        if(isshow_popup){
//            isAccessibilityEnabled();
        }

        ((Entireapp) getApplication()).setAppInForeground(true);
        ((Entireapp) getApplication()).setAppInBackground(false);
        // Register the BroadcastReceiver in onResume
       // registerReceiver(incomingCallReceiver, new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED));
    }
    private void checkAvailableSpace() {
        String externalStorageState = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(externalStorageState)) {
            // External storage is available
            StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long blockSize = statFs.getBlockSizeLong();
            long availableBlocks = statFs.getAvailableBlocksLong();

            long availableSpaceBytes = blockSize * availableBlocks;
            long availableSpaceKB = availableSpaceBytes / 1024;
            long availableSpaceMB = availableSpaceKB / 1024;
            long thresholdSpaceMB = 120;


            // Check if the available space is below a certain threshold (e.g., 10 MB)
            if (PreferenceUtils.getState(MainActivity.this)==1){
                 thresholdSpaceMB = 240;
                Log.d(TAG, "checkAvailableSpace: "+availableSpaceMB);
            } else if (PreferenceUtils.getState(MainActivity.this)==2) {
                thresholdSpaceMB = 480;
            } else if (PreferenceUtils.getState(MainActivity.this)==3) {
                thresholdSpaceMB = 720;
            }


            if (availableSpaceMB < thresholdSpaceMB) {
                Popuputils.showCustomDialog(this,getResources().getString(R.string.no_space_left_on_device));
//                Toast.makeText(this, "There is no enough space", Toast.LENGTH_SHORT).show();
                if (toggleSwitch.isChecked()) {
                    toggleSwitch.setChecked(false);
//                    viewModel.setToggleSwitchEnabled(false);
                    toggleSwitch.setEnabled(false); // Disable the ToggleSwitch
                }
                // There is low available space, take appropriate action
                // For example, show a warning to the user or free up space programmatically
            }
        } else {
            // External storage is not available or is read-only
            // Handle this situation as needed
        }
    }
    @Override
    public void onBackPressed() {
        NavController navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment_activity_main);
        NavDestination currentDestination = navController.getCurrentDestination();
        if (currentDestination != null) {
            int currentFragmentId = currentDestination.getId();

            if (currentFragmentId == R.id.plan_change_fragment) {
                defaultTopBar();
                navController.navigate(R.id.action_plan_change_fragment_to_navigation_setting);
            } else if (currentFragmentId == R.id.navigation_setting) {
                finishactivityfunc();

            } else if (currentFragmentId == R.id.navigation_playlist) {
                finishactivityfunc();
//
            } else if (currentFragmentId == R.id.navigation_home) {
                finishactivityfunc();


            }
        }
    }

    private void finishactivityfunc() {

        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            public void handleMessage(Message response) {

                finishAffinity();
                if (audioRecord != null) {
                    Log.d(TAG, "onDestroy: stopping");
                    audioRecord.stopRecording();
//                    int planTime = getTimeLimit(PreferenceUtils.getState(this))*60*1000;
//                    Log.d(TAG, "onCheckedChanged: planTime " + planTime);
//                    recordsFetcher.fetchMyRecords(this, directoryName, planTime);
                }
            }
        };
        Message message = Message.obtain(handler);
        ScheduleDeleteDialog scheduleDeleteDialog = new ScheduleDeleteDialog(this, message,getString(R.string.exit_string),getString(R.string.exit));

// Show the ScheduleDeleteDialog
        scheduleDeleteDialog.show();

    }

    @Override
    public void onIncomingCall(String incomingNumber) {
        if(incomingNumber.equals("OFFHOOK")){
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: live action");
                        if (toggleSwitch.isChecked()) {
                            toggleSwitch.setChecked(false);
                            //    viewModel.setToggleSwitchEnabled(false);
                            Ispaused = true;
                        }
                    }
                });
            }
            catch (Exception e) {
                    e.printStackTrace();
                }
        } else if (incomingNumber.equals("RINGING")) {
//            if (toggleSwitch.isChecked()) {
//                toggleSwitch.setChecked(false);
//                viewModel.setToggleSwitchEnabled(false);
//
//            }
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: live action");
                        if(toggleSwitch.isChecked()){
                            toggleSwitch.setChecked(false);
                            //    viewModel.setToggleSwitchEnabled(false);
                            Ispaused=true;
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }




        } else {
            Log.d(TAG, "onIncomingCall: "+Ispaused);
            if(Ispaused){
//                if(!toggleSwitch.isChecked()){
//                    toggleSwitch.setChecked(true);
////                    viewModel.setToggleSwitchEnabled(true);
//
//
//                }
                try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!toggleSwitch.isChecked()){
                            toggleSwitch.setChecked(true);
                            //viewModel.setToggleSwitchEnabled(true);
                            Ispaused=false;

                        }
                    }
                });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }


        Log.d(TAG, "Incoming call from: " + incomingNumber);
    }

    public  void stop_record_switch(){
        boolean isAppInBackground = ((Entireapp) MainActivity.this.getApplicationContext()).isAppInBacground();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(toggleSwitch.isChecked()){
                    toggleSwitch.setChecked(false);
                    if(isAppInBackground){
                        stopForegroundService();
                    }

                }


            }
        });

    }
    public static void promptToDisablePowerSavingMode(Context context) {
        Log.d(TAG, "promptToDisablePowerSavingMode: power 1");
        if (isPowerSavingModeEnabled(context)) {
            Toast.makeText(context, context.getResources().getString(R.string.disable_power_saving), Toast.LENGTH_LONG).show();

            // You can also open the power-saving settings screen to prompt the user to disable it.
            Intent intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
            context.startActivity(intent);
        }
    }
    public static boolean isPowerSavingModeEnabled(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        Log.d(TAG, "promptToDisablePowerSavingMode: power 2"+powerManager);
        if (powerManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return powerManager.isPowerSaveMode();
            } else {
                // For devices running versions below Lollipop, we can't directly check power saving mode.
                // You may want to provide alternative solutions for older devices.
                return false;
            }
        }

        return false;
    }
    public void defaultTopBar() {
        AppIcon.setVisibility(View.VISIBLE);
        title_brown.setVisibility(View.VISIBLE);
        recorderBar.setVisibility(View.VISIBLE);
        navView.setVisibility(View.VISIBLE);
        title_black.setText(getResources().getString(R.string.powerharras_text2));
        backToPrevious.setVisibility(View.GONE);
//                            backToPrevious.setOnClickListener();
    }

    public boolean isAccessibilityEnabled() {
        int accessibilityEnabled = 0;
        final String LIGHTFLOW_ACCESSIBILITY_SERVICE = "com.example.test/com.example.text.ccessibilityService";
        boolean accessibilityFound = false;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    this.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED
            );
            Log.d(LOGTAG, "ACCESSIBILITY: " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.d(LOGTAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        if (accessibilityEnabled == 1) {
            Log.d(LOGTAG, "***ACCESSIBILIY IS ENABLED***: ");
            String settingValue = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            );
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessabilityService = splitter.next();
                    Log.d(LOGTAG, "Setting: " + accessabilityService);
                    if (accessabilityService.equalsIgnoreCase(LIGHTFLOW_ACCESSIBILITY_SERVICE)) {
                        Log.d(LOGTAG, "Correct setting found - accessibility is switched on!");
                        accessibilityFound = true;
                        break;
                    }
                }
            }
            Log.d(LOGTAG, "***END***");
        } else {

            Popuputils.showCustomDialogWithBtn(this, getString(R.string.turn_on_accessibility), getResources().getString(R.string.next), new CustomDialogBtn.OnOkButtonClickListener() {
                @Override
                public void onOkButtonClick() {
                    String packageName = "com.ataraxia.pawahara";
                    String className = "com.ataraxia.pawahara.service.PowerAccesibilityService";
                    ComponentName componentName = new ComponentName(packageName, className);
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    intent.putExtra(Settings.EXTRA_SETTINGS_EMBEDDED_DEEP_LINK_INTENT_URI,"com.ataraxia.pawahara.service.PowerAccesibilityService");
                    startActivity(intent);
                    isshow_popup=true;

                }
            });

        }
        return accessibilityFound;
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Update the existing intent with the new intent
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && action.equals("NOTIFICATION_TAPPED")) {
                viewModel.check_schedule(this);
            }
        }
    }
    private void stopPhoneCallListener() {
        if (incomingCallReceiver != null) {
            try {
                unregisterReceiver(incomingCallReceiver);
                Log.d(TAG, "stopPhoneCallListener: unregisterReceiver");
            } catch (IllegalArgumentException e) {
                // Handle exception if the receiver is not registered
                e.printStackTrace();
            }
        }
    }
}