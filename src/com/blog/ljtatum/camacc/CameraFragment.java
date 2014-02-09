package com.blog.ljtatum.camacc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import com.example.navigationdrawer.R;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
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
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class CameraFragment extends Fragment implements SurfaceHolder.Callback, OnInitListener
{
    public static String TAG = CameraFragment.class.getSimpleName();


    Camera camera;
    private SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;
    boolean deviceSpeak = false;
    private Size size;
    private String stringId;
    TextToSpeech textToSpeech;
    private int checkData = 0;
    String metaString;

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    //private Progress_Listener pL;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Log.d(TAG, "getView()");
        View rootView =  inflater.inflate(R.layout.frag_container_one, container, false);
        surfaceView = (SurfaceView) rootView.findViewById(R.id.camera_preview);
        surfaceView.setOnClickListener(new SurfaceView.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                camera.takePicture(myShutterCallback,
                        myPictureCallback_RAW, myPictureCallback_JPG);

            }});


        configureSurface();

        textToSpeech = new TextToSpeech(getActivity(), this);
        return rootView;
    }


    HashMap<String, String> map = new HashMap<String, String>();

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
                    Log.i("onDone", "1234");
                    Intent intent = new Intent(getActivity(),Voice_Engine.class);
//                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
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
            Uri uriTarget = getActivity().getContentResolver().
                    insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
            Log.i(TAG, "uriTarget " + uriTarget.toString());
            OutputStream imageFileOS;
            try {
                imageFileOS = getActivity().getContentResolver().openOutputStream(uriTarget);
                imageFileOS.write(arg0);
                imageFileOS.flush();
                imageFileOS.close();

                Toast.makeText(getActivity(),
                        "Image saved: " + uriTarget.toString(),
                        Toast.LENGTH_SHORT).show();

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            ttsPath(1);
            //camera.startPreview();
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
                            "Would you like to filter, Say Filter.  " +
                            "Would you like to Share, Say Share." +
                            "Or take another picture, Say Picture.";
                    speakText(metaString);

                    break;
            }
            case 2:

        }
    }


    private void speakText(String text) {

        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == checkData) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
            }
            else{
                Intent installTTSFiles = new Intent(); //missing data, install it
                installTTSFiles.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSFiles);
            }

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
                    ttsPath(1);
                }
                else if (matches.contains("Start Face Recognition")||matches.contains("Start")
                    ||matches.contains("Face")||matches.contains("Recognition")||matches.contains("face recognition"))
                {
                    ttsPath(2);


                }
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
                camera.setFaceDetectionListener(new Speach());
                camera.startFaceDetection();
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

    class Speach implements Camera.FaceDetectionListener {

        @Override
        public void onFaceDetection(Face[] faces, Camera camera) {
            // TODO Auto-generated method stub
            Log.e(TAG, "face: " + faces.length + " deviceSpeak " + deviceSpeak);

            if (deviceSpeak != true) {
                if (faces.length == 0) {
                    Log.e(TAG, "faces detected: " + faces.length
                            + " Max Num Detected Faces: ");
                } else {
                    Log.e(TAG, "faces detected: " + faces.length
                            + " Max Num Detected Faces: "
                            + camera.getParameters().getMaxNumDetectedFaces());

                }
            }



        }

    }

    @Override
    public void onPause() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onPause();
    }
}
