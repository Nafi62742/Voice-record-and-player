package com.ataraxia.pawahara.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class IncomingCallReceiver  extends BroadcastReceiver {

    // Callback interface
    public interface OnIncomingCallListener {
        void onIncomingCall(String incomingNumber);
    }

    private OnIncomingCallListener listener;

    // Setter method for the callback
    public void setOnIncomingCallListener(OnIncomingCallListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("phone action", "onReceive: "+action);
        Log.d("listemter", "onReceive: "+listener);

        if (action != null && action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {

            String phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            Log.d("phoen state", "onReceive: "+phoneState);
            if (phoneState != null && phoneState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {

                Log.d("listemter", "onReceive: "+listener);
                // Notify the callback with the incoming call details
                if (listener != null) {
                    listener.onIncomingCall(TelephonyManager.EXTRA_STATE_RINGING);
                }
            }else if(phoneState != null && phoneState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                if (listener != null) {
                    listener.onIncomingCall(TelephonyManager.EXTRA_STATE_OFFHOOK);
                }
            } else if (phoneState != null && phoneState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                Log.d("idle", "onReceive: ");
                if (listener != null) {
                    Log.d("idle 1" , "onReceive: ");
                    listener.onIncomingCall(TelephonyManager.EXTRA_STATE_IDLE);
                }
            }
        }
    }
}
