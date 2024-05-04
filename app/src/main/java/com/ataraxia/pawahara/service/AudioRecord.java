package com.ataraxia.pawahara.service;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ataraxia.pawahara.R;

import java.io.File;
import java.io.IOException;

public class AudioRecord {
    private Context context;
    private MediaRecorder mediaRecorder;
    private File audioFile;
    private CountDownTimer countDownTimer;
//    private List<String> audiolist;
    String directoryName ;

    File dir ;


    private static final long TIMER_DURATION = 300000;
    private static final int PERMISSION_REQUEST_CODE = 100;

    public AudioRecord(Context context) {
        this.context = context;
    }

    public  void createfile_start(){
        directoryName= "/"+context.getResources().getString(R.string.harassment_amulet)
                +"/"+context.getResources().getString(R.string.myrecordingdirectory)+"/";
        File audioDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
//        File myDirectory = new File(audioDir, context.getResources().getString(R.string.harassment_amulet)+"/");
//        File audioDir = new File(context.getFilesDir(), "audio");
        dir = new File(audioDir, directoryName);



        //path test


        Log.d(ContentValues.TAG, "Audio Records from record: "+dir.getAbsolutePath());
//        File file = new File(myDirectory, "yourFileName.ext");
// Your file-saving logic here





        if (dir.exists()) {
            // Delete the directory and its contents (the audio file)

            dir.mkdirs();

        }else{
            dir.mkdirs();
        }
        Record();
    }
    void Record(){

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
        } else {

            startTimer();


        }

    }


    @SuppressLint("RestrictedApi")
    public void startRecording() {
        Log.d(ContentValues.TAG, "Audio Records from record2: "+dir.getAbsolutePath());
        try {
            audioFile = File.createTempFile("sound", ".wav", dir);
            Log.d(ContentValues.TAG, "Audio Records from record3: "+audioFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "External storage access error");
            return;
        }

        // Creating MediaRecorder and specifying audio source, output format, encoder, and output file
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioSamplingRate(44100); // Standard audio sampling rate
        mediaRecorder.setAudioChannels(1);
        mediaRecorder.setAudioEncodingBitRate(64000);
        mediaRecorder.setOutputFile(audioFile.getAbsolutePath());

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        mediaRecorder.start();
    }

    public void stopRecording() {
//        Toast.makeText(context, "Recording stopped1", Toast.LENGTH_SHORT).show();
        if (mediaRecorder != null) {
            Log.d("recorder", "stopRecording: stopped ");
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            stopTimer();

        }
    }



    private CountDownTimer startTimer() {
        countDownTimer = new CountDownTimer(TIMER_DURATION, TIMER_DURATION) {
            @SuppressLint("RestrictedApi")
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, "onTick: ticking ");
                // Do nothing on tick
                startRecording();
            }
            @Override
            public void onFinish() {
                stopRecording();
                startTimer();
            }
        };
        countDownTimer.start();
        return countDownTimer;
    }


    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

}
