package com.ataraxia.pawahara.helper;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.ataraxia.pawahara.R;

public class CustomDialogBtn extends Dialog implements
        View.OnClickListener {

    private String message;
    private String btnTextInput;
    public interface OnOkButtonClickListener {
        void onOkButtonClick();
    }
    private OnOkButtonClickListener onOkButtonClickListener; // Add this interface


    public CustomDialogBtn(Context context, String message,String btnTextInput) {
        super(context);
        this.message = message;
        this.btnTextInput = btnTextInput;
    }
    public void setOnOkButtonClickListener(OnOkButtonClickListener listener) {
        this.onOkButtonClickListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog_btn);
        setCanceledOnTouchOutside(false);

        TextView messageTextView = findViewById(R.id.messageTextView);
        TextView btnText = findViewById(R.id.btn_text);
        CardView okButton = findViewById(R.id.okButton);

        messageTextView.setText(message);
        btnText.setText(btnTextInput);
//        okButton.setOnClickListener(this);

//        CardView okButton = findViewById(R.id.okButton);

        // Set the listener for the "OK" button click
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the interface method when the "OK" button is clicked
                if (onOkButtonClickListener != null) {
                    onOkButtonClickListener.onOkButtonClick();
                }
                dismiss();
            }
        });
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

    }

    @Override
    public void onClick(View v) {
        //dismiss();
    }
    // turn it off to debug faster
    @Override
    public void onBackPressed() {

    }
}
