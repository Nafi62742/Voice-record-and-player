package com.ataraxia.pawahara.helper;/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;

import com.ataraxia.pawahara.MainActivity;
import com.ataraxia.pawahara.service.IncomingCallReceiver;
import com.ataraxia.pawahara.utils.CommonUtils;
import com.ataraxia.pawahara.utils.Constant;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ataraxia.pawahara.R;

import com.ataraxia.pawahara.service.MarkerView;
import com.ataraxia.pawahara.service.WaveformView;
import com.ataraxia.pawahara.soundfile.SoundFile;
import com.ataraxia.pawahara.utils.Popuputils;
import com.ataraxia.pawahara.utils.PreferenceUtils;

import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The activity for the Ringdroid main editor window.  Keeps track of
 * the waveform display, current horizontal offset, marker handles,
 * start / end text boxes, and handles all of the buttons and controls.
 */
public class RingdroidEditActivity extends Activity
        implements MarkerView.MarkerListener,
        WaveformView.WaveformListener,
        IncomingCallReceiver.OnIncomingCallListener {
    private InterstitialAd mInterstitialAd;

    private IncomingCallReceiver incomingCallReceiver;
    private Boolean Ispaused = false;
    private int pausePos;

    private long mLoadingLastUpdateTime;
    private boolean mLoadingKeepGoing;
    private long mRecordingLastUpdateTime;
    private boolean mRecordingKeepGoing;
    private double mRecordingTime;
    private boolean mFinishActivity;
    private TextView mTimerTextView;
    private AlertDialog mAlertDialog;
    private ProgressDialog mProgressDialog;
    private SoundFile mSoundFile;
    private File mFile;
    private String mFilename;
    private String mArtist;
    private String mTitle;
    private int mNewFileKind;
    private boolean mWasGetContentIntent;
    private WaveformView mWaveformView;
    private MarkerView mStartMarker;
    private MarkerView mEndMarker;
    private TextView mStartText;
    private TextView playingTimer;

    private TextView playerTitle;
    private TextView mEndText;
    private TextView howToUse;
    private ImageView howToUseIcon;
    private TextView mInfo;
    private String mInfoContent;
    private ImageButton mPlayButton;
    private ImageButton mPauseButton;
    private ImageButton mRewindButton;
    private ImageButton mFfwdButton;
    private BottomNavigationView bottomNavigationView;
    private boolean mKeyDown;
    private String mPlayingTime = "";
    private String mTotalTime = "";
    private File audioDir;
    private int mWidth;
    private int mMaxPos;
    private int mStartPos;
    private int mEndPos;
    private boolean mStartVisible;
    private boolean mEndVisible;
    private long mStartTime;
    private int mLastDisplayedStartPos;
    private int mLastDisplayedEndPos;
    private int mOffset;
    private int mOffsetGoal;
    private int mFlingVelocity;
    private int mPlayStartMsec;
    private int mPlayEndMsec;
    private Handler mHandler;
    private boolean mIsPlaying;
    private SamplePlayer mPlayer;
    private boolean mTouchDragging;
    private float mTouchStart;
    private int mTouchInitialOffset;
    private int mTouchInitialStartPos;
    private int mTouchInitialEndPos;
    private long mWaveformTouchStartMsec;
    private float mDensity;
    private int mMarkerLeftInset;
    private int mMarkerRightInset;
    private int mMarkerTopOffset;
    private int mMarkerBottomOffset;

    private Thread mLoadSoundFileThread;
    long totalTimeMillis;
    String directory;

    String newDirectoryName;
    String tempDirectoryName;
    private long mStartTimeMillis;
    private long currentRecordingTimeMillis = 0;
    long timeLeap = 15000;
    long timeOnWaveLeap = 0;
    boolean isInCall = false;

    private Thread mRecordAudioThread;
    private Thread mSaveSoundFileThread;

    // Result codes
    private static final int REQUEST_CODE_CHOOSE_CONTACT = 1;


//    public static final String EDIT = "com.ringdroid.action.EDIT";

    private void setStatusBarColor(int color) {
        Window window = getWindow();

        // Check if the Android version is Lollipop or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Set the status bar color
            window.setStatusBarColor(color);
        }
    }

    @Override
    public void onCreate(Bundle icicle) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        CommonUtils.checkMemory(this, " Ringroid");
        Log.v("Ringdroid", "EditActivity OnCreate");
        super.onCreate(icicle);
        setStatusBarColor(getResources().getColor(R.color.appbgcolor));
        setContentView(R.layout.editor);
        Intent intent = getIntent();
        triggerphonecallListener();

        if (PreferenceUtils.getState(this) == 0) {
            innitAdd();
        }

        newDirectoryName = "/" + getResources().getString(R.string.harassment_amulet)
                + "/" + getResources().getString(R.string.savecutdirectory);
        tempDirectoryName = "/" + getResources().getString(R.string.harassment_amulet)
                + "/" + getResources().getString(R.string.outputtempdirectory);


        mPlayer = null;
        mIsPlaying = false;

        mAlertDialog = null;
        mProgressDialog = null;

        mLoadSoundFileThread = null;
        mRecordAudioThread = null;
        mSaveSoundFileThread = null;


        mWasGetContentIntent = intent.getBooleanExtra("was_get_content_intent", false);
        audioDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

        String fileName = getIntent().getStringExtra("filename");
        String fileDirectory = intent.getStringExtra("directory");
        Log.d("Fetched Data", fileName + fileDirectory);


        directory = fileDirectory;
        if (directory.equals(tempDirectoryName)) {
            File dir2 = new File(audioDir, tempDirectoryName);
            Log.d("filePath", "file path check: " + audioDir.getAbsolutePath());
            if (dir2.exists() && dir2.isDirectory()) {
                File[] files = dir2.listFiles();
                Log.d("filePath", "files check: " + files.length);
                File fileToUse = new File(dir2, fileName);
                if (fileToUse.exists()) {
                    // Get the absolute path from the File
                    mFilename = fileToUse.getAbsolutePath();
                    Log.d("File Path", mFilename);
                } else {
                    Log.d("fileName", fileName + " not found in " + dir2.getAbsolutePath());
                }
            } else {
                Log.d("fileName", "Directory does not exist: " + dir2.getAbsolutePath());
            }
        } else {
            File dir = new File(audioDir, newDirectoryName);
            Log.d(TAG, "onBindViewHolder dir2: " + dir);
//        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), newDirectoryName);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null && files.length > 0) {
                    File fileToUse = new File(dir, fileName);
                    Log.d("File Path", fileToUse.getAbsolutePath());

                    if (fileToUse.exists()) {
                        mFilename = fileToUse.getAbsolutePath().replaceFirst("file://", "").replaceAll("%20", " ");
//                    Toast.makeText(getContext(), mFilename, Toast.LENGTH_SHORT).show();
                        Log.d("fileName", mFilename);
                    }
                } else {
                    Log.d("nullFileError", "no file found");
                }
            }
        }

        mSoundFile = null;
        mKeyDown = false;

        mHandler = new Handler();

        loadGui();


