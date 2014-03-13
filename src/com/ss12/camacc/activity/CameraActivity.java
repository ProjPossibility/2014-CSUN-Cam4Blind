package com.ss12.camacc.activity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import android.graphics.Rect;
import com.ss12.camacc.R;
import com.ss12.camacc.helper.PreferencesHelper;
import com.ss12.camacc.helper.VoiceEngineHelper;
import com.ss12.camacc.network.NetworkUtils;
import com.ss12.camacc.service.Voice_Engine;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
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
import android.provider.MediaStore;
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
 * CameraActivity is the main activity for taking pictures and running the
 * application. It includes features to manage the textToSpeech for the user,
 * voice options, network connection detection, facial recognition, error
 * handling, and sending results back to the call location.
 *
 * @author Leonard Tatum
 * @author Noah Anderson
 * @author Stefan Eng
 * @author Javier Pimentel
 * @author Kristoffer Larson
 */
public class CameraActivity extends Activity implements SurfaceHolder.Callback, OnInitListener
{
    public static String TAG = CameraActivity.class.getSimpleName();

    private PreferencesHelper prefHelper;
    public Voice_Engine listener = new Voice_Engine();

    private boolean ttsWrapper = false; //@true: TTS Engine is active
    private boolean faceDetectionWrapper = false; //@true: Face Detection mode active
    private boolean voiceController = false;
    private boolean isPreview = false;
    private boolean isConnected = false;
    private boolean isWarningSound;
    private boolean isOptionController = false;
    private boolean isFilterController = false;
    private boolean isCloseController = false;
    private boolean isSelfie = false;
    public static boolean isQualityImprove = false;

	public static Uri lastPictureTakenUri;
	private Uri filterUri;

	private static TextToSpeech textToSpeech;
    private Camera camera; //back camera object
    private Camera cameraSelf; //front camera object
    Camera.Parameters param;
    Camera.Parameters paramSelf;
    private Size size;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private AlertDialog alertDialog;
    private Ringtone r;
    private Vibrator v;

    private String metaString;

    long timeInMilliseconds;
    private int cameraId = 0;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    List<Intent> targetedShareIntents = new ArrayList<Intent>();
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

                	//disable voice engine
                	disableVoiceEngine();

                	//disable face detection
                	if (faceDetectionWrapper == true) {
                    	faceDetectionWrapper = !faceDetectionWrapper;
                    	camera.stopFaceDetection();
                    }

