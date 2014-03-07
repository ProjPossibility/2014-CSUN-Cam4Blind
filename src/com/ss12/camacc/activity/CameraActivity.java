package com.ss12.camacc.activity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.graphics.Rect;
import android.graphics.BitmapFactory.Options;

import com.ss12.camacc.R;
import com.ss12.camacc.helper.PreferencesHelper;
import com.ss12.camacc.helper.VoiceEngineHelper;
import com.ss12.camacc.network.NetworkUtils;
import com.ss12.camacc.service.Voice_Engine;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Face;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

/**
 *
 */
public class CameraActivity extends Activity implements SurfaceHolder.Callback, OnInitListener
{
    public static String TAG = CameraActivity.class.getSimpleName();
 
    private PreferencesHelper prefHelper;
    public Voice_Engine listener = new Voice_Engine();

    private boolean ttsWrapper = false; //@true: TTS Engine is active
    private boolean faceDetectionWrapper = false; //@true: Face Detection mode active
    private boolean isPreview = false;
    private boolean isConnected = false;
    private boolean isWarningSound;
    private boolean isOptionController = false;
    private boolean isFilterController = false;

	public static Uri lastPictureTakenUri;

	private static TextToSpeech textToSpeech;
    private Camera camera;
    private Size size;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private AlertDialog alertDialog;
    private Ringtone r;
    private Vibrator v;

    private String metaString;

    long timeInMilliseconds;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    HashMap<String, String> map = new HashMap<String, String>();
    private Handler mHandler = new Handler();
    
    //shared preference variables
    private boolean isFirstLaunch;
    private String sharedPrefFirstLaunch;
    
    private boolean isDescription;
    private String sharedPrefDescription;
    
