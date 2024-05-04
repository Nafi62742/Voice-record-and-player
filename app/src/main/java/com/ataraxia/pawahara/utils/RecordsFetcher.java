package com.ataraxia.pawahara.utils;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


import com.ataraxia.pawahara.model.Records_pojos;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecordsFetcher {

    private static final String TAG = "RecordsFetcher";

    private final MutableLiveData<List<Records_pojos>> recordsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Records_pojos>> masterLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> duration = new MutableLiveData<>();


    public MutableLiveData<List<Records_pojos>> getRecordsLiveData() {
        return recordsLiveData;
    }

    public MutableLiveData<List<Records_pojos>> getMasterLiveData() {
        return masterLiveData;
    }

    public LiveData<String> getDuration() {
        return duration;
    }

    public interface RecordsFetchCallback {
        void onRecordsFetched(List<Records_pojos> recordList, String totalDurationFormatted);
    }

    public void fetchMyRecords(Context context, String directoryName, long planTimeInMilis) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
//            List<Records_pojos> recordList = new ArrayList<>();
            long totalDurationMillis = 0;

            File audioDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            File dir = new File(audioDir, directoryName);
            Log.d(TAG, "fetchAllRecords from record: " + dir.getAbsolutePath());

            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    Arrays.sort(files, Comparator.comparingLong(File::lastModified));
                    boolean planLimitReached = false;

                    for (int i = files.length - 1; i >= 0; i--) {
                        File file = files[i];
                        long lastModified = file.lastModified();
                        Date date = new Date(lastModified);

                        Uri uri = Uri.fromFile(file);
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

                        try {
                            retriever.setDataSource(context, uri);
                            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            long durationMillis = durationStr != null ? Long.parseLong(durationStr) : 0;

                            if (totalDurationMillis + durationMillis <= planTimeInMilis && !planLimitReached) {
                                totalDurationMillis += durationMillis;
                                Log.d(TAG, "Accepting file time: " + date + " file no" + i + " " + totalDurationMillis);
                                planLimitReached = false;
                            } else {
                                planLimitReached = true;
                                Log.d(TAG, "Deleting file time: " + date + " file no" + i + " " + totalDurationMillis + " " + durationMillis);
                                deleteFile(file);
                            }
                        } catch (Exception e) {
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

            new Handler(Looper.getMainLooper()).post(() -> {

            });
        });
    }

    public static void deleteFile(File file) {
        if (file.exists() && file.delete()) {
            Log.d("FileDeleted", "File deleted: " + file.getAbsolutePath());
        } else {
            Log.e("FileDeleteError", "Failed to delete file: " + file.getAbsolutePath());
        }
    }

}