package com.ataraxia.pawahara.ui.plan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;


import com.ataraxia.pawahara.R;
import com.ataraxia.pawahara.adapter.PlanRecyclerViewAdapter;
import com.ataraxia.pawahara.databinding.ActivityPlanBinding;
import com.ataraxia.pawahara.model.Plan_pojos;
import com.ataraxia.pawahara.utils.CommonUtils;
import com.ataraxia.pawahara.utils.Constant;
import com.ataraxia.pawahara.utils.PermissionUtils;
import com.ataraxia.pawahara.utils.PreferenceUtils;
import com.google.android.gms.common.internal.service.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class PlansActivity extends AppCompatActivity {
    private  PlanViewModel planViewModel;
    ActivityPlanBinding binding;
    private RecyclerView recyclerView;
    private PlanRecyclerViewAdapter adapter;
    private ImageView planAppLogo;
    private static final int SETTINGS_REQUEST_CODE = 123;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding= DataBindingUtil.setContentView(this,R.layout.activity_plan);
        recyclerView=binding.recyclerView;
        planAppLogo=binding.planAppLogo;

        String currentLanguage = Locale.getDefault().getLanguage();
        requestNextPermission();
        planViewModel= new ViewModelProvider(this).get(PlanViewModel.class);
        List<Plan_pojos> plansList = new ArrayList<>();

        plansList.add(new Plan_pojos(Constant.freePlanType,getResources().getString(R.string.free)+" "+getResources().getString(R.string.plan),Constant.freePlanPrice,Constant.freePlanTime));
//        plansList.add(new Plan_pojos(Constant.PlanType1Hour,CommonUtils.getPlanName(this,1),Constant.PlanPrice1Hour,Constant.PlanTime1Hour));
//        plansList.add(new Plan_pojos(Constant.PlanType2Hour,CommonUtils.getPlanName(this,2),Constant.PlanPrice2Hour,Constant.PlanTime2Hour));
//        plansList.add(new Plan_pojos(Constant.PlanType3Hour,CommonUtils.getPlanName(this,3),Constant.PlanPrice3Hour,Constant.PlanTime3Hour));

        HashMap<Integer, String> indexPriceMap = PreferenceUtils.loadPriceMap(PlansActivity.this);

        for (int index : indexPriceMap.keySet()) {
            int value = Integer.parseInt(indexPriceMap.get(index).substring(1));
            String planName = CommonUtils.getPlanName(PlansActivity.this,index);
            int planTime = CommonUtils.getPlanTime(index);
            plansList.add(new Plan_pojos(index, planName, value, planTime));
        }

        adapter = new PlanRecyclerViewAdapter(this, this,plansList,v->{
            Plan_pojos plan = (Plan_pojos) v.getTag();
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        planViewModel.setPlansList(plansList);

        if ("ja".equals(currentLanguage)) {
            planAppLogo.setImageResource(R.drawable.ja_icon_image);
        } else {
            planAppLogo.setImageResource(R.drawable.en_icon_image);
        }
        planViewModel.getPlansList().observe(this, plans_list -> {
            adapter.setPlanslist(plans_list);
        });
        if (areAllPermissionsGranted()) {
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.GONE);
        }
    }


    private void requestNextPermission() {
        if (!PermissionUtils.hasRecordAudioPermission(this)) {
            PermissionUtils.requestRecordAudioPermissions(this);
        } else if (!PermissionUtils.hasStoragePermission(this)) {
            PermissionUtils.requestStoragePermissions(this);
        } else if (!PermissionUtils.hasPostNotificationsPermission(this)) {
            PermissionUtils.requestPostNotificationsPermissions(this);
        }
        else if (!PermissionUtils.hasmediaaudioPermission(this)) {
            PermissionUtils.requestmediaaudioPermissions(this);
        }else if (!PermissionUtils.hasphonestatePermission(this)) {
            PermissionUtils.requestphonestatePermissions(this);
        }else {
            if (areAllPermissionsGranted()) {
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.GONE);
            }
        }
        // All permissions are granted, proceed with your logic
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("permission management", "onRequestPermissionsResult: "+requestCode+ grantResults);
        if (requestCode == PermissionUtils.RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestNextPermission(); // Continue with next permission if needed
            } else {
                showPermissionRationaleDialog(getString(R.string.audio_record_premission_checking),
                        () -> PermissionUtils.requestRecordAudioPermissions(PlansActivity.this));
            }
        }
        if (requestCode == PermissionUtils.STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestNextPermission(); // Continue with next permission if needed
            } else {
                showPermissionRationaleDialog(getString(R.string.audio_record_premission_checking),
                        () -> PermissionUtils.requestRecordAudioPermissions(PlansActivity.this));
            }
        }
        if (requestCode == PermissionUtils.POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestNextPermission(); // Continue with next permission if needed
            } else {
                showPermissionRationaleDialog(getString(R.string.notification_permission_needed_for_notification),
                        () -> PermissionUtils.requestPostNotificationsPermissions(PlansActivity.this));
            }
        }
        if (requestCode == PermissionUtils.MEDIA_AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestNextPermission(); // Continue with next permission if needed
            } else {
                showPermissionRationaleDialog(getString(R.string.file_permission_needed),
                        () -> PermissionUtils.requestPostNotificationsPermissions(PlansActivity.this));
            }
        }

        if (requestCode == PermissionUtils.READ_PHONE_STATE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestNextPermission(); // Continue with next permission if needed
            } else {
                showPermissionRationaleDialog(getString(R.string.phone_state_permission_needed),
                        () -> PermissionUtils.requestPostNotificationsPermissions(PlansActivity.this));

            }

        }

        if (requestCode == PermissionUtils.RECORD_AUDIO_PERMISSION_REQUEST_CODE ||
                requestCode == PermissionUtils.STORAGE_PERMISSION_REQUEST_CODE ||
                requestCode == PermissionUtils.POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE ||
                requestCode == PermissionUtils.MEDIA_AUDIO_PERMISSION_REQUEST_CODE||
                requestCode == PermissionUtils.READ_PHONE_STATE_PERMISSION_REQUEST_CODE

        ) {
            if (areAllPermissionsGranted()) {
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.GONE);
            }
        }
    }

    private void showPermissionRationaleDialog(String message, Runnable tryAgainAction) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setNeutralButton(getResources().getString(R.string.settings), (dialog, which) -> {
                    openAppSettings();
                    dismissPermissionRationaleDialog(dialog);
                })
                .setCancelable(false)  // Make the dialog not cancelable when tapping outside of it
                .create()
                .show();
    }

    private void dismissPermissionRationaleDialog(DialogInterface dialog) {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, SETTINGS_REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("coming back", "onActivityResult: "+resultCode);
        if (requestCode == SETTINGS_REQUEST_CODE) {
            // Check if the user made changes in the app settings
            if (resultCode == RESULT_OK) {
                requestNextPermission();
            } else {
                requestNextPermission();
            }
        }
    }
    private boolean areAllPermissionsGranted() {
        return PermissionUtils.hasRecordAudioPermission(this) &&
                PermissionUtils.hasStoragePermission(this) &&
                PermissionUtils.hasmediaaudioPermission(this)&&
                PermissionUtils.hasphonestatePermission(this);
    }

}