    private boolean isAutoSocial;
    private String sharedPrefAutoSocial;
    
    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //only initialize app if there exists a solid network connection
        isNetWorkConnection();

    } //end onCreate
    
    
    /* setup the application to do initialization work inside of loadActivity().
     * This allows us to 'reload' (restart) this Activity from the beginning
     * during phases of lost network connection(s) and reestablished 
     * netowrk connection(s) */
    private void loadActivity() {
        
        if (isConnected == true) { 
            setContentView(R.layout.activity_main);
            Log.d(TAG, "Main layout loaded successfully");
            surfaceView = (SurfaceView) findViewById(R.id.camera_preview);
            surfaceView.setEnabled(true);
            surfaceView.setOnClickListener(new SurfaceView.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                	Log.i(TAG, "surface was clicked, taking picture via surfaceView button");
                	
                	//reset VoiceRecognition controllerOFF
                	VoiceEngineHelper.setVoiceController(true);
                	Voice_Engine.singletonVE.finish();
                	
                	//take picture and store image
                    camera.takePicture(myShutterCallback,
                            myPictureCallback_RAW, myPictureCallback_JPG);
                }});

            configureSurface();

            //check options stored in sharedPreferences
    		prefHelper = new PreferencesHelper(getApplicationContext());
    		isFirstLaunch = prefHelper.getPrefFirstLaunch(sharedPrefFirstLaunch);
    		isDescription = prefHelper.getPrefFirstLaunch(sharedPrefDescription);
    		isAutoSocial = prefHelper.getPrefFirstLaunch(sharedPrefAutoSocial);
            
    		//initialize TTS Engine (starts the beginning commands of the application)
    		textToSpeech = new TextToSpeech(CameraActivity.this, this);  

    		/* This runnable is used to allow TTS Engine to fully initialize. 
    		 * It takes time for TTS Engine to initialize, and the first initilization
    		 * is important for the entire application to work. TTS Engine is initialized 
    		 * in onStart() as a backup just in case there is failure here as well */
    		mHandler.postDelayed(new Runnable() {
				public void run() {
					if (isFirstLaunch == false) {
		    			ttsPath(0);
		    		} else if (isFirstLaunch == true && isDescription == false) {
		    			ttsPath(99);
		    		} else if (isFirstLaunch == true && isDescription == true) {
		    			ttsPath(4);
		    		}
				}
			}, 1000);

        } else {
        	//confirm network connection
        	isNetWorkConnection();
        }
        
    } //end loadActivity() method
    

    /**
     * Every time TTS Engine is initialized, onInit procs. We parsed
     * out the work to another method called ttsEngine() so that if
     * initialization fails, or at any other point in the application 
     * we would like to 
     * @param status
     */
    @Override
    public void onInit(int status) {
    	Log.e(TAG, "ONINIT(): Text-to-speech initialized!");

        if (status == TextToSpeech.SUCCESS) {
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
                	/* before we use Voice_Engine class we always check for 
					 * active network connection */
                	if (isConnected == true) {
                		//do not allow VoiceRecognition if face detection is active
                		if (faceDetectionWrapper == false) {
                			Log.i(TAG, "Activate voice engine");
                			VoiceEngineHelper.setVoiceController(false);
                    		Intent intent = new Intent(CameraActivity.this, Voice_Engine.class);
                			startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
                		}
                	} else {
                		//confirm network connection
                    	isNetWorkConnection();
                	}
                }
            }); //end OnUtteranceProgressListener()

            textToSpeech.setLanguage(Locale.US);
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");

            /* booleans assigned from sharedPreferences are used to setup different 
             * messages depending upon the options the user has saved/setup */
    		if (isFirstLaunch == false) {
    			ttsPath(0);
    		} else if (isFirstLaunch == true) {
    			ttsPath(99);
    		}

        } else if (status == TextToSpeech.ERROR){
        	//initialization of TTS failed so reinitialize new TTS Engine instance
        	Log.e(TAG, "TextToSpeech ERROR");
        	textToSpeech = new TextToSpeech(CameraActivity.this, this);
        	if (isFirstLaunch == false) {
    			ttsPath(0);
    		} else if (isFirstLaunch == true) {
    			ttsPath(99);
    		}
        }
    } //end onInit() method

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

            //interrupt TTS Engine if active
            while(textToSpeech.isSpeaking()) {
            	//make sure ttsWrapper is reset to default value
            	ttsWrapper = false;
            	textToSpeech.stop();     
            }
            
            //taking a picture, make sure surface view is unclickable
            surfaceView.setEnabled(false);
            
            //taking a picture, make sure face detection mode is inactive
            camera.stopFaceDetection();
            faceDetectionWrapper = false;

            
            File imgFileDir = getDir();
            if (!imgFileDir.exists() && !imgFileDir.mkdirs()) {
            	Log.e(TAG, "Directory does not exist");
            }

            //Locale.US to get local formatting
            SimpleDateFormat timeFormat = new SimpleDateFormat("hhmmss", Locale.US);
            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.US);
            String time = timeFormat.format(new Date());
            String date = dateFormat.format(new Date());
            String photoFile = date + "_camacc_" + time + ".jpg";       
            String filename = imgFileDir.getPath() + File.separator + photoFile;
          
            Log.i(TAG, "time: " + time + " date: " + date +
            		"\nfilename: " + filename + " photoFile: " + photoFile);

            File pictureFile = new File(filename);
            try {
            	
            	//FileOutputStream fos = new FileOutputStream(pictureFile);
            	BufferedOutputStream fos = new BufferedOutputStream
            			(new FileOutputStream(pictureFile));
            	fos.write(arg0);
            	fos.flush();
            	fos.close();
            	Log.i(TAG, "Image saved: " + pictureFile.toString());
            	Toast.makeText(CameraActivity.this,
            			"Image saved: " + pictureFile.toString(),
                	Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
            	Log.e(TAG, "Image could not be saved");
            	e.printStackTrace();
            }

            lastPictureTakenUri = Uri.parse(filename);
            //after image is taken and saved, stop face detection if active
            if (faceDetectionWrapper == true) {
            	faceDetectionWrapper = !faceDetectionWrapper;
            }

            //allow surface view camera
            surfaceView.setEnabled(true);
            
            //start camera preview again
            camera.startPreview();

            //start TTS service
            if (isDescription == false) {
            	ttsPath(1);
            } else {
            	ttsPath(5);
            }
            
        }};
        
    private File getDir() {
    	File sdDir = Environment.
    			getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    	return new File(sdDir, "Camacc");
    }
          
    /**
     *
     * @param id
     */
    private void ttsPath(final int id)
    {	
    	if (textToSpeech == null) {
    		//reinitialize TTS Engine, re-attempt failed speaking instance
    		
    		Log.e(TAG, "VERY BAD ERROR TEST THIS");
    		textToSpeech = new TextToSpeech(CameraActivity.this, this); 
    		
    		//allow TTS Engine to initialize and bound between speaking
			mHandler.postDelayed(new Runnable() {
				public void run() {
					int reuseID = id;
					ttsPath(reuseID);
				}
			}, 500);
    		
    	} else {
    		//do not allow VoiceRecognition while TTS is active
        	VoiceEngineHelper.setVoiceController(true);
        	
        	//TTS Engine is active marker
        	ttsWrapper = true;
            switch (id)
            {
                case 0:{
                		Log.i(TAG, "ttsPath: case 0");
                		prefHelper.savePrefFirstLaunch("firstLaunch", true);
                		metaString = "Welcome to CamAcc. Please say Picture to take a picture, " +
                				"or Face Recognition to detect faces or Options to change " +
                				"settings or Help for a list of commands.";
                        speakText(metaString);
                        break;
                }
                case 1:{
                		Log.i(TAG, "ttsPath: case 1");
                        metaString = "Your photo has been successfully saved. " +
                                "Say Picture at any time to take another picture, or say " +
                                "Face Detection to start face detection, or say " +
                                "Filter to apply a filter to the picture you just took.";
                        speakText(metaString);
                        break;
                }
                case 2:{
                		Log.i(TAG, "ttsPath: case 2");
                    	metaString = "What filter would you like to apply? " +
                    			"Some examples are Sepia, Gray Scale or Emboss. " +
                    			"You can even say Improve Quality to make the image better. " +
                    			"To hear all filter commands please say Help.";
                    	speakText(metaString);
                    	break;
                }
                case 3:{
            		Log.i(TAG, "ttsPath: case 3");
                	metaString = "You can toggle these settings by saying Long Descriptions " +
							"On or Off. Or Auto Social Media On or Off. Or you can say " +
							"Leave Options.";
                	speakText(metaString);
                	break;         
                }
                case 4:{
            		Log.i(TAG, "ttsPath: case 4");
                	metaString = "Please say a command.";
                	speakText(metaString);
                	break;         
                }
                case 5:{
            		Log.i(TAG, "ttsPath: case 5");
                	metaString = "Your photo has been successfully saved. To apply a " +
                			"filter say Filter.";
                	speakText(metaString);
                	break;         
                } 
                case 6:{
            		Log.i(TAG, "ttsPath: case 6");
                	metaString = "What filter would you like to apply?";
                	speakText(metaString);
                	break;
                }
                case 7:{
            		Log.i(TAG, "ttsPath: case 7");
                	metaString = "Long descriptions are now turned on. To exit options" +
                			"say Leave Options.";
                	speakText(metaString);
                	break;
                }
                case 8:{
            		Log.i(TAG, "ttsPath: case 8");
                	metaString = "Long descriptions are now turned off. To exit options" +
                			"say Leave Options.";
                	speakText(metaString);
                	break;
                }
                case 9:{
            		Log.i(TAG, "ttsPath: case 9");
                	metaString = "Auto social media is now turned on. To exit options" +
                			"say Leave Options.";
                	speakText(metaString);
                	break;
                }
                case 10:{
            		Log.i(TAG, "ttsPath: case 10");
                	metaString = "Auto social media is now turned off. To exit options" +
                			"say Leave Options.";
                	speakText(metaString);
                	break;
                }
                case 11:{
            		Log.i(TAG, "ttsPath: case 11");
                	metaString = "You must say Options first to toggle long descriptions. " +
    						"Please say Options or a normal command.";
                	speakText(metaString);
                	break;
                }
                case 12:{
            		Log.i(TAG, "ttsPath: case 12");
                	metaString = "You must say Options first to toggle auto social media. " +
    						"Please say Options or a normal command.";
                	speakText(metaString);
                	break;
                }
                case 13:{
            		Log.i(TAG, "ttsPath: case 13");
                	metaString = "You are already in option settings. You can exit at any " +
                			"time by saying Leave Options.";
                	speakText(metaString);
                	break;
                }
                case 14:{
            		Log.i(TAG, "ttsPath: case 14");
                	metaString = "You are not in option settings. If you want to enter" +
                			"option settings say Options, otherwise say a normal command.";
                	speakText(metaString);
                	break;
                }
                case 15:{
            		Log.i(TAG, "ttsPath: case 15");
                	metaString = "To do this action, please leave options first by saying " +
                			"Leave Options.";
                	speakText(metaString);
                	break;
                }
                case 16:{
            		Log.i(TAG, "ttsPath: case 16");
                	metaString = "Please say a filter command. Some examples are Sepia, " +
                			"Gray Scale or Emboss. You can even say Improve Quality to make " +
                			"the image better. To hear all filter commands please say Help.";
                	speakText(metaString);
                	break;
                }
                case 97:{
        				Log.i(TAG, "ttsPath: case 97");
        				metaString = "Sorry, I did not get that. Can you please repeat " +
        						"your command?";
        				speakText(metaString);
        				break;
                }             
                case 98:{
            			Log.i(TAG, "ttsPath: case 98");
            			metaString = "Please take a picture first before applying filter.";
            			speakText(metaString);
            			break;
                }
                case 99:{
                		Log.i(TAG, "ttsPath: case 99");
                		metaString = "Please say Picture, Face Recognition " +
                				"or Options.";
                		speakText(metaString);
                		break;
                }
            }

            //reset voice and tss controllers
            VoiceEngineHelper.setVoiceController(false);
            ttsWrapper = false;
    	}
    }

    /**
     *
     * @param text
     */
    private void speakText(String text) {
        if(textToSpeech.isSpeaking()) {
            return;
        } else {
        	textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, map);
        } 
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.i(TAG, "requestCode: " + requestCode + "\nresultCode: " + resultCode +
    			"\ndata: " + data);
    	
    	if (resultCode != RESULT_CANCELED) {
    		if (Voice_Engine.singletonVE != null) {
        		try {
        			Log.e(TAG, "Voice_Engine class force closed");
        			Voice_Engine.singletonVE.finish();
        		} catch (Exception e) {
        			
        		}
        	}

    		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
    			Log.e(TAG, "start checking matches");
    			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
    			int listSize = matches.size();

    			// compile list of recognized words by Voice_Engine class
    			for (int i=0; i<listSize; i++) {
    				Log.i(TAG, "Recognized words: " + matches.get(i));
    			}
                
    			if (matches.contains("take picture") || matches.contains("picture") ||
    					matches.contains("photo") || matches.contains("camera")) {

    				if (isOptionController == true) {
    					ttsPath(15);
    				} else {
    					if (isFilterController == true) {
    						isFilterController = false; //reset isFilterController
    						speakText("Ok, cancelling apply filter action to take another " +
    								"picture.");
    					} 
    					camera.takePicture(myShutterCallback,
                    			myPictureCallback_RAW, myPictureCallback_JPG);
    				}
                	
    			} else if (matches.contains("start face recognition") || matches.contains("start") ||
    					matches.contains("face") || matches.contains("recognition") || 
    					matches.contains("face recognition") || matches.contains("detection")) {
                		Log.i(TAG, "Face detection mode activated");
                	
                		if (isOptionController == true) {
        					ttsPath(15);
        				} else {
        					if (isFilterController == true) {
        						isFilterController = false; //reset isFilterController
        						speakText("Ok, cancelling apply filter action to start face " +
        								"recognition.");
        					} 
        					//do not allow VoiceRecognition while face detection is active
                        	VoiceEngineHelper.setVoiceController(true);
                        	Voice_Engine.singletonVE.finish();
                    		
                    		//allow the taking a picture by pressing anywhere on the app surface
                        	surfaceView.setEnabled(true);
                    		faceDetectionWrapper = true;
                    		camera.startFaceDetection();
        				}
             		
    			} else if (matches.contains("apply filter") || matches.contains("apply") ||
                        matches.contains("filter")) {
    				
    					if (isOptionController == true) {
    						ttsPath(15);
    					} else {
    						if (isFilterController == false) {
    							isFilterController = true;
    							if (isDescription == false) {
            						ttsPath(2);
            					} else {
            						ttsPath(1);
            					}    							
    						} else {	
    							ttsPath(16);
    						}

    					}
    			} else if (matches.contains("twitter")) {

    				try {
        				Intent i = new Intent(Intent.ACTION_SEND);
        				i.setType("text/plain"); //application/twitter
        				i.setPackage("com.twitter.android");
        				i.putExtra(Intent.EXTRA_TEXT, "Developer Test; please ignore");
        				i.putExtra(Intent.EXTRA_STREAM, lastPictureTakenUri);
        				//i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        				CameraActivity.this.startActivity(i);

    				} catch (Exception e) {
    					//official twitter code missing
    					Log.e(TAG, "Official twitter code missing");
    				}

    			} else if (matches.contains("option") || matches.contains("options") ||
                        matches.contains("setting") || matches.contains("settings")) {
    					
    					if (isOptionController == false) {
    						//do not allow VoiceRecognition
                    		VoiceEngineHelper.setVoiceController(true);
                    		
                    		if (isFilterController == true) {
        						isFilterController = false; //reset isFilterController
        					} 
                    		
                    		//if controller is on, option commands are forced
                    		isOptionController = true;
    						
    						if (isDescription == false) {
                    			
                    			speakText("Options allow you to disable and enable a variety of " +
            							"features built into CamAcc."); 						
        						speakText("Your current settings are.. long descriptions turned on.");
        						if (isAutoSocial == false) {
        							speakText("Auto social media turned off..");
        						} else {
        							speakText("Auto social media turned on..");
        						}

                        		ttsPath(3);
        					} else {
        						speakText("Your current settings are.. long descriptions turned off.");          		
        						if (isAutoSocial == false) {
        							speakText("Auto social media turned off..");
        						} else {
        							speakText("Auto social media turned on..");
        						}
        						
        						ttsPath(4);
        					} 
    					} else {
    						ttsPath(13);
    					}
    			} else if (matches.contains("description on") || matches.contains("descriptions on") ||
                        matches.contains("long description on") || matches.contains("long descriptions on") || 
                        matches.contains("description off") || matches.contains("descriptions off") ||
                        matches.contains("long description off") || matches.contains("long descriptions off") ||
                        matches.contains("description")) {
    					
    					if (isOptionController == true) {
    						if (isDescription == false) {
    							isDescription = !isDescription;
    							prefHelper.savePrefDescription("description", true);
    							ttsPath(8);
    						} else {
    							isDescription= !isDescription;
    							prefHelper.savePrefDescription("description", false);
    							ttsPath(7);
    						}
    					} else {
    						ttsPath(11);
    					}
    			} else if (matches.contains("auto") || matches.contains("auto social media on") ||
                        matches.contains("auto social media off") || matches.contains("social media") || 
                        matches.contains("social media on") || matches.contains("social media off") ||
                        matches.contains("social on") || matches.contains("social off") || 
                        matches.contains("social") || matches.contains("media")) {
    					
    					if (isOptionController == true) {
    						if (isAutoSocial == false) {
    							isAutoSocial = !isAutoSocial;
    							prefHelper.savePrefAutoSocial("autoSocial", true);
    							ttsPath(9);
    						} else {
    							isAutoSocial= !isAutoSocial;
    							prefHelper.savePrefAutoSocial("autoSocial", false);
    							ttsPath(10);
    						}
    					} else {
    						ttsPath(12);
    					}		    					
    			} else if (matches.contains("leave options") || matches.contains("leave option") || 
    					matches.contains("leave")) {
    					
    					if (isOptionController == true) {
    						isOptionController = false; //reset option controller
    						if (isDescription == false) {
    			    			ttsPath(99);
    			    		} else {
    			    			ttsPath(4);
    			    		}
    						
    					} else {
    						ttsPath(14);
    					}		

    			} else if (matches.contains("sepia") || matches.contains("yellow") ||
    					matches.contains("sophia") || matches.contains("sepia filter") ||
    					matches.contains("sofia")) {
    				
    					if (isFilterController == true) {
    						isFilterController = false; //reset isFilterController
    					}	
    				
    					if (isOptionController == true) {
    						ttsPath(15);
    					} else {
    						if (lastPictureTakenUri == null) {
        						ttsPath(98);
        					} else {
        						ImageProcess imageProcess = new ImageProcess();
        						Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                        		imageProcess.applyAndSave(this, lastPictureTakenUri, ImageProcess.FILTER_SEPIA);
                        		
                        		Log.i(TAG, "Sepia filter applied and saved");
                        		speakText("Sepia filter has been applied and saved. Would you like to share " +
                        				"this image? To post to Facebook say Facebook or to post to " +
                        				"Twitter say Twitter.");
        					}
    					}
    			} else if (matches.contains("gray scale") || matches.contains("gray") ||
    					matches.contains("grayscale") || matches.contains("gray scale filter") ||
    					matches.contains("grey scale") || matches.contains("grey") ||
    	    			matches.contains("greyscale") || matches.contains("grey scale filter")) {
    					
    					if (isFilterController == true) {
							isFilterController = false; //reset isFilterController
						}
    				
    					if (isOptionController == true) {
    						ttsPath(15);
    					} else {
    						if (lastPictureTakenUri == null) {
        						ttsPath(98);
        					} else {
        						ImageProcess imageProcess = new ImageProcess();
        						Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                        		imageProcess.applyAndSave(this, lastPictureTakenUri, ImageProcess.FILTER_GRAYSCALE);
                        		
                        		Log.i(TAG, "Grayscale filter applied and saved");
                        		speakText("Gray scale filter has been applied and saved. Would you like to share " +
                        				"this image? To post to Facebook say Facebook or to post to " +
                        				"Twitter say Twitter.");
        					}
    					}
    
    			} else if (matches.contains("emboss") || matches.contains("emboss filter") ||
    					matches.contains("boss")) {

    					if (isFilterController == true) {
							isFilterController = false; //reset isFilterController
						}
    				
    					if (isOptionController == true) {
    						ttsPath(15);
    					} else {
    						if (lastPictureTakenUri == null) {
        						ttsPath(98);
        					} else {
        						ImageProcess imageProcess = new ImageProcess();
        						Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                        		imageProcess.applyAndSave(this, lastPictureTakenUri, ImageProcess.FILTER_EMBOSS);
                        		
                        		Log.i(TAG, "Emboss filter applied and saved");
                        		speakText("Emboss filter has been applied and saved. Would you like to share " +
                        				"this image? To post to Facebook say Facebook or to post to " +
                        				"Twitter say Twitter.");
        					}
    					}
	
    			} else if (matches.contains("invert") || matches.contains("invert filter")) {
    				
    					if (isFilterController == true) {
    						isFilterController = false; //reset isFilterController
    					}
    				
    					if (isOptionController == true) {
    						ttsPath(15);
    					} else {
    						if (lastPictureTakenUri == null) {
        						ttsPath(98);
        					} else {
        						ImageProcess imageProcess = new ImageProcess();
        						Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                        		imageProcess.applyAndSave(this, lastPictureTakenUri, ImageProcess.FILTER_INVERT);
                        		
                        		Log.i(TAG, "Invert filter applied and saved");
                        		camera.stopPreview();
                        		isPreview = false;
                        		speakText("Invert filter has been applied and saved. Would you like to share " +
                        				"this image? To post to Facebook say Facebook or to post to " +
                        				"Twitter say Twitter.");
        					}
    					}

    			} else if (matches.contains("blur") || matches.contains("blur image") ||
    					matches.contains("blur filter")) {

    					if (isFilterController == true) {
							isFilterController = false; //reset isFilterController
						}
    					
    					if (isOptionController == true) {
    						ttsPath(15);
    					} else {
    						if (lastPictureTakenUri == null) {
        						ttsPath(98);
        					} else {
        						ImageProcess imageProcess = new ImageProcess();
        						Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                        		imageProcess.applyAndSave(this, lastPictureTakenUri, ImageProcess.FILTER_BLUR);
                        		
                        		Log.i(TAG, "Blur filter applied and saved");		
                        		camera.stopPreview();
                        		isPreview = false;
                        		speakText("Blur filter has been applied and saved. Would you like to share " +
                        				"this image? To post to Facebook say Facebook or to post to " +
                        				"Twitter say Twitter.");
        					}
    					}

    			} else if (matches.contains("sharpen") || matches.contains("sharpen filter") ||
    					matches.contains("sharp") || matches.contains("sharpen image")) {

    					if (isFilterController == true) {
    						isFilterController = false; //reset isFilterController
    					}
    				
    					if (isOptionController == true) {
    					ttsPath(15);
    					} else {
    						if (lastPictureTakenUri == null) {
        						ttsPath(98);
        					} else {
        						ImageProcess imageProcess = new ImageProcess();
        						Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                        		imageProcess.applyAndSave(this, lastPictureTakenUri, ImageProcess.FILTER_SHARPEN);
                        		
                        		Log.i(TAG, "Sharpen filter applied and saved");
                        		camera.stopPreview();
                        		isPreview = false;
                        		speakText("Image has been sharpened and saved. Would you like to share " +
                        				"this image? To post to Facebook say Facebook or to post to " +
                        				"Twitter say Twitter.");
        					}
    					}

    			} else if (matches.contains("morph") || matches.contains("morph filter") ||
    					matches.contains("morph image")) {

    					if (isFilterController == true) {
    						isFilterController = false; //reset isFilterController
    					}
    				
    					if (isOptionController == true) {
    					ttsPath(15);
    					} else {
    						if (lastPictureTakenUri == null) {
        						ttsPath(98);
        					} else {
        						ImageProcess imageProcess = new ImageProcess();
        						Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                        		imageProcess.applyAndSave(this, lastPictureTakenUri, ImageProcess.FILTER_MORPH);
                        		
                        		Log.i(TAG, "Morph filter applied and saved");
                        		camera.stopPreview();
                        		isPreview = false;
                        		speakText("Morph filter has been applied and saved. Would you like to share " +
                        				"this image? To post to Facebook say Facebook or to post to " +
                        				"Twitter say Twitter.");
        					}
    					}

    			} else if (matches.contains("gaussian") || matches.contains("gaussian filter")) {

    					if (isFilterController == true) {
    						isFilterController = false; //reset isFilterController
    					}
    				
    					if (isOptionController == true) {
    						ttsPath(15);
    					} else {
    						if (lastPictureTakenUri == null) {
        						ttsPath(98);
        					} else {
        						ImageProcess imageProcess = new ImageProcess();
        						Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                        		imageProcess.applyAndSave(this, lastPictureTakenUri, ImageProcess.FILTER_GAUSSIAN);
                        		
                        		Log.i(TAG, "Gaussian filter applied and saved");
                        		camera.stopPreview();
                        		isPreview = false;
                        		speakText("Gaussian filter has been applied and saved. Would you like to share " +
                        				"this image? To post to Facebook say Facebook or to post to " +
                        				"Twitter say Twitter.");
        					}
    					}

    			} else if (matches.contains("improve") || matches.contains("improve image") || 
    					matches.contains("bright") || matches.contains("brighten") || 
    					matches.contains("groove") || matches.contains("improved") || 
    					matches.contains("proove")) {

    					if (isFilterController == true) {
    						isFilterController = false; //reset isFilterController
    					}
    				
    					if (isOptionController == true) {
    						ttsPath(15);
    					} else {
    						if (lastPictureTakenUri.getPath() == null) {
        						ttsPath(98);
        					} else {
        						/* perform two filter processes, both brightness correction and sharpen, to  
            					 * improve the look of the image(s) */
            					ImageProcess imageProcess = new ImageProcess();
            					Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
            					imageProcess.applyAndSave(this, lastPictureTakenUri, ImageProcess.FILTER_BRIGHTNESS);
            					//imageProcess.applyAndSave(this, lastPictureTakenUri, ImageProcess.FILTER_SHARPEN);
            					
            					Log.i(TAG, "Brightness correction and sharpen filters applied and saved");
            					camera.stopPreview();
            					isPreview = false;
            					speakText("Image has been improved with brightness correction and sharpen filters and " +
            							"saved. Would you like to share this image? To post to Facebook say " +
            							"Facebook or to post to Twitter say Twitter.");
        					}
    					}

    			} else if (matches.contains("close app") || matches.contains("close") || 
    					matches.contains("shutdown app") || matches.contains("shutdown") ||
    					matches.contains("exit app") || matches.contains("exit")) {

    					//close application    
    					surfaceView.setEnabled(false);
    					camera.stopPreview();
    					isPreview = false;
    					speakText("Application closing.");	
    					textToSpeech.shutdown();
    					Voice_Engine.singletonVE.finish();
    					finish();
    			} else if (!matches.isEmpty()) {
    					//reinitialize voice engine
    					Log.e(TAG, "Ignoring non-commands; normal talking or " + 
    							"noise inferred.");
    					
    					//add short delay between reactivating voice engine
    					mHandler.postDelayed(new Runnable() {
							public void run() {
								VoiceEngineHelper.setVoiceController(false);
		                		Intent intent = new Intent(CameraActivity.this, Voice_Engine.class);
		            			startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);speakText("One face Centered detected.");
							}
						}, 1000);		
    			}
    		} //end if-statement: requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode != 0
        } else {
        	//Voice Engine interrupted. Affirm last action or reinitialize TTS based on last action
        	if (data == null) {
        		Log.i(TAG, "Voice engine interrupted, surfaceView button was pressed");
            	VoiceEngineHelper.setVoiceController(true);
            	Voice_Engine.singletonVE.finish();
            	camera.takePicture(myShutterCallback,
                      myPictureCallback_RAW, myPictureCallback_JPG);
        	}
        }
    } //end onActivityForResult()


    /**
     *
     * @param holder
     * @param format
     * @param width
     * @param height
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height)
    {
        // TODO Auto-generated method stub
        if(isPreview)
        {
            camera.stopPreview();
            isPreview = false;
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
                camera.setFaceDetectionListener(new FaceDetection());
                isPreview = true;
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        camera = Camera.open();
        Camera.Parameters parameter = camera.getParameters();

        if(parameter.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_AUTO))	{
        	parameter.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        	camera.setParameters(parameter);
        }

        Camera.Size size = getBestPreviewSize(surfaceView.getWidth(), surfaceView.getHeight(), parameter);
        parameter.setPreviewSize(size.width, size.height);
        surfaceView.getTop();
    }

    /**
     *
     * @param width
     * @param height
     * @param parameters
     * @return
     */
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


    /**
     *
     * @param holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        camera.stopPreview();

		/* Call release() to release the camera for use by other applications.
		 * Applications should release the camera immediately in onPause()
		 * and re-open() it in onResume() */
        camera.release();
        camera = null;
        isPreview = false;
    }

    