//        mHandler.postDelayed(mTimerRunnable, 100);
        Log.d("TAG", "onCreate: works");
        if (mFilename != null) {
            if (!mFilename.equals("record")) {
                loadFromFile();
            } else {

                Handler handler = new Handler();

// Create a new thread
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Sleep for 30 seconds
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // Use the handler to post the runnable to the main thread
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                // Call the recordAudio() function
                                recordAudio();
                            }
                        });
                    }
                });

// Start the thread
                thread.start();
            }
        } else {
            Log.d("GettingNull", "True");
        }

    }

    private void triggerphonecallListener() {
        Log.d(TAG, "triggerphonecallListener: in phone call");
        incomingCallReceiver = new IncomingCallReceiver();
        incomingCallReceiver.setOnIncomingCallListener(RingdroidEditActivity.this);
        registerReceiver(incomingCallReceiver, new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED));

    }

    private void stopPhoneCallListener() {
        if (incomingCallReceiver != null) {
            try {
                unregisterReceiver(incomingCallReceiver);
            } catch (IllegalArgumentException e) {
                // Handle exception if the receiver is not registered
                e.printStackTrace();
            }
        }
    }

    public void onIncomingCall(String incomingNumber) {
        if (incomingNumber.equals("OFFHOOK")) {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: live action");
                        if (mIsPlaying) {
//                            mIsPlaying=false;
                            isInCall = true;
                            handleStop();
                            Ispaused = true;
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (incomingNumber.equals("RINGING")) {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: live action RINGING");
                        if (mIsPlaying) {
//                            mIsPlaying=false;
                            isInCall = true;
                            handleStop();
                            Ispaused = true;
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Log.d(TAG, "onIncomingCall: " + Ispaused);
            if (Ispaused) {

                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!mIsPlaying) {
                                Ispaused = false;
                                isInCall = false;

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


    private void closeThread(Thread thread) {
        if (thread != null && thread.isAlive()) {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Called when the activity is finally destroyed.
     */
    @Override
    protected void onDestroy() {
        Log.v("Ringdroid", "EditActivity OnDestroy");

        mLoadingKeepGoing = false;
        mRecordingKeepGoing = false;
        closeThread(mLoadSoundFileThread);
        closeThread(mRecordAudioThread);
        closeThread(mSaveSoundFileThread);
        mLoadSoundFileThread = null;
        mRecordAudioThread = null;
        mSaveSoundFileThread = null;
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }

        if (mPlayer != null) {
            if (mPlayer.isPlaying() || mPlayer.isPaused()) {
                mPlayer.stop();
            }
            mPlayer.release();
            mPlayer = null;
        }

        super.onDestroy();
    }

    /**
     * Called with an Activity we started with an Intent returns.
     */
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent dataIntent) {
//        Log.v("Ringdroid", "EditActivity onActivityResult");
        if (requestCode == REQUEST_CODE_CHOOSE_CONTACT) {
            // The user finished saving their ringtone and they're
            // just applying it to a contact.  When they return here,
            // they're done.
            finish();
        }
    }

    /**
     * Called when the orientation changes and/or the keyboard is shown
     * or hidden.  We don't need to recreate the whole activity in this
     * case, but we do need to redo our layout somewhat.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
//        Log.v("Ringdroid", "EditActivity onConfigurationChanged");
        final int saveZoomLevel = mWaveformView.getZoomLevel();
        super.onConfigurationChanged(newConfig);

        loadGui();

        mHandler.postDelayed(new Runnable() {
            public void run() {
                mStartMarker.requestFocus();
                markerFocus(mStartMarker);

                mWaveformView.setZoomLevel(saveZoomLevel);
                mWaveformView.recomputeHeights(mDensity);

                updateDisplay();
            }
        }, 500);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            onPlay(mStartPos);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * WaveformListener
     * Every time we get a message that our waveform drew, see if we need to
     * animate and trigger another redraw.
     */
    public void waveformDraw() {
        mWidth = mWaveformView.getMeasuredWidth();
        if (mOffsetGoal != mOffset && !mKeyDown)
            updateDisplay();
        else if (mIsPlaying) {
            updateDisplay();
        } else if (mFlingVelocity != 0) {
            updateDisplay();
        }
    }

    boolean swiped = false;
    int lastPos;
    boolean tapped = false;
    int waveTapPos;

    public void waveformTouchStart(float x) {
        mTouchDragging = true;
        mTouchStart = x;
        mTouchInitialOffset = mOffset;
        mFlingVelocity = 0;
//        mPlayButton.setImageResource(R.drawable.play);
//        mPauseButton.setImageResource(R.drawable.pause);
//        mPauseButton.setImageResource(R.drawable.pause_deep);
        mWaveformTouchStartMsec = getCurrentTime();
//        if(x>pausePos){
//            onPause((int) x);
//        }

    }

    public void waveformTouchMove(float x) {
        mOffset = trap((int) (mTouchInitialOffset + (mTouchStart - x)));
        Log.d(TAG, "waveformTouchMove: mTouchStart " + mTouchStart);
        if (mPlayer.isPlaying()) {
            swiped = true;
            lastPos = mPlayer.getCurrentPosition();
            handlePause();
        }
//        updateDisplay();
    }

    public void waveformTouchEnd() {
        mTouchDragging = false;
        mOffsetGoal = mOffset;

        long elapsedMsec = getCurrentTime() - mWaveformTouchStartMsec;
        if (elapsedMsec < 300) {
            if (mIsPlaying) {
//                mPlayButton.setImageResource(R.drawable.play_button_deep);
//                mPauseButton.setImageResource(R.drawable.pause);
                int seekMsec = mWaveformView.pixelsToMillisecs(
                        (int) (mTouchStart + mOffset));

                Log.d(TAG, "waveformTouchEnd: seekMsec " + seekMsec);
                if (seekMsec >= mPlayStartMsec &&
                        seekMsec < mPlayEndMsec) {
                    currentRecordingTimeMillis = seekMsec;
                    timeOnWaveLeap = seekMsec;
//                    mPlayButton.setImageResource(R.drawable.play_button_deep);
//                    mPauseButton.setImageResource(R.drawable.pause);
                    Log.d(TAG, "waveformTouchEnd: " + seekMsec + " " + totalTimeMillis);
                    mPlayer.seekTo(seekMsec);
                } else if (seekMsec >= 0 &&
                        seekMsec < mPlayEndMsec) {
                    currentRecordingTimeMillis = seekMsec;
                    timeOnWaveLeap = seekMsec;
//                    mPlayButton.setImageResource(R.drawable.play_button_deep);
//                    mPauseButton.setImageResource(R.drawable.pause);
                    Log.d(TAG, "waveformTouchEnd: " + seekMsec + " " + totalTimeMillis);
                    mPlayer.seekTo(seekMsec);
                } else {
//                    handlePause();
                }
            } else {

                int secondFromWAV = mWaveformView.pixelsToMillisecs(
                        (int) (mTouchStart + mOffset));
                currentRecordingTimeMillis = secondFromWAV;
                if (totalTimeMillis >= secondFromWAV) {
                    timeOnWaveLeap = secondFromWAV;


                    Log.d(TAG, "waveformTouchEnd: " + secondFromWAV + " " + totalTimeMillis);
                    tapped = true;
                    waveTapPos = secondFromWAV;
                    onPlay(mStartPos);
//                    mPlayer.seekTo((int) (mTouchStart + mOffset));
//                    updateDisplay();
                }
            }
        }
    }

    public void waveformFling(float vx) {
        mTouchDragging = false;
        mOffsetGoal = mOffset;
        mFlingVelocity = (int) (-vx);
        updateDisplay();
    }

    public void waveformZoomIn() {
        mWaveformView.zoomIn();
        mStartPos = mWaveformView.getStart();
        mEndPos = mWaveformView.getEnd();
        mMaxPos = mWaveformView.maxPos();
        mOffset = mWaveformView.getOffset();
        mOffsetGoal = mOffset;
        updateDisplay();
    }

    public void waveformZoomOut() {
        mWaveformView.zoomOut();
        mStartPos = mWaveformView.getStart();
        mEndPos = mWaveformView.getEnd();
        mMaxPos = mWaveformView.maxPos();
        mOffset = mWaveformView.getOffset();
        mOffsetGoal = mOffset;
        updateDisplay();
    }

    /**
     * MarkerListener
     */

    public void markerDraw() {
    }

    public void markerTouchStart(MarkerView marker, float x) {
        mTouchDragging = true;
        mTouchStart = x;
        mTouchInitialStartPos = mStartPos;
        mTouchInitialEndPos = mEndPos;
    }

    public void markerTouchMove(MarkerView marker, float x) {
        float delta = x - mTouchStart;

        if (marker == mStartMarker) {
            mStartPos = trap((int) (mTouchInitialStartPos + delta));
            mEndPos = trap((int) (mTouchInitialEndPos + delta));
        } else {
            mEndPos = trap((int) (mTouchInitialEndPos + delta));
            if (mEndPos < mStartPos)
                mEndPos = mStartPos;
        }

        updateDisplay();
    }

    public void markerTouchEnd(MarkerView marker) {
        mTouchDragging = false;
        if (marker == mStartMarker) {
            setOffsetGoalStart();
        } else {
            setOffsetGoalEnd();
        }
    }

    public void markerLeft(MarkerView marker, int velocity) {
        mKeyDown = true;

        if (marker == mStartMarker) {
            int saveStart = mStartPos;
            mStartPos = trap(mStartPos - velocity);
            mEndPos = trap(mEndPos - (saveStart - mStartPos));
            setOffsetGoalStart();
        }

        if (marker == mEndMarker) {
            if (mEndPos == mStartPos) {
                mStartPos = trap(mStartPos - velocity);
                mEndPos = mStartPos;
            } else {
                mEndPos = trap(mEndPos - velocity);
            }

            setOffsetGoalEnd();
        }

        updateDisplay();
    }

    public void markerRight(MarkerView marker, int velocity) {
        mKeyDown = true;

        if (marker == mStartMarker) {
            int saveStart = mStartPos;
            mStartPos += velocity;
            if (mStartPos > mMaxPos)
                mStartPos = mMaxPos;
            mEndPos += (mStartPos - saveStart);
            if (mEndPos > mMaxPos)
                mEndPos = mMaxPos;

            setOffsetGoalStart();
        }

        if (marker == mEndMarker) {
            mEndPos += velocity;
            if (mEndPos > mMaxPos)
                mEndPos = mMaxPos;

            setOffsetGoalEnd();
        }

        updateDisplay();
    }

    public void markerEnter(MarkerView marker) {
    }

    public void markerKeyUp() {
        mKeyDown = false;
        updateDisplay();
    }

    public void markerFocus(MarkerView marker) {
        mKeyDown = false;
        if (marker == mStartMarker) {
            setOffsetGoalStartNoUpdate();
        } else {
            setOffsetGoalEndNoUpdate();
        }

        // Delay updaing the display because if this focus was in
        // response to a touch event, we want to receive the touch
        // event too before updating the display.
        mHandler.postDelayed(new Runnable() {
            public void run() {
                updateDisplay();
            }
        }, 100);
    }


    /**
     * Called from both onCreate and onConfigurationChanged
     * (if the user switched layouts)
     */


    private String formatTime(long milliseconds) {
        // Convert the time from milliseconds to a formatted string (HH:MM:SS)
        int seconds = (int) (milliseconds / 1000);
        int minutes = seconds / 60;
        seconds %= 60;
        int hours = minutes / 60;
        minutes %= 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void loadGui() {
        // Inflate our UI from its XML layout description.
        setContentView(R.layout.editor);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mDensity = metrics.density;

        mMarkerLeftInset = (int) (36 * mDensity);
        mMarkerRightInset = (int) (48 * mDensity);
        mMarkerTopOffset = (int) (10 * mDensity);
        mMarkerBottomOffset = (int) (10 * mDensity);

//        mStartText = (TextView)findViewById(R.id.starttext);
//        mStartText.addTextChangedListener(mTextWatcher);
//        mEndText = (TextView)findViewById(R.id.endtext);
//        mEndText.addTextChangedListener(mTextWatcher);

        mPlayButton = (ImageButton) findViewById(R.id.play);
        mPlayButton.setOnClickListener(mPlayListener);
        mPauseButton = (ImageButton) findViewById(R.id.pause);
        mPauseButton.setOnClickListener(mPauseListener);
        mRewindButton = (ImageButton) findViewById(R.id.rew);

        mFfwdButton = (ImageButton) findViewById(R.id.ffwd);


        howToUse = (TextView) findViewById(R.id.how_to_use);
        howToUseIcon = (ImageView) findViewById(R.id.how_to_use_icon);
        howToUse.setOnClickListener(mHowToUseLink);
        howToUseIcon.setOnClickListener(mHowToUseLink);


//
//        TextView markStartButton = (TextView) findViewById(R.id.mark_start);
//        markStartButton.setOnClickListener(mMarkStartListener);
//        TextView markEndButton = (TextView) findViewById(R.id.mark_end);
//        markEndButton.setOnClickListener(mMarkEndListener);

//        enableDisableButtons();

        mWaveformView = (WaveformView) findViewById(R.id.waveform);
        mWaveformView.setListener(this);

        mInfo = (TextView) findViewById(R.id.info);
        mInfo.setText(mTotalTime);

        // Get the current time


        mMaxPos = 0;
        mLastDisplayedStartPos = 0;
        mLastDisplayedEndPos = 0;

        if (mSoundFile != null && !mWaveformView.hasSoundFile()) {
            mWaveformView.setSoundFile(mSoundFile);
            mWaveformView.recomputeHeights(mDensity);
            mMaxPos = mWaveformView.maxPos();
        }

        mStartMarker = (MarkerView) findViewById(R.id.startmarker);
        mStartMarker.setListener(this);
        mStartMarker.setAlpha(1f);
        mStartMarker.setFocusable(true);
        mStartMarker.setFocusableInTouchMode(true);
        mStartVisible = true;

        mEndMarker = (MarkerView) findViewById(R.id.endmarker);
        mEndMarker.setListener(this);
        mEndMarker.setAlpha(1f);
        mEndMarker.setFocusable(true);
        mEndMarker.setFocusableInTouchMode(true);
        mEndVisible = true;


        TextView cancelTrim = findViewById(R.id.cancel_BTN);
        TextView listText = findViewById(R.id.backbtn_text);
        TextView gotoList = findViewById(R.id.toList_BTN);
        mStartText = findViewById(R.id.play_text);
        playingTimer = findViewById(R.id.playTimer);


        Button atStart = findViewById(R.id.at_start_btn);
        Button cutButton = findViewById(R.id.cut_btn);
        Button atEnd = findViewById(R.id.at_end_btn);
        Button savebtn = findViewById(R.id.save_record_btn);

        ImageView backToList = findViewById(R.id.backbtn_img);
        LinearLayout secondLinearLayout = findViewById(R.id.secondLinearLayout);
        LinearLayout secondLinearLayout1 = findViewById(R.id.secondLinearLayout1);
        LinearLayout secondLinearLayout2 = findViewById(R.id.secondLinearLayout2);

        playerTitle = findViewById(R.id.title_player);


        atStart.setBackgroundColor(getColor(R.color.black));
        atEnd.setBackgroundColor(getColor(R.color.black));
        cancelTrim.setBackgroundColor(getColor(R.color.appbgcolor));
        savebtn.setBackgroundColor(getColor(R.color.Red));

        cutButton.setBackgroundColor(getColor(R.color.Red));
        atStart.setBackgroundColor(getColor(R.color.black));
        atEnd.setBackgroundColor(getColor(R.color.black));
        cancelTrim.setBackgroundColor(getColor(R.color.appbgcolor));
        savebtn.setBackgroundColor(getColor(R.color.Red));
//        gotoList.setBackgroundColor(getColor(R.color.appbgcolor));


//
        bottomNavigationView = findViewById(R.id.nav_view_playlist);


        mRewindButton.setOnClickListener(mRewindListener);
        mFfwdButton.setOnClickListener(mFfwdListener);

        atStart.setOnClickListener(mToStartListener);
        atEnd.setOnClickListener(mToEndListener);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    // Handle other menu items if needed

                    case R.id.navigation_playlist:
                        // Finish the current activity when the corresponding menu item is selected
                        finish();
                        return true;

                    default:
                        return false;
                }
            }
        });


        savebtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mFilename != null) {
                    //Toast.makeText(RingdroidEditActivity.this, "Saving", Toast.LENGTH_SHORT).show();
                    onSave();
                } else {
                    Popuputils.showCustomDialog(v.getContext(), getString(R.string.no_file_found_to_save));
                }
            }
        });
        cancelTrim.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finishOpeningSoundFile();
                mStartText.setText(R.string.play);
                mStartMarker.setVisibility(View.INVISIBLE);
                mEndMarker.setVisibility(View.INVISIBLE);
                secondLinearLayout2.setVisibility(View.GONE);
                secondLinearLayout.setVisibility(View.GONE);
                secondLinearLayout1.setVisibility(View.VISIBLE);
                mPlayButton.setImageResource(R.drawable.play);
                mPauseButton.setImageResource(R.drawable.pause);
