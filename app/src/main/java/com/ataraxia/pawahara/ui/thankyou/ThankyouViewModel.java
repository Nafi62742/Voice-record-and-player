package com.ataraxia.pawahara.ui.thankyou;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ThankyouViewModel extends ViewModel {
    private MutableLiveData<SpannableString> spannableTextLiveData = new MutableLiveData<>();

    // Getter method for the LiveData
    public LiveData<SpannableString> getSpannableTextLiveData() {
        return spannableTextLiveData;
    }

    // Method to update the LiveData with the formatted text
    public void updateSpannableText() {
        // Get the string resource
        String text = "ご利用には 「マイク」 の許可と \n「プッシュ通知」、 「利用規約」 の同意をお願いします。";

        // Create a SpannableString from the text
        SpannableString spannableString = new SpannableString(text);

        // Find the starting and ending positions of the target text "プッシュ通知"
        int startIndex = text.indexOf("「プッシュ通知」");
        int endIndex = startIndex + "「プッシュ通知」".length();

        // Create a ClickableSpan for the target text
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // Handle the click action here (e.g., open a link)
                // For now, let's show a Toast message as an example
                       }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                // Set the text color to yellow
                ds.setColor(Color.BLUE);
                // Underline the text
                ds.setUnderlineText(false);
            }
        };

        // Apply the ClickableSpan to the target text
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        // Set the SpannableString to the LiveData
        spannableTextLiveData.setValue(spannableString);
    }
}
