package com.ataraxia.pawahara.helper;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.ataraxia.pawahara.R;

public class CustomDialog extends Dialog implements
        android.view.View.OnClickListener {

    private String message;

    public CustomDialog(Context context, String message) {
        super(context);
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);
        setCanceledOnTouchOutside(false);

        TextView messageTextView = findViewById(R.id.messageTextView);
        CardView okButton = findViewById(R.id.okButton);

        messageTextView.setText(message);
        okButton.setOnClickListener(this);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }
}