//                onPause(mStartPos);

                mPlayButton.setImageResource(R.drawable.play);
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                }

                currentRecordingTimeMillis = 0;
                playingTimer.setText(formatTime(currentRecordingTimeMillis));
                mWaveformView.setPlayback(-1);
                mIsPlaying = false;
                updateDisplay();
                finishOpeningSoundFile();
            }
        });
        cutButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String textEnglish = getString(R.string.trimming_stop_recording);
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.RED);
                String currentLanguage = Locale.getDefault().getLanguage();


// Apply the color span to the characters after "Trimming" or "切取り"

                if ("ja".equals(currentLanguage)) {
                    SpannableString spannableStringJapanese = new SpannableString(textEnglish);
                    int endIndexOfJapaneseTrimming = textEnglish.indexOf("切取り") + "切取り".length();
                    spannableStringJapanese.setSpan(colorSpan, endIndexOfJapaneseTrimming, textEnglish.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // Japanese localization
                    mStartText.setText(spannableStringJapanese);
                } else {
                    SpannableString spannableStringEnglish = new SpannableString(textEnglish);
                    int endIndexOfTrimming = textEnglish.indexOf("Trimming") + "Trimming".length();
                    spannableStringEnglish.setSpan(colorSpan, endIndexOfTrimming, textEnglish.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // Default to English if the language is not Japanese
                    mStartText.setText(spannableStringEnglish);
                }
//                mStartText.setText(R.string.trimming_stop_recording);
                mStartMarker.setVisibility(View.VISIBLE);
                mEndMarker.setVisibility(View.VISIBLE);
                secondLinearLayout2.setVisibility(View.VISIBLE);
                secondLinearLayout.setVisibility(View.VISIBLE);
                secondLinearLayout1.setVisibility(View.GONE);
                mPlayButton.setImageResource(R.drawable.play);
                mPauseButton.setImageResource(R.drawable.pause);
//                onPause(mStartPos);
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                }

                currentRecordingTimeMillis = 0;
                playingTimer.setText(formatTime(currentRecordingTimeMillis));
                handleStop();

            }
        });


        mPlayButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        CommonUtils.elevateButton(mPlayButton);
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        CommonUtils.lowerButton(mPlayButton);
                        break;
                }
                return false;
            }
        });
        backToList.setOnClickListener(mNavigateToList);
        listText.setOnClickListener(mNavigateToList);
        gotoList.setOnClickListener(mNavigateToList);

        updateDisplay();
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
                lastClickTime = currentTime;
            }
        }
    };

    private void loadFromFile() {

//        if(mFilename.equals("TempOutput.wav")){
//
//        }
        mFile = new File(mFilename);

        SongMetadataReader metadataReader = new SongMetadataReader(
                this, mFilename);
        mTitle = metadataReader.mTitle;

        Log.d("Metadata222", mTitle);
        mArtist = metadataReader.mArtist;
        if (mTitle.equals("TempOutput")) {
            playerTitle.setText(R.string.masterdata);
        } else {
            playerTitle.setText(mTitle);
        }

        String titleLabel = mTitle;
        if (mArtist != null && mArtist.length() > 0) {
            titleLabel += " - " + mArtist;
        }
        setTitle(titleLabel);

        mLoadingLastUpdateTime = getCurrentTime();
        mLoadingKeepGoing = true;
        mFinishActivity = false;
        mProgressDialog = new ProgressDialog(RingdroidEditActivity.this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle(R.string.progress_dialog_loading);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        mLoadingKeepGoing = false;
                        mFinishActivity = true;
                    }
                });
        mProgressDialog.show();

        final SoundFile.ProgressListener listener =
                new SoundFile.ProgressListener() {
                    public boolean reportProgress(double fractionComplete) {
                        long now = getCurrentTime();
                        if (now - mLoadingLastUpdateTime > 100) {
                            mProgressDialog.setProgress(
                                    (int) (mProgressDialog.getMax() * fractionComplete));
                            mLoadingLastUpdateTime = now;
                        }
                        return mLoadingKeepGoing;
                    }
                };

        // Load the sound file in a background thread
        mLoadSoundFileThread = new Thread() {
            public void run() {
                try {

                    mSoundFile = SoundFile.create(mFile.getAbsolutePath(), listener);
                    Log.d(TAG, "run: mSoundFile" + mSoundFile);
                    if (mSoundFile == null) {
                        mProgressDialog.dismiss();
                        String name = mFile.getName().toLowerCase();
                        String[] components = name.split("\\.");
                        String err;
                        if (components.length < 2) {
                            err = getResources().getString(
                                    R.string.no_extension_error);
                        } else {
                            err = getResources().getString(
                                    R.string.bad_extension_error) + " " +
                                    components[components.length - 1];
                        }
                        final String finalErr = err;
                        Runnable runnable = new Runnable() {
                            public void run() {
                                showFinalAlert(new Exception(), finalErr);
                            }
                        };
                        mHandler.post(runnable);
                        return;
                    }
                    mPlayer = new SamplePlayer(mSoundFile);
                } catch (final Exception e) {
                    mProgressDialog.dismiss();
                    e.printStackTrace();
                    mInfoContent = e.toString();
                    runOnUiThread(new Runnable() {
                        public void run() {
//                            mInfo.setText(mInfoContent);
                        }
                    });

                    Runnable runnable = new Runnable() {
                        public void run() {
                            showFinalAlert(e, getResources().getText(R.string.read_error));
                        }
                    };
                    mHandler.post(runnable);
                    return;
                }
                mProgressDialog.dismiss();
                if (mLoadingKeepGoing) {
                    Runnable runnable = new Runnable() {
                        public void run() {
                            finishOpeningSoundFile();
                        }
                    };
                    mHandler.post(runnable);
                } else if (mFinishActivity) {
                    RingdroidEditActivity.this.finish();
                }
            }
        };
        mLoadSoundFileThread.start();
    }

    private void recordAudio() {
        mFile = null;
        mTitle = null;
        mArtist = null;

        mRecordingLastUpdateTime = getCurrentTime();
        mRecordingKeepGoing = true;
        mFinishActivity = false;
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(RingdroidEditActivity.this);
        adBuilder.setTitle(getResources().getText(R.string.progress_dialog_recording));
        adBuilder.setCancelable(true);
        adBuilder.setNegativeButton(
                getResources().getText(R.string.progress_dialog_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mRecordingKeepGoing = false;
                        mFinishActivity = true;
                    }
                });
        adBuilder.setPositiveButton(
                getResources().getText(R.string.progress_dialog_stop),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mRecordingKeepGoing = false;
                    }
                });
        // TODO(nfaralli): try to use a FrameLayout and pass it to the following inflate call.
        // Using null, android:layout_width etc. may not work (hence text is at the top of view).
        // On the other hand, if the text is big enough, this is good enough.
        adBuilder.setView(getLayoutInflater().inflate(R.layout.record_audio, null));
        mAlertDialog = adBuilder.show();
        mTimerTextView = (TextView) mAlertDialog.findViewById(R.id.record_audio_timer);

        final SoundFile.ProgressListener listener =
                new SoundFile.ProgressListener() {
                    public boolean reportProgress(double elapsedTime) {
                        long now = getCurrentTime();
                        if (now - mRecordingLastUpdateTime > 5) {
                            mRecordingTime = elapsedTime;
                            // Only UI thread can update Views such as TextViews.
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    if(now>0&&now<mMaxPos){
                                        int min = (int) (mRecordingTime / 60);
                                        float sec = (float) (mRecordingTime - 60 * min);
                                        mTimerTextView.setText(String.format("%d:%05.2f", min, sec));
                                    }
                                }
                            });
                            mRecordingLastUpdateTime = now;
                        }
                        return mRecordingKeepGoing;
                    }
                };

        // Record the audio stream in a background thread
        mRecordAudioThread = new Thread() {
            public void run() {
                try {
                    mSoundFile = SoundFile.record(listener);
                    if (mSoundFile == null) {
                        mAlertDialog.dismiss();
                        Runnable runnable = new Runnable() {
                            public void run() {
                                showFinalAlert(
                                        new Exception(),
                                        getResources().getText(R.string.record_error)
                                );
                            }
                        };
                        mHandler.post(runnable);
                        return;
                    }
                    mPlayer = new SamplePlayer(mSoundFile);

                } catch (final Exception e) {
                    mAlertDialog.dismiss();
                    e.printStackTrace();
                    mInfoContent = e.toString();
                    runOnUiThread(new Runnable() {
                        public void run() {
//                            mInfo.setText(mInfoContent);
                        }
                    });

                    Runnable runnable = new Runnable() {
                        public void run() {
                            showFinalAlert(e, getResources().getText(R.string.record_error));
                        }
                    };
                    mHandler.post(runnable);
                    return;
                }
                mAlertDialog.dismiss();
                if (mFinishActivity) {
                    RingdroidEditActivity.this.finish();
                } else {
                    Runnable runnable = new Runnable() {
                        public void run() {
                            finishOpeningSoundFile();
                        }
                    };
                    mHandler.post(runnable);
                }
            }
        };
        mRecordAudioThread.start();
    }

    private void finishOpeningSoundFile() {
        mWaveformView.setSoundFile(mSoundFile);
        mWaveformView.recomputeHeights(mDensity);

        mMaxPos = mWaveformView.maxPos();
        mLastDisplayedStartPos = -1;
        mLastDisplayedEndPos = -1;

        mTouchDragging = false;

        mOffset = 0;
        mOffsetGoal = 0;
        mFlingVelocity = 0;
        resetPositions();
        if (mEndPos > mMaxPos)
            mEndPos = mMaxPos;
//        mTotalTime =
//                mSoundFile.getFiletype() + ", " +
//                        mSoundFile.getSampleRate() + " Hz, " +
//                        mSoundFile.getAvgBitrateKbps() + " kbps, " +
//                        formatTime(mMaxPos) + " " +
//                        getResources().getString(R.string.time_seconds);
        mTotalTime = formatTime(mMaxPos);
        mInfo.setText(mTotalTime);
        totalTimeMillis = convertTimeToMillis(mTotalTime);
        updateDisplay();
    }

    private static long convertTimeToMillis(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

        long totalMillis = hours * 3600000L + minutes * 60000L + seconds * 1000L;
        return totalMillis;
    }

    private synchronized void updateDisplay() {
        if (mIsPlaying) {
            int now = mPlayer.getCurrentPosition();
            int frames = mWaveformView.millisecsToPixels(now);

            long timer = mPlayer.getCurrentPosition();
//            Log.d(TAG, "updateDisplay: mPlayer time " + timer);
            playingTimer.setText(formatTime(timer));
            mWaveformView.setPlayback(frames);
            setOffsetGoalNoUpdate(frames - mWidth / 2);
            if (now >= mPlayEndMsec) {
                handleStop();
            }
        }

        if (!mTouchDragging) {
            int offsetDelta;

            if (mFlingVelocity != 0) {
                offsetDelta = mFlingVelocity / 30;
                if (mFlingVelocity > 80) {
                    mFlingVelocity -= 80;
                } else if (mFlingVelocity < -80) {
                    mFlingVelocity += 80;
                } else {
                    mFlingVelocity = 0;
                }

                mOffset += offsetDelta;

                if (mOffset + mWidth / 2 > mMaxPos) {
                    mOffset = mMaxPos - mWidth / 2;
                    mFlingVelocity = 0;
                }
                if (mOffset < 0) {
                    mOffset = 0;
                    mFlingVelocity = 0;
                }
                mOffsetGoal = mOffset;
            } else {
                offsetDelta = mOffsetGoal - mOffset;

                if (offsetDelta > 10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta > 0)
                    offsetDelta = 1;
                else if (offsetDelta < -10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta < 0)
                    offsetDelta = -1;
                else
                    offsetDelta = 0;

                mOffset += offsetDelta;
            }
        }

        mWaveformView.setParameters(mStartPos, mEndPos, mOffset);
        mWaveformView.invalidate();

        mStartMarker.setContentDescription(
                getResources().getText(R.string.start_marker) + " " +
                        formatTime(mStartPos));
        mEndMarker.setContentDescription(
                getResources().getText(R.string.end_marker) + " " +
                        formatTime(mEndPos));

        int startX = mStartPos - mOffset - mMarkerLeftInset;
        if (startX + mStartMarker.getWidth() >= 0) {
            if (!mStartVisible) {
                // Delay this to avoid flicker
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        mStartVisible = true;
                        mStartMarker.setAlpha(1f);
                    }
                }, 0);
            }
        } else {
            if (mStartVisible) {
                mStartMarker.setAlpha(0f);
                mStartVisible = false;
            }
            startX = 0;
        }

        int endX = mEndPos - mOffset - mEndMarker.getWidth() + mMarkerRightInset;
        if (endX + mEndMarker.getWidth() >= 0) {
            if (!mEndVisible) {
                // Delay this to avoid flicker
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        mEndVisible = true;
                        mEndMarker.setAlpha(1f);
                    }
                }, 0);
            }
        } else {
            if (mEndVisible) {
                mEndMarker.setAlpha(0f);
                mEndVisible = false;
            }
            endX = 0;
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(
                startX,
                mMarkerTopOffset,
                -mStartMarker.getWidth(),
                -mStartMarker.getHeight());
        mStartMarker.setLayoutParams(params);

        params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(
                endX,
                mWaveformView.getMeasuredHeight() - mEndMarker.getHeight() - mMarkerBottomOffset,
                -mStartMarker.getWidth(),
                -mStartMarker.getHeight());
        mEndMarker.setLayoutParams(params);
    }