//    private Intent getShareIntent(String type, String subject, String text) 
//	{
//	    boolean found = false;
//	    Intent share = new Intent(android.content.Intent.ACTION_SEND);
//	    share.setType("text/plain");
//
//	    // gets the list of intents that can be loaded.
//	    List<ResolveInfo> resInfo = this.getPackageManager().queryIntentActivities(share, 0);
//	    System.out.println("resinfo: " + resInfo);
//	    if (!resInfo.isEmpty()){
//	        for (ResolveInfo info : resInfo) {
//	            if (info.activityInfo.packageName.toLowerCase().contains(type) || 
//	                    info.activityInfo.name.toLowerCase().contains(type) ) {
//	                share.putExtra(Intent.EXTRA_SUBJECT,  subject);
//	                share.putExtra(Intent.EXTRA_TEXT,     text);
//	                share.setPackage(info.activityInfo.packageName);
//	                found = true;
//	                break;
//	            }
//	        }
//	        if (!found)
//	            return null;
//	        return share;
//	    }
//	    return null;
//	} //end getShareIntent()
    
    
    
    /**
     * Checks for an active network connection. If there exists
     * no detected network connection or connection is too slow
     * networkWarning() method is called. 
     * @return isConnected true if an active network connection exists
     */
    private boolean isNetWorkConnection() {
    	isConnected = false;
    	
    	/* check connectivity and take special consideration for less 
    	 * stable connections such as WIFI. This is done so by
    	 * making sure connecton strength is good enough to run
    	 * our voice engine */
    	if (NetworkUtils.isNetworkActive(CameraActivity.this) &&
    			NetworkUtils.isConnectedFast(CameraActivity.this)) {
    		Log.i(TAG, "isConnectedWifi: " + NetworkUtils.isConnectedWifi(CameraActivity.this) +
    				" isConnectedMobile: " + NetworkUtils.isConnectedMobile(CameraActivity.this));
    		
    		if (isConnected == false) {
    			isConnected = true;
    			loadActivity();
    		}
    	} else {
    		//no connection or connection is not string enough
    		Log.e(TAG, "No internet connection");
    		networkWarning();
    	}
    	return isConnected;	
    
    } //end isNetworkConnection()
 
    /**
     * Notifies end user that there is no detected network connection
     * or that the network connection is too slow with three
     * consecutive ringtone beeps and three 500 millisecond vibrations.
     * This method also allows a short period of time to reconnect to the 
     * internet, otherwise it will close the application.
     * 
     * @return isConnected true if an active network connection exists
     */
    private void networkWarning() {

    	try {
    		isWarningSound = false;
    		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    		r = RingtoneManager.getRingtone(getApplicationContext(), notification);
    		v = (Vibrator) CameraActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
    		/* suspend app using alert dialog. This allows us to suspend the 
    		 * main UI, and then proceed with connectivity checks, reconnection
    		 * checks, and warning sounds before application closes for not
    		 * detecting a proper network connection */

        	AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
    		dialogBuilder.setTitle("No Internet Connection Detected");
    		dialogBuilder.setIcon(R.drawable.ic_launcher);
    		dialogBuilder
    				.setMessage("Please connect to the internet...").setCancelable(false);
		
    		alertDialog = dialogBuilder.create();
    		alertDialog.show();
    		
    		new CountDownTimer(6000, 1500) {

    			/* The calls to onTick(long) are synchronized to this 
    			 * object so that one call to onTick(long) won't ever 
    			 * occur before the previous callback is complete */
    			public void onTick(long millisUntilFinished) {
    				Log.i(TAG, "seconds remaining (1): " + millisUntilFinished / 1000);
    				if (isWarningSound == false) {
    					r.play();
    					v.vibrate(500);
    				}
    				
    		    }

    		    public void onFinish() {
    		    	/* recheck for internet connection every second for
    		    	 * 10 seconds. If no connection is detected after the 
    		    	 * allowed 10 seconds close application */
      		    	new CountDownTimer(10000, 1000) {

    					@Override
    					public void onTick(long millisUntilFinished) {
    						// TODO Auto-generated method stub
    						if (NetworkUtils.isConnected(CameraActivity.this) &&
    				    			NetworkUtils.isConnectedFast(CameraActivity.this)) {
    				    		Log.i(TAG, "isConnectedWifi: " + NetworkUtils.isConnectedWifi(CameraActivity.this) +
    				    				" isConnectedMobile: " + NetworkUtils.isConnectedMobile(CameraActivity.this) +
    				    				"\nseconds remaining (2): " + millisUntilFinished / 1000);

    				    		// set this boolean only once
    				    		if (isConnected == false) {
    				    			isConnected = true;
    				    		}
    				    		
    				    	}
    					}
    					
    					@Override
    					public void onFinish() {
    						// TODO Auto-generated method stub
    						if (isConnected == false) {
    							r.play();
    							v.vibrate(500);
    							alertDialog.dismiss();
    					        finish();
    						} else {
    							alertDialog.dismiss();
    							loadActivity();
    						}
    					}
    		    		
    		    	}.start();
    		    	
    		    }
    		}.start();
 
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    } //end networkWarning() method
    

    /**
     *
     */
    class FaceDetection implements Camera.FaceDetectionListener {
        /**
         * Detects faces in the Camera view according to FaceDetectionListener.
         * All faces detected will be stored into a Face array to be passed to
         * be processed by this method. For one face it will detect if the face
         * is centered. For two faces detects centered if neither face is in the
         * center of the screen. For three or more faces it says multiple faces
         * detected.
         * @param faces   the number of faces detected by Camera.FaceDetectionListener
         * @param camera  not used. Exists for overriding onFaceDetection from
         *                FaceDetectionListener
         */
        @Override
        public void onFaceDetection(Face[] faces, Camera camera) {

			if (faceDetectionWrapper == true) {

				// no faces detected
				if (faces.length == 0) {
					Log.i(TAG, "faces detected: " + faces.length
							+ " Max Num Detected Faces: "
							+ camera.getParameters().getMaxNumDetectedFaces());
				} else if (faces.length == 1) {
					int vWidth = surfaceView.getWidth();
					int vHeight = surfaceView.getHeight();
					int l = faces[0].rect.left;
					int t = faces[0].rect.top;
					int r = faces[0].rect.right;
					int b = faces[0].rect.bottom;
					int left = (l + 1000) * vWidth / 2000;
					int top = (t + 1000) * vHeight / 2000;
					int right = (r + 1000) * vWidth / 2000;
					int bottom = (b + 1000) * vHeight / 2000;

					Rect realFaceRect = new Rect(left, top, right, bottom);

					int halfWidth = surfaceView.getRight() / 2;
					int halfHeight = surfaceView.getBottom() / 2;
					Rect middle = new Rect(halfWidth - 50, halfHeight - 50,
							halfWidth + 50, halfHeight + 50);
					if (middle.intersect(realFaceRect)) {
						Log.i(TAG, "People detected: " + faces.length);
						speakText("One face Centered detected.");
					} else {
						Log.i(TAG, "People detected: " + faces.length
								+ " // realFaceRect: " + realFaceRect.toString());
						speakText("Face detected, not centered.");
					}	
				} else if (faces.length == 2) {
					Log.i(TAG, "People detected: " + faces.length
							+ " // faces[0].rect: " + faces[0].rect.toString());
					speakText("Two people detected, both are centered.");
				} else if (faces.length >= 3) {
					Log.i(TAG, "People detected: " + faces.length);
					speakText("Multiple people detected.");
				}
			}
		}

    } //end FaceDetection class

    /**
	 * @return the lastPictureTakenUri
	 */
	public static Uri getLastPictureTakenUri() {
		return lastPictureTakenUri;
	}


	/**
	 * @param lastPictureTakenUri the lastPictureTakenUri to set
	 */
	public static void setLastPictureTakenUri(Uri lastPictureTakenUri) {
		CameraActivity.lastPictureTakenUri = lastPictureTakenUri;
	}

    
    /**
     * 
     */
    @Override
    public void onBackPressed() {
    	//not only close Activity, but stop services as well
    	textToSpeech.stop();
        textToSpeech.shutdown();
        Voice_Engine.singletonVE.finish();
        finish();
    }
    
    /**
     * 
     */
    @Override
    public void onStart() {
    	Log.d(TAG, "onStart()");
    	textToSpeech = new TextToSpeech(CameraActivity.this, this); 
    	super.onStart();
    }
    
    /**
    *
    */
    @Override
    public void onStop() {
    	Log.d(TAG, "onStop()");
    	if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onStop();
    }
    
    /**
    *
    */
    @Override
    public void onPause() {
    	Log.d(TAG, "onPause()");
        super.onPause();
    }

    /**
     *
     */
    @Override
    public void onDestroy(){
    	Log.d(TAG, "onDestroy()");
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        Voice_Engine.singletonVE.finish();
        super.onDestroy();
    }

} //end CameraActivity class
