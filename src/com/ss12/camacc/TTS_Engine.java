package com.ss12.camacc;

import android.speech.tts.UtteranceProgressListener;
import android.app.Activity;
import android.speech.tts.TextToSpeech;
import java.util.HashMap;

public class TTS_Engine extends Activity implements TextToSpeech.OnInitListener {



    //    HashMap hashMap;
    TextToSpeech textToSpeech;

    public TTS_Engine ( Activity myActivity )
    {
        textToSpeech = new TextToSpeech(myActivity, this);
    }

    public void say(String message)
    {
        textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null );
    }
    @Override
    public void onInit(int i) {
        textToSpeech.setOnUtteranceProgressListener(new Progress_Listener(){

        });
    }
    private class Progress_Listener extends UtteranceProgressListener {


        @Override
        public void onStart(String s) {

        }

        @Override
        public void onDone(String s) {

        }

        @Override
        public void onError(String s) {

        }
    }
}