//    private void enableDisableButtons() {
//        if (mIsPlaying) {
//            mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
//            mPlayButton.setContentDescription(getResources().getText(R.string.stop));
//        } else {
//            mPlayButton.setImageResource(android.R.drawable.ic_media_play);
//            mPlayButton.setContentDescription(getResources().getText(R.string.play));
//        }
//    }


    // set ringroid bar size
    private void resetPositions() {
        mStartPos = mWaveformView.secondsToPixels(0.0);
        mEndPos = mWaveformView.secondsToPixels(mMaxPos);
    }

    private int trap(int pos) {
        if (pos < 0)
            return 0;
        if (pos > mMaxPos)
            return mMaxPos;
        return pos;
    }

    private void setOffsetGoalStart() {
        setOffsetGoal(mStartPos - mWidth / 2);
    }

    private void setOffsetGoalStartNoUpdate() {
        setOffsetGoalNoUpdate(mStartPos - mWidth / 2);
    }

    private void setOffsetGoalEnd() {
        setOffsetGoal(mEndPos - mWidth / 2);
    }

    private void setOffsetGoalEndNoUpdate() {
        setOffsetGoalNoUpdate(mEndPos - mWidth / 2);
    }

    private void setOffsetGoal(int offset) {
        setOffsetGoalNoUpdate(offset);
        updateDisplay();
    }

    private void setOffsetGoalNoUpdate(int offset) {
        if (mTouchDragging) {
            return;
        }

        mOffsetGoal = offset;
        if (mOffsetGoal + mWidth / 2 > mMaxPos)
            mOffsetGoal = mMaxPos - mWidth / 2;
        if (mOffsetGoal < 0)
            mOffsetGoal = 0;
    }

    //        private String formatTime(int pixels) {
