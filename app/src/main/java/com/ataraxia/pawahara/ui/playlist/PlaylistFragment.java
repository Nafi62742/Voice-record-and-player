package com.ataraxia.pawahara.ui.playlist;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ataraxia.pawahara.MainActivity;
import com.ataraxia.pawahara.R;
import com.ataraxia.pawahara.adapter.PlaylistRecyclerViewAdapter;

import com.ataraxia.pawahara.databinding.FragmentPlaylistBinding;
import com.ataraxia.pawahara.model.Records_pojos;
import com.ataraxia.pawahara.model.ToggleState;
import com.ataraxia.pawahara.service.AudioRecord;
import com.ataraxia.pawahara.ui.home.HomeViewModel;
import com.ataraxia.pawahara.utils.CommonUtils;
import com.ataraxia.pawahara.utils.Constant;
import com.ataraxia.pawahara.utils.Popuputils;
import com.ataraxia.pawahara.utils.PreferenceUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlaylistFragment extends Fragment {

    private FragmentPlaylistBinding binding;
    private PlaylistRecyclerViewAdapter adapter;
    private HomeViewModel homeViewModel;
    private PlaylistViewModel playlistViewModel;
    private AudioRecord audioRecord;
    private RecyclerView recyclerView;
    private List<Records_pojos> recordsData;
    private LinearLayout masterData;
    private TextView listText;
    private TextView recordStateTV;
    private TextView noEditedDataText;
    private TextView noRecordText;
    String directoryName;
    String newDirectoryName;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        playlistViewModel = new ViewModelProvider(requireActivity()).get(PlaylistViewModel.class);
        directoryName= "/"+getResources().getString(R.string.harassment_amulet)
                +"/"+getResources().getString(R.string.myrecordingdirectory);
        newDirectoryName = "/"+getResources().getString(R.string.harassment_amulet)
                +"/"+getResources().getString(R.string.savecutdirectory);

        TextView totalTime =binding.totalTime;
        TextView dateTextView =binding.dateTextView;
        recordStateTV =binding.recordStateTV;
        masterData=binding.masterData;
        listText =binding.listText;
        noEditedDataText =binding.noEditedData;
        noRecordText =binding.noRecordData;

        playlistViewModel.getDuration().observe(getViewLifecycleOwner(), duration -> {
            // Update UI with the new duration
            totalTime.setText(String.valueOf(duration));
            // Save the duration to SharedPreferences
            PreferenceUtils.saveDuration(requireContext(), duration);
        });
        List<Records_pojos> recordList =new ArrayList<>();
        List<Records_pojos> dataList =new ArrayList<>();
        int planType = PreferenceUtils.getState(getContext());

        int planTime = CommonUtils.getTimeLimit(planType)*60*1000;
        Log.d("timeInSeconds", String.valueOf(planTime));
        playlistViewModel.fetchMyRecords(requireContext(),directoryName,planTime);
        playlistViewModel.getMasterRecords().observe(getViewLifecycleOwner(), newRecords -> {
            dataList.clear();
            dataList.addAll(newRecords);
            if (!dataList.isEmpty()) {
                masterData.setVisibility(View.VISIBLE);
                listText.setVisibility(View.VISIBLE);
                noRecordText.setVisibility(View.GONE);
                // Set the date from the first index to dateTextView
                Date record_date_time =dataList.get(0).getRecord_date_time();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String  formattedDate= dateFormat.format(record_date_time);
//                String recordStatusText =formattedDate;
//                Collections.reverse(dataList); // Reverse the order of the list

                dateTextView.setText(formattedDate);
            }
            else{
                masterData.setVisibility(View.GONE);
                listText.setVisibility(View.GONE);
                noRecordText.setVisibility(View.VISIBLE);
            }
        });
        recordsData=dataList;

        recordList.clear();
        audioRecord=new AudioRecord(requireContext());
        recyclerView = binding.playlistRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PlaylistRecyclerViewAdapter(recordList,this,getContext());
//        recordsData=recordList;
        playlistViewModel.fetchAllRecords(requireContext(),newDirectoryName);
        //audioRecord.fetchAllRecords(adapter,recordList);
        playlistViewModel.getRecords().observe(getViewLifecycleOwner(), newRecords -> {

            recordList.clear();
            recordList.addAll(newRecords);
            if (!recordList.isEmpty()) {
                noEditedDataText.setVisibility(View.GONE);
                Collections.reverse(recordList); // Reverse the order of the list
                adapter.notifyDataSetChanged();
            }else{
                if (adapter.getItemCount() == 0) {
                    noEditedDataText.setVisibility(View.VISIBLE);
                } else {
                    noEditedDataText.setVisibility(View.GONE);
                }            }
            if (recordList.size() ==0) {
                // Data has been deleted
                noEditedDataText.setVisibility(View.VISIBLE);
            }
        });
//        adapter.notifyDataSetChanged();

        // Trigger the fetch operation

        recyclerView.setAdapter(adapter);

        homeViewModel.getisstartrecord().observe(getViewLifecycleOwner(), isstartrecord -> {
          //  Log.d(TAG, "onCreate: from vm"+isstartrecord);
            //scheduleTask(scheduletimes);//13:27
            if(isstartrecord){

                recordStateTV.setVisibility(View.VISIBLE);
                String stateOfRecording = " -"+getString(R.string.recording);
                recordStateTV.setText(stateOfRecording);
            }
        });


    }



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        HomeViewModel homeViewModel =
//            new ViewModelProvider(this).get(HomeViewModel.class);
//        PlaylistViewModel playlistViewModel =
//                new ViewModelProvider(this).get(PlaylistViewModel.class);

        binding = FragmentPlaylistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        Switch toggleSwitch = root.findViewById(R.id.toggle_switch);
//        TextView currenttime=root.findViewById(R.id.rec_start_timeTV);
// demo data adding       recordList.add(new Records_pojos(true,"test","2 seconds", "10/12/23"));

//        LinearLayout options = root.findViewById(R.id.options);

        LinearLayout master_data=binding.masterData;
        MainActivity mainActivity = (MainActivity) getActivity();
        CommonUtils.checkMemory(mainActivity,"playlist");
        master_data.setOnClickListener(new View.OnClickListener() {
            private long lastClickTime = 0;
            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();

                if (currentTime - lastClickTime > Constant.DEBOUNCE_INTERVAL) {
                    // Sufficient time has passed since the last click, show the popup
                    Popuputils.showPlayTrimPopup(v.getContext(),
                            recordsData,
                            mainActivity);
                    lastClickTime = currentTime;
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override
    public void onResume() {
        super.onResume();
        playlistViewModel.fetchAllRecords(requireContext(), newDirectoryName);
        adapter.notifyDataSetChanged();


    }

}