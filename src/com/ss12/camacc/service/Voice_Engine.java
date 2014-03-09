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
 * Voice_Engine is the functionality for processing voice commands.
 * Includes features to manage the full/partial recognition, error
 * handling, and sending results back to the call location.
 *
 * @author Leonard Tatum
 * @author Noah Anderson
 * @author Stefan Eng
 * @author Javier Pimentel
 * @author Kristoffer Larson
 */
public class Voice_Engine extends Activity {
    private static final String TAG = "Voice_Engine_Class";
    /**
     * Voice_Engine self reference
     */
    public static Voice_Engine singletonVE = null;
    /**
     * SpeechRecognizer object
     */
    private SpeechRecognizer sr;
    
    /**
     * Starts voice recognition if voiceController is off. Otherwise
     * stops voice recognition. Called by the system when the service
     * is first created. Do not call this method directly.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle
     *                           contains the data it most recently supplied in
     *                           onSaveInstanceState(Bundle). Otherwise it is null.
     */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        //This allows this class to be closed from another Activity
        singletonVE = this;

        //Only allow voice recognition if voiceController is OFF
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
     * Listens for voice commands from the user.
     * Used for receiving notifications from the SpeechRecognizer when the
     * recognition related events occur. All the callbacks are executed on
     * the Application main thread.
     */
    class listener implements RecognitionListener
    {
        /**
         * Called when the endpointer is ready for the user to start speaking.
         *
         * @param params Parameters set by the recognition service. Reserved
         *               for future use
         */
        public void onReadyForSpeech(Bundle params)
        {
            Log.d(TAG, "onReadyForSpeech");
        }//end onReadyForSpeech

        /**
         * The user has started to speak.
         */
        public void onBeginningOfSpeech()
        {
            Log.d(TAG, "onBeginningOfSpeech");
        }//end onBeginningOfSpeech

        /**
         * The sound level in the audio stream has changed.
         *
         * @param rmsdB The new RMS dB value
         */
        public void onRmsChanged(float rmsdB)
        {
        }//end onRmsChanged

        /**
         * More sound has been received.
         *
         * @param buffer A buffer containing a sequence of
         *               big-endian 16-bit integers representing
         *               a single channel audio stream.
         */
        public void onBufferReceived(byte[] buffer)
        {
            Log.d(TAG, "onBufferReceived");
        }//end onBufferReceived

        /**
         * Called after the user stops speaking.
         */
        public void onEndOfSpeech()
        {
            Log.d(TAG, "onEndofSpeech");
        }//end onEndOfSpeech

        /**
         * A network or recognition error occurred.
         *
         * @param error Code is defined in SpeechRecognizer
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
            
        }//end onError

        /**
         * Called when recognition results are ready.
         *
         * @param results The recognition results
         */
        public void onResults(Bundle results)
        {
            Log.e(TAG, "onResults " + results);
            ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            Intent i = new Intent();
            i.putStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS, data);
            setResult(RESULT_OK , i);
            finish(); 
        }//end onResults

        /**
         * Called when partial recognition results are available.
         *
         * @param partialResults The returned results
         */
        public void onPartialResults(Bundle partialResults)
        {
            Log.d(TAG, "onPartialResults");
        }//end onPartialResults

        /**
         * Reserved for adding future events.
         *
         * @param eventType The type of the occurred event
         * @param params    A Bundle containing the passed parameters
         */
        public void onEvent(int eventType, Bundle params)
        {
            Log.d(TAG, "onEvent " + eventType);
        }//end onEvent
        
    } //end listener class

    /**
     *Called by the system to remove the Service when it is no longer used.
     * Ends SpeechRecognizer, as well as calling Activity's
     * onDestroy(). The service should clean up any resources it holds (threads,
     * registered receivers, etc) at this point. Upon return, there will be no
     * more calls in to this Service object and it is effectively dead. Do not
     * call this method directly.
     */
    @Override
    protected void onDestroy() {
    	Log.d(TAG, "onDestroy()");
        sr.stopListening();
        sr.destroy();
        super.onDestroy();
    }//end onDestroy
}//end Voice_Engine class
