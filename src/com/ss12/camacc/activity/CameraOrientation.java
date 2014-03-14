package com.ss12.camacc.activity;

import android.app.Activity;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;

/**
 * Listens for orientation changes and sets a boolean to true if the phone
 * is within set levels.
 */
public class CameraOrientation extends Activity {

    /**
     * OrientationEventListener object
     */
    private OrientationEventListener sensorListener;
    /**
     * A boolean that is true if the phone orientation is leveled.
     */
    private boolean balanced = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * Listens for the orientation of the phone.
         */
        sensorListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(final int orientation) {
                CameraOrientation.this.runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                //True when at certain orientations
                                if((orientation > 355) || (orientation < 5) || ((orientation > 85)
                                        && (orientation < 95)) || ((orientation > 175) && (orientation < 185))
                                        ||((orientation > 265) &&(orientation < 285))) {

                                    balanced = true;
                                }
                                else {
                                    balanced = false;
                                }
                            }//end run
                        });//end runOnUiThread
            }//end onOrientationChanged
        };//end OrientationEventListener
        sensorListener.enable(); //Enables Listener
    }//end onCreate

    public boolean getBalanced() {
        return balanced;
    }//end getBalanced

}//end CameraOrientation class