//        if (mWaveformView != null && mWaveformView.isInitialized()) {
//            return formatDecimal(mWaveformView.pixelsToSeconds(pixels));
//        } else {
//            return "";
//        }
//    }
    private String formatTime(int pixels) {
        if (mWaveformView != null && mWaveformView.isInitialized()) {
            double seconds = mWaveformView.pixelsToSeconds(pixels);

            int hours = (int) seconds / 3600;
            int minutes = (int) (seconds % 3600) / 60;
            int remainingSeconds = (int) seconds % 60;

            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, remainingSeconds);
        } else {
            return "";
        }
    }

    private String formatDecimal(double x) {
        int xWhole = (int) x;
        int xFrac = (int) (100 * (x - xWhole) + 0.5);

        if (xFrac >= 100) {
            xWhole++; //Round up
            xFrac -= 100; //Now we need the remainder after the round up
            if (xFrac < 10) {
                xFrac *= 10; //we need a fraction that is 2 digits long
            }
        }

        if (xFrac < 10)
            return xWhole + ".0" + xFrac;
        else
            return xWhole + "." + xFrac;
    }

    private synchronized void handlePause() {
        stopPhoneCallListener();
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
//            mPlayButton.setImageResource(R.drawable.play);
        }
        mIsPlaying = false;
