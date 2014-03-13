package com.ss12.camacc.activity;

import android.app.Activity;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;

/**
 * Created by Kristoffer on 3/12/14.
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

        sensorListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(final int orientation) {
                CameraOrientation.this.runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                //orientXValue.setText("Orientation: " + orientation);
                                //Log.e("the x value is :", ""+orientXValue.getX()); //Log phone orientation

                                //True when at certain orientations
                                if((orientation > 355) || (orientation < 5) || ((orientation > 85)
                                        && (orientation < 95)) || ((orientation > 175) && (orientation < 185))
                                        ||((orientation > 265) &&(orientation < 285))) {

                                    balanced = true;
                                }
                                else {
                                    balanced = false;
                                }

                                if (balanced){
                                    Log.e("yay", "it works!!!"); //Log test
                                }
                            }
                        });
            }
        };
        sensorListener.enable(); //Enables Listener
    }//end onCreate

    public boolean getBalanced() {
        return balanced;
    }
}//end CameraOrientation class
