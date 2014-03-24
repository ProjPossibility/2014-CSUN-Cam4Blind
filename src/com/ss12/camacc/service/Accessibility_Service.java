package com.ss12.camacc.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityEvent;
import android.util.*;

/**
 * Connects to the AccessibilityService that is built into the phone and makes use of it.
 * If the service is off, the application uses it's built in textToSpeech. If the service
 * is on, the application uses the AccessibilityService and turns off textToSpeech.
 */
public class Accessibility_Service extends AccessibilityService {

    public static final String TAG = "volumeMaster";
    /**
     * Callback for AccessibilityEvents.
     *
     * @param event An event
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.v(TAG,"on Accessibility Event");

    }//end onAccessibilityEvent

    /**
     * Passes information to AccessibilityServiceInfo.
     */
    @Override
    public void onServiceConnected()
    {
        Log.v(TAG,"on Service Connected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.packageNames = new String[] {"com.ss12.camacc"};
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED | AccessibilityEvent.TYPE_VIEW_CLICKED |
                AccessibilityEvent.TYPE_VIEW_FOCUSED | AccessibilityEvent.TYPE_VIEW_HOVER_ENTER;
        info.notificationTimeout = 100;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        setServiceInfo(info);

    }//end onServiceConnected

    /**
     * Called on an interrupt.
     */
    @Override
    public void onInterrupt() {
        Log.v(TAG, "***** onInterrupt");

    }//end onInterrupt
}//end Accessibility_Service class