//        enableDisableButtons();
    }

    private synchronized void handleStop() {
        stopPhoneCallListener();

//        Log.d(TAG, "handleStop: Stopping");
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
//            mPlayButton.setImageResource(R.drawable.play);
            currentRecordingTimeMillis = 0;
            playingTimer.setText(formatTime(currentRecordingTimeMillis));
//            Log.d(TAG, "handleStop: Stopped");
        }
        mWaveformView.setPlayback(-1);
        mIsPlaying = false;
//        Log.d(TAG, "handleStop: reset");
    }

    private synchronized void onPlay(int startPosition) {
        triggerphonecallListener();
        if (mIsPlaying) {
//            handlePause();
            return;
        }
        if (isInCall) {
            return;
        }
        if (mPlayer == null) {
            // Not initialized yet
            return;
        }
        try {
            mPlayStartMsec = mWaveformView.pixelsToMillisecs(startPosition);
            if (startPosition < mStartPos) {
                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mStartPos);
            } else if (startPosition > mEndPos) {
                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mMaxPos);
            } else {
                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mEndPos);
            }
            // player finished playing
            mPlayer.setOnCompletionListener(new SamplePlayer.OnCompletionListener() {
                @Override
                public void onCompletion() {
                    handleStop();
                }
            });
            mIsPlaying = true;
            if (Ispaused) {
                if (mPlayStartMsec < pausePos) {
                    mPlayStartMsec = pausePos;
                }
                Ispaused = false;
                pausePos = 0;
            }

            mPlayer.seekTo(mPlayStartMsec);