                	//take picture and store image
                    camera.takePicture(myShutterCallback,
                            myPictureCallback_RAW, myPictureCallback_JPG);
                }});

            configureSurface();


            //check options stored in sharedPreferences
    		prefHelper = new PreferencesHelper(getApplicationContext());

    		isFirstLaunch = prefHelper.getPrefFirstLaunch(sharedPrefFirstLaunch);
    		isDescription = prefHelper.getPrefDescription(sharedPrefDescription);
    		isAutoSocial = prefHelper.getPrefAutoSocial(sharedPrefAutoSocial);

    		Log.i(TAG, "isFirstLaunch(" + isFirstLaunch + ")" +
    				" // isDescription(" + isDescription + ")" +
    				" // isAutoSocial(" + isAutoSocial + ")");

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
		    		} else {
		    			if (isDescription == true) {
		    				ttsPath(99);
		    			} else if (isDescription == false) {
		    				ttsPath(4);
		    			}
		    		}
				}
			}, 700);

        } else {
        	//confirm network connection
        	isNetWorkConnection();
        }

    } //end loadActivity() method


    /**
     * Every time TTS Engine is initialized, onInit procs. We parsed
     * out the work to another method called ttsEngine() so that if
     * initialization fails, or at any other point in the application
     * we would like to.
     *
     * @param status SUCCESS or ERROR.
     */
    @Override
    public void onInit(int status) {
    	Log.e(TAG, "ONINIT(): Text-to-speech initialized!");

        if (status == TextToSpeech.SUCCESS) {
            /**
             * Listener for events relating to the progress of an utterance through the synthesis
             * queue. Each utterance is associated with a call to speak(String, int, HashMap) or
             * synthesizeToFile(String, HashMap, String) with an associated utterance identifier,
             * as per KEY_PARAM_UTTERANCE_ID. The callbacks specified in this method can be called
             * from multiple threads.
             */
        	textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                /**
                 * Called when an utterance "starts" as perceived by the caller. This will be soon
                 * before audio is played back in the case of a speak(String, int, HashMap) or
                 * before the first bytes of a file are written to storage in the case of
                 * synthesizeToFile(String, HashMap, String).
                 *
                 * @param utteranceId The utterance ID of the utterance
                 */
                @Override
                public void onStart(String utteranceId) {
                    // TODO Auto-generated method stub

                }//end onStart

                /**
                 * Called when an error has occurred during processing. This can be called at
                 * any point in the synthesis process. Note that there might be calls to
                 * onStart(String) for specified utteranceId but there will never be a call to
                 * both onDone(String) and onError(String) for the same utterance.
                 *
                 * @param utteranceId The utterance ID of the utterance
                 */
                @Override
                public void onError(String utteranceId) {
                    // TODO Auto-generated method stub

                }//end onError

                /**
                 * Called when an utterance has successfully completed processing. All audio will
                 * have been played back by this point for audible output, and all output will
                 * have been written to disk for file synthesis requests. This request is
                 * guaranteed to be called after onStart(String).
                 *
                 * @param utteranceId The utterance ID of the utterance
                 */
                @Override
                public void onDone(String utteranceId) {
                	/* before we use Voice_Engine class we always check for
					 * active network connection */
                	if (isConnected == true && isCloseController == false) {
                		//do not allow VoiceRecognition if face detection is active
                		if (faceDetectionWrapper == false && isSelfie == false
                				&& voiceController == false) {

                    		Intent intent = new Intent(CameraActivity.this, Voice_Engine.class);
                			startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
                		}
                	} else {
                		//confirm network connection
                    	isNetWorkConnection();
                	}
                }//end onDone
            }); //end setOnUtteranceProgressListener()

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
    		} else {
    			ttsPath(99);
    		}
        }
    } //end onInit() method

    /**
     *
     */
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
    }//end configureSurface

    /**
     * Callback interface used to notify on completion of camera auto focus.
     * Devices that do not support auto-focus will receive a "fake" callback
     * to this interface.
     */
    AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {

        /**
         * Called when the camera auto focus completes.
         *
         * @param arg0 True if focus was successful, false if otherwise
         * @param arg1 The Camera service object
         */
        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            // TODO Auto-generated method stub
        //end onAutoFocus
        }};//end myAutoFocusCallback

    /**
     * Callback interface used to signal the moment of actual image capture.
     */
    ShutterCallback myShutterCallback = new ShutterCallback(){

        /**
         * Called as near as possible to the moment when a photo is captured from the sensor.
         */
        @Override
        public void onShutter() {
            // TODO Auto-generated method stub
        //end onShutter
        }};//end myShutterCallback
    /**
     * Callback interface used to signal the moment of a RAW image.
     */
    PictureCallback myPictureCallback_RAW = new PictureCallback(){

        /**
         * Called when image data is available after a picture is taken.
         *
         * @param arg0 A byte array of the picture data
         * @param arg1 The Camera service object
         */
        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            // TODO Auto-generated method stub
        //end onPictureTaken
        }};//end myPictureCallback_RAW

    /**
     * Callback interface used to signal the moment of a JPG image.
     */
    PictureCallback myPictureCallback_JPG = new PictureCallback(){

        /**
         * Called when image data is available after a picture is taken.
         *
         * @param arg0 A byte array of the picture data
         * @param arg1 The Camera service object
         */
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

            	BufferedOutputStream fos = new BufferedOutputStream
            			(new FileOutputStream(pictureFile));
            	fos.write(arg0);
            	fos.flush();
            	fos.close();
            	Log.i(TAG, "Image saved: " + pictureFile.toString());

            } catch (Exception e) {
            	Log.e(TAG, "Image could not be saved");
            	e.printStackTrace();
            }

            lastPictureTakenUri = Uri.parse(filename);

            //setup back camera if selfie mode is active
            if (isSelfie == true) {
            	backCamera();
            }

            //allow surface view camera
            surfaceView.setEnabled(true);

            //start camera preview again
            camera.startPreview();

            //enable voice engine
            enableVoiceEngine();

            //start TTS service and/or reset selfie flag to default
            if (isSelfie == true) {
            	isSelfie = false;
            	ttsPath(18);
            } else {
            	if (isDescription == true) {
                	ttsPath(1);
                } else {
                	ttsPath(5);
                }
            }
        //end onPictureTaken
        }};//end myPictureCallback_JPG

    /**
     * Get the directory of the phone's photo gallery
     *
     * @return The file for photos to be saved in
     */
    private File getDir() {
    	File sdDir = Environment.
    			getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    	return new File(sdDir, "Camacc");
    }//end getDir

    /**
     * Initializes textToSpeech and lays out the available options for the user, as
     * well as creating a usage path for the user to take.
     *
     * @param id The program path that is being taken
     */
    private void ttsPath(final int id)
    {
    	if (textToSpeech == null) {
    		//reinitialize TTS Engine, re-attempt failed speaking instance
    		textToSpeech = new TextToSpeech(CameraActivity.this, this);

    		//allow TTS Engine to initialize and bound between speaking
			mHandler.postDelayed(new Runnable() {
				public void run() {
					int reuseID = id;
					ttsPath(reuseID);
				}
			}, 500);

    	} else {
        	//TTS Engine is active marker
        	ttsWrapper = true;
            switch (id)
            {
                case 0:{
                		Log.i(TAG, "ttsPath: case 0");
                		prefHelper.savePrefFirstLaunch("firstLaunch", true);
                		isFirstLaunch = true;
                		metaString = "Welcome to CamAcc. Please say Picture to take a picture, " +
                				"or Detection to detect faces or Options to change " +
                				"settings or Help for a list of commands.";
                        speakText(metaString);
                        break;
                }
                case 1:{
                		Log.i(TAG, "ttsPath: case 1");
                        metaString = "Your photo has been successfully saved. " +
                                "Say Picture at any time to take another picture, or say " +
                                "Detection to start face detection, or say " +
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
                	metaString = "Your photo has been saved.";
                	speakText(metaString);
                	break;
                }
                case 6:{
            		Log.i(TAG, "ttsPath: case 6");
                	metaString = "What filter?";
                	speakText(metaString);
                	break;
                }
                case 7:{
            		Log.i(TAG, "ttsPath: case 7");
                	metaString = "Long descriptions are now turned on. To exit options " +
                			"say Leave Options.";
                	speakText(metaString);
                	break;
                }
                case 8:{
            		Log.i(TAG, "ttsPath: case 8");
                	metaString = "Long descriptions are now turned off. To exit options " +
                			"say Leave Options";
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
                case 17:{
            		Log.i(TAG, "ttsPath: case 17");
                	metaString = "To do this action, please take a picture first";
                	speakText(metaString);
                	break;
                }
                case 18:{
            		Log.i(TAG, "ttsPath: case 18");
            		metaString = "Your selfie has been successfully saved. To apply a " +
                			"filter say Filter.";
                	speakText(metaString);
                	break;
                }
                case 19:{
            		Log.i(TAG, "ttsPath: case 19");
                	metaString = "Long descriptions are already turned on. To exit options" +
                			"say Leave Options.";
                	speakText(metaString);
                	break;
                }
                case 20:{
            		Log.i(TAG, "ttsPath: case 20");
                	metaString = "Long descriptions are already turned off. To exit options" +
                			"say Leave Options.";
                	speakText(metaString);
                	break;
                }
                case 21:{
            		Log.i(TAG, "ttsPath: case 21");
                	metaString = "Auto social media is already turned on. To exit options" +
                			"say Leave Options.";
                	speakText(metaString);
                	break;
                }
                case 22:{
            		Log.i(TAG, "ttsPath: case 22");
                	metaString = "Auto social media is already turned off. To exit options" +
                			"say Leave Options.";
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
                		metaString = "Please say Picture, Detection " +
                				"or Options.";
                		speakText(metaString);
                		break;
                }
            }

            //reset voice and tss controllers
            VoiceEngineHelper.setVoiceController(false);
            ttsWrapper = false;
    	}
    }//end ttsPath

    /**
     * Clears the textToSpeech queue if nothing is being said.
     *
     * @param text The text textToSpeech is saying
     */
    private void speakText(String text) {
        if(textToSpeech.isSpeaking()) {
            return;
        } else {
        	textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, map);
        }
    }//end speakText

    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it. The resultCode will be
     * RESULT_CANCELED if the activity explicitly returned that, didn't return any result, or
     * crashed during its operation. This handles user speech commands to the application.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(),
     *                    allowing you to identify who this result came from
     * @param resultCode  The integer result code returned by the child activity through its
     *                    setResult().
     * @param data        An Intent, which can return result data to the caller.
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
        			e.printStackTrace();
        		}
        	}

    		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
    			Log.d(TAG, "start checking matches");
    			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
    			int listSize = matches.size();

    			// compile list of recognized words by Voice_Engine class
    			for (int i=0; i<listSize; i++) {
    				Log.i(TAG, "Recognized words: " + matches.get(i));
    			}

    			if (matches.contains("take picture") || matches.contains("picture") ||
    					matches.contains("take photo") || matches.contains("photo")) {

    				if (isFilterController == true) {
						isFilterController = !isFilterController; //reset isFilterController
					}

    				if (isOptionController == true) {
    					ttsPath(15);
    				} else {
    					camera.takePicture(myShutterCallback,
                    			myPictureCallback_RAW, myPictureCallback_JPG);
    				}

    			} else if (matches.contains("selfie") || matches.contains("self") ||
    					matches.contains("take selfie")) {

    				if (isOptionController == true) {
    					ttsPath(15);
    				} else {
    					if (isFilterController == true) {
    						isFilterController = false; //reset isFilterController
    						speakText("Ok, preparing to take selfie.");
    					}
    					isSelfie = true;
    					frontCamera();
    				}
    			} else if (matches.contains("detection") || matches.contains("start detection") ||
    					matches.contains("detect") || matches.contains("face") ||
    					matches.contains("recognition") || matches.contains("start recognition")) {

                		if (isOptionController == true) {
        					ttsPath(15);
        				} else {

        					//do not allow voice engine
        					disableVoiceEngine();

                    		//allow the taking a picture by pressing anywhere on the app surface
                        	surfaceView.setEnabled(true);
                    		faceDetectionWrapper = true;
                    		camera.startFaceDetection();
                    		camera.setFaceDetectionListener(new FaceDetection());
        				}
    			} else if (matches.contains("apply filter") || matches.contains("apply") ||
                        matches.contains("filter") &&
                        !matches.contains("twitter")) { //filter can be mistook for twitter

    				if (lastPictureTakenUri == null) {
    					ttsPath(17);
    				} else {
    					if (isOptionController == true) {
    						ttsPath(15);
    					} else {
    						if (isFilterController == false) {
    							isFilterController = true;
    							if (isDescription == true) {
            						ttsPath(2);
            					} else {
            						ttsPath(6);
            					}
    						} else {
    							ttsPath(16);
    						}

    					}
    				}

    			/*} else if (matches.contains("facebook") || matches.contains("face book")) {


    				}*/

    			} else if (matches.contains("twitter") || matches.contains("tweet")) {
    				//twitter set up with ACTION_SEND
    				if (lastPictureTakenUri == null) {
    					ttsPath(17);
    				} else {
    					String agendaFilename = lastPictureTakenUri.toString();
        				final ContentValues values = new ContentValues(2);
        				values.put(MediaStore.Images.Media.MIME_TYPE, "image/*");
        				values.put(MediaStore.Images.Media.DATA, agendaFilename);
        				final Uri contentUriFile = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        				try {
        					final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        					intent.setType("image/*");
        					intent.putExtra(Intent.EXTRA_TEXT, "Check out this new image taken with #CamAcc! " +
        							"Retweet or respond back with comments.");
        					intent.putExtra(android.content.Intent.EXTRA_STREAM, contentUriFile);
        					CameraActivity.this.startActivity(intent);
        				} catch (Exception e) {
        					e.printStackTrace();
        				}
    				}

    			} else if (matches.contains("option") || matches.contains("options") ||
                        matches.contains("setting") || matches.contains("settings")) {

    					Log.i(TAG, "isDescription(" + isDescription + ")" +
    	    				" // isAutoSocial(" + isAutoSocial + ")");

    					if (isOptionController == false) {

                    		if (isFilterController == true) {
        						isFilterController = false; //reset isFilterController
        					}

                    		//if controller is on, option commands are forced
                    		isOptionController = true;

                    		if (isDescription == true) {
                    			if (isAutoSocial == false) {
                    				speakText("Options allow you to disable and enable a variety of " +
                							"features. Your current settings are, long " +
                							"descriptions turned on. Auto social media turned off.");
                    			} else {
                    				speakText("Options allow you to disable and enable a variety of " +
                							"features. Your current settings are, long " +
                							"descriptions turned on. Auto social media turned on.");
                    			}
                    			ttsPath(3);
                    		} else {
                    			if (isAutoSocial == false) {
                    				speakText("You settings are, long descriptions turned off. " +
                    						"Auto social media turned off.");
                    			} else {
                    				speakText("Your settings are, long descriptions turned off. " +
                    						"Auto social media turned on.");
                    			}
                    			ttsPath(4);
                    		}
    					} else {
    						ttsPath(13);
    					}
    			} else if (matches.contains("description on") || matches.contains("descriptions on") ||
                        matches.contains("long description on") || matches.contains("long descriptions on")) {

    					if (isOptionController == true) {
    						if (isDescription == true) {
    							ttsPath(19);
    						} else {
    							isDescription= !isDescription;
    							prefHelper.savePrefDescription("description", true);
    							Log.i(TAG, "isDescription(" + isDescription + ")");
    							ttsPath(7);
    						}
    					} else {
    						ttsPath(11);
    					}
    			} else if (matches.contains("description off") || matches.contains("descriptions off") ||
                        matches.contains("long description off") || matches.contains("long descriptions off")) {

    					if (isOptionController == true) {
    						if (isDescription == true) {
    							isDescription = !isDescription;
    							prefHelper.savePrefDescription("description", false);
    							Log.i(TAG, "isDescription(" + isDescription + ")");
    							ttsPath(8);
    						} else {
    							ttsPath(20);
    						}
    					} else {
    						ttsPath(11);
    					}
    			} else if (matches.contains("auto social media on") || matches.contains("social media on") ||
                        matches.contains("social on")) {

    					if (isOptionController == true) {
    						if (isAutoSocial == true) {
    							ttsPath(21);
    						} else {
    							isAutoSocial= !isAutoSocial;
    							prefHelper.savePrefAutoSocial("autoSocial", true);
    							Log.i(TAG, "isAutoSocial(" + isAutoSocial + ")");
    							ttsPath(9);
    						}
    					} else {
    						ttsPath(12);
    					}
    			} else if (matches.contains("auto social media off") ||matches.contains("social media off") ||
                        matches.contains("social off")) {

    					if (isOptionController == true) {
    						if (isAutoSocial == true) {
    							isAutoSocial = !isAutoSocial;
    							prefHelper.savePrefAutoSocial("autoSocial", false);
    							Log.i(TAG, "isAutoSocial(" + isAutoSocial + ")");
    							ttsPath(10);
    						} else {
    							ttsPath(22);
    						}
    					} else {
    						ttsPath(12);
    					}
    			} else if (matches.contains("leave options") || matches.contains("leave option") ||
    					matches.contains("leave")) {

    					if (isOptionController == true) {
    						isOptionController = false; //reset option controller
    						if (isDescription == true) {
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

    					if (isOptionController == true) {
    						ttsPath(15);
    					} else {
    						if (lastPictureTakenUri == null) {
        						ttsPath(98);
        					} else {
        						Toast.makeText(CameraActivity.this, "One moment please.",
        								Toast.LENGTH_SHORT).show();

        						ImageProcess imageProcess = new ImageProcess();
        						Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                        		imageProcess.applyAndSave(this, lastPictureTakenUri, ImageProcess.FILTER_SEPIA);

                        		Log.i(TAG, "Sepia filter applied and saved");
                        		if (isDescription == true) {
                        			speakText("Sepia filter has been applied and saved. Would you like to share " +
                            				"this image? To post to Facebook say Facebook or to post to " +
                            				"Twitter say Twitter.");
                        		} else {
                        			speakText("Sepia filter has been applied and saved.");
                        		}
        					}
    					}
    			} else if (matches.contains("gray scale") || matches.contains("gray") ||
    					matches.contains("grayscale") || matches.contains("gray scale filter") ||
    					matches.contains("grey scale") || matches.contains("grey") ||
    	    			matches.contains("greyscale") || matches.contains("grey scale filter")) {

    					if (isOptionController == true) {
    						ttsPath(15);
    					} else {
    						if (lastPictureTakenUri == null) {
        						ttsPath(98);
        					} else {
        						Toast.makeText(CameraActivity.this, "One moment please.",
        								Toast.LENGTH_SHORT).show();

        						ImageProcess imageProcess = new ImageProcess();
        						Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                        		imageProcess.applyAndSave(this, lastPictureTakenUri, ImageProcess.FILTER_GRAYSCALE);

                        		Log.i(TAG, "Grayscale filter applied and saved");
                        		if (isDescription == true) {
                        			speakText("Gray scale filter has been applied and saved. Would you like to share " +
                            				"this image? To post to Facebook say Facebook or to post to " +
                            				"Twitter say Twitter.");
                        		} else {
                        			speakText("Gray scale filter has been applied and saved.");
                        		}
        					}
    					}

    			} else if (matches.contains("emboss") || matches.contains("emboss filter") ||
    					matches.contains("boss") || matches.contains("in bose")) {

    					if (isOptionController == true) {
    						ttsPath(15);
    					} else {
    						if (lastPictureTakenUri == null) {
        						ttsPath(98);
        					} else {
        						Toast.makeText(CameraActivity.this, "One moment please.",
        								Toast.LENGTH_SHORT).show();

        						ImageProcess imageProcess = new ImageProcess();
        						Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                        		imageProcess.applyAndSave(this, lastPictureTakenUri, ImageProcess.FILTER_EMBOSS);

                        		Log.i(TAG, "Emboss filter applied and saved");
                        		if (isDescription == true) {
                        			speakText("Emboss filter has been applied and saved. Would you like to share " +
                            				"this image? To post to Facebook say Facebook or to post to " +
                            				"Twitter say Twitter.");
                        		} else {
                        			speakText("Emboss filter has been applied and saved.");
                        		}
        					}
    					}

    			} else if (matches.contains("invert") || matches.contains("invert filter")) {

    					if (isOptionController == true) {
    						ttsPath(15);
    					} else {
    						if (lastPictureTakenUri == null) {
        						ttsPath(98);
        					} else {
        						Toast.makeText(CameraActivity.this, "One moment please.",
        								Toast.LENGTH_SHORT).show();

        						ImageProcess imageProcess = new ImageProcess();
        						Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                        		imageProcess.applyAndSave(this, lastPictureTakenUri, ImageProcess.FILTER_INVERT);

                        		Log.i(TAG, "Invert filter applied and saved");
                        		if (isDescription == true) {
                        			speakText("Invert filter has been applied and saved. Would you like to share " +
                            				"this image? To post to Facebook say Facebook or to post to " +
                            				"Twitter say Twitter.");
                        		} else {
                        			speakText("Invert filter has been applied and saved.");
                        		}
        					}
    					}

    			} else if (matches.contains("blur") || matches.contains("blur image") ||
    					matches.contains("blur filter")) {

    					if (isOptionController == true) {
    						ttsPath(15);
    					} else {
    						if (lastPictureTakenUri == null) {
        						ttsPath(98);
        					} else {
        						Toast.makeText(CameraActivity.this, "One moment please.",
        								Toast.LENGTH_SHORT).show();

        						ImageProcess imageProcess = new ImageProcess();
        						Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                        		imageProcess.applyAndSave(this, lastPictureTakenUri, ImageProcess.FILTER_BLUR);

                        		Log.i(TAG, "Blur filter applied and saved");
                        		if (isDescription == true) {
                        			speakText("Blur filter has been applied and saved. Would you like to share " +
                            				"this image? To post to Facebook say Facebook or to post to " +
                            				"Twitter say Twitter.");
                        		} else {
                        			speakText("Blur filter has been applied and saved.");
                        		}
        					}
    					}

    			} else if (matches.contains("sharpen") || matches.contains("sharpen filter") ||
    					matches.contains("sharp") || matches.contains("sharpen image")) {

    					if (isOptionController == true) {
    					ttsPath(15);
    					} else {
    						if (lastPictureTakenUri == null) {
        						ttsPath(98);
        					} else {
        						Toast.makeText(CameraActivity.this, "One moment please.",
        								Toast.LENGTH_SHORT).show();

        						ImageProcess imageProcess = new ImageProcess();
        						Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                        		imageProcess.applyAndSave(this, lastPictureTakenUri, ImageProcess.FILTER_SHARPEN);

                        		Log.i(TAG, "Sharpen filter applied and saved");
                        		if (isDescription == true) {
                        			speakText("Image has been sharpened and saved. Would you like to share " +
                            				"this image? To post to Facebook say Facebook or to post to " +
                            				"Twitter say Twitter.");
                        		} else {
                        			speakText("Image has been sharpened and saved.");
                        		}
        					}
    					}

    			} else if (matches.contains("morph") || matches.contains("morph filter") ||
    					matches.contains("morph image")) {

    					if (isOptionController == true) {
    					ttsPath(15);
    					} else {
    						if (lastPictureTakenUri == null) {
        						ttsPath(98);
        					} else {
        						Toast.makeText(CameraActivity.this, "One moment please.",
        								Toast.LENGTH_SHORT).show();

        						ImageProcess imageProcess = new ImageProcess();
        						Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                        		imageProcess.applyAndSave(this, lastPictureTakenUri, ImageProcess.FILTER_MORPH);

                        		Log.i(TAG, "Morph filter applied and saved");
                        		if (isDescription == true) {
                        			speakText("Morph filter has been applied and saved. Would you like to share " +
                            				"this image? To post to Facebook say Facebook or to post to " +
                            				"Twitter say Twitter.");
                        		} else {
                        			speakText("Morph filter has been applied and saved.");
                        		}
        					}
    					}

    			} else if (matches.contains("gossi in") || matches.contains("gossi in filter") ||
    					matches.contains("ga cn") || matches.contains("ga cn filter") ||
    					matches.contains("gossen") || matches.contains("gossen filter")) {

    					if (isOptionController == true) {
    						ttsPath(15);
    					} else {
    						if (lastPictureTakenUri == null) {
        						ttsPath(98);
        					} else {
        						Toast.makeText(CameraActivity.this, "One moment please.",
        								Toast.LENGTH_SHORT).show();

        						ImageProcess imageProcess = new ImageProcess();
        						Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                        		imageProcess.applyAndSave(this, lastPictureTakenUri, ImageProcess.FILTER_GAUSSIAN);

                        		Log.i(TAG, "Gaussian filter applied and saved");
                        		if (isDescription == true) {
                        			speakText("Gaussian filter has been applied and saved. Would you like to share " +
                            				"this image? To post to Facebook say Facebook or to post to " +
                            				"Twitter say Twitter.");
                        		} else {
                        			speakText("Gaussian filter has been applied and saved.");
                        		}
        					}
    					}

    			} else if (matches.contains("improve") || matches.contains("improve image") ||
    					matches.contains("improve quality") || matches.contains("bright") ||
    					matches.contains("groove") || matches.contains("brighten") ||
    					matches.contains("improved") || matches.contains("proove")) {

    					if (isOptionController == true) {
    						ttsPath(15);
    					} else {
    						if (lastPictureTakenUri.getPath() == null) {
        						ttsPath(98);
        					} else {
        						Toast.makeText(CameraActivity.this, "One moment please.",
        								Toast.LENGTH_SHORT).show();

        						/* perform two filter processes, both brightness correction and sharpen, to
            					 * improve the look of the image(s) */
            					ImageProcess imageProcess = new ImageProcess();
            					isQualityImprove = true; //will result with improved image (1 filter) without save
            					Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
            					imageProcess.applyAndSave(this, lastPictureTakenUri, ImageProcess.FILTER_BRIGHTNESS);

            					isQualityImprove = false; //will result with improved image (2 filters) with save
            					filterUri = ImageProcess.getFilterUri();
            					Log.d(TAG, "filterUri: " + filterUri);
            					imageProcess.applyAndSave(this, filterUri, ImageProcess.FILTER_SHARPEN);

            					Log.i(TAG, "Brightness correction and sharpen filters applied and saved");
            					if (isDescription == true) {
            						speakText("Image has been improved with brightness correction and sharpen filters and " +
                							"saved. Would you like to share this image? To post to Facebook say " +
                							"Facebook or to post to Twitter say Twitter.");
                        		} else {
                        			speakText("Image has been improved with brightness correction and sharpen " +
            							"filters and saved.");
                        		}
        					}
    					}

    			} else if (matches.contains("close app") || matches.contains("close") ||
    					matches.contains("shutdown app") || matches.contains("shutdown") ||
    					matches.contains("exit app") || matches.contains("exit")) {

    					//close application
    					surfaceView.setEnabled(false);
    					camera.stopPreview();
    					isPreview = false;
    					disableVoiceEngine();
    					speakText("Application closing.");
    					Toast.makeText(CameraActivity.this,
								"Application closing.", Toast.LENGTH_SHORT).show();
    					mHandler.postDelayed(new Runnable() {
							public void run() {
								finish();
							}
						}, 1000);

    			} else {
    					//reinitialize voice engine
    					Log.e(TAG, "Ignoring non-commands; normal talking or " +
    							"noise inferred.");

    					//add short delay between reactivating voice engine
    					mHandler.postDelayed(new Runnable() {
							public void run() {
								enableVoiceEngine();
		                		Intent intent = new Intent(CameraActivity.this, Voice_Engine.class);
		            			startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
							}
						}, 1000);
    			}
    		} //end if-statement: requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode != 0
        } else {
        	/* Voice Engine interrupted. Affirm last action and then
        	 * reinitialize TTS based on last action */
        	if (data == null) {
        		Log.i(TAG, "Voice engine interrupted, surfaceView button was pressed");
            	camera.takePicture(myShutterCallback,
                      myPictureCallback_RAW, myPictureCallback_JPG);
        	}
        }
    } //end onActivityForResult()

    /**
    *
    * @param holder
    */
   @Override
   public void surfaceCreated(SurfaceHolder holder)
   {
	   //start application with back camera object
	   backCamera();
   	}

    /**
     * This is called immediately after any structural changes
     * (format or size) have been made to the surface. Resets
     * the preview of what the camera is looking at.
     *
     * @param holder The SurfaceHolder whose surface has changed
     * @param format The new PixelFormat of the surface
     * @param width  The new width of the surface
     * @param height The new height of the surface
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height)
    {
        // TODO Auto-generated method stub
        if(isPreview == true)
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
                if (faceDetectionWrapper == true) {
                	camera.setFaceDetectionListener(new FaceDetection());
                }
                isPreview = true;
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }//end surfaceChanged

    /**
    *
    * @param holder
    */
   @Override
   public void surfaceDestroyed(SurfaceHolder holder)
   {
	   if (camera != null) {
		   camera.stopPreview();

			/* Call release() to release the camera for use by other applications.
			 * Applications should release the camera immediately in onPause()
			 * and re-open() it in onResume() */
	       camera.release();
	       camera = null;
	       isPreview = false;
	   }

   }

    private void frontCamera() {

		if (isSelfie == true) {
			cameraId = findFrontFacingCamera();
			if (cameraId < 0) {
				isSelfie = false;
				speakText("Unable to take selfie picture. Your device has no " +
						"front camera. Please say another command.");
			} else {
				//front camera exists; create new camera object for selfie photos
		    	if (camera != null) {
		    		camera.release();
		    	}

		    	cameraId = 1; //set 0 for back camera, 1 for front camera
				cameraSelf = Camera.open(cameraId);
				paramSelf = cameraSelf.getParameters();

	    		paramSelf.set("camera-id", 2); //set 1 for back camera, 2 for front camera
	    		Camera.Size size = getBestPreviewSize(surfaceView.getWidth(), surfaceView.getHeight(), paramSelf);
	    		paramSelf.setPreviewSize(size.width, size.height);

	    		//continuous auto focus mode intended for taking pictures
	   			paramSelf.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

	   			/* correct for white balance (balance of light/shadows from
	   			 * daylight, shade, twilight, ect */
	   			paramSelf.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);

	   			//allow pictures of fast moving objects
	   			paramSelf.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);

	    		try {
	    			cameraSelf.setPreviewDisplay(surfaceHolder);
	    			cameraSelf.setDisplayOrientation(90);
	   			} catch (Throwable ignored) {
	   				Log.e(TAG, "set preview error: ", ignored);
	   			}

	    		surfaceView.getTop();
	            cameraSelf.setParameters(paramSelf);
	            cameraSelf.startPreview();

	            //do not allow voice engine
            	disableVoiceEngine();

	            speakText("Hold camera still, 3, 2, 1.");

				//give time for preview to update and adjustment of picture
	            mHandler.postDelayed(new Runnable() {
					public void run() {
						//cameraSelf.takePicture(null, null, myPictureCallback_JPG);
						cameraSelf.takePicture(myShutterCallback,
			        			myPictureCallback_RAW, myPictureCallback_JPG);

					}
				}, 3500);

			}
		}
	}

    private void backCamera() {

    	//prepare camera
	    if (cameraSelf != null) {
	    	cameraSelf.release();
	    }

	    cameraId = 0; //set 0 for back camera, 1 for front camera
		camera = Camera.open(cameraId);
		param = camera.getParameters();

		//auto detect if flash is needed or not.
		//Note: front camera does not support flash mode
		if(param.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_AUTO)) {
			param.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
		}

		//setup camera for best photo results according to device surface size
		Camera.Size size = getBestPreviewSize(surfaceView.getWidth(), surfaceView.getHeight(), param);
		param.setPreviewSize(size.width, size.height);
		surfaceView.getTop();

		//continuous auto focus mode intended for taking pictures
		param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

		/* correct for white balance (balance of light/shadows from
		 * daylight, shade, twilight, ect */
		param.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);

		//allow pictures of fast moving objects
		param.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);

		param.set("camera-id", 1); //set 1 for back camera, 2 for front camera

		try {
			camera.setPreviewDisplay(surfaceHolder);
			camera.setDisplayOrientation(90);
		} catch (Throwable ignored) {
			Log.e(TAG, "set preview error: ", ignored);
		}
		camera.setParameters(param);
    }

    private int findFrontFacingCamera() {
    	cameraId = -1;
    	//search for the front facing camera
    	int numberOfCameras = Camera.getNumberOfCameras();
    	for (int i = 0; i<numberOfCameras; i++) {
    		CameraInfo cInfo = new CameraInfo();
    		Camera.getCameraInfo(i, cInfo);
    		if (cInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
    			Log.d(TAG, "Front Camera found");
    			cameraId = i;
    			break;
    		}
    	}
    	return cameraId;
    }


    /**
     *
     * @param width      The new width of the surface
     * @param height     The height of the surface
     * @param parameters Camera parameters
     * @return           The Camera size
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
     * Checks for an active network connection. If there exists
     * no detected network connection or connection is too slow
     * networkWarning() method is called.
     *
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
    				.setMessage("Please connect to the internet.").setCancelable(false);
    		Toast.makeText(CameraActivity.this,
        			"This application requires an internet connection, please connect " +
        			"to the internet now.", Toast.LENGTH_SHORT).show();

    		alertDialog = dialogBuilder.create();
    		alertDialog.show();

    		new CountDownTimer(6000, 1500) {

    			/* The calls to onTick(long) are synchronized to this
    			 * object so that one call to onTick(long) won't ever
    			 * occur before the previous callback is complete */

                /**
                 * Every tick give a beep warning and vibration warning.
                 *
                 * @param millisUntilFinished Time in milliseconds remaining
                 */
     			public void onTick(long millisUntilFinished) {
    				Log.i(TAG, "seconds remaining (1): " + millisUntilFinished / 1000);
    				if (isWarningSound == false) {
    					r.play();
    					v.vibrate(500);
    				}

    		    }

                /**
                 * Check if there is an internet connection. If not keep checking until
                 * the designated timer ends.
                 */
    		    public void onFinish() {
    		    	/* recheck for internet connection every second for
    		    	 * 10 seconds. If no connection is detected after the
    		    	 * allowed 12 seconds close application */
      		    	new CountDownTimer(12000, 1000) {

                        /**
                         * Check if there is a network connection every tick.
                         *
                         * @param millisUntilFinished Time in milliseconds until check
                         *                            is finished
                         */
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

                        /**
                         * When the timer is done it plays a tone and an vibrate.
                         * Ends the alertDialog object.
                         */
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
     * Detects faces in the Camera view according to FaceDetectionListener.
     * All faces detected will be stored into a Face array to be passed to
     * be processed by this method. For one face it will detect if the face
     * is centered. For two faces detects centered if neither face is in the
     * center of the screen. For three or more faces it says multiple faces
     * detected.
     */
    class FaceDetection implements Camera.FaceDetectionListener {
        /**
         * Notify the listener of the detected faces in the preview frame.
         *
         * @param faces   The detected faces in a list
         * @param camera  The Camera service object
         */
        @Override
        public void onFaceDetection(Face[] faces, Camera camera) {

			if (faceDetectionWrapper == true) {
				Log.i(TAG, "Face detection mode activated");

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
				}else if (faces.length == 2) {
                //dimensions for the middle rect
                int vWidth = surfaceView.getWidth();
                int vHeight = surfaceView.getHeight();
                int halfWidth = surfaceView.getRight() / 2;
                int halfHeight = surfaceView.getBottom() / 2;
                //face one
                int l1 = faces[0].rect.left;
                int t1 = faces[0].rect.top;
                int r1 = faces[0].rect.right;
                int b1 = faces[0].rect.bottom;
                int left1 = (l1 + 1000) * vWidth / 2000;
                int top1 = (t1 + 1000) * vHeight / 2000;
                int right1 = (r1 + 1000) * vWidth / 2000;
                int bottom1 = (b1 + 1000) * vHeight / 2000;
                Rect realFaceRect1 = new Rect(left1, top1, right1+50, bottom1);
                Rect middle1 = new Rect(halfWidth - 50, halfHeight - 50,
                        halfWidth + 150, halfHeight + 150);
                //face two
                int l2 = faces[1].rect.left;
                int t2 = faces[1].rect.top;
                int r2 = faces[1].rect.right;
                int b2 = faces[1].rect.bottom;
                int left2 = (l2 + 1000) * vWidth / 2000;
                int top2 = (t2 + 1000) * vHeight / 2000;
                int right2 = (r2 + 1000) * vWidth / 2000;
                int bottom2 = (b2 + 1000) * vHeight / 2000;
                Rect realFaceRect2 = new Rect(left2, top2, right2+50, bottom2);
                Rect middle2 = new Rect(halfWidth - 50, halfHeight - 50,
                        halfWidth + 50, halfHeight + 150);
                //we need two middle rect because .intersects changes middle2
                if ((middle2.intersect(realFaceRect1))&(middle1.intersect(realFaceRect2))) {
                    Log.i(TAG, "People detected: " + faces.length);
                    speakText("two face Centered.");
                }
                speakText("Two people detected");
            }else if (faces.length >= 3) {
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
     * Called when the activity has detected the user's press of the back key.
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
     * Called after onCreate(Bundle) — or after onRestart() when
     * the activity had been stopped, but is now again being displayed
     * to the user. It will be followed by onResume().
     */
    @Override
    public void onStart() {
    	Log.d(TAG, "onStart()");
    	textToSpeech = new TextToSpeech(CameraActivity.this, this);
    	super.onStart();
    }
        /**
        * Called when you are no longer visible to the user.
        */
    @Override
    public void onStop() {
    	Log.d(TAG, "onStop()");
    	if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onStop();
    }//end onStop

    /**
    * Called as part of the activity lifecycle when an activity is going into
    * the background, but has not (yet) been killed.
    */
    @Override
    public void onPause() {
    	Log.d(TAG, "onPause()");
        super.onPause();
    }//end onPause

    /**
     * Called by the system to remove the Service when it is no longer used.
     * Ends textToSpeech and Voice_Engine, as well as calling Activity's
     * onDestroy(). The service should clean up any resources it holds (threads,
     * registered receivers, etc) at this point. Upon return, there will be no
     * more calls in to this Service object and it is effectively dead. Do not
     * call this method directly.
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
