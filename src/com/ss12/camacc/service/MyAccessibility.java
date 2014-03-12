//package com.ss12.camacc.service;
//
///**
// * Created by Noah on 3/10/14.
// */
//import android.accessibilityservice.AccessibilityService;
//import android.accessibilityservice.AccessibilityServiceInfo;
//import android.speech.tts.TextToSpeech;
//import android.view.accessibility.AccessibilityEvent;
//
//import java.util.HashMap;
//import java.util.Queue;
//
//public class MyAccessibility extends AccessibilityService implements TextToSpeech.OnInitListener {
//    @Override
//    public void onServiceConnected() {
//
//        class Info{
//            int eventTypes, feedbackType, notificationTimeout;
//            String packageNames;
//
//            public Info(){
//            }
//
//            public Info(int eT,int fT,int nT, String pN){
//                this.eventTypes = eT;
//                this.feedbackType = fT;
//                this.notificationTimeout = nT;
//                this.packageNames = pN;
//            }
//
//        }
//        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
//        // Set the type of events that this service wants to listen to.  Others
//        // won't be passed to this service.
//        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED |
//                AccessibilityEvent.TYPE_VIEW_FOCUSED;
//
//        // If you only want this service to work with specific applications, set their
//        // package names here.  Otherwise, when the service is activated, it will listen
//        // to events from all applications.
//        info.packageNames = new String[]
//                {"com.ss12.camacc"};
//
////        // Set the type of feedback your service will provide.
////        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
////
////        // Default services are invoked only if no package-specific ones are present
////        // for the type of AccessibilityEvent generated.  This service *is*
////        // application-specific, so the flag isn't necessary.  If this was a
////        // general-purpose service, it would be worth considering setting the
////        // DEFAULT flag.
////
////        // info.flags = AccessibilityServiceInfo.DEFAULT;
//
//        info.notificationTimeout = 100;
//
//        this.setServiceInfo(info);
//
//    }
//
//    @Override
//    public void onAccessibilityEvent(AccessibilityEvent event) {
//
////        final int eventType = event.getEventType();
////        String eventText = null;
////        switch(eventType) {
////            case AccessibilityEvent.TYPE_VIEW_CLICKED:
////                eventText = "Focused: ";
////                break;
////            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
////                eventText = "Focused: ";
////                break;
////        }
////
////        eventText = eventText + event.getContentDescription();
////
////        // Do something nifty with this text, like speak the composed string
////        // back to the user.
////        TextToSpeech textToSpeech = new TextToSpeech(getApplicationContext(),this);
////        HashMap<String,String> map = new HashMap<String, String>();
////        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
////
////        textToSpeech.speak(eventText, TextToSpeech.QUEUE_FLUSH, map );
//
//
//    }
//
//    @Override
//    public void onInit(int arg0) {
//        // TODO Auto-generated method stub
//
//    }
//
//
//    @Override
//    public void onInterrupt() {
//
//    }
//}
//
//
