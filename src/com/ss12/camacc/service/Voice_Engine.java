package com.ss12.camacc.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Window;

import java.util.ArrayList;

import com.ss12.camacc.R;
import com.ss12.camacc.activity.CameraActivity;
import com.ss12.camacc.helper.VoiceEngineHelper;

/**
 * Created by bookieztopp on 2/8/14.
 */
public class Voice_Engine extends Activity {
    private static final String TAG = "Voice_Engine_Class";
    
    public static Voice_Engine singletonVE = null;
    
    private SpeechRecognizer sr;
    
    /**
     *
     *
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        //this allows this class to be closed from another Activity
        singletonVE = this;

        //only allow voice recognition if voiceController is OFF
        if (VoiceEngineHelper.getVoiceController() == false) {
        	setContentView(R.layout.wait_for_speech);
            sr = SpeechRecognizer.createSpeechRecognizer(this);
            sr.setRecognitionListener(new listener());
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplication().getClass().getName());
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
            sr.startListening(intent);
        } else {
        	sr.stopListening();
            sr.destroy();
            finish();
        }
        
    
    } //end onCreate

    /**
     *
     */
    class listener implements RecognitionListener
    {
        /**
         *
         * @param params
         */
        public void onReadyForSpeech(Bundle params)
        {
            Log.d(TAG, "onReadyForSpeech");
        }

        /**
         *
         */
        public void onBeginningOfSpeech()
        {
            Log.d(TAG, "onBeginningOfSpeech");
        }

        /**
         *
         * @param rmsdB
         */
        public void onRmsChanged(float rmsdB)
        {
        }

        /**
         *
         * @param buffer
         */
        public void onBufferReceived(byte[] buffer)
        {
            Log.d(TAG, "onBufferReceived");
        }

        /**
         *
         */
        public void onEndOfSpeech()
        {
            Log.d(TAG, "onEndofSpeech");
        }

        /**
         *
         * @param error
         */
        public void onError(int error)
        {
        	Log.e(TAG, "onError " + error);
        	/* Legend: Error Codes
        	 * @1 network operation timed out
        	 * @2 other network related errors
        	 * @3 audio recoding error
        	 * @4 server sends error status
        	 * @5 other client side errors
        	 * @6 no speech input
        	 * @7 no recognition result matched
        	 * @8 RecognitionService busy
        	 * @9 insufficient permissions
        	 */
        	
        	//only continue listening if voiceController is not active
        	if (VoiceEngineHelper.getVoiceController() == false) {	
                sr.cancel();
                Intent intent = new Intent();
                sr.startListening(intent);
        	} else {
        		sr.stopListening();
                sr.destroy();
                finish();
        	}
            
        }

        /**
         *
         * @param results
         */
        public void onResults(Bundle results)
        {
            Log.e(TAG, "onResults " + results);
            ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            Intent i = new Intent();
            i.putStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS, data);
            setResult(RESULT_OK , i);
            finish(); 
        }

        /**
         *
         * @param partialResults
         */
        public void onPartialResults(Bundle partialResults)
        {
            Log.d(TAG, "onPartialResults");
        }

        /**
         *
         * @param eventType
         * @param params
         */
        public void onEvent(int eventType, Bundle params)
        {
            Log.d(TAG, "onEvent " + eventType);
        }
        
    } //end listener class

    /**
     *
     */
    @Override
    protected void onDestroy() {
    	Log.d(TAG, "onDestroy()");
        sr.stopListening();
        sr.destroy();
        super.onDestroy();
    }
}