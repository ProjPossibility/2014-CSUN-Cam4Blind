package com.ss12.camacc.activity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.graphics.Rect;
import com.example.navigationdrawer.R;
import com.ss12.camacc.service.Voice_Engine;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Face;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

public class CameraActivity extends Activity implements SurfaceHolder.Callback, OnInitListener
{
    public static String TAG = CameraActivity.class.getSimpleName();

    private boolean wrapper = false;
    boolean previewing = false;
    boolean deviceSpeak = false;
    
    Camera camera;
    private Size size;
    private SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    private String stringId;
    String metaString;
    TextToSpeech textToSpeech;
    
    private int checkData = 0;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    
    HashMap<String, String> map = new HashMap<String, String>();
    //private Progress_Listener pL;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "Main layout loaded successfully");
        
        surfaceView = (SurfaceView) findViewById(R.id.camera_preview);
        surfaceView.setOnClickListener(new SurfaceView.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                camera.takePicture(myShutterCallback,
                        myPictureCallback_RAW, myPictureCallback_JPG);

            }});


        configureSurface();

        textToSpeech = new TextToSpeech(CameraActivity.this, this);  

    } //end onCreate

    @Override
    public void onInit(int status)
    {
        if (status == TextToSpeech.SUCCESS)
        {
            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {

                @Override
                public void onStart(String utteranceId) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onError(String utteranceId) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onDone(String utteranceId) {
//                    PackageManager pm = getActivity().getPackageManager();
//                    List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(getActivity(),
//                            Voice_Engine.class), 0);
                    Log.i("onDone", "1"+wrapper);

                    if (wrapper)
                    {
                        Log.i("onDone", "1"+wrapper);

                        Intent intent = new Intent(CameraActivity.this,Voice_Engine.class);
    //                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
    //                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
                        wrapper = false;
                    }
//                    if (activities.size() == 0)
//                    {
//                        Toast.makeText(getActivity(), "No Voice Recognizer Software Installed",
//                                Toast.LENGTH_LONG).show();
//                    } else
//                    {
//                    }
                }
            });

            textToSpeech.setLanguage(Locale.US);
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
            wrapper = true;
            ttsPath(0);
        }
    }



    private void configureSurface()
    {
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        /* Although deprecated SURFACE_TYPE_PUSH_BUFFERS generates several buffers
         * for the SurfaceView. Components are locking (fill with data) and pushing
         * (display data) these buffers deep in the OS code. To be specific, the
         * camera hardware can fill a push buffer directly and die graphics hardware
         * can display a push buffer directly (they share buffers): Deprecated in 3.0 */
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            // TODO Auto-generated method stub
            surfaceView.setEnabled(true);
        }};

    ShutterCallback myShutterCallback = new ShutterCallback(){

        @Override
        public void onShutter() {
            // TODO Auto-generated method stub

        }};

    PictureCallback myPictureCallback_RAW = new PictureCallback(){

        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            // TODO Auto-generated method stub

        }};

    PictureCallback myPictureCallback_JPG = new PictureCallback(){

        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            // TODO Auto-generated method stub

            Log.i(TAG, "TAKING PICTURE");
            Uri uriTarget = CameraActivity.this.getContentResolver().
                    insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
            Log.i(TAG, "uriTarget " + uriTarget.toString());
            OutputStream imageFileOS;
            try {
                imageFileOS = CameraActivity.this.getContentResolver().openOutputStream(uriTarget);
                imageFileOS.write(arg0);
                imageFileOS.flush();
                imageFileOS.close();

                Toast.makeText(CameraActivity.this,
                        "Image saved: " + uriTarget.toString(),
                        Toast.LENGTH_SHORT).show();

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            while(textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
            wrapper= true;

            ttsPath(1);
            camera.stopFaceDetection();
        }};

    private void ttsPath(int id)
    {

        switch (id)
        {
            case 0:{
                    metaString = "Welcome to CamAcc. Say Take Picture or Face Recognition";
                    speakText(metaString);
                    break;
            }
            case 1:{
                    deviceSpeak = true;
                    surfaceView.setEnabled(false); //disable taking pics without command
                    metaString = "Your photo has been Saved. " +
                            //"Would you like to filter, Say Filter.  " +
                            //"Would you like to Share, Say Share." +
                            "If you like to take another picture, Say Picture or" +
                            "say face detection to start face detection";
                    speakText(metaString);

                    break;
            }
            case 2:

        }
    }


    private void speakText(
                           String text) {
        if(textToSpeech.isSpeaking()) {
            return;
        }
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, map);

    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String reqCode= Integer.toString(requestCode);
        Log.i("onDone","2");
//        if (requestCode == checkData) {
//            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
//                // success, create the TTS instance
//            }
//            else{
//                Intent installTTSFiles = new Intent(); //missing data, install it
//                installTTSFiles.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
//                startActivity(installTTSFiles);
//            }

            if (requestCode == VOICE_RECOGNITION_REQUEST_CODE  )
            {
                ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                int listSize = matches.size();

                for (int i=0; i<listSize; i++) {
                    Log.i(TAG, "recognized word: " + matches.get(i));

                }
                if (matches.contains("take picture")|| matches.contains("picture")||
                        matches.contains("take")||matches.contains("camera") ){

                    camera.takePicture(myShutterCallback,
                            myPictureCallback_RAW, myPictureCallback_JPG);
                    camera.startPreview();
                    wrapper = false;
                }
                else if (matches.contains("Start Face Recognition")||matches.contains("Start")
                    ||matches.contains("Face")||matches.contains("Recognition")||matches.contains("face recognition")
                        ||matches.contains("Detection"))
                {
//                    ttsPath(2);
                    Log.i("FACE", "FACE");

                    camera.startFaceDetection();

                }
            }
        }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height)
    {
        // TODO Auto-generated method stub
        if(previewing)
        {
            camera.stopPreview();
            previewing = false;
        }

        if (camera != null)
        {
            try
            {
				/* Must pass a fully initialized SurfaceHolder to
				 * setPreviewDisplay(SurfaceHolder). Without a surface,
				 * the camera will be unable to start the preview */
                camera.setPreviewDisplay(surfaceHolder);
                camera.setDisplayOrientation(90);
				/* Call startPreview() to start updating the preview
				 * surface. Preview must be started before you can take
				 * a picture */
                camera.startPreview();
                camera.setFaceDetectionListener(new Speech());

                previewing = true;
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        camera = Camera.open();

        Camera.Parameters parameter = camera.getParameters();
        Camera.Size size = getBestPreviewSize(surfaceView.getWidth(), surfaceView.getHeight(), parameter);
        parameter.setPreviewSize(size.width, size.height);
        surfaceView.getTop();
        Log.e(TAG, "surfaceView.getTop() " + surfaceView.getTop()
                + " surfaceView.getBottom() "  + surfaceView.getBottom()
                + " surfaceView.getLeft " + surfaceView.getLeft()
                + " surfaceView.getRight "  + surfaceView.getRight());
    }

    private Camera.Size getBestPreviewSize(int width, int height,
                                           Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }

        this.size = result;
        return (result);
    }



    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        camera.stopPreview();

		/* Call release() to release the camera for use by other applications.
		 * Applications should release the camera immediately in onPause()
		 * and re-open() it in onResume() */
        camera.release();
        camera = null;
        previewing = false;
    }

    class Speech implements Camera.FaceDetectionListener {

        @Override
        public void onFaceDetection(Face[] faces, Camera camera) {

            Log.e(TAG, "faces detected: " + faces.length);

            //no faces detected
            boolean twoReady= false;
            if (faces.length == 0)
            {

                Log.e(TAG, "faces detected: " + faces.length
                        + " Max Num Detected Faces: "
                        + camera.getParameters().getMaxNumDetectedFaces());
            }
            else if(faces.length==1)
            {
                int vWidth = surfaceView.getWidth();
                int vHeight = surfaceView.getHeight();
                int l = faces[0].rect.left;
                int t = faces[0].rect.top;
                int r = faces[0].rect.right;
                int b = faces[0].rect.bottom;
                int left = (l+1000) * vWidth/2000;
                int top  = (t+1000) * vHeight/2000;
                int right = (r+1000) * vWidth/2000;
                int bottom = (b+1000) * vHeight/2000;

                Rect realFaceRect = new Rect(left, top, right, bottom);

                int halfWidth = surfaceView.getRight() / 2;
                int halfHeight = surfaceView.getBottom() / 2;
                Rect middle= new Rect(halfWidth - 50, halfHeight - 50, halfWidth + 50, halfHeight + 50);
                if(middle.intersect(realFaceRect))
                {
                    speakText("one face Centered detected");
                    Log.e(TAG, "people detected:1");
                }

                Log.e(TAG, "people detected:1"+realFaceRect.toString());
                speakText("face detected not centered ");
            }//else if
            else if(faces.length==2)
            {
                //forms a square that covers the right part of the screen
                Rect side = new Rect(-1000,-1000,100,200);
//                if((side.contains(faces[1].rect))&&(side.contains(faces[0].rect)==false))
//                {
                    //outputs if the person on the right is ready
                    // if this is false then the two people are on the left
                speakText("two people detected");
                Log.e(TAG, "people detected:2"+faces[0].rect.toString());

//                }
            }
            else if(faces.length>=3) {
                speakText("Multiple people in picture");
            }
        }

    }


//    @Override
//    public void onPause() {
//        if (textToSpeech != null) {
//            textToSpeech.stop();
//            textToSpeech.shutdown();
//        }
//        super.onPause();
//    }
    @Override
    public void onDestroy(){
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}