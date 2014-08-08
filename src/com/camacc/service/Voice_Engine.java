package com.camacc.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Window;

import java.util.ArrayList;

import com.camacc.R;
import com.camacc.helper.VoiceEngineHelper;


/**
 * Created by bookieztopp on 2/8/14.
 */
public class Voice_Engine extends Activity {
	private String TAG = Voice_Engine.class.getSimpleName();
    private boolean speakingDone = false;
    public static boolean speakingInterrupted = false;
    
    public static Voice_Engine singletonVE = new Voice_Engine();
    
    private SpeechRecognizer sr = SpeechRecognizer.createSpeechRecognizer(this);
    
    /**
     *The Voice engine call is always destroyed if it is interrupted and
     * keeps track of whether it was interrupted with the speakingInterrupted boolean
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
        Log.d(TAG, "VoiceEngineHelper.getVoiceController(): " +
        		VoiceEngineHelper.getVoiceController());
        
        setContentView(R.layout.wait_for_speech);

        if (VoiceEngineHelper.getVoiceController() == false) {
            speakingInterrupted = false;
            sr.setRecognitionListener(new listener());
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); //LANGUAGE_MODEL_WEB_SEARCH
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplication().getClass().getName());
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please say a command");
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 4000);
            sr.startListening(intent);
        } else {
            speakingInterrupted = false;
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
            speakingInterrupted = true;
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
            speakingInterrupted = false;
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
            speakingDone = true;
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


    @Override
    protected void onPause() {
        Log.d(TAG,"onPause()");
        if((speakingDone == false)){
            finish();
        }

        super.onPause();
    }

    /*

             */
    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");
        /**
         * When speaking is true finish()
         * has already been called
         */
        if(speakingDone == false){
            finish();
        }

        super.onStop();
    }

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