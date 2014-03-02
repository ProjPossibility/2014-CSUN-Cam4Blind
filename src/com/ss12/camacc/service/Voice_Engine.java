package com.ss12.camacc.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.RecognizerResultsIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Window;
import com.example.navigationdrawer.R;

import java.util.ArrayList;

/**
 * Created by bookieztopp on 2/8/14.
 */
public class Voice_Engine extends Activity {
    private SpeechRecognizer sr;
    private static final String TAG = "Voice_Engine_Class";

    /**
     *
     *
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.wait_for_speech);
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new listener());
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplication().getClass().getName());
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 100);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 100);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 100);



        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);

        sr.startListening(intent);


    }

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
            Log.d(TAG, "onRmsChanged");
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
            Log.d(TAG, "onError " + error);
            sr.cancel();
            Intent intent = new Intent();
            sr.startListening(intent);
        }

        /**
         *
         * @param results
         */
        public void onResults(Bundle results)
        {
            String str = new String();
            Log.d(TAG, "onResults " + results);
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
    }

    /**
     *
     */
    @Override
    protected void onDestroy() {
        sr.stopListening();
        sr.destroy();
        super.onDestroy();
    }
}