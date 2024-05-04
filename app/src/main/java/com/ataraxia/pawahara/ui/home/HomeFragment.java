package com.ataraxia.pawahara.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ataraxia.pawahara.R;
import com.ataraxia.pawahara.adapter.ScheduleRecyclerViewAdapter;
import com.ataraxia.pawahara.databinding.FragmentHomeBinding;
import com.ataraxia.pawahara.model.Schedule_Pojos;
import com.ataraxia.pawahara.service.AudioRecord;
import com.ataraxia.pawahara.utils.Popuputils;
import com.ataraxia.pawahara.utils.PreferenceUtils;

import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private RecyclerView recyclerView;
    private ScheduleRecyclerViewAdapter adapter;
    private AudioRecord audioRecord;
    private HomeViewModel viewModel;
    private TimeViewModel t_viewModel;
    private  TextView presenttimer_day;
    private  TextView presenttimer_time;



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        t_viewModel=new ViewModelProvider(this).get(TimeViewModel.class);
        Schedule_Pojos schedule_pojos = new Schedule_Pojos();
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        recyclerView = binding.scheduleRecyclerView;
        presenttimer_day=binding.presentdateTV;
        presenttimer_time=binding.presentTimeTV;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        audioRecord=new AudioRecord(requireContext());
        PreferenceUtils pref =new PreferenceUtils();


        List<Schedule_Pojos> scheduleList =pref.getScheduleListFromSharedPreferences(root.getContext());
        adapter = new ScheduleRecyclerViewAdapter(getContext(),scheduleList);
        recyclerView.setAdapter(adapter);

        ImageView btnAdd = root.findViewById(R.id.add_btn);
        t_viewModel.getCurrentTime().observe(getViewLifecycleOwner(), newTime -> {
            // Update the TextView with the new time
            presenttimer_time.setText(newTime);
        });
        t_viewModel.getCurrentDate().observe(getViewLifecycleOwner(), newTime -> {
            // Update the TextView with the new time
            presenttimer_day.setText(newTime);
        });
        btnAdd.setOnClickListener(new View.OnClickListener() {
            private static final long DEBOUNCE_INTERVAL = 1000; // Set the debounce interval in milliseconds
            private long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();

                if (currentTime - lastClickTime > DEBOUNCE_INTERVAL) {
                    int dummy =0;
                    Popuputils.showAddingDataPopup(root.getContext(),dummy,scheduleList,adapter,schedule_pojos);
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
}