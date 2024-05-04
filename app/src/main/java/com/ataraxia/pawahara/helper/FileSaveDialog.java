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
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;

import com.ataraxia.pawahara.R;

import java.util.ArrayList;

public class FileSaveDialog extends Dialog {

    // File kinds - these should correspond to the order in which
    // they're presented in the spinner control


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


    public FileSaveDialog(Context context,
                          Resources resources,
                          String originalName,
                          Message response) {
        super(context);

        // Inflate our UI from its XML layout description.
        setContentView(R.layout.file_save);

        setTitle("resources.getString(R.string.file_save_title)");
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mFilename = (EditText)findViewById(R.id.filename);
        mOriginalName = originalName;


        setFilenameEditBoxFromName(false);
        CardView save = (CardView) findViewById(R.id.save);
        save.setOnClickListener(saveListener);
        CardView cancel = (CardView) findViewById(R.id.cancel);
        cancel.setOnClickListener(cancelListener);
        ImageView cancel_btn = (ImageView) findViewById(R.id.cancel_icon_common);
        cancel_btn.setOnClickListener(cancelListener);
        mResponse = response;
    }
    private void setFilenameEditBoxFromName(boolean onlyIfNotEdited) {
        if (onlyIfNotEdited) {
            CharSequence currentText = mFilename.getText();
            String expectedText = mOriginalName;

            if (!expectedText.contentEquals(currentText)) {
                return;
            }
        }
        mFilename.setText(mOriginalName);
    }

    private View.OnClickListener saveListener = new View.OnClickListener() {
            public void onClick(View view) {
                mResponse.obj = mFilename.getText();
//                mResponse.arg1 = mTypeSpinner.getSelectedItemPosition();
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
