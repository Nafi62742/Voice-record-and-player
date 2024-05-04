package com.ataraxia.pawahara.subscription;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.ataraxia.pawahara.MainActivity;
import com.ataraxia.pawahara.ui.splash.SplashActivity;
import com.ataraxia.pawahara.utils.CommonUtils;
import com.ataraxia.pawahara.utils.PreferenceUtils;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubscriptionManager {

    private BillingClient billingClient;
    private String subsname, phases, des, dur,Price;
    Context context;
    private int productID;
    private boolean isSuccess = false;
    private int subscriptionType;

    public SubscriptionManager(Context con) {
        this.context= con;
        // Initialize your variables and setup BillingClient here
        billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        // Start connection
    }

    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
//            Log.d(TAG, "onPurchasesUpdated: on work1");

            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "onPurchasesUpdated: on work4");

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

                case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                    Log.d(TAG, "onPurchasesUpdated: ITEM_ALREADY_OWNED");
                    isSuccess = true;
                    Connection.premium = true;
                    Connection.locked = false;
                    PreferenceUtils.saveState(context, productID);
                    billingClient.endConnection();

                    if (!(context instanceof MainActivity)) {
                        CommonUtils.startNewActivityAndFinishCurrent((Activity) context, MainActivity.class);
                    }else{
                       // CommonUtils.startNewActivityAndFinishCurrent((Activity) context, SplashActivity.class);
                    }

                    break;
                case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                    Log.d(TAG, "onPurchasesUpdated: FEATURE_NOT_SUPPORTED");
                    billingClient.endConnection();
                    break;
                case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                    Log.d(TAG, "onPurchasesUpdated: BILLING_UNAVAILABLE");
                    billingClient.endConnection();
                    break;
                case BillingClient.BillingResponseCode.USER_CANCELED:
                    Log.d(TAG, "onPurchasesUpdated: USER_CANCELED");
                    billingClient.endConnection();
                    break;
                case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                    Log.d(TAG, "onPurchasesUpdated: DEVELOPER_ERROR");
                    billingClient.endConnection();
                    break;
                case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                    Log.d(TAG, "onPurchasesUpdated: ITEM_UNAVAILABLE");
                    billingClient.endConnection();
                    break;
                case BillingClient.BillingResponseCode.NETWORK_ERROR:
                    Log.d(TAG, "onPurchasesUpdated: NETWORK_ERROR");
                    billingClient.endConnection();
                    break;
                case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                    Log.d(TAG, "onPurchasesUpdated: SERVICE_DISCONNECTED");
                    billingClient.endConnection();
                    break;
                default:
                    Log.d(TAG, "onPurchasesUpdated: default Error ");
                    Toast.makeText(context, "Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        // Other methods remain unchanged
    };
    void handlePurchase(Purchase purchase) {
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // Handle the success of the consume operation.
                }
            }
        };

        billingClient.consumeAsync(consumeParams, listener);
        if(purchase.getPurchaseState()== Purchase.PurchaseState.PURCHASED){
            if(!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())){
                Toast.makeText(context, "Error : purchase invalid.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!purchase.isAcknowledged()){
                AcknowledgePurchaseParams acknowledgePurchaseParams =AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams,acknowledgePurchaseResponseListener);
                PreferenceUtils.saveState(context, productID);
                if(currentPlanPrice.isEmpty()){
                    PreferenceUtils.savePrice(context, currentPlanPrice);
                }
                billingClient.endConnection();
                Log.d(TAG, "handlePurchase: purchase.getPackageName() "+purchase.getPackageName());
                if (!(context instanceof MainActivity)) {
                    CommonUtils.startNewActivityAndFinishCurrent((Activity) context, MainActivity.class);
                }else{
                    CommonUtils.startNewActivityAndFinishCurrent((Activity) context, SplashActivity.class);
                }

                isSuccess = true;
            }else if(purchase.getPurchaseState()==Purchase.PurchaseState.PENDING){
//                binding.Status.setText("Subscription pending");
                Log.d(TAG, "onPurchasesUpdated: PENDING");
                billingClient.endConnection();
            }else if(purchase.getPurchaseState()==Purchase.PurchaseState.UNSPECIFIED_STATE){
//                binding.Status.setText("Subscription UNSPECIFIED STATE");
                Log.d(TAG, "onPurchasesUpdated: UNSPECIFIED_STATE");

                billingClient.endConnection();
            }
        }
    }

    String currentPlanPrice = "";
    public boolean subscriptionTrigger(Activity activity, int index){
        Log.d(TAG, "subscribe: works1");
        productID=index;
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {


                List<QueryProductDetailsParams.Product> productList =List.of(
                        QueryProductDetailsParams.Product.newBuilder()
                                .setProductId("pawahara1")
                                .setProductType(BillingClient.ProductType.SUBS)
                                .build()
                );

                Log.d(TAG, "productList_: "+productList.get(0).zza());
                QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                        .setProductList(productList)
                        .build();
                Log.d(TAG, "subscribe: works2");
                billingClient.queryProductDetailsAsync(
                        params,
                        new ProductDetailsResponseListener() {
                            public void onProductDetailsResponse(BillingResult billingResult,
                                                                 List<ProductDetails> productDetailsList) {
                                for (ProductDetails productDetails : productDetailsList) {
//                                    Log.d(TAG, "subscribe: works3 "+productDetails.getProductId());
//                                    Log.d(TAG, "subscribe: works3 "+productDetails.getDescription());
//                                    Log.d(TAG, "subscribe: works3 "+productDetails.getTitle());
//                                    Log.d(TAG, "subscribe: works3 "+productDetails.getName());

                                    int planId = 0;

                                    boolean isFree = true;

                                    for (int i = 0; i < productDetails.getSubscriptionOfferDetails().size(); i++){

                                        int planIndex = getIndexOfPlanID(productDetails.getSubscriptionOfferDetails().get(i).getBasePlanId());

                                        Log.d(TAG, "subscribe: p-- planIndex : "+planIndex);
                                        Log.d(TAG, "subscribe: p-- index : "+index);

                                        if(planIndex == index){
                                            isFree = false;
                                            planId = i;
                                            Log.d(TAG, "subscribe: p-- worked: "+planId);
                                        }
                                    }

                                    try {
//                                        Log.d(TAG, "subscribe: hour1 "+productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getBillingPeriod());
//                                        Log.d(TAG, "subscribe: hour1 "+productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice());
//                                        Log.d(TAG, "subscribe: hour1 "+productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getRecurrenceMode());
//                                        Log.d(TAG, "subscribe: hour1 "+productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getPriceCurrencyCode());
//                                        Log.d(TAG, "subscribe: hour1 "+productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getBillingCycleCount());

                                    }catch (Exception e){

                                    }

                                    if(isFree){

                                        PreferenceUtils.saveState(context, productID);
                                        if (!(context instanceof MainActivity)) {
                                            CommonUtils.startNewActivityAndFinishCurrent((Activity) context, MainActivity.class);
                                        }else{
                                            CommonUtils.startNewActivityAndFinishCurrent((Activity) context, SplashActivity.class);
                                        }

                                    }else {

                                        String offerToken = productDetails.getSubscriptionOfferDetails().get(planId).getOfferToken();
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

                                        currentPlanPrice = productDetails.getSubscriptionOfferDetails().get(planId).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
                                        Log.d(TAG, "currentPlanPrice : "+currentPlanPrice);
                                        // Launch the billing flow
                                        billingClient.launchBillingFlow(activity, billingFlowParams);
                                    }


                                }
                            }
                        }
                );
            }
            @Override
            public void onBillingServiceDisconnected() {

            }
        });
        return true;
    }


    public interface PriceCallback {
        void onPriceReceived(HashMap<Integer, String> indexPriceMap);
    }


    HashMap<Integer, String> indexPriceMap = new HashMap<>();

    public void getSubPrice( PriceCallback callback){
        Log.d(TAG, "subscribe: works1");

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {


                List<QueryProductDetailsParams.Product> productList =List.of(
                        QueryProductDetailsParams.Product.newBuilder()
                                .setProductId("pawahara1")
                                .setProductType(BillingClient.ProductType.SUBS)
                                .build()
                );

                Log.d(TAG, "productList_: "+productList.get(0).zza());
                QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                        .setProductList(productList)
                        .build();
                Log.d(TAG, "subscribe: works2");
                billingClient.queryProductDetailsAsync(
                        params,
                        new ProductDetailsResponseListener() {
                            public void onProductDetailsResponse(BillingResult billingResult,
                                                                 List<ProductDetails> productDetailsList) {

                                Log.d(TAG, "onProductDetailsResponse: "+billingResult.getDebugMessage());
                                Log.d(TAG, "onProductDetailsResponse: "+billingResult.getResponseCode());

                                Log.d(TAG, "onProductDetailsResponse: "+productDetailsList.size());
                                Log.d(TAG, "onProductDetailsResponse: "+productDetailsList);

//                                // for test purpose
                                HashMap<Integer, String> indexPriceMap1 = new HashMap<>();
                                indexPriceMap1.put(3, "¥360");
                                indexPriceMap1.put(2, "¥250");
                                indexPriceMap1.put(1, "¥130");

                                callback.onPriceReceived(indexPriceMap1);
                                //end test
//
                                if(productDetailsList.size() == 0){

                                    callback.onPriceReceived(PreferenceUtils.loadPriceMap(context));

                                }else {

                                    for (ProductDetails productDetails : productDetailsList) {
                                        Log.d(TAG, "subscribe: works3 "+productDetails.getProductId());

                                        int planId = 0;

                                        for (int i = 0; i < productDetails.getSubscriptionOfferDetails().size(); i++){

                                            int planIndex = getIndexOfPlanID(productDetails.getSubscriptionOfferDetails().get(i).getBasePlanId());

                                            String price = productDetails.getSubscriptionOfferDetails().get(i).getPricingPhases()
                                                    .getPricingPhaseList().get(0).getFormattedPrice();

                                            indexPriceMap.put(planIndex,price);
                                            Log.d(TAG, "subscribe: p-- planIndex : "+planIndex);
                                            Log.d(TAG, "subscribe: p-- price : "+price);
                                            Log.d(TAG, "subscribe: p-- indexPriceMap : "+indexPriceMap);
                                        }

                                        for (int i = 1; i <=3; i++){
                                            Log.d(TAG, "indexPriceMap: "+  indexPriceMap.get(i));
                                        }

                                        String price = productDetails.getSubscriptionOfferDetails().
                                                get(planId).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();

                                        Log.d(TAG, "onProductDetailsResponse 2: "+billingResult.getResponseCode());
                                        Log.d(TAG, "onProductDetailsResponse 2: "+billingResult.getDebugMessage());

                                        callback.onPriceReceived(indexPriceMap);

                                    }

                                }
                            }
                        }
                );
            }
            @Override
            public void onBillingServiceDisconnected() {

            }
        });

    }


    public static int getIndexOfPlanID(String plan) {
        switch (plan.toLowerCase()) {
            case "1hourplan":
                return 1;
            case "2hourplan":
                return 2;
            case "3hour-plan":
                return 3;
            default:
                return 0;
        }
    }


    public void checkSubscription() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                    Log.d(TAG, "onBillingSetupFinished 1: "+billingResult.getResponseCode());
                    // Query for purchases and handle them
                    queryCheckPurchases();
                }else {

                    Log.d(TAG, "onBillingSetupFinished 2: "+billingResult.getResponseCode());

                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Handle billing service disconnection

                Log.d(TAG, "onBillingSetupFinished 3: ");
            }
        });
    }

    private void queryCheckPurchases() {

        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                new PurchasesResponseListener() {
                    public void onQueryPurchasesResponse(BillingResult billingResult, List purchases) {

                        try{

                            Log.d(TAG, "onQueryPurchasesResponse: "+purchases);
                            Log.d(TAG, "onQueryPurchasesResponse: "+purchases.size());
                            Log.d(TAG, "onQueryPurchasesResponse: "+purchases.get(0));
                            Log.d(TAG, "onQueryPurchasesResponse: "+purchases.get(0));


                            if (purchases.size() == 0) {

                                Log.d(TAG, "onQueryPurchasesResponse: purchases.size() Yes "+purchases.size());

                                productID = 0;
                                PreferenceUtils.saveState(context, productID);
                            } else {
                                Log.d(TAG, "onQueryPurchasesResponse: purchases.size() Not "+purchases.size());
                            }

                        }catch (Exception e){

                        }


                    }
                }
        );
    }


