/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ataraxia.pawahara.helper;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.ataraxia.pawahara.R;

import java.util.ArrayList;

public class ScheduleDeleteDialog extends Dialog {

//    private Spinner mTypeSpinner;
    private EditText mFilename;
    private Message mResponse;
    private String mOriginalName;
    private ArrayList<String> mTypeArray;
    private int mPreviousSelection;

    /**
     * Return a human-readable name for a kind (music, alarm, ringtone, ...).
     * These won't be displayed on-screen (just in logs) so they shouldn't
     * be translated.
     */

    public ScheduleDeleteDialog(Context context, Message response,String title, String buttonText) {
        super(context);

        // Inflate our UI from its XML layout description.
        setContentView(R.layout.file_delete);

        setTitle("resources.getString(R.string.file_save_title)");
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        CardView save = (CardView) findViewById(R.id.delete);
        CardView cancel = (CardView) findViewById(R.id.cancel);
        ImageView cancel_btn = (ImageView) findViewById(R.id.cancel_icon_common);
        TextView questionTxt = (TextView) findViewById(R.id.question_text);
        TextView positiveResponseTxt = (TextView) findViewById(R.id.positive_response_text);

        questionTxt.setText(title);
        positiveResponseTxt.setText(buttonText);

        save.setOnClickListener(saveListener);
        cancel.setOnClickListener(cancelListener);
        cancel_btn.setOnClickListener(cancelListener);


        mResponse = response;
    }    private View.OnClickListener saveListener = new View.OnClickListener() {
            public void onClick(View view) {
                mResponse.sendToTarget();
                dismiss();
            }
        };

    private View.OnClickListener cancelListener = new View.OnClickListener() {
            public void onClick(View view) {
                dismiss();
            }
        };
}
