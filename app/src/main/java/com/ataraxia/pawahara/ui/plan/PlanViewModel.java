package com.ataraxia.pawahara.ui.plan;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ataraxia.pawahara.model.Plan_pojos;

import java.util.List;

public class PlanViewModel extends ViewModel {
    private MutableLiveData<List<Plan_pojos>> plansList = new MutableLiveData<>();

    public void setPlansList(List<Plan_pojos> plan_pojos) {
        plansList.setValue(plan_pojos);
    }

    public LiveData<List<Plan_pojos>> getPlansList() {
        return plansList;
    }
}
