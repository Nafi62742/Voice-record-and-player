package com.ataraxia.pawahara.ui.thankyou;

import static android.content.ContentValues.TAG;
import static com.ataraxia.pawahara.utils.CommonUtils.startNewActivityAndFinishCurrent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ataraxia.pawahara.R;
import com.ataraxia.pawahara.databinding.ActivityThankyouBinding;
import com.ataraxia.pawahara.helper.CustomDialogBtn;
import com.ataraxia.pawahara.subscription.SubscriptionManager;
import com.ataraxia.pawahara.ui.plan.PlansActivity;
import com.ataraxia.pawahara.utils.Constant;
import com.ataraxia.pawahara.utils.Popuputils;
import com.ataraxia.pawahara.utils.PreferenceUtils;

import java.util.HashMap;
import java.util.Locale;

public class ThankyouActivity extends AppCompatActivity {
    private ActivityThankyouBinding activityThankyouBinding;
    private  ThankyouViewModel thankyouViewModel;
    private ImageView planAppLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSubsPrices();

        activityThankyouBinding= DataBindingUtil.setContentView(this,R.layout.activity_thankyou);

        String currentLanguage = Locale.getDefault().getLanguage();

        final CardView terms_conditionBtn=activityThankyouBinding.termsBtn;
        final  TextView textView=activityThankyouBinding.termsTvId;
        thankyouViewModel= new ViewModelProvider(this).get(ThankyouViewModel.class);
        thankyouViewModel.getSpannableTextLiveData().observe(this, new Observer<SpannableString>() {
            @Override
            public void onChanged(SpannableString spannableString) {
//                // Update the TextView with the formatted text
//                // Replace with your TextView's ID
//                textView.setText(spannableString);
//
//                // Make the TextView clickable
//                textView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        });
        thankyouViewModel.updateSpannableText();
        planAppLogo=activityThankyouBinding.planAppLogo;

        if ("ja".equals(currentLanguage)) {
            planAppLogo.setImageResource(R.drawable.ja_icon_image);
        } else {
            planAppLogo.setImageResource(R.drawable.en_icon_image);
        }
        terms_conditionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = getString(R.string.term_popup_title);
                String details = "Terms";
                String btn_text = getResources().getString(R.string.terms_confirm_btn);

                HashMap<Integer, String> indexPriceMap = PreferenceUtils.loadPriceMap(ThankyouActivity.this);



                Popuputils.showCommonPopup(ThankyouActivity.this, title,details,btn_text, terms_conditionBtn -> {

                    if (Constant.isConnected(ThankyouActivity.this)) {
                        Log.d(TAG, "Internet Connected");

                        if(!indexPriceMap.keySet().isEmpty()){
                            startNewActivityAndFinishCurrent(ThankyouActivity.this, PlansActivity.class);
                        }
                        else {
                            Log.d(TAG, "No Subscription Connection");
                            //Change here to bypass
                            startNewActivityAndFinishCurrent(ThankyouActivity.this, PlansActivity.class);
//                            Popuputils.showCustomDialog(ThankyouActivity.this, getString(R.string.something_went_wrong));
                            //何か問題が発生しました。
                            //Something went wrong
                        }
                    } else {
                        Log.d(TAG, "No Internet Connection");
                        Popuputils.showCustomDialog(ThankyouActivity.this, getString(R.string.no_internet_connection));
                        //インターネット接続がありません
                        //No Internet Connection
                    }

                });

            }
        });

    }

    public void getSubsPrices(){

        SubscriptionManager subscriptionManager = new SubscriptionManager(ThankyouActivity.this);
        subscriptionManager.getSubPrice( new SubscriptionManager.PriceCallback() {
            @Override
            public void onPriceReceived(HashMap<Integer, String> indexPriceMap) {
                Log.d(TAG, "onPriceReceived: indexPriceMap " +indexPriceMap);
                PreferenceUtils.savePriceMap(ThankyouActivity.this, indexPriceMap);
            }
        });

    }
}