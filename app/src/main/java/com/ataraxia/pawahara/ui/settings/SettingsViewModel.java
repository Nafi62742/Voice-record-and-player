package com.ataraxia.pawahara.ui.settings;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ataraxia.pawahara.R;

public class SettingsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public SettingsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Settings");
//        mText.setValue(context.getResources().getString(R.string.settings));
    }

    public LiveData<String> getText() {
        return mText;
    }
}