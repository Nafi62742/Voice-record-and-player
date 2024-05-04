package com.ataraxia.pawahara.adapter;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ataraxia.pawahara.R;
import com.ataraxia.pawahara.model.Schedule_Pojos;
import com.ataraxia.pawahara.utils.Popuputils;
import com.ataraxia.pawahara.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScheduleRecyclerViewAdapter extends RecyclerView.Adapter<ScheduleRecyclerViewAdapter.ScheduleViewHolder> {
    private List<Schedule_Pojos> scheduleList;
    private ScheduleRecyclerViewAdapter adapter;
    private Context context;


    public ScheduleRecyclerViewAdapter(Context context,List<Schedule_Pojos> scheduleList) {
        this.context = context;
        this.scheduleList = scheduleList;
        this.adapter=this;
        Collections.reverse(this.scheduleList);
    }

    @Override
    public ScheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_item, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ScheduleViewHolder holder, int position) {


        Schedule_Pojos schedule = scheduleList.get(position);

        int index =position;

        holder.scheduleNameTextView.setText(schedule.getSchedule_name());
        String currentLanguage = Locale.getDefault().getLanguage();

        String scheduleDaysText;
        HashMap<Integer, String> dayMapping = new HashMap<Integer, String>() {{
            put(Calendar.MONDAY, context.getString(R.string.mo));
            put(Calendar.TUESDAY, context.getString(R.string.tu));
            put(Calendar.WEDNESDAY, context.getString(R.string.we));
            put(Calendar.THURSDAY, context.getString(R.string.th));
            put(Calendar.FRIDAY, context.getString(R.string.fr));
            put(Calendar.SATURDAY, context.getString(R.string.sa));
            put(Calendar.SUNDAY, context.getString(R.string.su));
        }};

        StringBuilder scheduleDaysTextBuilder = new StringBuilder();
        List<Integer> scheduleDays = schedule.getSchedule_days();


        Collections.sort(scheduleDays, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                // Custom sorting logic: treat 1 as the largest, then sort the rest
                if (o1 == 1) {
                    return 1;   // o1 comes last
                } else if (o2 == 1) {
                    return -1;  // o2 comes last
                } else {
                    // For other values, use standard integer comparison
                    return Integer.compare(o1, o2);
                }
            }
        });
        Log.d(TAG, "onBindViewHolder: "+scheduleDays);
        for (int day : scheduleDays) {
            String dayName = dayMapping.get(day);
            if (dayName != null) {
                scheduleDaysTextBuilder.append(dayName).append(" ");
            }
        }
        // Remove the trailing comma and space
//        if (scheduleDaysTextBuilder.length() > 0) {
//            scheduleDaysTextBuilder.setLength(scheduleDaysTextBuilder.length() - 2);
//        }

        scheduleDaysText = scheduleDaysTextBuilder.toString();
        holder.scheduleDaysTextView.setText(scheduleDaysText);
        String timeText = String.format("%s - %s", schedule.getStart_time(), schedule.getEnd_time());
        holder.scheduleTimeTextView.setText(timeText);
        holder.scheduleSwitch.setChecked(schedule.getSchedule_switch());
        holder.listItem.setOnClickListener(new View.OnClickListener() {
            private static final long DEBOUNCE_INTERVAL = 1000; // Set the debounce interval in milliseconds
            private long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();

                if (currentTime - lastClickTime > DEBOUNCE_INTERVAL) {
                    // Sufficient time has passed since the last click, show the popup
                    Popuputils.showAddingDataPopup(v.getContext(), index, scheduleList, adapter, schedule);
                    lastClickTime = currentTime;
                }
            }
        });

        holder.scheduleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    schedule.setSchedule_switch(isChecked);
                    PreferenceUtils pref =new PreferenceUtils();
                    pref.saveScheduleListToSharedPreferences(buttonView.getContext(), scheduleList);
            }
        });
    }
    public void setScheduleList(List<Schedule_Pojos> scheduleList) {
        Collections.reverse(scheduleList);
        this.scheduleList = scheduleList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        LinearLayout listItem;
        TextView scheduleNameTextView;
        TextView scheduleDaysTextView;
        TextView scheduleTimeTextView;
        Switch scheduleSwitch;

        public ScheduleViewHolder(View itemView) {
            super(itemView);
            listItem = itemView.findViewById(R.id.list_item);
            scheduleNameTextView = itemView.findViewById(R.id.schedule_nameTV);
            scheduleDaysTextView = itemView.findViewById(R.id.schedule_daysTV);
            scheduleTimeTextView = itemView.findViewById(R.id.scheduletimesTV);
            scheduleSwitch = itemView.findViewById(R.id.schedule_switch);
        }
    }
}