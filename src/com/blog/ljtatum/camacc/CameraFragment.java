package com.blog.ljtatum.camacc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import com.example.navigationdrawer.R;
import android.app.Fragment;
import android.content.ContentValues;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import java.util.ArrayList;
import android.content.Intent;
import android.speech.RecognizerIntent;


public class CameraFragment extends Fragment implements SurfaceHolder.Callback
{
	public static String TAG = CameraFragment.class.getSimpleName();
	
	Camera camera;
	private SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	boolean previewing = false;
	Button buttonTakePicture;
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) 
	{
		Log.d(TAG, "getView()");
		View rootView =  inflater.inflate(R.layout.frag_container_one, container, false);
		surfaceView = (SurfaceView) rootView.findViewById(R.id.camera_preview);
        buttonTakePicture = (Button)rootView.findViewById(R.id.btn_pic);
        buttonTakePicture.setOnClickListener(new Button.OnClickListener() 
        {

			@Override
			public void onClick(View arg0) 
			{
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
			buttonTakePicture.setEnabled(true);
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
		
	PictureCallback myPictureCallback_JPG = new PictureCallback()
	{

		@Override
		public void onPictureTaken(byte[] arg0, Camera arg1) 
		{
			// TODO Auto-generated method stub
			Uri uriTarget = getActivity().getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, 
					new ContentValues());

			OutputStream imageFileOS;
			try 
			{
				imageFileOS = getActivity().getContentResolver().openOutputStream(uriTarget);
				imageFileOS.write(arg0);
				imageFileOS.flush();
				imageFileOS.close();
				
				Toast.makeText(getActivity(), 
						"Image saved: " + uriTarget.toString(), 
							Toast.LENGTH_SHORT).show();
				
			} 
			catch (FileNotFoundException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			camera.startPreview();
		}};

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
				
				/* Call startPreview() to start updating the preview
				 * surface. Preview must be started before you can take
				 * a picture */
				camera.startPreview();
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

	
	
}
