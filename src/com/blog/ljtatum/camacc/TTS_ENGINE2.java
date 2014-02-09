package com.blog.ljtatum.camacc;

import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
/**
 * Created by bookieztopp on 2/8/14.
 */
public class TTS_ENGINE2 extends Activity implements TextToSpeech.OnInitListener{




        private TextToSpeech tts;
        private Button btnSpeak;
        private EditText txtText;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);

            tts = new TextToSpeech(this, this);

            btnSpeak = (Button) findViewById(R.id.btnSpeak);

            txtText = (EditText) findViewById(R.id.txtText);

            // button on click event
            btnSpeak.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    speakOut();
                }

            });
        }

        @Override
        public void onDestroy() {
            // Don't forget to shutdown tts!
            if (tts != null) {
                tts.stop();
                tts.shutdown();
            }
            super.onDestroy();
        }

        @Override
        public void onInit(int status) {

            if (status == TextToSpeech.SUCCESS) {

                int result = tts.setLanguage(Locale.US);

                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "This Language is not supported");
                } else {
                    btnSpeak.setEnabled(true);
                    speakOut();
                }

            } else {
                Log.e("TTS", "Initilization Failed!");
            }

        }

        private void speakOut() {

            String text = txtText.getText().toString();

            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
