package com.ataraxia.pawahara.ui.settings;


import static android.content.ContentValues.TAG;

import static com.ataraxia.pawahara.utils.CommonUtils.startNewActivityAndFinishCurrent;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;

import com.ataraxia.pawahara.MainActivity;
import com.ataraxia.pawahara.R;

import com.ataraxia.pawahara.adapter.PlanRecyclerViewAdapter;
import com.ataraxia.pawahara.databinding.FragmentSettingsBinding;
import com.ataraxia.pawahara.subscription.SubscriptionManager;
import com.ataraxia.pawahara.ui.plan.PlanViewModel;
import com.ataraxia.pawahara.ui.plan.PlansActivity;
import com.ataraxia.pawahara.ui.thankyou.ThankyouActivity;
import com.ataraxia.pawahara.utils.CommonUtils;
import com.ataraxia.pawahara.utils.Constant;
import com.ataraxia.pawahara.utils.Popuputils;
import com.ataraxia.pawahara.utils.PreferenceUtils;

import java.util.HashMap;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private PlanViewModel planViewModel;

    private PlanRecyclerViewAdapter adapter;
    private LinearLayout planeTab;



    private AlertDialog alertDialog;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);


        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        getSubsPrices();

        final TextView textView = binding.textNotifications;

        textView.setText(getResources().getString(R.string.settings));
        planeTab =binding.planDetailsCard;

        TextView termsBtn =binding.termsSettingBtn;
        TextView policy =binding.policy;
        termsBtn.setPaintFlags(termsBtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        policy.setPaintFlags(policy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        planViewModel= new ViewModelProvider(this).get(PlanViewModel.class);

        planViewModel.getPlansList().observe(getViewLifecycleOwner(), plans_list -> {
            adapter.setPlanslist(plans_list);
        });

        int planStatus = PreferenceUtils.getState(getContext());
        TextView name = binding.nameTextView;
        name.setText(getResources().getString(R.string.plan));


        TextView setting_plan_details = binding.settingPlanDetails;
        int value;
        HashMap<Integer, String> indexPriceMap = PreferenceUtils.loadPriceMap(getContext());
        if(planStatus>0){
            value = Integer.parseInt(indexPriceMap.get(planStatus).substring(1));
        }
        else{
            value=0;
        }
        String plan_type = CommonUtils.getPlanName(getContext(),planStatus) + "  "+value+" "+ getString(R.string.yen_per_month);
//        String plan_type = CommonUtils.getPlanName(getContext(),planStatus) + "  "+getDetailsString(planStatus)+" "+ getString(R.string.yen_per_month);

        setting_plan_details.setText(plan_type);
        MainActivity mainActivity = (MainActivity) getActivity();


        planeTab.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController((MainActivity) v.getContext(), R.id.nav_host_fragment_activity_main);
            NavDestination currentDestination = navController.getCurrentDestination();
            if (currentDestination != null) {
                int currentFragmentId = currentDestination.getId();

               // HashMap<Integer, String> indexPriceMap = PreferenceUtils.loadPriceMap(getContext());


                if (Constant.isConnected(getContext())) {
                    Log.d(TAG, "Internet Connected");

                    if(!indexPriceMap.keySet().isEmpty()){

                        if (currentFragmentId == R.id.navigation_setting) {
                            mainActivity.backToSettingUi();
                            navController.navigate(R.id.action_navigation_setting_to_plan_change_fragment);
                        }
                    }
                    else {
                        Log.d(TAG, "No Subscription Connection");
                        //Change here to bypass
                        if (currentFragmentId == R.id.navigation_setting) {
                            mainActivity.backToSettingUi();
                            navController.navigate(R.id.action_navigation_setting_to_plan_change_fragment);
                        }
//                        Popuputils.showCustomDialog(getContext(), getString(R.string.something_went_wrong));
                        //何か問題が発生しました。
                        //Something went wrong
                    }
                } else {
                    Log.d(TAG, "No Internet Connection");
                    Popuputils.showCustomDialog(getContext(), getString(R.string.no_internet_connection));
                    //インターネット接続がありません
                    //No Internet Connection
                }


                    // Now you can check the ID of the current fragment
//                    if (currentFragmentId == R.id.navigation_setting) {
//                        mainActivity.backToSettingUi();
//                        navController.navigate(R.id.action_navigation_setting_to_plan_change_fragment);
//                    }


            } else {
            }

//
        });

        termsBtn.setOnClickListener(new View.OnClickListener() {
            long DEBOUNCE_INTERVAL = 1000; // Set the debounce interval in milliseconds
            long lastClickTime = 0;
            @Override
            public void onClick(View v) {
                String title = getString(R.string.terms_of_service);
                String details = "Terms";
                String btn_text = "";
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime > DEBOUNCE_INTERVAL) {
                    Popuputils.showCommonPopup(getContext(), title, details, btn_text, termsBtn -> {
                    });
                    lastClickTime = currentTime;
                }
            }
        });

        policy.setOnClickListener(new View.OnClickListener() {
            long DEBOUNCE_INTERVAL = 1000; // Set the debounce interval in milliseconds
            long lastClickTime = 0;
            @Override
            public void onClick(View v) {
                String title = getResources().getString(R.string.privacy_policy);
                String btn_text = "";
                String details ="Policy";
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime > DEBOUNCE_INTERVAL) {
                    Popuputils.showCommonPopup(getContext(), title, details, btn_text, termsBtn -> {
                    });
                    lastClickTime = currentTime;
                }
            }
        });

        return root;
    }

    public void getSubsPrices(){

        SubscriptionManager subscriptionManager = new SubscriptionManager(getContext());
        subscriptionManager.getSubPrice( new SubscriptionManager.PriceCallback() {
            @Override
            public void onPriceReceived(HashMap<Integer, String> indexPriceMap) {
                Log.d(TAG, "onPriceReceived: indexPriceMap " +indexPriceMap);
                PreferenceUtils.savePriceMap(getContext(), indexPriceMap);


//                if (currentFragmentId == R.id.navigation_setting) {
//                    mainActivity.backToSettingUi();
//                    navController.navigate(R.id.action_navigation_setting_to_plan_change_fragment);
//                }

//                HashMap<Integer, String> indexPriceMap1 = new HashMap<>();
//                indexPriceMap1.put(3, "¥360");
//                indexPriceMap1.put(2, "¥250");
//                indexPriceMap1.put(1, "¥130");
//
//                PreferenceUtils.savePriceMap(getContext(), indexPriceMap1);

            }
        });

    }



    private String getDetailsString(int statusCode) {
        switch (statusCode) {
            case 0:
                return "0";
            case 1:
                return "130";
            case 2:
                return "250";
            case 3:
                return "360";
            // Add more cases as needed
            default:
                return "Unknown";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}