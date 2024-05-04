package com.ataraxia.pawahara.ui.settings;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ataraxia.pawahara.R;
import com.ataraxia.pawahara.adapter.PlanRecyclerViewAdapter;
import com.ataraxia.pawahara.databinding.FragmentPlanChangeFragmentBinding;
import com.ataraxia.pawahara.model.Plan_pojos;
import com.ataraxia.pawahara.subscription.Subs;
import com.ataraxia.pawahara.subscription.SubscriptionManager;
import com.ataraxia.pawahara.ui.plan.PlanViewModel;
import com.ataraxia.pawahara.utils.CommonUtils;
import com.ataraxia.pawahara.utils.Constant;
import com.ataraxia.pawahara.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class plan_change_fragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private FragmentPlanChangeFragmentBinding binding;

    private PlanViewModel planViewModel;

    private PlanRecyclerViewAdapter adapter;
    private LinearLayout planeTab;



    private AlertDialog alertDialog;

    public plan_change_fragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPlanChangeFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textNotifications;

        textView.setText(getResources().getString(R.string.change_plan));

        planeTab =binding.planTab;
        TextView restorePurchaseTv =binding.restorePurchaseTv;
        TextView cancelSubscriptionTv =binding.cancelSubscriptionTv;
        RecyclerView recyclerView=binding.recyclerViewSettings;
        planViewModel= new ViewModelProvider(this).get(PlanViewModel.class);
        List<Plan_pojos> plansList = new ArrayList<>();
//        plansList.add(new Plan_pojos(Constant.freePlanType,getResources().getString(R.string.free)+" "+getResources().getString(R.string.plan),Constant.freePlanPrice,Constant.freePlanTime));
//        plansList.add(new Plan_pojos(Constant.PlanType1Hour,CommonUtils.getPlanName(this,1),Constant.PlanPrice1Hour,Constant.PlanTime1Hour));
//        plansList.add(new Plan_pojos(Constant.PlanType2Hour,CommonUtils.getPlanName(this,2),Constant.PlanPrice2Hour,Constant.PlanTime2Hour));
//        plansList.add(new Plan_pojos(Constant.PlanType3Hour,CommonUtils.getPlanName(this,3),Constant.PlanPrice3Hour,Constant.PlanTime3Hour));


        plansList.add(new Plan_pojos(Constant.freePlanType,getResources().getString(R.string.free)+" "+getResources().getString(R.string.plan),Constant.freePlanPrice,Constant.freePlanTime));

        HashMap<Integer, String> indexPriceMap = PreferenceUtils.loadPriceMap(getContext());

        for (int index : indexPriceMap.keySet()) {
            int value = Integer.parseInt(indexPriceMap.get(index).substring(1));
            String planName = CommonUtils.getPlanName(getContext(),index);
            int planTime = CommonUtils.getPlanTime(index);
            plansList.add(new Plan_pojos(index, planName, value, planTime));
        }

        adapter = new PlanRecyclerViewAdapter(getContext(), getActivity(),plansList, v->{
            Plan_pojos plan = (Plan_pojos) v.getTag();
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        planViewModel.setPlansList(plansList);
        planViewModel.getPlansList().observe(getViewLifecycleOwner(), plans_list -> {
            adapter.setPlanslist(plans_list);

        });

//        restorePurchaseTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                int versionCode = BuildConfig.VERSION_CODE;
//                String versionName = BuildConfig.VERSION_NAME;
//
//                Log.d(TAG, "versionCode: "+versionCode);
//                Log.d(TAG, "versionName: "+versionName);
//
////                Intent intent = new Intent(v.getContext(), Subs.class);
////                startActivity(intent);
//            }
//        });
//        restorePurchaseTv.setOnClickListener(mHowToUseLink);
        restorePurchaseTv.setPaintFlags(restorePurchaseTv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        cancelSubscriptionTv.setOnClickListener(mCancelSubLink);
        cancelSubscriptionTv.setPaintFlags(cancelSubscriptionTv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

//        cancelSubscriptionTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int planStatus = PreferenceUtils.getState(getContext());
//
//                SubscriptionManager subscriptionManager = new SubscriptionManager(v.getContext());
//                subscriptionManager.subscriptionTrigger(getActivity(),0);
//            }
//        });
        return root;

    }


    private View.OnClickListener mCancelSubLink = new View.OnClickListener() {
        long lastClickTime = 0;
        public void onClick(View sender) {
            // Open Google link on click
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime > Constant.DEBOUNCE_INTERVAL) {
                String googleLink = Constant.CancelSubLink;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(googleLink));
                startActivity(intent);
                lastClickTime = currentTime;
            }
        }
    };
}