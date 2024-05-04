package com.ataraxia.pawahara.subscription;

import static android.content.ContentValues.TAG;

import static com.android.billingclient.api.BillingClient.*;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.ataraxia.pawahara.MainActivity;
import com.ataraxia.pawahara.R;
import com.ataraxia.pawahara.databinding.ActivitySubsBinding;
import com.ataraxia.pawahara.ui.splash.SplashActivity;
import com.ataraxia.pawahara.utils.CommonUtils;
import com.ataraxia.pawahara.utils.PreferenceUtils;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Subs extends AppCompatActivity {
    private BillingClient billingClient;
    String subsname, phases, des, dur,serial;
    boolean isSuccess = false;
    ActivitySubsBinding binding;
    int subscriptionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subs);

        binding = ActivitySubsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        subscriptionType= PreferenceUtils.getState(this);

        billingClient = newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        // Start connection

        getPrice();
    }

    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
//            Log.d(TAG, "onPurchasesUpdated: on work1");

            if (billingResult.getResponseCode() == BillingResponseCode.OK) {
//                Log.d(TAG, "onPurchasesUpdated: on work4");

                if (purchases != null) {
                    handleOkPurchases(purchases);
                } else {
                    // Handle null purchases
//                    Log.e(TAG, "onPurchasesUpdated: Null purchases list");
                }
            } else {
                handleErrorResponse(billingResult);
            }
        }

        private void handleOkPurchases(List<Purchase> purchases) {
//            Log.d(TAG, "onPurchasesUpdated: on work");
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        }

        private void handleErrorResponse(BillingResult billingResult) {
            Log.d(TAG, "onPurchasesUpdated: error");

            switch (billingResult.getResponseCode()) {

                case BillingResponseCode.ITEM_ALREADY_OWNED:
                    binding.Status.setText("ITEM_ALREADY_OWNED");
                    isSuccess = true;
                    Connection.premium = true;
                    Connection.locked = false;
                    binding.subscribe.setVisibility(View.GONE);
                    break;
                case BillingResponseCode.FEATURE_NOT_SUPPORTED:
                    binding.Status.setText("FEATURE_NOT_SUPPORTED");
                    break;
                case BillingResponseCode.BILLING_UNAVAILABLE:
                    binding.Status.setText("BILLING_UNAVAILABLE");
                    break;
                case BillingResponseCode.USER_CANCELED:
                    binding.Status.setText("USER_CANCELED");
                    break;
                case BillingResponseCode.DEVELOPER_ERROR:
                    binding.Status.setText("DEVELOPER_ERROR");
                    break;
                case BillingResponseCode.ITEM_UNAVAILABLE:
                    binding.Status.setText("ITEM_UNAVAILABLE");
                    break;
                case BillingResponseCode.NETWORK_ERROR:
                    binding.Status.setText("NETWORK_ERROR");
                    break;
                case BillingResponseCode.SERVICE_DISCONNECTED:
                    binding.Status.setText("SERVICE_DISCONNECTED");
                    break;
                default:
                    Toast.makeText(Subs.this, "Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };


    private void getPrice() {
        subscriptionType= PreferenceUtils.getState(this);
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingResponseCode.OK) {

                    // The BillingClient is ready. You can query purchases here.
                    Log.d(TAG, "onBillingSetupFinished: ");
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            List<QueryProductDetailsParams.Product> productList =List.of(
                                    QueryProductDetailsParams.Product.newBuilder()
                                            .setProductId("pawahara1")
                                            .setProductType(BillingClient.ProductType.SUBS)
                                            .build()
                            );
                            QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                                    .setProductList(productList)
                                    .build();
                            Log.d(TAG, "onBillingSetupFinished: 2 " +productList);


                            billingClient.queryProductDetailsAsync(
                                    params,new ProductDetailsResponseListener() {
                                        public void onProductDetailsResponse(BillingResult billingResult,
                                                                             List<ProductDetails> productDetailsList) {
                                            Log.d(TAG, "onBillingSetupFinished: 3" + productDetailsList);

                                            for (ProductDetails productDetails : productDetailsList) {
                                                Log.d(TAG, "onBillingSetupFinished: 4 "+ productDetails.getProductId());


                                                String offerToken = productDetails.getSubscriptionOfferDetails().get(subscriptionType).getOfferToken();
                                                ImmutableList productDetailsParamsList =
                                                        ImmutableList.of(
                                                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                                                        // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                                                        .setProductDetails(productDetails)
                                                                        // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                                                        // for a list of offers that are available to the user
                                                                        .setOfferToken(offerToken)
                                                                        .build()
                                                        );
                                                subsname = productDetails.getName();
                                                des = productDetails.getDescription();
                                                serial =productDetails.getTitle();


//
                                                String formattedPrice = productDetails.getSubscriptionOfferDetails().get(subscriptionType)
                                                        .getPricingPhases().getPricingPhaseList().get(subscriptionType).getFormattedPrice();
                                                String billingPeriod = productDetails.getSubscriptionOfferDetails().get(subscriptionType)
                                                        .getPricingPhases().getPricingPhaseList().get(subscriptionType).getBillingPeriod();
                                                int recurrenceMode = productDetails.getSubscriptionOfferDetails().get(subscriptionType)
                                                        .getPricingPhases().getPricingPhaseList().get(subscriptionType).getRecurrenceMode();

                                                String n, duration, bp;
                                                bp = billingPeriod;
                                                n = billingPeriod.substring(1, 2);
                                                duration = billingPeriod.substring(2, 3);
                                                if (recurrenceMode == 2) {
                                                    if (duration.equals("M")) {
                                                        dur = " For " + n + " hour";
                                                    } else if (duration.equals("Y")) {
                                                        dur = " For " + n + " hour";
                                                    } else if (duration.equals("W")) {
                                                        dur = " For " + n + " hour";
                                                    } else if (duration.equals("D")) {
                                                        dur = " For " + n + " hour";
                                                    }

                                                } else {
                                                    if (duration.equals("P1M")) {
                                                        dur = " For " + n + " hour";
                                                    } else if (duration.equals("P6M")) {
                                                        dur = " For " + n + " hour";
                                                    } else if (duration.equals("P1Y")) {
                                                        dur = " For " + n + " hour";
                                                    } else if (duration.equals("P1W")) {
                                                        dur = " For " + n + " hour";
                                                    } else if (duration.equals("P3W")) {
                                                        dur = " For " + n + " hour";
                                                    }
                                                }
                                                phases = formattedPrice + " " + duration;
                                            }
                                        }
                                    }
                            );

                        }
                    });

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            binding.type.setText(subsname);
                            binding.txtSubs.setText("Prices "+phases);
                            binding.des.setText(des);
                            binding.planNo.setText(serial);

                        }
                    });
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
            }
        });
    }
    public void GetPrice(View view) {
        getPrice();
    }
    public void subscribe(View view) {
        Log.d(TAG, "subscribe: works");
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                Log.d(TAG, "subscribe: works2");

                List<QueryProductDetailsParams.Product> productList =List.of(
                  QueryProductDetailsParams.Product.newBuilder()
                          .setProductId("pawahara1")
                          .setProductType(ProductType.SUBS)
                          .build()
                );
                QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                        .setProductList(productList)
                                .build();

                billingClient.queryProductDetailsAsync(
                        params,
                        new ProductDetailsResponseListener() {
                            public void onProductDetailsResponse(BillingResult billingResult,
                                                                 List<ProductDetails> productDetailsList) {
                                for (ProductDetails productDetails : productDetailsList) {
                                    Log.d(TAG, "subscribe: works3");
                                    String offerToken = productDetails.getSubscriptionOfferDetails().get(0).getOfferToken();
                                    ImmutableList productDetailsParamsList =
                                            ImmutableList.of(
                                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                                            .setProductDetails(productDetails)
                                                            .setOfferToken(offerToken)
                                                            .build()
                                            );
                                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                            .setProductDetailsParamsList(productDetailsParamsList)
                                            .build();

// Launch the billing flow
                                    billingResult = billingClient.launchBillingFlow(Subs.this, billingFlowParams);
                                    Log.d(TAG, "onProductDetailsResponse: "+billingResult);

                                }
                                // check billingResult
                                // process returned productDetailsList
                            }
                        }
                );
            }
            @Override
            public void onBillingServiceDisconnected() {

            }
        });

    }

    void handlePurchase(Purchase purchase) {

        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.

        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                if (billingResult.getResponseCode() == BillingResponseCode.OK) {
                    // Handle the success of the consume operation.
                }
            }
        };
        List<String> skuList = new ArrayList<>();
        skuList.add("pawahara1");
        skuList.add("2hourplan");

        // Build SkuDetailsParams
        SkuDetailsParams params = SkuDetailsParams.newBuilder()
                .setSkusList(skuList)
                .setType(BillingClient.SkuType.INAPP)  // or SkuType.SUBS for subscriptions
                .build();
        billingClient.querySkuDetailsAsync(params, new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                    for (SkuDetails skuDetails : skuDetailsList) {
                        // Access SKU details
                        String sku = skuDetails.getSku();

                        String title = skuDetails.getTitle();
                        String price = skuDetails.getPrice();
                        Log.d(TAG, "onSkuDetailsResponse: skuInfo "+ sku+" " + title+" " + price );
                        // Handle other details as needed
                    }
                } else {
                    // Handle the error
                }
            }
        });
        billingClient.consumeAsync(consumeParams, listener);
        if(purchase.getPurchaseState()== Purchase.PurchaseState.PURCHASED){
            if(!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())){
                Toast.makeText(this, "Error : purchase invalid.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!purchase.isAcknowledged()){
                AcknowledgePurchaseParams acknowledgePurchaseParams =AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams,acknowledgePurchaseResponseListener);
                binding.Status.setText("Subscribed");
                PreferenceUtils.saveState(this, 1);
                CommonUtils.startNewActivityAndFinishCurrent((Activity) this, SplashActivity.class);
                isSuccess = true;
            }else if(purchase.getPurchaseState()==Purchase.PurchaseState.PENDING){
                binding.Status.setText("Subscription pending");
            }else if(purchase.getPurchaseState()==Purchase.PurchaseState.UNSPECIFIED_STATE){
                binding.Status.setText("Subscription UNSPECIFIED STATE");
            }
        }
    }
    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
            if (billingResult.getResponseCode() == BillingResponseCode.OK) {
                binding.Status.setText("Subscribed");
                isSuccess = true;
                Connection.premium = true;
                Connection.locked = false;
//                binding.subscribe.setVisibility(View.GONE);
            }
        }
    };
    private  boolean verifyValidSignature(String signedData, String signature){
        try {
                String base64Key ="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiK/9DbV4+3yyrfxsoLc0fyE8H4hDW6DtTATGuVkdnVM6dzGDXNIAuIN4XZ7xytbPno4QWFSd8SKX+oR7IJ5hyPaxbnz0aGSB346yYbaUKaIjO6FAVoJR4U2WddrwMUW/GBV/yQpiesl8Pv7OlwdAJ6S5jpZlvBCQBtAO/RcvB1da8i0t/9mVPJyZqQJ0oAGxvKs1zoO7OoIgCKQ+R1DPqx8QBjxo7WDBhjUBl4nzklzD0MzuAYiR23xFda+vmzI7b18Fex/ineSQMoRelyzHOPhc5fBuMcfOy2SYvJBxRk+RCqqL/H0LPpIY6fKRw1bPXdnh12wYreMdB/yfL+JC3wIDAQAB";
                return Security.verifyPurchase(base64Key,signedData,signature);
        }catch (IOException e){
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(billingClient!=null){
            billingClient.endConnection();
        }
    }
}