//            mPlayButton.setImageResource(R.drawable.play_button_deep);
//            mPauseButton.setImageResource(R.drawable.pause);
            updateDisplay();
            mPlayer.start();
            if (swiped) {

                if (mPlayStartMsec < lastPos) {
                    mPlayer.seekTo(lastPos);
                }
                swiped = false;
                lastPos = 0;
            }
            if (tapped) {

                if (mPlayStartMsec < waveTapPos) {
                    mPlayer.seekTo(waveTapPos);
                }
                tapped = false;
                waveTapPos = 0;
            }
        } catch (Exception e) {
            showFinalAlert(e, R.string.play_error);
        }
    }

    private synchronized void onPause(int startPosition) {
        if (mIsPlaying) {

            Ispaused = true;
            Log.d(TAG, "onPause: Offset  " + startPosition);
            int pauseLocation = mPlayer.getCurrentPosition();
            pausePos = pauseLocation;

            handlePause();
            return;
        }

    }

    /**
     * Show a "final" alert dialog that will exit the activity
     * after the user clicks on the OK button.  If an exception
     * is passed, it's assumed to be an error condition, and the
     * dialog is presented as an error, and the stack trace is
     * logged.  If there's no exception, it's a success message.
     */
    private void showFinalAlert(Exception e, CharSequence message) {
        CharSequence title;
        if (e != null) {
            Log.e("Ringdroid", "Error: " + message);
            Log.e("Ringdroid", getStackTrace(e));
            title = getResources().getText(R.string.alert_title_failure);
            setResult(RESULT_CANCELED, new Intent());
        } else {
            Log.v("Ringdroid", "Success: " + message);
            title = getResources().getText(R.string.alert_title_success);
        }

        new AlertDialog.Builder(RingdroidEditActivity.this)

                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                        R.string.alert_ok_button,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
//                                finish();
                            }
                        })
                .setCancelable(false)
                .show();
    }

    private void showFinalAlert(Exception e, int messageResourceId) {
        showFinalAlert(e, getResources().getText(messageResourceId));
    }

    private String makeRingtoneFilename(CharSequence title, String extension) {

        String externalRootDir = audioDir.getAbsolutePath();
        if (!externalRootDir.endsWith(newDirectoryName + "/")) {
            externalRootDir += newDirectoryName + "/";
        }

        String parentdir = externalRootDir;
        Log.d(TAG, "makeRingtoneFilename: testing path" + parentdir);
        // Create the parent directory
        File parentDirFile = new File(parentdir);
        parentDirFile.mkdirs();

        // If we can't write to that special path, try just writing
        // directly to the sdcard
        if (!parentDirFile.isDirectory()) {
            parentdir = externalRootDir;
        }

        // Turn the title into a filename
        String filename = "";
        for (int i = 0; i < title.length(); i++) {
            if (Character.isLetterOrDigit(title.charAt(i))) {
                filename += title.charAt(i);
            }
        }

        // Try to make the filename unique
        String path = null;
        for (int i = 0; i < 100; i++) {
            String testPath;
            if (i > 0)
                testPath = parentdir + filename + i + extension;
            else
                testPath = parentdir + filename + extension;

            try {
                RandomAccessFile f = new RandomAccessFile(new File(testPath), "r");
                f.close();
            } catch (Exception e) {
                // Good, the file didn't exist
                path = testPath;
                break;
            }
        }
        Log.d(TAG, "Filename and path: " + path);
        return path;
    }

    private void saveRingtone(final CharSequence title) {
        if (title == null || title.toString().isEmpty()) {
            showFinalAlert(new Exception(), R.string.same_name_or_empty_name_field);
            return;
        }
        double startTime = mWaveformView.pixelsToSeconds(mStartPos);
        double endTime = mWaveformView.pixelsToSeconds(mEndPos);
        final int startFrame = mWaveformView.secondsToFrames(startTime);
        final int endFrame = mWaveformView.secondsToFrames(endTime);
        final int duration = (int) (endTime - startTime + 0.5);

        // Create an indeterminate progress dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle(R.string.progress_dialog_saving);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();


        // Save the sound file in a background thread
        mSaveSoundFileThread = new Thread() {
            public void run() {
                // Try AAC first.
//                String outPath = mFilename.replaceFirst(directoryName, newDirectoryName).replaceAll("%20", " ");
//                Log.d("OutputFile3", title.toString());
                String outPath = makeRingtoneFilename(title, ".wav");


//
                File outputFile = new File(outPath);


                Boolean fallbackToWAV = true;


                // Try to create a .wav file if creating a .m4a file failed.
                if (fallbackToWAV) {
//                    outPath = makeRingtoneFilename(title, ".wav");
                    if (outputFile == null) {
                        Runnable runnable = new Runnable() {
                            public void run() {
                                showFinalAlert(new Exception(), R.string.no_unique_filename);
                            }
                        };
                        mHandler.post(runnable);
                        return;
                    }
//                    outFile = new File(outPath);
//                    Log.d("OutputFile", outFile.getAbsolutePath());
                    try {
                        // create the .wav file
                        mSoundFile.WriteWAVFile(outputFile, startFrame, endFrame - startFrame);

//                        clearTempDirectory();

                    } catch (Exception e) {
                        // Creating the .wav file also failed. Stop the progress dialog, show an
                        // error message and exit.
                        mProgressDialog.dismiss();
                        if (outputFile.exists()) {
                            outputFile.delete();
                        }
                        mInfoContent = e.toString();
                        runOnUiThread(new Runnable() {
                            public void run() {
//                                mInfo.setText(mInfoContent);
                            }
                        });

                        CharSequence errorMessage;
                        if (e.getMessage() != null
                                && e.getMessage().equals(getString(R.string.no_space_left_on_device))) {
                            errorMessage = getResources().getText(R.string.no_space_error);
                            e = null;
                        } else {
                            errorMessage = getResources().getText(R.string.write_error);
                        }
                        final CharSequence finalErrorMessage = errorMessage;
                        final Exception finalException = e;
                        Runnable runnable = new Runnable() {
                            public void run() {
                                showFinalAlert(finalException, finalErrorMessage);
                            }
                        };
                        mHandler.post(runnable);
                        return;
                    }
                }

                // Try to load the new file to make sure it worked
                try {
                    final SoundFile.ProgressListener listener =
                            new SoundFile.ProgressListener() {
                                public boolean reportProgress(double frac) {
                                    // Do nothing - we're not going to try to
                                    // estimate when reloading a saved sound
                                    // since it's usually fast, but hard to
                                    // estimate anyway.
                                    return true;  // Keep going
                                }
                            };
                    SoundFile.create(outPath, listener);
                } catch (final Exception e) {
                    mProgressDialog.dismiss();
                    e.printStackTrace();
                    mInfoContent = e.toString();
                    runOnUiThread(new Runnable() {
                        public void run() {
//                            mInfo.setText(mInfoContent);
                        }
                    });

                    Runnable runnable = new Runnable() {
                        public void run() {
                            showFinalAlert(e, getResources().getText(R.string.write_error));
                        }
                    };
                    mHandler.post(runnable);
                    return;
                }

                mProgressDialog.dismiss();

                final String finalOutPath = outPath;
                Runnable runnable = new Runnable() {
                    public void run() {
                        afterSavingRingtone(title,
                                finalOutPath,
                                duration);
                    }
                };
                mHandler.post(runnable);
            }
        };
        mSaveSoundFileThread.start();
    }

    private void afterSavingRingtone(CharSequence title,
                                     String outPath,
                                     int duration) {
        File outFile = new File(outPath);
        long fileSize = outFile.length();
        if (fileSize <= 512) {
            outFile.delete();
            new AlertDialog.Builder(this)
                    .setTitle(R.string.alert_title_failure)
                    .setMessage(R.string.too_small_error)
                    .setPositiveButton(R.string.alert_ok_button, null)
                    .setCancelable(false)
                    .show();
            return;
        }

        // Create the database record, pointing to the existing file path
        String mimeType;
        if (outPath.endsWith(".m4a")) {
            mimeType = "audio/mp4a-latm";
//            Toast.makeText(this,
//                            R.string.save_success_message,
//                            Toast.LENGTH_SHORT)
//                    .show();
//            CommonUtils.startNewActivityAndFinishCurrent(RingdroidEditActivity.this, MainActivity.class);
            finish();
        } else if (outPath.endsWith(".wav")) {
            mimeType = "audio/wav";
            if (mInterstitialAd != null) {
                mInterstitialAd.show(this);
            } else {
                Log.d("TAG", "The interstitial ad wasn't ready yet.");
            }
//                        Toast.makeText(this,
//                            R.string.save_success_message,
//                            Toast.LENGTH_SHORT)
//                    .show();
//            CommonUtils.startNewActivityAndFinishCurrent(RingdroidEditActivity.this, MainActivity.class);
            finish();
        } else {
            // This should never happen.
            mimeType = "audio/mpeg";
//            Toast.makeText(this,
//                            R.string.bad_extension_error,
//                            Toast.LENGTH_SHORT)
//                    .show();
//            CommonUtils.startNewActivityAndFinishCurrent(RingdroidEditActivity.this, MainActivity.class);
            finish();
        }

//        String artist = "" + getResources().getText(R.string.artist_name);

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, outPath);
        values.put(MediaStore.MediaColumns.TITLE, title.toString());
        values.put(MediaStore.MediaColumns.SIZE, fileSize);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);

