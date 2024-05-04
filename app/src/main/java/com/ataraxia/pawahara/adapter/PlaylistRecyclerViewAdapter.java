package com.ataraxia.pawahara.adapter;


import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.FileProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.ataraxia.pawahara.MainActivity;
import com.ataraxia.pawahara.R;
import com.ataraxia.pawahara.helper.FileSaveDialog;
import com.ataraxia.pawahara.helper.RingdroidEditActivity;
import com.ataraxia.pawahara.helper.ScheduleDeleteDialog;
import com.ataraxia.pawahara.model.Records_pojos;
import com.ataraxia.pawahara.ui.playlist.PlaylistFragment;
import com.ataraxia.pawahara.utils.Constant;
import com.ataraxia.pawahara.utils.Popuputils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlaylistRecyclerViewAdapter extends RecyclerView.Adapter<PlaylistRecyclerViewAdapter.RecordsViewHolder> {
    private List<Records_pojos> recordList;

    private List<RecordsViewHolder> holderList; // Initialize your list of holders

    private static final int REQUEST_CODE_EDIT = 1;
    private PlaylistFragment playlistFragment; // Reference to the PlaylistFragment
    String newDirectoryName;
    File audioDir;
    File dir;

    private File oldFile;
    private boolean mWasGetContentIntent;
    RecyclerView recyclerView;
    private PlaylistRecyclerViewAdapter adapter;

    private Context context; // Add a member variable for Context

    public PlaylistRecyclerViewAdapter(List<Records_pojos> recordList, PlaylistFragment playlistFragment, Context context) {
        this.recordList = recordList;
        this.adapter = this;
        this.holderList = new ArrayList<>();
        this.playlistFragment = playlistFragment;
        this.context = context; // Initialize the Context


    }


    @Override
    public RecordsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item, parent, false);

        return new RecordsViewHolder(view);
    }

    private String formatDuration(String durationInSecondsStr) {
        try {
            long durationInSeconds = Long.parseLong(durationInSecondsStr);
            long hours = durationInSeconds / 3600;
            long minutes = (durationInSeconds % 3600) / 60;
            long seconds = durationInSeconds % 60;

            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } catch (NumberFormatException e) {
            // Handle the case where the input is not a valid number.
            return "00:00:00"; // Default value if the input is not a valid number.
        }
    }

    private void hideOptionsForOtherItems(int clickedPosition, PlaylistRecyclerViewAdapter adapter) {
        for (int i = 0; i < holderList.size(); i++) {
            if (i != clickedPosition) {
                // Hide options for other holders
                RecordsViewHolder otherHolder = holderList.get(i);
                if (otherHolder != null) {
                    otherHolder.optionLayout.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(RecordsViewHolder holder, int position) {
        Records_pojos records = recordList.get(position);

        int index = position;
        holder.recordNameTextView.setText(records.getRecord_name());
        Date record_date_time = records.getRecord_date_time();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = dateFormat.format(record_date_time);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
        String ModifiedTime = timeFormat.format(record_date_time);
        String dateTime = formattedDate;
        holder.recordDurationView.setText(formatDuration(records.getRecord_duration()));
        newDirectoryName = "/" + context.getResources().getString(R.string.harassment_amulet)
                + "/" + context.getResources().getString(R.string.savecutdirectory);
        audioDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        dir = new File(audioDir, newDirectoryName);
//        Log.d(TAG, "onBindViewHolder dir2: "+dir);

        holder.recordDateView.setText(dateTime);
        if (!holderList.contains(holder)) {
            holderList.add(holder);
        }
        holder.optionsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                int visibility = holder.optionLayout.getVisibility();

                if (visibility == View.GONE) {
                    holder.optionLayout.setVisibility(View.VISIBLE);

                    // Hide option buttons for all items except the clicked one
                    hideOptionsForOtherItems(holder.getAdapterPosition(), adapter);
                    // Hide option buttons for all items except the clicked one
                } else {
                    holder.optionLayout.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        holder.playTrimList.setOnClickListener(new View.OnClickListener() {
            long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime > Constant.DEBOUNCE_INTERVAL) {

                    MainActivity mainActivity = (MainActivity) v.getContext();
                    boolean isToggleSwitchChecked = mainActivity.isToggleSwitchChecked();
                    if (isToggleSwitchChecked) {
                        mainActivity.setToggleSwitchChecked(!mainActivity.isToggleSwitchChecked());
                    }

                    Log.d("PATH", records.getRecord_name());
                    try {
                        Intent intent = new Intent(v.getContext(), RingdroidEditActivity.class);
                        //Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse("record"));
                        intent.putExtra("was_get_content_intent", mWasGetContentIntent);
                        intent.putExtra("filename", records.getRecord_name());
                        intent.putExtra("directory", newDirectoryName);

//                    intent.putExtra("filename",);
                        intent.setClassName("com.ataraxia.pawahara", "com.ataraxia.pawahara.helper.RingdroidEditActivity");
                        ((Activity) v.getContext()).startActivityForResult(intent, REQUEST_CODE_EDIT);
                    } catch (Exception e) {
                        Log.e("Ringdroid", "Couldn't start editor");
                    }
                    lastClickTime = currentTime;
                }
            }
        });
        holder.playTrimList2.setOnClickListener(new View.OnClickListener() {
            long DEBOUNCE_INTERVAL = 1000; // Set the debounce interval in milliseconds
            long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime > DEBOUNCE_INTERVAL) {
                    MainActivity mainActivity = (MainActivity) v.getContext();
                    boolean isToggleSwitchChecked = mainActivity.isToggleSwitchChecked();
                    if (isToggleSwitchChecked) {
                        mainActivity.setToggleSwitchChecked(!mainActivity.isToggleSwitchChecked());
                    }
                    Log.d("PATH", records.getRecord_name());
                    try {
                        Intent intent = new Intent(v.getContext(), RingdroidEditActivity.class);
                        intent.putExtra("was_get_content_intent", mWasGetContentIntent);
                        intent.putExtra("filename", records.getRecord_name());
                        intent.putExtra("directory", newDirectoryName);
                        intent.setClassName("com.ataraxia.pawahara", "com.ataraxia.pawahara.helper.RingdroidEditActivity");
                        ((Activity) v.getContext()).startActivityForResult(intent, REQUEST_CODE_EDIT);
                    } catch (Exception e) {
                        Log.e("Ringdroid", "Couldn't start editor");
                    }
                    lastClickTime = currentTime;
                }

            }
        });
        LinearLayout delete_icon = holder.optionLayout.findViewById(R.id.delete_icon);
        LinearLayout cancel_icon = holder.optionLayout.findViewById(R.id.cancel_icon);
        LinearLayout share_icon = holder.optionLayout.findViewById(R.id.share_icon);
        LinearLayout rename_icon = holder.optionLayout.findViewById(R.id.rename_icon);

        // Set a click listener for the cancel icon
        cancel_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.optionLayout.setVisibility(View.GONE);
//                alertDialog.dismiss();
            }
        });

        rename_icon.setOnClickListener(new View.OnClickListener() {
            long lastClickTime = 0;
            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime > Constant.DEBOUNCE_INTERVAL) {
                    @SuppressLint("HandlerLeak") final Handler handler = new Handler() {

                        public void handleMessage(Message response) {
                            CharSequence newTitle = (CharSequence) response.obj;
//                        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), newDirectoryName);

                            if (dir.exists() && dir.isDirectory()) {
                                File[] files = dir.listFiles();
                                if (files != null && files.length > 0) {
                                    File from = new File(dir, records.getRecord_name());
                                    String fileWithExtension = makeFilename(newTitle, ".wav");
                                    Log.d(TAG, "handleMessage: File with extension" + fileWithExtension);
                                    if (fileWithExtension != null) {
                                        File to = new File(dir, fileWithExtension);

                                        // Check if the original file exists before attempting to rename
                                        if (from.exists()) {
                                            Log.d("Record Lists", "onCreateView: " + to.getName());
                                            if (from.renameTo(to)) {
                                                // Creating the updated Records_pojos object
                                                Records_pojos updatedRecord = new Records_pojos(
                                                        true,
                                                        to.getName(),
                                                        to.getAbsolutePath(),
                                                        records.getRecord_duration(),
                                                        records.getRecord_date_time()
                                                );

                                                // Updating the recordList with the updated Records_pojos object
                                                recordList.set(index, updatedRecord);

                                                // Notify the adapter that the data set has changed
                                                adapter.notifyDataSetChanged();

                                                if (v.getContext() instanceof MainActivity) {
                                                    NavController navController = Navigation.findNavController((MainActivity) v.getContext(), R.id.nav_host_fragment_activity_main);
                                                    NavDestination currentDestination = navController.getCurrentDestination();
                                                    if (currentDestination != null) {
                                                        int currentFragmentId = currentDestination.getId();

                                                        // Now you can check the ID of the current fragment
                                                        if (currentFragmentId == R.id.navigation_playlist) {
                                                            // The NavController is currently on the "navigation_playlist" fragment
                                                            navController.navigate(R.id.navigation_playlist);
                                                        }
                                                    } else {
                                                    }
                                                }

                                            } else {
                                                // Handle the case where renaming failed
                                                Popuputils.showCustomDialog(v.getContext(), context.getResources().getString(R.string.failed_to_rename_the_file));


                                            }

                                        } else {
                                            // Handle the case where the original file doesn't exist
                                            Popuputils.showCustomDialog(context, context.getString(R.string.original_file_not_found));
                                        }
                                    } else {
                                        Popuputils.showCustomDialog(v.getContext(), context.getResources().getString(R.string.same_name_or_empty_name_field));
                                        adapter.notifyDataSetChanged();
                                        if (v.getContext() instanceof MainActivity) {
                                            NavController navController = Navigation.findNavController((MainActivity) v.getContext(), R.id.nav_host_fragment_activity_main);
                                            NavDestination currentDestination = navController.getCurrentDestination();
                                            if (currentDestination != null) {
                                                int currentFragmentId = currentDestination.getId();

                                                // Now you can check the ID of the current fragment
                                                if (currentFragmentId == R.id.navigation_playlist) {
                                                    // The NavController is currently on the "navigation_playlist" fragment
                                                    navController.navigate(R.id.navigation_playlist);
                                                }
                                            } else {
                                            }
                                        }
                                    }
//                        }
                                } else {
                                    // Handle the case where there are no files in the directory.
                                }
                            } else {
                                Popuputils.showCustomDialog(v.getContext(), context.getString(R.string.directory_not_found));
                            }
//                        adapter.notifyDataSetChanged();

                        }
                    };
                    Message message = Message.obtain(handler);
                    oldFile = new File(records.getRecord_name());

                    String justTitle = getBasename(records.getRecord_name());

                    FileSaveDialog dlog = new FileSaveDialog(
                            v.getContext(), v.getResources(), justTitle, message);


                    dlog.show();
                    holder.optionLayout.setVisibility(View.GONE);
                    lastClickTime = currentTime;

                }
            }
        });
        share_icon.setOnClickListener(new View.OnClickListener() {
            long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime > Constant.DEBOUNCE_INTERVAL) {
                    // Replace with the path to the file you want to share
                    if (dir.exists() && dir.isDirectory()) {
                        File fileToShare = new File(dir, records.getRecord_name());
                        Log.d(TAG, "onClick: fileToShare " + fileToShare.getAbsolutePath());

                        if (fileToShare.exists()) {
                            // Create a content URI for the file
                            Uri fileUri = FileProvider.getUriForFile(v.getContext(), v.getContext().getPackageName() + ".fileprovider", fileToShare);

                            Intent share = new Intent(Intent.ACTION_SEND);
                            share.setType("audio/*");
                            share.putExtra(Intent.EXTRA_STREAM, fileUri);
                            // Grant read permission to the receiving app
                            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            // Start the sharing activity
                            ((Activity) v.getContext()).startActivity(Intent.createChooser(share, "Share Sound File"));
                        } else {
                            Popuputils.showCustomDialog(v.getContext(), context.getString(R.string.directory_not_found));
                        }
                    }

                    lastClickTime = currentTime;
                }
            }
        });
        delete_icon.setOnClickListener(new View.OnClickListener() {
            long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime > Constant.DEBOUNCE_INTERVAL) {
                    @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
                        public void handleMessage(Message response) {
                            Log.d(TAG, "handleMessage: " + dir.getAbsolutePath());
                            if (dir.exists() && dir.isDirectory()) {
                                File[] files = dir.listFiles();
                                if (files != null && files.length > 0) {
                                    File fileToDelete = new File(dir, records.getRecord_name());


                                    Log.d("File Path", fileToDelete.getAbsolutePath());

                                    if (fileToDelete.exists()) {
                                        boolean deleted = fileToDelete.delete();
                                        if (deleted) {
                                            // File has been successfully deleted
                                            // You can also update your data list and notify the adapter
                                            if (index >= 0 && index < recordList.size()) {
                                                recordList.remove(index);

                                                if (recordList.isEmpty() && playlistFragment != null) {
                                                    TextView noEditedDataText = playlistFragment.getView().findViewById(R.id.noEditedData);
                                                    if (noEditedDataText != null) {
                                                        noEditedDataText.setVisibility(View.VISIBLE);
                                                    }
                                                }
                                                // Notify the adapter that an item has been removed
                                                adapter.notifyDataSetChanged();
                                            } else {
                                                Popuputils.showCustomDialog(v.getContext(), context.getResources().getString(R.string.unable_to_delete_file));
                                            }
                                            holder.optionLayout.setVisibility(View.GONE);
                                        } else {
                                            Popuputils.showCustomDialog(v.getContext(), context.getResources().getString(R.string.unable_to_delete_file));
                                        }
                                    } else {
                                        Popuputils.showCustomDialog(v.getContext(), context.getString(R.string.original_file_not_found));
                                    }
                                    adapter.notifyDataSetChanged();
                                }
                            } else {
                                Popuputils.showCustomDialog(v.getContext(), context.getString(R.string.directory_not_found));

                            }
                        }
                    };
                    Message message = Message.obtain(handler);
                    ScheduleDeleteDialog scheduleDeleteDialog = new ScheduleDeleteDialog(v.getContext(),
                            message, context.getResources().getString(R.string.delete_question), context.getResources().getString(R.string.delete));

// Show the ScheduleDeleteDialog
                    scheduleDeleteDialog.show();

                    holder.optionLayout.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    lastClickTime = currentTime;
                }
            }
        });

    }


    private String makeFilename(CharSequence title, String extension) {
        // Turn the title into a filename
        String filename = "";
        for (int i = 0; i < title.length(); i++) {
            if (Character.isLetterOrDigit(title.charAt(i))) {
                filename += title.charAt(i);
            }
        }
        String path = filename + extension;
        String testPath = dir + "/" + path;
        File testFile = new File(testPath);
        Log.d("testFile", testFile.toString());
        if (!testFile.exists() && filename.length() > 0) {
            // The file doesn't exist or is empty
            return path;
        } else {
            // The file exists, return null
            return null;
        }

    }

    private String getBasename(String filename) {
        return filename.substring(filename.lastIndexOf('/') + 1,
                filename.lastIndexOf('.'));
    }

    public void setRecordList(List<Records_pojos> recordList) {
        this.recordList = recordList;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public static class RecordsViewHolder extends RecyclerView.ViewHolder {
        LinearLayout listItem;
        LinearLayout optionLayout;
        LinearLayout optionsButton;
        LinearLayout playTrimList;
        LinearLayout playTrimList2;
        TextView recordNameTextView;
        TextView recordDurationView;
        TextView recordDateView;

        public RecordsViewHolder(View itemView) {
            super(itemView);
            optionLayout = itemView.findViewById(R.id.options);
            listItem = itemView.findViewById(R.id.record_item);
            playTrimList = itemView.findViewById(R.id.startLayout);
            playTrimList2 = itemView.findViewById(R.id.leftLayout);
            optionsButton = itemView.findViewById(R.id.optionsButton);
            recordNameTextView = itemView.findViewById(R.id.nameTextView);
            recordDurationView = itemView.findViewById(R.id.durationTextView);
            recordDateView = itemView.findViewById(R.id.dateTextView);
//            scheduleSwitch = itemView.findViewById(R.id.schedule_switch);
        }
    }
}