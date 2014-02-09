package com.blog.ljtatum.camacc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import com.example.navigationdrawer.R;


import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Face;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class CameraFragment extends Fragment implements SurfaceHolder.Callback,
	OnInitListener, OnUtteranceCompletedListener
{
	public static String TAG = CameraFragment.class.getSimpleName();

	Camera camera;
	private SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	boolean previewing = false;
	boolean deviceSpeak = false;
	private Size size;
	
	TextToSpeech textToSpeech;
	private int checkData = 0;
	String metaString;

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) 
	{
		Log.d(TAG, "getView()");
		View rootView =  inflater.inflate(R.layout.frag_container_one, container, false);
		surfaceView = (SurfaceView) rootView.findViewById(R.id.camera_preview);
			
		Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, checkData);
        
       
        
        surfaceView.setOnClickListener(new SurfaceView.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				camera.takePicture(myShutterCallback, 
						myPictureCallback_RAW, myPictureCallback_JPG);

			}});
        
        
		configureSurface();
		return rootView;
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
			initialTTS();
			//camera.startPreview();
		}};		

	private void initialTTS() {
		deviceSpeak = true;
		surfaceView.setEnabled(false); //disable taking pics without command
		metaString = "Photo has been taken. " +
				"Please say options for more options or " +
				"take picture to take another picture";
		speakText(metaString);	
		
		//confirm voice recognition installed
//		PackageManager pm = getActivity().getPackageManager();
//		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
//				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
//
//		if (activities.size() == 0) {
//			Toast.makeText(getActivity(), "No Voice Recognizer Software Installed", 
//					Toast.LENGTH_LONG).show();
//		} else {
//			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//		    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//		        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//		    intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
//		        "Please say a command");
//		    startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
//		}
			
		//camera.startPreview();
	}
	
	@Override
	public void onUtteranceCompleted(String utteranceId) {
		// TODO Auto-generated method stub
		if (utteranceId.equalsIgnoreCase("done")) {
			Log.i(TAG, "onUtteranceCompleted");
		}
	}
	
	private void speakText(String text) {
		textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == checkData) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				textToSpeech = new TextToSpeech(getActivity(), this);
			} else {
				Intent installTTSFiles = new Intent(); //missing data, install it
				installTTSFiles.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installTTSFiles);
			}
		}
		
		super.onActivityResult(requestCode, requestCode, data);
		if(requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
		
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
	public void onInit(int status) {
		// TODO Auto-generated method stub
		if(status == TextToSpeech.SUCCESS)
		{
			textToSpeech.setLanguage(Locale.US);
		}
		else if(status == TextToSpeech.ERROR)
		{
			Log.e(TAG, "TTS INIT: ERROR");
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