//    public void innerCheckSubscription() {
//        billingClient.queryPurchasesAsync(
//                QueryPurchasesParams.newBuilder()
//                        .setProductType(BillingClient.ProductType.SUBS)
//                        .build(),
//                new PurchasesResponseListener() {
//                    public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> purchases) {
//
//                        Log.d(TAG, "onQueryPurchasesResponse: "+purchases);
//                        Log.d(TAG, "onQueryPurchasesResponse: "+purchases.size());
////                        Log.d(TAG, "onQueryPurchasesResponse: "+purchases.get(0).getOriginalJson());
////                        Log.d(TAG, "onQueryPurchasesResponse: "+purchases.get(0));
//
//
//
//                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
//                            if (purchases.size() == 0) {
//
//                                Log.d(TAG, "onQueryPurchasesResponse: "+purchases.size());
//                                Log.d(TAG, "onQueryPurchasesResponse: "+purchases.get(0).getOriginalJson());
//                                Log.d(TAG, "onQueryPurchasesResponse: "+purchases.get(0));
//                                Log.d(TAG, "onQueryPurchasesResponse: "+purchases);
//
////                                productID = 0;
////                                PreferenceUtils.saveState(context, productID);
//                            } else {
//                                // Handle the case when there are purchases
//                                // You might want to iterate through the purchases and perform additional actions
//                            }
//                        }
//                    }
//                }
//        );
//    }



    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                isSuccess = true;
                Connection.premium = true;
                Connection.locked = false;
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

}

