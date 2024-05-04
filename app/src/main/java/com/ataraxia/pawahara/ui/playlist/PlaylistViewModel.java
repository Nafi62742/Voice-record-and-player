package com.ataraxia.pawahara.ui.playlist;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ataraxia.pawahara.model.Records_pojos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaylistViewModel extends ViewModel {
    private MutableLiveData<List<Records_pojos>> recordsLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Records_pojos>> masterLiveData = new MutableLiveData<>();
    public LiveData<List<Records_pojos>> getRecords() {
        return recordsLiveData;
    }    public LiveData<List<Records_pojos>> getMasterRecords() {
        return masterLiveData;
    }
    private final MutableLiveData<String> mText;
    private final MutableLiveData<String> duration;

    public PlaylistViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is playlist fragment");
        duration =new MutableLiveData<>();

    }
    //            int planType = PreferenceUtils.getState(context);
//            int planTime = getTimeLimit(planType)*60;
//            Log.d("fetchMyRecords_plan_fetching", String.valueOf(planTime));
    public LiveData<String> getDuration() {
        return duration;
    }

    public void fetchAllRecords(Context context, String directoryName) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Records_pojos> recordList = new ArrayList<>();

            File audioDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            File dir = new File(audioDir, directoryName);
            Log.d(TAG, "fetchAllRecords from cutSave: "+dir.getAbsolutePath());
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        Log.d("FileName", "fetchAllRecords2: " + file.getAbsoluteFile());
                        long lastModified = file.lastModified();
                        Date date = new Date(lastModified);

                        Uri uri = Uri.fromFile(file);
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        try {
                            retriever.setDataSource(context, uri);
                            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            long durationMillis = durationStr != null ? Long.parseLong(durationStr) : 0;

                            String recordDuration = String.valueOf(durationMillis / 1000);
                            Log.d(recordDuration, recordDuration + " seconds?");
                            recordList.add(new Records_pojos(true, file.getName(), file.getAbsoluteFile().toString(), recordDuration, date));
                        } catch (Exception e) {
                            // Handle exceptions
                            e.printStackTrace();
                        } finally {
                            try {
                                retriever.release();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
            // Post the result back to the LiveData object on the main thread


                new Handler(Looper.getMainLooper()).post(() ->{

                            recordsLiveData.setValue(recordList);
                }

                );

        });
    }



    private String formatTime(long timeInMillis) {
        long seconds = (timeInMillis / 1000) % 60;
        long minutes = (timeInMillis / (1000 * 60)) % 60;
        long hours = (timeInMillis / (1000 * 60 * 60)) % 24;

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes,seconds);
    }

    public void fetchMyRecords(Context context, String directoryName,long planTimeInSeconds) {
//        planTimeInSeconds+=(4*60*1000);
        long planTimeInMilis =planTimeInSeconds;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<Records_pojos> recordList = new ArrayList<>();
            long totalDurationMillis = 0;
//            File audioDir = new File(context.getFilesDir(), "audio");
            File audioDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

            File dir = new File(audioDir, directoryName);
            Log.d(TAG, "fetchAllRecords from record: "+dir.getAbsolutePath());

//            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), directoryName);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    // Sort the files based on last modified time in ascending order (oldest first)
                    Arrays.sort(files, Comparator.comparingLong(File::lastModified));
                    boolean planLimitReached= false;
                    for (int i = files.length - 1; i >= 0; i--) {
                        File file = files[i];

//                        Log.d("FileName", "fetchAllRecords: " + file.getAbsoluteFile());
                        long lastModified = file.lastModified();
                        Date date = new Date(lastModified);

                        Uri uri = Uri.fromFile(file);
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        Log.d(TAG, "fetchMyRecords: stage 1");
                        try {
                            retriever.setDataSource(context, uri);
                            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            long durationMillis = durationStr != null ? Long.parseLong(durationStr) : 0;
                            String recordDuration = String.valueOf(durationMillis / 1000);

                            Log.d(TAG, "fetchMyRecords: stage 2");
                            if (totalDurationMillis + durationMillis <= planTimeInMilis && !planLimitReached) {
                                totalDurationMillis += durationMillis;
                                planLimitReached= false;
                                Log.d(TAG, "fetchMyRecords: stage 3");
                                recordList.add(new Records_pojos(true, file.getName(), file.getAbsoluteFile().toString(), recordDuration, date));
                            } else {
                                planLimitReached= true;
                                Log.d(TAG, "fetchMyRecords: stage 4");
                                break;
//                                deleteFile(file);

                            }
                        } catch (Exception e) {
                            // Handle exceptions
                            e.printStackTrace();
                        } finally {
                            try {
                                retriever.release();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                }
            }
            Collections.reverse(recordList);

            String totalDurationFormatted = formatTime(totalDurationMillis);
                new Handler(Looper.getMainLooper()).post(() -> {
//                    Collections.reverse(recordList);
                    masterLiveData.setValue(recordList);
                    duration.setValue(totalDurationFormatted);
                });
        });
    }


// ...

    private void deleteFile(File file) {
        if (file.exists() && file.delete()) {
            Log.d("FileDeleted", "File deleted: " + file.getAbsolutePath());
        } else {
            Log.e("FileDeleteError", "Failed to delete file: " + file.getAbsolutePath());
        }
    }



    public LiveData<String> getText() {
        return mText;
    }
}