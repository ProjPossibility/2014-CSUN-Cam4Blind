package com.ss12.camacc.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.speech.tts.TextToSpeech;
import android.view.accessibility.AccessibilityEvent;
import android.util.*;

import java.util.HashMap;

/**
 * Created by Noah on 3/10/14.
 */
public class Accessibility_Service extends AccessibilityService implements TextToSpeech.OnInitListener {

    public static final String TAG = "volumeMaster";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.v(TAG,"on Accessibility Event");

    }
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

    }
    @Override
    public void onInit(int arg0) {
        // TODO Auto-generated method stub

    }
    @Override
    public void onInterrupt() {
        Log.v(TAG, "***** onInterrupt");

    }
}