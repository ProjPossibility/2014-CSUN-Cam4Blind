package com.blog.ljtatum.camacc;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

import com.example.navigationdrawer.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class CopyOfMainActivity extends Activity implements SurfaceHolder.Callback {
	public static String TAG = CopyOfMainActivity.class.getSimpleName();
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mOptionElements;
    private int VOICE_RECOGNITION_REQUEST_CODE= 1234;
	Camera camera;
	static SurfaceView surfaceView;
	static SurfaceHolder surfaceHolder;
	boolean previewing = false;
	static LayoutInflater controlInflater = null;
	
	Button buttonTakePicture;
	final int RESULT_SAVEIMAGE = 0;
	
	LayoutInflater inflater;
	View viewDefault = null;
	ViewGroup CONTAINERS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "Main layout loaded successfully");

        mTitle = mDrawerTitle = getTitle();
        mOptionElements = getResources().getStringArray(R.array.option_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mOptionElements));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);


        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }




    }

//    public void startVoiceRecognitionActivity() {
//        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
//                "Speech recognition demo");
//        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
//    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it
            // could have heard
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//	        mList.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, matches));

            // if (matches.contains("keyword here") { startActivity(new
            // Intent("name.of.manifest.ACTIVITY")

            int listSize = matches.size();
            Log.d(TAG, "listSize(" + listSize + ")");
            for (int i=0; i<listSize; i++) {
                Log.i(TAG, "recognized word: " + matches.get(i));

            }

            if (matches.contains("take picture")|| matches.contains("shoot")||matches.contains("snap")||matches.contains("shutter")) {
                Toast.makeText(this, "success!!!  :)",
                        Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(this, "failed!!!  :(",
                        Toast.LENGTH_LONG).show();

            }

            super.onActivityResult(requestCode, resultCode, data);
        }
//        else{
//
//        }

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
		
	PictureCallback myPictureCallback_JPG = new PictureCallback(){

		@Override
		public void onPictureTaken(byte[] arg0, Camera arg1) {
			// TODO Auto-generated method stub
			Uri uriTarget = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, 
					new ContentValues());

			OutputStream imageFileOS;
			try {
				imageFileOS = getContentResolver().openOutputStream(uriTarget);
				imageFileOS.write(arg0);
				imageFileOS.flush();
				imageFileOS.close();
				
				Toast.makeText(CopyOfMainActivity.this, 
						"Image saved: " + uriTarget.toString(), 
							Toast.LENGTH_SHORT).show();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			camera.startPreview();
		}};

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		if(previewing){
			camera.stopPreview();
			previewing = false;
		}
		
		if (camera != null){
			try {
				/* Must pass a fully initialized SurfaceHolder to
				 * setPreviewDisplay(SurfaceHolder). Without a surface,
				 * the camera will be unable to start the preview */
				camera.setPreviewDisplay(surfaceHolder);
				
				/* Call startPreview() to start updating the preview
				 * surface. Preview must be started before you can take
				 * a picture */
				camera.startPreview();
				previewing = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub

        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(surfaceView.getWidth(), surfaceView.getHeight());
        camera.setParameters(parameters);
//        parameters.setRotation();
        camera = Camera.open();

    }

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		camera.stopPreview();
		
		/* Call release() to release the camera for use by other applications.
		 * Applications should release the camera immediately in onPause()
		 * and re-open() it in onResume() */
		camera.release();
		camera = null;
		previewing = false;
	}
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
        case R.id.action_websearch:
            // create intent to perform web search for this planet
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
            // catch event that there's no activity to handle intent
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }



    private void selectItem(int position) {

        if (position==1)
        {
            Intent intent = new Intent(this,Voice_Engine.class);
            startActivityForResult(intent, 1234);
        }
        // update the main content by replacing fragments
        CameraFragment cameraFragment = new CameraFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, cameraFragment).commit();
        
        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mOptionElements[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


}
