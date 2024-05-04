package com.ataraxia.pawahara.ui.splash;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SplashViewmodel extends ViewModel {
    private MutableLiveData<String> textLiveData;
    private MutableLiveData<String> textLiveheaderData;
    private MutableLiveData<String> inputtext;

    public SplashViewmodel() {
        textLiveData = new MutableLiveData<>();
        inputtext = new MutableLiveData<>();
        textLiveheaderData = new MutableLiveData<>();
        textLiveData.setValue("This is text from ViewModel");

        textLiveheaderData.setValue("This is header text from ViewModel");
    }

    public LiveData<String> getText() {
        return textLiveData;
    }
    public LiveData<String> getheaderText() {
        return textLiveheaderData;
    }

    public void setInputText(String inputText) {
        textLiveData.setValue(inputText);
    }



}
