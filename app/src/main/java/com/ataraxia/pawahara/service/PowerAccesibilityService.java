package com.ataraxia.pawahara.service;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class PowerAccesibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            // Check if the event is related to power accessibility

            String eventText = event.getText().toString();
            if (eventText.contains("power accessibility")) {
                // Power accessibility event detected
                // Perform actions based on the power accessibility event
            }
        }
    }

    @Override
    public void onInterrupt() {
        // Handle interruptions (e.g., when the service is stopped)
    }
}