//        values.put(MediaStore.Audio.Media.ARTIST, artist);
        values.put(MediaStore.Audio.Media.DURATION, duration);

    }

    private void onSave() {
        if (mIsPlaying) {
            handlePause();
//            mPlayButton.setImageResource(R.drawable.play);
        }

        final Handler handler = new Handler() {
            public void handleMessage(Message response) {
                CharSequence newTitle = (CharSequence) response.obj;
                mNewFileKind = response.arg1;
                saveRingtone(newTitle);
            }
        };
//        if(mTitle.equals("TempOutput")){
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
// Generate the new filename with the timestamp
        mTitle = timestamp;
//        }else{
//            mTitle = sample
//        }

        Message message = Message.obtain(handler);
        FileSaveDialog dlog = new FileSaveDialog(
                this, getResources(), mTitle, message);


        dlog.show();
    }


    private OnClickListener mPlayListener = new OnClickListener() {
        public void onClick(View sender) {
            onPlay(mStartPos);
        }
    };
    private OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(View sender) {
//            mPlayButton.setImageResource(R.drawable.play);
//            mPauseButton.setImageResource(R.drawable.pause_deep);

            onPause(mStartPos);

        }
    };

    private OnClickListener mRewindListener = new OnClickListener() {
        public void onClick(View sender) {
//            mPauseButton.setImageResource(R.drawable.pause);
            if (mIsPlaying) {
                Log.d(TAG, "onClick: player" + mPlayer.getCurrentPosition());
                int newPos = mPlayer.getCurrentPosition() - 15000;
                if (newPos < mPlayStartMsec)
                    newPos = mPlayStartMsec;
                mPlayer.seekTo(newPos);
            }
        }
    };
    private OnClickListener mFfwdListener = new OnClickListener() {
        public void onClick(View sender) {
//            mPauseButton.setImageResource(R.drawable.pause);
            if (mIsPlaying) {
                int newPos = 15000 + mPlayer.getCurrentPosition();
                if (newPos >= mPlayEndMsec) {
                    currentRecordingTimeMillis = 0;
                    playingTimer.setText(formatTime(currentRecordingTimeMillis));
                    //                    newPos = mPlayEndMsec;
                    handleStop();
                } else {
                    mPlayer.seekTo(newPos);

                }

            }

        }
    };
    private OnClickListener mToEndListener = new OnClickListener() {
        public void onClick(View sender) {
//            mPauseButton.setImageResource(R.drawable.pause);
            mEndMarker.requestFocus();
            markerFocus(mEndMarker);
        }
    };
    private OnClickListener mToStartListener = new OnClickListener() {
        public void onClick(View sender) {
//            mPauseButton.setImageResource(R.drawable.pause);
            mStartMarker.requestFocus();
            markerFocus(mStartMarker);
        }
    };
    private OnClickListener mNavigateToList = new OnClickListener() {
        public void onClick(View sender) {
//            CommonUtils.startNewActivityAndFinishCurrent(RingdroidEditActivity.this, MainActivity.class);
            finish();
//            clearTempDirectory();
        }
    };


    //wave touch listener
    private OnClickListener mMarkStartListener = new OnClickListener() {
        public void onClick(View sender) {
            if (mIsPlaying) {
                mStartPos = mWaveformView.millisecsToPixels(
                        mPlayer.getCurrentPosition());
                updateDisplay();
            }
        }
    };

    private OnClickListener mMarkEndListener = new OnClickListener() {
        public void onClick(View sender) {
            if (mIsPlaying) {
                mEndPos = mWaveformView.millisecsToPixels(
                        mPlayer.getCurrentPosition());
                updateDisplay();
                handlePause();
            }
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start,
                                      int count, int after) {
        }

        public void onTextChanged(CharSequence s,
                                  int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            if (mStartText.hasFocus()) {
                try {
                    mStartPos = mWaveformView.secondsToPixels(
                            Double.parseDouble(
                                    mStartText.getText().toString()));
                    updateDisplay();
                } catch (NumberFormatException e) {
                }
            }
            if (mEndText.hasFocus()) {
                try {
                    mEndPos = mWaveformView.secondsToPixels(
                            Double.parseDouble(
                                    mEndText.getText().toString()));
                    updateDisplay();
                } catch (NumberFormatException e) {
                }
            }
        }
    };

    private long getCurrentTime() {
        return System.nanoTime() / 1000000;
    }

    private void innitAdd() {


        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, getString(R.string.interstitial_id), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });
    }

    private String getStackTrace(Exception e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    public void onBackPressed() {
//        NavController navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment_activity_main);
//        NavDestination currentDestination = navController.getCurrentDestination();
//        if (currentDestination != null) {
//            int currentFragmentId = currentDestination.getId();
//
//        }
//        CommonUtils.startNewActivityAndFinishCurrent(RingdroidEditActivity.this, MainActivity.class);
        finish();
    }
}
