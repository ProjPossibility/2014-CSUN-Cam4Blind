package com.camacc.activity;

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
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
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
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;

import com.camacc.R;
import com.camacc.helper.MessageHelper;
import com.camacc.helper.PreferencesHelper;
import com.camacc.helper.VoiceEngineHelper;
import com.camacc.network.NetworkUtils;
import com.camacc.service.Accessibility_Service;
import com.camacc.service.Voice_Engine;

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
public class CameraActivity extends Activity implements SurfaceHolder.Callback,
        OnInitListener {
    private String TAG = CameraActivity.class.getSimpleName();

    private OrientationEventListener sensorListener;
    private PreferencesHelper prefHelper;

    private int keyEventPos = 0;
    private String strContentUriFile;

    private boolean keyEventLv = false;
    private boolean balanced = false;
    private boolean returnSocialMedia = false;
    private boolean faceDetectionWrapper = false;
    private boolean isPreview = false;
    private boolean isConnected = false;
    private boolean isWarningSound;
    private boolean isOptionController = false;
    private boolean isFilterController = false;
    private boolean isSelfieController = false;
    public static boolean isQualityImprove = false;

    public static Uri lastPictureTakenUri;
    private Uri filterUri;

    private static TextToSpeech textToSpeech;

    private Camera cameraBack; // back camera object
    private Camera cameraFront; // front camera object
    Camera.Parameters paramCamBack;
    Camera.Parameters paramCamFront;

    Size size;
    public static SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private AlertDialog alertDialog;
    private Ringtone r;
    private Vibrator v;

    long timeInMilliseconds;
    private int cameraId = 0;

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    private static HashMap<String, String> map = new HashMap<String, String>();
    private Handler mHandler = new Handler();

    // shared preference variables
    private boolean isFirstLaunch;
    private String sharedPrefFirstLaunch;

    private boolean isVoice;
    private String sharedPrefVoice;

    private boolean isAdvance;
    private String sharedPrefAdvance;

    private boolean isAutoDetect;
    private String sharedPrefAutoDetect;

    /**
     * Checks for a network connection, and initializes the application if a
     * stable connection exists. Called by the system when the service is first
     * created. Do not call this method directly.
     *
     * @param savedInstanceState
     *            If the activity is being re-initialized after previously being
     *            shut down then this Bundle contains the data it most recently
     *            supplied in onSaveInstanceState(Bundle). Otherwise it is null.
     */

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        if(isVoice == false)
        {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (action == KeyEvent.ACTION_UP) {
                        Log.d(TAG, "KEYCODE_VOLUME_UP");
                        // interrupt TTS Engine if active
                        while (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }

                        //perform the actions
                        if (keyEventLv == true) {
                            if (keyEventPos == 0 || keyEventPos == 1) {
                                Log.i(TAG, "(KEYCODE UP) Main options");
                                keyEventLv = false;
                                isSelfieController = false;
                                keyEventPos = 0;
                                backCamera();
                                speakText("Selfie mode deactivated, main options");
                            } else if (keyEventPos == 2) {
                                Log.i(TAG, "(KEYCODE UP) Main options");
                                keyEventLv = false;
                                keyEventPos = 0;
                                speakText("Main options");
                            } else if (keyEventPos == 3) {
                                if (lastPictureTakenUri == null) {
                                    Log.i(TAG, "(KEYCODE UP) No picture taken");
                                    speakText("Unable to post to Twitter, please take a picture first");
                                } else {
                                    Log.i(TAG, "(KEYCODE UP) Main options");
                                    keyEventLv = false;
                                    keyEventPos = 0;
                                    speakText("Main options");
                                }
                            } else if (keyEventPos == 4) {
                                Log.i(TAG, "(KEYCODE UP) Main options");
                                keyEventLv = false;
                                keyEventPos = 0;
                                speakText("Main options");
                            } else if (keyEventPos == 5) {
                                Log.i(TAG, "(KEYCODE UP) Main options");
                                keyEventLv = false;
                                keyEventPos = 0;
                                speakText("Main options");
                            } else if (keyEventPos == 6) {
                                if (lastPictureTakenUri == null) {
                                    Log.i(TAG, "(KEYCODE UP) No picture taken");
                                    speakText("To do this action, please take a picture first");
                                } else {
                                    speakText("One moment please, improving image.");
                                    /*
                                     * perform two filter processes, both brightness
                                     * correction and sharpen, to improve the look of
                                     * the image(s)
                                     */
                                    ImageProcess imageProcess = new ImageProcess();
                                    // will result with improved image (1 filter) without save
                                    isQualityImprove = true;

                                    Log.d(TAG, "lastPictureTakenUri: "
                                            + lastPictureTakenUri);
                                    imageProcess.applyAndSave(this,
                                            lastPictureTakenUri,
                                            ImageProcess.FILTER_BRIGHTNESS);

                                    // will result with improved image (2 filters) with save
                                    isQualityImprove = false;

                                    filterUri = ImageProcess.getFilterUri();
                                    Log.d(TAG, "filterUri: " + filterUri);

                                    // update lastPictureTakenUri
                                    lastPictureTakenUri = filterUri;

                                    imageProcess.applyAndSave(this,
                                            lastPictureTakenUri,
                                            ImageProcess.FILTER_SHARPEN);

                                    speakText("Your image has been improved with brightness " +
                                            "correction and sharpen filters and saved.");
                                }
                            } else if (keyEventPos == 7) {
                                if (lastPictureTakenUri == null) {
                                    Log.i(TAG, "(KEYCODE UP) No picture taken");
                                    speakText("To do this action, please take a picture first");
                                } else {
                                    ImageProcess imageProcess = new ImageProcess();
                                    Log.d(TAG, "lastPictureTakenUri: "
                                            + lastPictureTakenUri);
                                    imageProcess.applyAndSave(this,
                                            lastPictureTakenUri,
                                            ImageProcess.FILTER_SEPIA);

                                    speakText("Sepia filter has been applied and saved.");
                                }
                            } else if (keyEventPos == 8) {
                                if (lastPictureTakenUri == null) {
                                    Log.i(TAG, "(KEYCODE UP) No picture taken");
                                    speakText("To do this action, please take a picture first");
                                } else {
                                    ImageProcess imageProcess = new ImageProcess();
                                    Log.d(TAG, "lastPictureTakenUri: "
                                            + lastPictureTakenUri);
                                    imageProcess.applyAndSave(this,
                                            lastPictureTakenUri,
                                            ImageProcess.FILTER_GRAYSCALE);

                                    speakText("Gray scale filter has been applied and saved.");
                                }
                            } else if (keyEventPos == 9) {
                                if (lastPictureTakenUri == null) {
                                    Log.i(TAG, "(KEYCODE UP) No picture taken");
                                    speakText("To do this action, please take a picture first");
                                } else {
                                    ImageProcess imageProcess = new ImageProcess();
                                    Log.d(TAG, "lastPictureTakenUri: "
                                            + lastPictureTakenUri);
                                    imageProcess.applyAndSave(this,
                                            lastPictureTakenUri,
                                            ImageProcess.FILTER_EMBOSS);

                                    speakText("Emboss filter has been applied and saved.");
                                }
                            } else if (keyEventPos == 10) {
                                if (lastPictureTakenUri == null) {
                                    Log.i(TAG, "(KEYCODE UP) No picture taken");
                                    speakText("To do this action, please take a picture first");
                                } else {
                                    ImageProcess imageProcess = new ImageProcess();
                                    Log.d(TAG, "lastPictureTakenUri: "
                                            + lastPictureTakenUri);
                                    imageProcess.applyAndSave(this,
                                            lastPictureTakenUri,
                                            ImageProcess.FILTER_INVERT);

                                    speakText("Invert filter has been applied and saved.");
                                }
                            } else if (keyEventPos == 11) {
                                if (lastPictureTakenUri == null) {
                                    Log.i(TAG, "(KEYCODE UP) No picture taken");
                                    speakText("To do this action, please take a picture first");
                                } else {
                                    ImageProcess imageProcess = new ImageProcess();
                                    Log.d(TAG, "lastPictureTakenUri: "
                                            + lastPictureTakenUri);
                                    imageProcess.applyAndSave(this,
                                            lastPictureTakenUri,
                                            ImageProcess.FILTER_BLUR);

                                    speakText("Blur filter has been applied and saved.");
                                }
                            } else if (keyEventPos == 12) {
                                if (lastPictureTakenUri == null) {
                                    Log.i(TAG, "(KEYCODE UP) No picture taken");
                                    speakText("To do this action, please take a picture first");
                                } else {
                                    ImageProcess imageProcess = new ImageProcess();
                                    Log.d(TAG, "lastPictureTakenUri: "
                                            + lastPictureTakenUri);
                                    imageProcess.applyAndSave(this,
                                            lastPictureTakenUri,
                                            ImageProcess.FILTER_SHARPEN);

                                    speakText("Sharpen filter has been applied and saved.");
                                }
                            } else if (keyEventPos == 13) {
                                if (lastPictureTakenUri == null) {
                                    Log.i(TAG, "(KEYCODE UP) No picture taken");
                                    speakText("To do this action, please take a picture first");
                                } else {
                                    ImageProcess imageProcess = new ImageProcess();
                                    Log.d(TAG, "lastPictureTakenUri: "
                                            + lastPictureTakenUri);
                                    imageProcess.applyAndSave(this,
                                            lastPictureTakenUri,
                                            ImageProcess.FILTER_MORPH);

                                    speakText("Morph filter has been applied and saved.");
                                }
                            } else if (keyEventPos == 14) {
                                if (lastPictureTakenUri == null) {
                                    Log.i(TAG, "(KEYCODE UP) No picture taken");
                                    speakText("To do this action, please take a picture first");
                                } else {
                                    ImageProcess imageProcess = new ImageProcess();
                                    Log.d(TAG, "lastPictureTakenUri: "
                                            + lastPictureTakenUri);
                                    imageProcess.applyAndSave(this,
                                            lastPictureTakenUri,
                                            ImageProcess.FILTER_BRIGHTNESS);

                                    speakText("Brightness correction filter has been applied and saved.");
                                }
                            } else if (keyEventPos == 15) {
                                if (lastPictureTakenUri == null) {
                                    Log.i(TAG, "(KEYCODE UP) No picture taken");
                                    speakText("To do this action, please take a picture first");
                                } else {
                                    ImageProcess imageProcess = new ImageProcess();
                                    Log.d(TAG, "lastPictureTakenUri: "
                                            + lastPictureTakenUri);
                                    imageProcess.applyAndSave(this,
                                            lastPictureTakenUri,
                                            ImageProcess.FILTER_GAUSSIAN);

                                    speakText("Gaussian filter has been applied and saved.");
                                }
                            } else if (keyEventPos == 16) {
                                Log.i(TAG, "(KEYCODE UP) Main options");
                                keyEventLv = false;
                                keyEventPos = 0;
                                speakText("Main options");
                            } else if (keyEventPos == 17) {
                                if (lastPictureTakenUri == null) {
                                    Log.i(TAG, "(KEYCODE UP) No picture taken");
                                    speakText("Unable to post to Twitter, please take a picture first");
                                }else {
                                    Intent twitterIntent = getShareIntent(
                                            "twitter",
                                            "CamAccc",
                                            "CamAcc is a great photo capturing and " +
                                                    "sharing application aimed for the Blind " +
                                                    "and visually impaired. Check it out!");

                                    if (twitterIntent == null) {
                                        MessageHelper.ttsPath(28); // twitter app not installed
                                    }else {
                                        Log.i(TAG, "Preparing to post to Twitter w/ URI: " + lastPictureTakenUri);
                                        String strUri = lastPictureTakenUri
                                                .toString();
                                        final ContentValues values = new ContentValues(2);
                                        values.clear();
                                        values.put(MediaStore.Images.Media.MIME_TYPE,
                                                "image/*");
                                        values.put(MediaStore.Images.Media.DATA,
                                                strUri);
                                        Uri contentUriFile = getContentResolver()
                                                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                        values);

                                        Log.i(TAG, "(1) contentUriFile: " + contentUriFile);
                                        if (contentUriFile != null) {
                                            strContentUriFile = contentUriFile.toString();
                                        } else {
                                            contentUriFile = Uri.parse(strContentUriFile);
                                        }
                                        Log.i(TAG, "(2) contentUriFile: " + contentUriFile);
                                        try {
                                            returnSocialMedia = true;
                                            final Intent intent = new Intent(
                                                    android.content.Intent.ACTION_SEND);
                                            intent.setPackage("com.twitter.android");
                                            intent.setType("image/*");
                                            intent.putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "Check out this new image taken with #CamAcc! "
                                                            + "Retweet or respond back with comments.");
                                            intent.putExtra(
                                                    android.content.Intent.EXTRA_STREAM,
                                                    contentUriFile);
                                            CameraActivity.this.startActivity(intent);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            } else if (keyEventPos == 18) {
                                if (lastPictureTakenUri == null) {
                                    Log.i(TAG, "(KEYCODE UP) No picture taken");
                                    speakText("Unable to post to Facebook, please take a picture first");
                                } else {
                                    Intent facebookIntent = getShareIntent(
                                            "facebook",
                                            "CamAcc",
                                            "CamAcc is a great photo capturing and " +
                                                    "sharing application aimed for the Blind " +
                                                    "and visually impaired. Check it out!");

                                    if (facebookIntent == null) {
                                        MessageHelper.ttsPath(27); // facebook app not installed
                                    } else {
                                        Log.i(TAG, "Preparing to post to Facebook w/ URI: " + lastPictureTakenUri);
                                        String strUri = lastPictureTakenUri
                                                .toString();
                                        final ContentValues values = new ContentValues(2);
                                        values.put(MediaStore.Images.Media.MIME_TYPE,
                                                "image/*");
                                        values.put(MediaStore.Images.Media.DATA,
                                                strUri);
                                        Uri contentUriFile = getContentResolver()
                                                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                        values);

                                        Log.i(TAG, "(1) contentUriFile: " + contentUriFile);
                                        if (contentUriFile != null) {
                                            strContentUriFile = contentUriFile.toString();
                                        } else {
                                            contentUriFile = Uri.parse(strContentUriFile);
                                        }
                                        Log.i(TAG, "(2) contentUriFile: " + contentUriFile);

                                        try {
                                            returnSocialMedia = true;
                                            final Intent intent = new Intent(
                                                    android.content.Intent.ACTION_SEND);
                                            intent.setPackage("com.facebook.katana");
                                            intent.setType("image/*");
                                            intent.putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "Check out this new image taken with CamAcc!");
                                            intent.putExtra(
                                                    android.content.Intent.EXTRA_STREAM,
                                                    contentUriFile);
                                            CameraActivity.this.startActivity(intent);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            } else if (keyEventPos == 19) {
                                if (lastPictureTakenUri == null) {
                                    Log.i(TAG, "(KEYCODE UP) No picture taken");
                                    speakText("Unable to post to Instegram, please take a picture first");
                                } else {
                                    Intent instagramIntent = getShareIntent(
                                            "Instagram",
                                            "CamAccc",
                                            "CamAcc is a great photo capturing and " +
                                                    "sharing application aimed for the Blind " +
                                                    "and visually impaired. Check it out!");

                                    if (instagramIntent != null) {
                                        MessageHelper.ttsPath(29); // instagram app not installed
                                    } else {
                                        String strUri = lastPictureTakenUri
                                                .toString();
                                        final ContentValues values = new ContentValues(2);
                                        values.put(MediaStore.Images.Media.MIME_TYPE,
                                                "image/*");
                                        values.put(MediaStore.Images.Media.DATA,
                                                strUri);
                                        Uri contentUriFile = getContentResolver()
                                                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                        values);

                                        Log.i(TAG, "(1) contentUriFile: " + contentUriFile);
                                        if (contentUriFile != null) {
                                            strContentUriFile = contentUriFile.toString();
                                        } else {
                                            contentUriFile = Uri.parse(strContentUriFile);
                                        }
                                        Log.i(TAG, "(2) contentUriFile: " + contentUriFile);

                                        try {
                                            returnSocialMedia = true;
                                            final Intent intent = new Intent(
                                                    android.content.Intent.ACTION_SEND);
                                            intent.setPackage("com.instagram.android");
                                            intent.setType("image/*");
                                            intent.putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "Check out this new image taken with CamAcc!");
                                            intent.putExtra(
                                                    android.content.Intent.EXTRA_STREAM,
                                                    contentUriFile);
                                            CameraActivity.this.startActivity(intent);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            } else if (keyEventPos == 20) {
                                Log.i(TAG, "(KEYCODE UP) Main options");
                                keyEventLv = false;
                                keyEventPos = 0;
                                speakText("Main options");
                            } else if (keyEventPos == 21) {
                                Log.i(TAG, "(KEYCODE UP) Advance mode toggle");
                                if (isAdvance == true) {
                                    prefHelper.savePrefAdvance("strAdvance", false);
                                    isAdvance = !isAdvance;
                                    speakText("Advance mode is now turned off");
                                } else {
                                    isAdvance = !isAdvance;
                                    prefHelper.savePrefAdvance("strAdvance", true);
                                    speakText("Advance mode is now turned on");
                                }
                                Log.d(TAG, "isAdvance(" + isAdvance + ")" + " " +
                                        prefHelper
                                                .getPrefAdvance(sharedPrefAdvance));
                            } else if (keyEventPos == 22) {
                                Log.i(TAG, "(KEYCODE UP) Voice mode toggle");
                                isNetWorkConnection();
                                if (isConnected == true) {
                                    prefHelper.savePrefVoice("strVoice", true);
                                    enableVoiceEngine();
                                    isVoice = !isVoice;
                                    if (isFirstLaunch == false) {
                                        prefHelper.savePrefFirstLaunch("strFirstLaunch", true);
                                        isFirstLaunch = true;
                                        MessageHelper.ttsPath(0);
                                    } else {
                                        if (isAdvance == true) {
                                            MessageHelper.ttsPath(99);
                                        } else if (isAdvance == false) {
                                            MessageHelper.ttsPath(4);
                                        }
                                    }
                                } else {
                                    // no connection or connection is not strong enough
                                    Log.e(TAG, "No internet connection");
                                    networkWarning();
                                }
                                Log.d(TAG, "isVoice(" + isVoice + ")" + " " +
                                        prefHelper
                                                .getPrefVoice(sharedPrefVoice));
                            } else if (keyEventPos == 23) {
                            	Log.i(TAG, "(KEYCODE UP) Main options");

//                                Log.i(TAG, "(KEYCODE UP) Auto detect toggle");
//                                if (isAutoDetect == true) {
//                                    prefHelper.savePrefAutoDetect("strAutoDetect", false);
//                                    isAutoDetect = !isAutoDetect;
//                                    speakText("Auto detection is now turned off");
//                                } else {
//                                    isAutoDetect = !isAutoDetect;
//                                    prefHelper.savePrefAutoDetect("strAutoDetect", true);
//                                    speakText("Auto detection is now turned on");
//                                }
//                                Log.d(TAG, "isAutoDetect(" + isAutoDetect + ")" + " " +
//                                        prefHelper
//                                                .getPrefAutoDetect(sharedPrefAutoDetect));
                            } else if (keyEventPos == 24) {
                                Log.i(TAG, "(KEYCODE UP) Main options");
                                keyEventLv = false;
                                keyEventPos = 0;
                                speakText("Main options");
                            } else if (keyEventPos == 25) {
                                Log.i(TAG, "(KEYCODE UP) Main options");
                                keyEventLv = false;
                                isSelfieController = false;
                                keyEventPos = 0;
                                backCamera();
                                speakText("Selfie mode deactivated, main options");
                            }
                        } else {
                            // first pressed UP, from main menu options
                            if (keyEventPos == 0 || keyEventPos == 1) {
                                keyEventLv = true;
                                isSelfieController = true;
                                frontCamera();
                                speakText("Selfie mode active");
                            } else if (keyEventPos == 2) {
                                keyEventLv = true;
                                isFilterController = true;
                                speakText("Select filter to apply");
                            } else if (keyEventPos == 3) {
                                keyEventLv = true;
                                speakText("Select where to post your image");
                            } else if (keyEventPos == 4) {
                                keyEventLv = true;
                                speakText("Select what options to change");
                            } else if (keyEventPos == 5) {
                                speakText("Application closing");
                                mHandler.postDelayed(new Runnable() {
                                    public void run() {
                                        finish();
                                    }
                                }, 1100);
                            }
                        }
                    }
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (action == KeyEvent.ACTION_DOWN) {
                        Log.d(TAG, "KEYCODE_VOLUME_DOWN");
                        // interrupt TTS Engine if active
                        while (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        if (keyEventLv == true) {
                            if (keyEventPos == 0 || keyEventPos == 1) {
                                Log.i(TAG, "(KEYCODE DOWN) Exit selfie mode");
                                keyEventPos = 25;
                                speakText("Exit selfie mode");
                            } else if (keyEventPos == 2) {
                                Log.i(TAG, "(KEYCODE DOWN) No filter");
                                keyEventPos = 5;
                                speakText("No filter");
                            } else if (keyEventPos == 3) {
                                Log.i(TAG, "(KEYCODE DOWN) Twitter");
                                keyEventPos = 17;
                                speakText("Twitter");
                            } else if (keyEventPos == 4) {
                                Log.i(TAG, "(KEYCODE DOWN) Advance mode toggle");
                                keyEventPos = 21;
                                if (isAdvance == true) {
                                    speakText("Advance mode is currently turned on");
                                } else {
                                    speakText("Advance mode is currently turned off");
                                }
                                Log.d(TAG, "isAdvance(" + isAdvance + ")" + " " +
                                        prefHelper
                                                .getPrefAdvance(sharedPrefAdvance));
                            } else if (keyEventPos == 5) { //sub menus filters begin
                                Log.i(TAG, "(KEYCODE DOWN) Improve quality");
                                keyEventPos = 6;
                                speakText("Improve quality");
                            } else if (keyEventPos == 6) {
                                Log.i(TAG, "(KEYCODE DOWN) Sepia");
                                keyEventPos = 7;
                                speakText("Sepia");
                            } else if (keyEventPos == 7) {
                                Log.i(TAG, "(KEYCODE DOWN) Grayscale");
                                keyEventPos = 8;
                                speakText("Grayscale");
                            } else if (keyEventPos == 8) {
                                Log.i(TAG, "(KEYCODE DOWN) Emboss");
                                keyEventPos = 9;
                                speakText("Emboss");
                            } else if (keyEventPos == 9) {
                                Log.i(TAG, "(KEYCODE DOWN) Invert");
                                keyEventPos = 10;
                                speakText("Invert");
                            } else if (keyEventPos == 10) {
                                Log.i(TAG, "(KEYCODE DOWN) Blur");
                                keyEventPos = 11;
                                speakText("Blur");
                            } else if (keyEventPos == 11) {
                                Log.i(TAG, "(KEYCODE DOWN) Sharpen");
                                keyEventPos = 12;
                                speakText("Sharpen");
                            } else if (keyEventPos == 12) {
                                Log.i(TAG, "(KEYCODE DOWN) Morph");
                                keyEventPos = 13;
                                speakText("Morph");
                            } else if (keyEventPos == 13) {
                                Log.i(TAG, "(KEYCODE DOWN) Brightness correction");
                                keyEventPos = 14;
                                speakText("Brightness correction");
                            } else if (keyEventPos == 14) {
                                Log.i(TAG, "(KEYCODE DOWN) Gaussian blur");
                                keyEventPos = 15;
                                speakText("Gaussian blur");
                            } else if (keyEventPos == 15) {
                                Log.i(TAG, "(KEYCODE DOWN) Exit filter sub menu");
                                keyEventPos = 16;
                                speakText("Exit filter sub menu");
                            } else if (keyEventPos == 16) { //sub menus filters end
                                Log.i(TAG, "(KEYCODE DOWN) No filter");
                                keyEventPos = 5;
                                speakText("No filter");
                            } else if (keyEventPos == 17) { //sub menus social media begin
                                Log.i(TAG, "(KEYCODE DOWN) Facebook");
                                keyEventPos = 18;
                                speakText("Facebook");
                            } else if (keyEventPos == 18) {
                                Log.i(TAG, "(KEYCODE DOWN) Instagram");
                                keyEventPos = 19;
                                speakText("Instegram");
                            } else if (keyEventPos == 19) {
                                Log.i(TAG, "(KEYCODE DOWN) Exit social media sub menu");
                                keyEventPos = 20;
                                speakText("Exit social media sub menu");
                            } else if (keyEventPos == 20) { //sub menus social media end
                                Log.i(TAG, "(KEYCODE DOWN) Twitter");
                                keyEventPos = 17;
                                speakText("Twitter");
                            } else if (keyEventPos == 21) { //sub menus options begin
                                Log.i(TAG, "(KEYCODE DOWN) Voice mode toggle");
                                keyEventPos = 22; 
                                if (isVoice == true) {
                                    speakText("Communication mode is currently turned on");
                                } else {
                                    speakText("Communication mode is currently turned off");
                                }
                                Log.d(TAG, "isVoice(" + isVoice + ")" + " " +
                                        prefHelper
                                                .getPrefVoice(sharedPrefVoice));
                            } else if (keyEventPos == 22) {
                                Log.i(TAG, "(KEYCODE DOWN) Auto detect toggle");
                                keyEventPos = 24; // skipping auto detect 23
                                speakText("Exit options sub menu");
//                                if (isAutoDetect == true) {
//                                    speakText("Auto detection is currently turned on");
//                                } else {
//                                    speakText("Auto detection is currently turned off");
//                                }
                                Log.d(TAG, "isAutoDetect(" + isAutoDetect + ")" + " " +
                                        prefHelper
                                                .getPrefAutoDetect(sharedPrefAutoDetect));
                            }else if (keyEventPos == 23) {
                                Log.i(TAG, "(KEYCODE DOWN) Exit options sub menu");
                                keyEventPos = 24;
                                speakText("Exit options sub menu");
                            } else if (keyEventPos == 24) { //sub menus options end
                                Log.i(TAG, "(KEYCODE DOWN) Advance mode toggle");
                                keyEventPos = 21;
                                if (isAdvance == true) {
                                    speakText("Advance mode is currently turned on");
                                } else {
                                    speakText("Advance mode is currently turned off");
                                }
                                Log.d(TAG, "isAdvance(" + isAdvance + ")" + " " +
                                        prefHelper
                                                .getPrefAdvance(sharedPrefAdvance));
                            } else if (keyEventPos == 25) { //sub menu selfie begin/end
                                Log.i(TAG, "(KEYCODE DOWN) Exit selfie mode");
                                speakText("Exit selfie mode");
                            }
                        } else {
                            if (keyEventPos == 0) {
                                Log.i(TAG, "(KEYCODE DOWN) Selfie");
                                speakText("Selfie");
                                keyEventPos = 1;
                            } else if (keyEventPos == 1) {
                                Log.i(TAG, "(KEYCODE DOWN) Filter");
                                speakText("Filter");
                                keyEventPos = 2;
                            } else if (keyEventPos == 2) {
                                Log.i(TAG, "(KEYCODE DOWN) Social media");
                                speakText("Social media");
                                keyEventPos = 3;
                            } else if (keyEventPos == 3) {
                                Log.i(TAG, "(KEYCODE DOWN) Options");
                                speakText("Options");
                                keyEventPos = 4;
                            } else if (keyEventPos == 4) {
                                Log.i(TAG, "(KEYCODE DOWN) Close application");
                                speakText("Close application");
                                keyEventPos = 5;
                            } else if (keyEventPos == 5) {
                                Log.i(TAG, "(KEYCODE DOWN) Selfie");
                                speakText("Selfie");
                                keyEventPos = 1;
                            }
                        }

                    }
                    return true;
                default:
                    return super.dispatchKeyEvent(event);
            }
        }
        else
        {
            return super.dispatchKeyEvent(event);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);
        Log.d(TAG, "Main layout loaded successfully");

        Intent ini = new Intent(this, Accessibility_Service.class);
        startService(ini);

        // check options stored in sharedPreferences
        prefHelper = new PreferencesHelper(getApplicationContext());

        isFirstLaunch = prefHelper
                .getPrefFirstLaunch(sharedPrefFirstLaunch);
        isVoice = prefHelper
                .getPrefVoice(sharedPrefVoice);
        isAdvance = prefHelper
                .getPrefAdvance(sharedPrefAdvance);
        isAutoDetect = prefHelper
                .getPrefAutoDetect(sharedPrefAutoDetect);

        Log.i(TAG, "isFirstLaunch(" + isFirstLaunch + ")"
                + " // isVoice(" + isVoice + ")"
                + " // isAdvance(" + isAdvance + ")"
                + " // isAutoDetect(" + isAutoDetect + ")");

        // initialize TTS Engine
        textToSpeech = new TextToSpeech(CameraActivity.this, this);
        textToSpeech.setLanguage(Locale.US);
        textToSpeech.setPitch(8 / 10);
        textToSpeech.setSpeechRate(15 / 12);

        surfaceView = (SurfaceView) findViewById(R.id.camera_preview);
        surfaceView.setEnabled(true);
        surfaceView.setOnClickListener(new SurfaceView.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.d(TAG, "surface button clicked");
                // disable face detection
                if (isAutoDetect == false) {
                    if (faceDetectionWrapper == true) {
                        if (isSelfieController == true) {
                            cameraFront.stopFaceDetection();
                        } else {
                            cameraBack.stopFaceDetection();
                        }
                        faceDetectionWrapper = !faceDetectionWrapper;
                    }
                }

                if (isSelfieController == true) {
                    // take picture and store image
                    cameraFront.takePicture(myShutterCallback,
                            myPictureCallback_RAW, myPictureCallback_JPG);
                } else {
                    // take picture and store image
                    cameraBack.takePicture(myShutterCallback,
                            myPictureCallback_RAW, myPictureCallback_JPG);
                }

            }
        });

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        /**
         * Listens for the orientation of the phone.
         */
        sensorListener = new OrientationEventListener(CameraActivity.this,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {

                // holding phone right side up
                if ((orientation >= 0) && (orientation <= 10)
                        || ((orientation >= 80) && (orientation <= 95))
                        || ((orientation >= 170) && (orientation <= 185))
                        || ((orientation >= 260) && (orientation <= 275))
                        || ((orientation >= 350) && (orientation <= 360))) {

                    balanced = true;
					/*Log.e(TAG, "orientation: " + orientation +
							" // balanced: " + balanced);*/
                } else {
                    balanced = false;
					/*Log.e(TAG, "orientation: " + orientation +
							" // balanced: " + balanced);*/
                }
                /*
                    Rotates the camera as the phone orientation changes to ensure that the picture is saved
                    with the right orientation every time.
                 */

                if (orientation == ORIENTATION_UNKNOWN) return;
                android.hardware.Camera.CameraInfo info =
                        new android.hardware.Camera.CameraInfo();
                android.hardware.Camera.getCameraInfo(cameraId, info);
                orientation = (orientation + 45) / 90 * 90;
                int rotation = 0;
                if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                    rotation = (info.orientation - orientation + 360) % 360;
                } else {  // back-facing camera

                    rotation = (info.orientation + orientation) % 360;
                }
                if ((cameraFront != null)&&
                        (isSelfieController == true)&&
                        (paramCamFront.flatten().contains(("rotation="+rotation))) == false)
                {
                    paramCamFront.setRotation(rotation);
                    cameraFront.setParameters(paramCamFront);
                }
                else if((cameraBack != null)&&
                        (isSelfieController == false)&&
                        (paramCamBack.flatten().contains("rotation="+rotation)) == false)
                {
                    paramCamBack.setRotation(rotation);
                    cameraBack.setParameters(paramCamBack);
                }
            }
        };

        if (isVoice == true) {
            Log.i(TAG, "Starting communication mode");
            isNetWorkConnection();
            if (isConnected == false) {
                // no connection or connection is not strong enough
                surfaceView.setEnabled(false);
                Log.e(TAG, "No internet connection");
                networkWarning();
            }
        }

        if (isVoice == true) {
            if (isConnected == true) {
                // give time for TTS to initialize
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        if (isAdvance == true) {
                            MessageHelper.ttsPath(99);
                        } else if (isAdvance == false) {
                            MessageHelper.ttsPath(4);
                        }
                    }
                }, 700);
            }
        }
    } // end onCreate

    public static void speakText(String text) {
        if (textToSpeech.isSpeaking()) {
            return;
        } else {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, map);
        }
    }

    @Override
    public void onInit(int status) {
        Log.e(TAG, "ONINIT(): Text-to-speech initialized!");

        if (status == TextToSpeech.SUCCESS) {

            textToSpeech
                    .setOnUtteranceProgressListener(new UtteranceProgressListener() {

                        @Override
                        public void onStart(String utteranceId) {
                            // TODO Auto-generated method stub
                            Log.d(TAG, "onStart(String utteranceId): " + utteranceId);
                        }

                        @Override
                        public void onError(String utteranceId) {
                            // TODO Auto-generated method stub
                            Log.e(TAG, "onError(String utteranceId): " + utteranceId);
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            // TODO Auto-generated method stub
                            Log.d(TAG, "onDone(String utteranceId): "+utteranceId);

                            if (isVoice == true) {
                                isNetWorkConnection();
                                if (isConnected == true) {
									/* while in communication mode, do not allow
									 * voice recognition if face detection is active
									 */
                                    //stops the voice engine from starting when the onDone is called
                                    if ((faceDetectionWrapper == false)&&
                                            (VoiceEngineHelper.getVoiceController() == false)) {
                                        Log.d(TAG, "Starting Voice_Engine");
                                        Intent intent = new Intent(
                                                CameraActivity.this, Voice_Engine.class);
                                        startActivityForResult(intent,
                                                VOICE_RECOGNITION_REQUEST_CODE);
                                    }
                                }
                            }
                        }
                    }); // end OnUtteranceProgressListener()

            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");

			/*
			 * booleans assigned from sharedPreferences are used to setup
			 * different messages depending upon the options the user has
			 * saved/setup
			 */
        } else if (status == TextToSpeech.ERROR) {
            // initialization of TTS failed so reinitialize new TTS Engine
            Log.e(TAG, "TextToSpeech ERROR");
            textToSpeech = new TextToSpeech(CameraActivity.this, this);
            textToSpeech.setLanguage(Locale.US);
            textToSpeech.setPitch(8/10);
            textToSpeech.setSpeechRate(15/12);
        }
        /**
         * Prompts the user for a command when the app is restarted
         */
        if(isVoice == true){
            if(isOptionController == true ){
                isOptionController = false;
            }
            if(VoiceEngineHelper.getVoiceController() == true){
                enableVoiceEngine();
            }
            MessageHelper.ttsPath(4);
        }

    } // end onInit() method

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated(SurfaceHolder holder)");
        // start application with back camera object
        if(isSelfieController == true)
        {
            frontCamera();
        }
        else
        {
            backCamera();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
        if (isPreview == true) {
            cameraBack.stopPreview();
            isPreview = false;
        }

        if (cameraBack != null) {
            try {
				/*
				 * Must pass a fully initialized SurfaceHolder to
				 * setPreviewDisplay(SurfaceHolder). Without a surface, the
				 * camera will be unable to start the preview
				 */
                cameraBack.setPreviewDisplay(surfaceHolder);
                cameraBack.setDisplayOrientation(90);
				/*
				 * Call startPreview() to start updating the preview surface.
				 * Preview must be started before you can take a picture
				 */
                cameraBack.startPreview();
                isPreview = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (cameraBack != null) {
            cameraBack.stopPreview();

			/*
			 * Call release() to release the camera for use by other
			 * applications. Applications should release the camera immediately
			 * in onPause() and re-open() it in onResume()
			 */
            cameraBack.release();
            cameraBack = null;
            isPreview = false;
        }
        if (cameraFront != null) {
            cameraFront.stopPreview();

			/*
			 * Call release() to release the camera for use by other
			 * applications. Applications should release the camera immediately
			 * in onPause() and re-open() it in onResume()
			 */
            cameraFront.release();
            cameraBack = null;
        }

    }

    AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onAutoFocus()");
        }
    };

    ShutterCallback myShutterCallback = new ShutterCallback() {

        @Override
        public void onShutter() {
            // TODO Auto-generated method stub
            Log.d(TAG, "onShutter()");

        }
    };

    PictureCallback myPictureCallback_RAW = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onPictureTaken()");
        }
    };

    PictureCallback myPictureCallback_JPG = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            // TODO Auto-generated method stub
            //
            // saving picture; make sure surface view is unclickable
            surfaceView.setEnabled(false);

            if (isVoice == true) {
                // disable voice engine
                disableVoiceEngine();
            }

            // interrupt TTS Engine if active
            while (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }

            File imgFileDir = getDir();
            if (!imgFileDir.exists() && !imgFileDir.mkdirs()) {
                Log.e(TAG, "Directory does not exist");
            }

            // Locale.US to get local formatting
            SimpleDateFormat timeFormat = new SimpleDateFormat("hhmmss",
                    Locale.US);
            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy",
                    Locale.US);
            String time = timeFormat.format(new Date());
            String date = dateFormat.format(new Date());
            String photoFile = date + "_camacc_" + time + ".jpg";
            String filename = imgFileDir.getPath() + File.separator + photoFile;

            Log.i(TAG, "time(hhmmss): " + time + " date(ddmmyyyy): " + date + "\nfilename: "
                    + filename + " photoFile: " + photoFile);

            File pictureFile = new File(filename);

            try {
                BufferedOutputStream fos = new BufferedOutputStream(
                        new FileOutputStream(pictureFile));

                fos.write(arg0);
                fos.flush();
                fos.close();
                Log.i(TAG, "Image saved: " + pictureFile.toString());

            } catch (Exception e) {
                Log.e(TAG, "Image could not be saved");
                e.printStackTrace();
            }

            lastPictureTakenUri = Uri.parse(filename);

//			// setup back camera if selfie & voice modes are active
//			if (isSelfie == true && isVoice == true) {
//				backCamera();
//			}

            // allow surface view camera
            surfaceView.setEnabled(true);

            // enable voice engine
            if (isVoice == true) {
                enableVoiceEngine();
            }

            // start camera preview again
            if (isSelfieController == true) {
                cameraFront.startPreview();
            } else {
                cameraBack.startPreview();
            }

			/* taking a picture while in communication mode resets
			 * the options so that end user can give commands again.
			 * This reset needs to be reflected on volume controls.
			 */
            
            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (isVoice == true) {
		                keyEventPos = 0;
		                if (isSelfieController == true) {
		                    MessageHelper.ttsPath(18);
		                } else {
		                    if (isAdvance == true) {
		                        MessageHelper.ttsPath(1);
		                    } else {
		                        MessageHelper.ttsPath(22);
		                    }
		                }
		            } else {
		                if (isSelfieController == true) {
		                    MessageHelper.ttsPath(26);
		                } else {
		                    MessageHelper.ttsPath(5);
		                }
		            }
				}	
            }, 500);
            
//            if (isVoice == true) {
//                keyEventPos = 0;
//                if (isSelfieController == true) {
//                    MessageHelper.ttsPath(18);
//                } else {
//                    if (isAdvance == true) {
//                        MessageHelper.ttsPath(1);
//                    } else {
//                        MessageHelper.ttsPath(22);
//                    }
//                }
//            } else {
//                if (isSelfieController == true) {
//                    MessageHelper.ttsPath(26);
//                } else {
//                    MessageHelper.ttsPath(5);
//                }
//            }

        }
    };

    private File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "Camacc");
    }

    private void backCamera() {

        // release front camera if instantiated
        if (cameraFront != null) {
            cameraFront.stopPreview();
            cameraFront.release();
            cameraFront=null;
        }

        cameraId = 0; // set 0 for back camera, 1 for front camera
        cameraBack = Camera.open(cameraId);
        paramCamBack = cameraBack.getParameters();

        // setup camera for best photo results according to device surface size
        Camera.Size size = getBestPreviewSize(surfaceView.getWidth(),
                surfaceView.getHeight(), paramCamBack);
        paramCamBack.setPreviewSize(size.width, size.height);
        surfaceView.getTop();

        // auto detect if flash is needed or not.
        // Note: front camera does not support flash mode
        if ((paramCamBack.getSupportedFlashModes() != null)&&
                (paramCamBack.getSupportedFlashModes().contains(
                        Camera.Parameters.FLASH_MODE_AUTO))) {
            paramCamBack.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        }

        // continuous auto focus mode intended for taking pictures
        if (paramCamBack.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            paramCamBack.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

		/*
		 * correct for white balance (balance of light/shadows from daylight,
		 * shade, twilight, ect
		 */
        if ((paramCamBack.getSupportedWhiteBalance() != null)&&
                (paramCamBack.getSupportedWhiteBalance().contains(Camera.Parameters.WHITE_BALANCE_AUTO))) {
            paramCamBack.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        }

        // allow pictures of fast moving objects
        if ((paramCamBack.getSupportedSceneModes() != null)&&
                (paramCamBack.getSupportedSceneModes().contains(Camera.Parameters.SCENE_MODE_AUTO))) {
            paramCamBack.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        }

        // set quality to 100
        paramCamBack.setExposureCompensation(0);
        paramCamBack.setJpegQuality(100);
        paramCamBack.setJpegThumbnailQuality(100);


        paramCamBack.set("camera-id", 1); // set 1 for back camera, 2 for front camera

        try {
            cameraBack.setPreviewDisplay(surfaceHolder);
            cameraBack.setDisplayOrientation(90);
        } catch (Throwable ignored) {
            Log.e(TAG, "set preview (back camera) error: ", ignored);
        }
        cameraBack.setParameters(paramCamBack);
        cameraBack.startPreview();
    }

    private void frontCamera() {

        if (isSelfieController == true) {
            cameraId = findFrontFacingCamera();
            if (cameraId < 0) {
                isSelfieController = false;
                speakText("Cannot perform action. No front camera detected");
            } else {
                // release back camera if instantiated
                if (cameraBack != null) {
                    cameraBack.startPreview();
                    cameraBack.release();
                    cameraBack=null;
                    isPreview = false;
                }

                // update volume button controls
                if (isVoice == true) {
                    keyEventPos = 26;
                }

                cameraId = 1; // set 0 for back camera, 1 for front camera
                cameraFront = Camera.open(cameraId);
                paramCamFront = cameraFront.getParameters();

                // set 1 for back camera, 2 for front camera
                paramCamFront.set("camera-id", 2);

                Camera.Size size = getBestPreviewSize(surfaceView.getWidth(),
                        surfaceView.getHeight(), paramCamFront);
                paramCamFront.setPreviewSize(size.width, size.height);

                // continuous auto focus mode intended for taking pictures
                if (paramCamFront.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
                {
                    paramCamFront.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }

				/*
				 * correct for white balance (balance of light/shadows from daylight,
				 * shade, twilight, ect
				 */
                if ((paramCamFront.getSupportedWhiteBalance() != null)&&
                        (paramCamFront.getSupportedWhiteBalance().contains(Camera.Parameters.WHITE_BALANCE_AUTO)))
                {
                    paramCamFront.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
                }

                // allow pictures of fast moving objects
                if ((paramCamFront.getSupportedSceneModes() != null)&&
                        (paramCamFront.getSupportedSceneModes().contains(Camera.Parameters.SCENE_MODE_AUTO)))
                {
                    paramCamFront.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
                }

                // set quality to 100
                paramCamBack.setExposureCompensation(0);
                paramCamFront.setJpegQuality(100);
                paramCamFront.setJpegThumbnailQuality(100);

                try {
                    cameraFront.setPreviewDisplay(surfaceHolder);
                    cameraFront.setDisplayOrientation(90);
                } catch (Throwable ignored) {
                    Log.e(TAG, "set preview error: ", ignored);
                }

                surfaceView.getTop();
                cameraFront.setParameters(paramCamFront);
                cameraFront.startPreview();
            }
        }
    }

    private int findFrontFacingCamera() {
        cameraId = -1;
        // search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo cInfo = new CameraInfo();
            Camera.getCameraInfo(i, cInfo);
            if (cInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
                Log.i(TAG, "Front Camera exists");
                cameraId = i;
                break;
            }
        }
        return cameraId;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "requestCode: " + requestCode + "\nresultCode: "
                + resultCode + "\ndata: " + data);

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
                ArrayList<String> matches = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                int listSize = matches.size();

                // compile list of recognized words by Voice_Engine class
                for (int i = 0; i < listSize; i++) {
                    Log.i(TAG, "Recognized words: " + matches.get(i));
                }

                // command set 1: TAKE PICTURE
                if(matches.contains("take picture")
                        || matches.contains("picture")
                        || matches.contains("take photo")
                        || matches.contains("photo")
                        || matches.contains("foto")
                        || matches.contains("take foto")) {

                    if (isFilterController == true) {
                        isFilterController = !isFilterController;
                    }

                    if (isOptionController == true) {
                        isOptionController = !isOptionController;
                    }

                    if (isSelfieController == true) {
                        cameraFront.takePicture(myShutterCallback,
                                myPictureCallback_RAW, myPictureCallback_JPG);
                    } else {
                        cameraBack.takePicture(myShutterCallback,
                                myPictureCallback_RAW, myPictureCallback_JPG);
                    }

                    // command set 2: SELFIE MODE
                } else if (matches.contains("selfie")
                        || matches.contains("self")
                        || matches.contains("sophia")
                        || matches.contains("take selfie")
                        || matches.contains("front camera")) {

                    if (isSelfieController == true) {
                        speakText("Selfie mode is already active. To exit selfie mode say Exit");
                    } else {
                        if (isFilterController == true) {
                            isFilterController = !isFilterController;
                        }

                        if (isOptionController == true) {
                            isOptionController = !isOptionController;
                        }

                        isSelfieController = true;
                        speakText("Selfie mode active");
                        frontCamera();
                    }

                    // command set 3: DETECTION	MODE
                } else if (matches.contains("detect")
                        || matches.contains("detection")
                        || matches.contains("start detection")
                        || matches.contains("recoginition")
                        || matches.contains("start recognition")
                        || matches.contains("face")
                        || matches.contains("face detection")
                        || matches.contains("face recognition")) {

                    if (isFilterController == true) {
                        isFilterController = !isFilterController;
                    }

                    if (isOptionController == true) {
                        isOptionController = !isOptionController;
                    }

                    // do not allow voice engine
                    disableVoiceEngine();

                    // allow taking picture via surface button
                    surfaceView.setEnabled(true);
                    faceDetectionWrapper = true;

                    if (isSelfieController == true) {
                        cameraFront.startFaceDetection();
                        cameraFront.setFaceDetectionListener(new FaceDetection());
                    } else {
                        cameraBack.startFaceDetection();
                        cameraBack.setFaceDetectionListener(new FaceDetection());
                    }

                    // command set 4: FILTER MODE
                } else if (matches.contains("filter")
                        || matches.contains("filters")
                        || matches.contains("apply filter")) {

                    if (lastPictureTakenUri == null) {
                        MessageHelper.ttsPath(17);
                    } else {
                        if (isFilterController == false) {
                            // update volume button controls
                            keyEventPos = 5;

                            isFilterController = !isFilterController;
                            if (isAdvance == true) {
                                MessageHelper.ttsPath(2);
                            } else {
                                MessageHelper.ttsPath(6);
                            }
                        } else { // already in filter mode
                            MessageHelper.ttsPath(16);
                        }
                    }

                    // command set 5: HELP
                } else if (matches.contains("help")) {

                    if (isFilterController == true) {
                        speakText("The filter commands are: Improve Quality, Sepia, "
                                + "Gray Scale, Emboss, Invert, Blur, Sharpen, Morph, "
                                + "Brightness Correction");
                    } else if (isOptionController == true) {
                        if (isAdvance == true) {
                            speakText("Options allow you to disable and enable a variety of "
                                    + "features. Your current settings are: advanced mode "
                                    + "on, communication mode on, auto detection off, ");
                        } else { // advance mode is OFF
                            speakText("Options allow you to disable and enable a variety of "
                                    + "features. Your current settings are: advanced mode "
                                    + "off, communication mode on, auto detection off, ");
                        }
                    } else { // not in option mode
                        speakText("The main level commands are: Picture, Selfie, Detection, "
                                + "Options or Close App to exit application. Once you take a "
                                + "picture your sub level commands are: Filter, Twitter, "
                                + "Facebook, Instregram");
                    }

                    // command set 6: SOCIAL MEDIA FACEBOOK
                } else if (matches.contains("Facebook")
                        || matches.contains("facebook")
                        || matches.contains("face book")) {

                    if (lastPictureTakenUri == null) {
                        MessageHelper.ttsPath(17);
                    } else {
                        Intent facebookIntent = getShareIntent(
                                "facebook",
                                "CamAcc",
                                "CamAcc is a great photo capturing and " +
                                        "sharing application aimed for the Blind " +
                                        "and visually impaired. Check it out!");

                        if (facebookIntent == null) {
                            MessageHelper.ttsPath(23); // facebook app not installed
                        } else {
                            Log.i(TAG, "Preparing to post to Facebook");
                            String strUri = lastPictureTakenUri
                                    .toString();
                            final ContentValues values = new ContentValues(2);
                            values.put(MediaStore.Images.Media.MIME_TYPE,
                                    "image/*");
                            values.put(MediaStore.Images.Media.DATA,
                                    strUri);
                            Uri contentUriFile = getContentResolver()
                                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                            values);

                            Log.i(TAG, "(1) contentUriFile: " + contentUriFile);
                            if (contentUriFile != null) {
                                strContentUriFile = contentUriFile.toString();
                            } else {
                                contentUriFile = Uri.parse(strContentUriFile);
                            }
                            Log.i(TAG, "(2) contentUriFile: " + contentUriFile);

                            try {
                                returnSocialMedia = true;
                                final Intent intent = new Intent(
                                        android.content.Intent.ACTION_SEND);
                                intent.setPackage("com.facebook.katana");
                                intent.setType("image/*");
                                intent.putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Check out this new image taken with CamAcc!");
                                intent.putExtra(
                                        android.content.Intent.EXTRA_STREAM,
                                        contentUriFile);
                                CameraActivity.this.startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    // command set 7: SOCIAL MEDIA TWITTER
                } else if (matches.contains("Twitter")
                        || matches.contains("twitter")
                        || matches.contains("tweet")) {

                    if (lastPictureTakenUri == null) {
                        MessageHelper.ttsPath(17);
                    } else {
                        Intent twitterIntent = getShareIntent(
                                "twitter",
                                "CamAccc",
                                "CamAcc is a great photo capturing and " +
                                        "sharing application aimed for the Blind " +
                                        "and visually impaired. Check it out!");

                        if (twitterIntent == null) {
                            MessageHelper.ttsPath(24); // twitter app not installed
                        } else {
                            String strUri = lastPictureTakenUri
                                    .toString();
                            final ContentValues values = new ContentValues(2);
                            values.put(MediaStore.Images.Media.MIME_TYPE,
                                    "image/*");
                            values.put(MediaStore.Images.Media.DATA,
                                    strUri);
                            Uri contentUriFile = getContentResolver()
                                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                            values);

                            Log.i(TAG, "(1) contentUriFile: " + contentUriFile);
                            if (contentUriFile != null) {
                                strContentUriFile = contentUriFile.toString();
                            } else {
                                contentUriFile = Uri.parse(strContentUriFile);
                            }
                            Log.i(TAG, "(2) contentUriFile: " + contentUriFile);

                            try {
                                returnSocialMedia = true;
                                final Intent intent = new Intent(
                                        android.content.Intent.ACTION_SEND);
                                intent.setPackage("com.twitter.android");
                                intent.setType("image/*");
                                intent.putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Check out this new image taken with #CamAcc! "
                                                + "Retweet or respond back with comments.");
                                intent.putExtra(
                                        android.content.Intent.EXTRA_STREAM,
                                        contentUriFile);
                                CameraActivity.this.startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    // command set 8: SOCIAL MEDIA INSTAGRAM
                } else if (matches.contains("Instagram")
                        || matches.contains("instagram")) {

                    if (lastPictureTakenUri == null) {
                        MessageHelper.ttsPath(17);
                    } else {
                        Intent instagramIntent = getShareIntent(
                                "Instagram",
                                "CamAccc",
                                "CamAcc is a great photo capturing and " +
                                        "sharing application aimed for the Blind " +
                                        "and visually impaired. Check it out!");

                        if (instagramIntent == null) {
                            MessageHelper.ttsPath(25); // instagram app not installed
                        } else {
                            String strUri = lastPictureTakenUri
                                    .toString();
                            final ContentValues values = new ContentValues(2);
                            values.put(MediaStore.Images.Media.MIME_TYPE,
                                    "image/*");
                            values.put(MediaStore.Images.Media.DATA,
                                    strUri);
                            Uri contentUriFile = getContentResolver()
                                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                            values);

                            Log.i(TAG, "(1) contentUriFile: " + contentUriFile);
                            if (contentUriFile != null) {
                                strContentUriFile = contentUriFile.toString();
                            } else {
                                contentUriFile = Uri.parse(strContentUriFile);
                            }
                            Log.i(TAG, "(2) contentUriFile: " + contentUriFile);

                            try {
                                returnSocialMedia = true;
                                final Intent intent = new Intent(
                                        android.content.Intent.ACTION_SEND);
                                intent.setPackage("com.instagram.android");
                                intent.setType("image/*");
                                intent.putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Check out this new image taken with CamAcc!");
                                intent.putExtra(
                                        android.content.Intent.EXTRA_STREAM,
                                        contentUriFile);
                                CameraActivity.this.startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    // command set 9: OPTIONS
                } else if (matches.contains("option")
                        || matches.contains("options")
                        || matches.contains("setting")
                        || matches.contains("settings")) {

                    if (isOptionController == false) {

                        if (isFilterController == true) {
                            isFilterController = !isFilterController;
                        }

                        // if controller is on, option commands are forced
                        isOptionController = true;

                        if (isAdvance == true) {
                            speakText("Your current settings are: advanced mode "
                                    + "on, communication mode on, auto detection off, ");
                            MessageHelper.ttsPath(3);
                        } else { // advance mode is OFF
                            speakText("Options allow you to disable and enable a variety of "
                                    + "features. Your current settings are: advanced mode "
                                    + "off, communication mode on, auto detection off, ");
                            MessageHelper.ttsPath(4);
                        }
                    } else {
                        MessageHelper.ttsPath(13);
                    }

                    // command set 10: OPTIONS ADVANCE MODE TOGGLE ON
                } else if (matches.contains("advance on")
                        || matches.contains("advance mode on")) {

                    if (isOptionController == true) {
                        if (isAdvance == true) {
                            MessageHelper.ttsPath(19);
                        } else {
                            isAdvance = !isAdvance;
                            prefHelper.savePrefAdvance("strAdvance", true);
                            Log.i(TAG, "isAdvance(" + isAdvance + ")" +
                                    " " + prefHelper.getPrefAdvance(sharedPrefAdvance));
                            MessageHelper.ttsPath(7);
                        }
                    } else {
                        MessageHelper.ttsPath(11);
                    }

                    // command set 11: OPTIONS ADVANCE MODE TOGGLE OFF
                } else if (matches.contains("advance off")
                        || matches.contains("advance mode off")) {

                    if (isOptionController == true) {
                        if (isAdvance == true) {
                            isAdvance = !isAdvance;
                            prefHelper.savePrefAdvance("strAdvance", false);
                            Log.i(TAG, "isAdvance(" + isAdvance + ")" +
                                    " " + prefHelper.getPrefAdvance(sharedPrefAdvance));
                            MessageHelper.ttsPath(8);
                        } else {
                            MessageHelper.ttsPath(20);
                        }
                    } else {
                        MessageHelper.ttsPath(11);
                    }

                    // command set 12: OPTIONS COMMUNICATION MODE TOGGLE OFF
                } else if (matches.contains("voice")
                        || matches.contains("voice off")
                        || matches.contains("voice mode off")
                        || matches.contains("communication")
                        || matches.contains("communication off")
                        || matches.contains("communication mode off")) {

                    prefHelper.savePrefVoice("strVoice", false);
                    isVoice = !isVoice;
                    disableVoiceEngine();
                    speakText("Communication mode deactivated");
                    keyEventLv = false;
                    keyEventPos = 0;

                    // command set 13: OPTIONS AUTO SOCIAL MEDIA ON
                }else if (matches.contains("exit")
                        || matches.contains("exit option")
                        || matches.contains("exit options")
                        || matches.contains("exit selfie")) {

                    if (isSelfieController == true) {
                        isSelfieController = !isSelfieController;
                        speakText("Selfie mode deactivated");
                        keyEventLv = false;
                        isSelfieController = false;
                        keyEventPos = 0;
                        backCamera();
                    } else if (isOptionController == true) {
                        isOptionController = !isOptionController;
                        if (isAdvance == true) {
                            MessageHelper.ttsPath(99);
                        } else {
                            MessageHelper.ttsPath(4);
                        }

                    } else {
                        MessageHelper.ttsPath(14);
                    }

                    // command set 16: APPLY FILTER IMPROVE QUALITY
                } else if (matches.contains("improve")
                        || matches.contains("improve filter")
                        || matches.contains("improve quality")
                        || matches.contains("prove")
                        || matches.contains("prove filter")
                        || matches.contains("prove quality")
                        || matches.contains("improved")) {

                    if (isOptionController == true) {
                        MessageHelper.ttsPath(15);
                    } else {
                        if (lastPictureTakenUri.getPath() == null) {
                            MessageHelper.ttsPath(98);
                        } else {

							/*
							 * perform two filter processes, both brightness
							 * correction and sharpen, to improve the look of
							 * the image(s)
							 */
                            ImageProcess imageProcess = new ImageProcess();
                            isQualityImprove = true;

                            Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                            imageProcess.applyAndSave(this,
                                    lastPictureTakenUri,
                                    ImageProcess.FILTER_BRIGHTNESS);

                            isQualityImprove = false;
                            filterUri = ImageProcess.getFilterUri();
                            Log.d(TAG, "filterUri: " + filterUri);

                            lastPictureTakenUri = filterUri;
                            imageProcess.applyAndSave(this,
                                    lastPictureTakenUri,
                                    ImageProcess.FILTER_SHARPEN);

                            Log.i(TAG, "Brightness correction and sharpen " +
                                    "filters applied and saved");
                            if (isAdvance == true) {
                                speakText("Your image has been improved with brightness "
                                        + "correction and sharpen filters and saved. Would "
                                        + "you like to share this image? You can post to "
                                        + "twitter, face book and instegram");
                            } else {
                                speakText("Your image has been improved with brightness " +
                                        "correction and sharpen filters and saved.");
                            }
                        }
                    }

                    // command set 17: APPLY FILTER SEPIA
                } else if (matches.contains("sepia")
                        || matches.contains("sepia filter")
                        || matches.contains("sopia")
                        || matches.contains("sofia filter")) {

                    if (isOptionController == true) {
                        MessageHelper.ttsPath(15);
                    } else {
                        if (lastPictureTakenUri == null) {
                            MessageHelper.ttsPath(98);
                        } else {
                            ImageProcess imageProcess = new ImageProcess();
                            Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                            imageProcess.applyAndSave(this,
                                    lastPictureTakenUri,
                                    ImageProcess.FILTER_SEPIA);

                            Log.i(TAG, "Sepia filter applied and saved");
                            if (isAdvance == true) {
                                speakText("Sepia filter has been applied and saved. Would you "
                                        + "like to share this image? You can post to twitter, "
                                        + "face book and instegram");
                            } else {
                                speakText("Sepia filter has been applied and saved.");
                            }
                        }
                    }

                    // command set 18: APPLY FILTER GRAYSCALE
                } else if (matches.contains("gray")
                        || matches.contains("grayscale")
                        || matches.contains("gray scale")
                        || matches.contains("grayscale filter")
                        || matches.contains("gray scale filter")
                        || matches.contains("grey")
                        || matches.contains("greyscale")
                        || matches.contains("grey scale")
                        || matches.contains("greyscale filter")
                        || matches.contains("grey scale filter")) {

                    if (isOptionController == true) {
                        MessageHelper.ttsPath(15);
                    } else {
                        if (lastPictureTakenUri == null) {
                            MessageHelper.ttsPath(98);
                        } else {
                            ImageProcess imageProcess = new ImageProcess();
                            Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                            imageProcess.applyAndSave(this,
                                    lastPictureTakenUri,
                                    ImageProcess.FILTER_GRAYSCALE);

                            Log.i(TAG, "Grayscale filter applied and saved");
                            if (isAdvance == true) {
                                speakText("Gray scale filter has been applied and saved. Would you "
                                        + "like to share this image? You can post to twitter, "
                                        + "face book and instegram");
                            } else {
                                speakText("Gray scale filter has been applied and saved.");
                            }
                        }
                    }

                    // command set 19: APPLY FILTER EMBOSS
                } else if (matches.contains("emboss")
                        || matches.contains("emboss filter")
                        || matches.contains("boss")
                        || matches.contains("boss filter")
                        || matches.contains("in bose")
                        || matches.contains("in bose filter")) {

                    if (isOptionController == true) {
                        MessageHelper.ttsPath(15);
                    } else {
                        if (lastPictureTakenUri == null) {
                            MessageHelper.ttsPath(98);
                        } else {
                            ImageProcess imageProcess = new ImageProcess();
                            Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                            imageProcess.applyAndSave(this,
                                    lastPictureTakenUri,
                                    ImageProcess.FILTER_EMBOSS);

                            Log.i(TAG, "Emboss filter applied and saved");
                            if (isAdvance == true) {
                                speakText("Emboss filter has been applied and saved. Would you "
                                        + "like to share this image? You can post to twitter, "
                                        + "face book and instegram");
                            } else {
                                speakText("Emboss filter has been applied and saved.");
                            }
                        }
                    }

                    // command set 20: APPLY FILTER INVERT
                } else if (matches.contains("invert")
                        || matches.contains("invert filter")) {

                    if (isOptionController == true) {
                        MessageHelper.ttsPath(15);
                    } else {
                        if (lastPictureTakenUri == null) {
                            MessageHelper.ttsPath(98);
                        } else {
                            ImageProcess imageProcess = new ImageProcess();
                            Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                            imageProcess.applyAndSave(this,
                                    lastPictureTakenUri,
                                    ImageProcess.FILTER_INVERT);

                            Log.i(TAG, "Invert filter applied and saved");
                            if (isAdvance == true) {
                                speakText("Invert filter has been applied and saved. Would you "
                                        + "like to share this image? You can post to twitter, "
                                        + "face book and instegram");
                            } else {
                                speakText("Invert filter has been applied and saved.");
                            }
                        }
                    }

                    // command set 21: APPLY FILTER BLUR
                } else if (matches.contains("blur")
                        || matches.contains("blur filter")) {

                    if (isOptionController == true) {
                        MessageHelper.ttsPath(15);
                    } else {
                        if (lastPictureTakenUri == null) {
                            MessageHelper.ttsPath(98);
                        } else {
                            ImageProcess imageProcess = new ImageProcess();
                            Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                            imageProcess.applyAndSave(this,
                                    lastPictureTakenUri,
                                    ImageProcess.FILTER_BLUR);

                            Log.i(TAG, "Blur filter applied and saved");
                            if (isAdvance == true) {
                                speakText("Blur filter has been applied and saved. Would you "
                                        + "like to share this image? You can post to twitter, "
                                        + "face book and instegram");
                            } else {
                                speakText("Blur filter has been applied and saved.");
                            }
                        }
                    }

                    // command set 22: APPLY FILTER SHARPEN
                } else if (matches.contains("sharpen")
                        || matches.contains("sharpen filter")
                        || matches.contains("sharp")
                        || matches.contains("sharp filter")) {

                    if (isOptionController == true) {
                        MessageHelper.ttsPath(15);
                    } else {
                        if (lastPictureTakenUri == null) {
                            MessageHelper.ttsPath(98);
                        } else {
                            ImageProcess imageProcess = new ImageProcess();
                            Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                            imageProcess.applyAndSave(this,
                                    lastPictureTakenUri,
                                    ImageProcess.FILTER_SHARPEN);

                            Log.i(TAG, "Sharpen filter applied and saved");
                            if (isAdvance == true) {
                                speakText("Sharpen filter has been applied and saved. Would you "
                                        + "like to share this image? You can post to twitter, "
                                        + "face book and instegram");
                            } else {
                                speakText("Sharpen filter has been applied and saved.");
                            }
                        }
                    }

                    // command set 23: APPLY FILTER MORPH
                } else if (matches.contains("morph")
                        || matches.contains("morph filter")) {

                    if (isOptionController == true) {
                        MessageHelper.ttsPath(15);
                    } else {
                        if (lastPictureTakenUri == null) {
                            MessageHelper.ttsPath(98);
                        } else {
                            ImageProcess imageProcess = new ImageProcess();
                            Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                            imageProcess.applyAndSave(this,
                                    lastPictureTakenUri,
                                    ImageProcess.FILTER_MORPH);

                            Log.i(TAG, "Morph filter applied and saved");
                            if (isAdvance == true) {
                                speakText("Morph filter has been applied and saved. Would you "
                                        + "like to share this image? You can post to twitter, "
                                        + "face book and instegram");
                            } else {
                                speakText("Morph filter has been applied and saved.");
                            }
                        }
                    }

                    // command set 24: APPLY FILTER BRIGHTNESS CORRECTION
                } else if (matches.contains("bright")
                        || matches.contains("bright correction")
                        || matches.contains("brightness")
                        || matches.contains("brightness correction")
                        || matches.contains("brighten")
                        || matches.contains("correction")) {

                    if (isOptionController == true) {
                        MessageHelper.ttsPath(15);
                    } else {
                        if (lastPictureTakenUri == null) {
                            MessageHelper.ttsPath(98);
                        } else {
                            ImageProcess imageProcess = new ImageProcess();
                            Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                            imageProcess.applyAndSave(this,
                                    lastPictureTakenUri,
                                    ImageProcess.FILTER_BRIGHTNESS);

                            Log.i(TAG, "Brightness correction filter applied and saved");
                            if (isAdvance == true) {
                                speakText("Brightness correction has been applied and saved. "
                                        + "Would you like to share this image? You can post "
                                        + "to twitter, face book and instegram");
                            } else {
                                speakText("Brightness correction filter has been applied and saved.");
                            }
                        }
                    }

                    // command set 25: APPLY FILTER GAUSSIAN
                } else if (matches.contains("gossi in")
                        || matches.contains("gossi in filter")
                        || matches.contains("ga cn")
                        || matches.contains("ga cn filter")
                        || matches.contains("gossen")
                        || matches.contains("gossen filter")) {

                    if (isOptionController == true) {
                        MessageHelper.ttsPath(15);
                    } else {
                        if (lastPictureTakenUri == null) {
                            MessageHelper.ttsPath(98);
                        } else {
                            ImageProcess imageProcess = new ImageProcess();
                            Log.d(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
                            imageProcess.applyAndSave(this,
                                    lastPictureTakenUri,
                                    ImageProcess.FILTER_GAUSSIAN);

                            Log.i(TAG, "Gaussian filter applied and saved");
                            if (isAdvance == true) {
                                speakText("Gaussian filter has been applied and saved. Would you "
                                        + "like to share this image? You can post to twitter, "
                                        + "face book and instegram");
                            } else {
                                speakText("Gaussian filter has been applied and saved.");
                            }
                        }
                    }


                    // command set 26: CLOSE APPLICATION
                } else if (matches.contains("close")
                        || matches.contains("close app")
                        || matches.contains("close application")
                        || matches.contains("close out")
                        || matches.contains("shutdown")
                        || matches.contains("shutdown app")
                        || matches.contains("shutdown application")
                        || matches.contains("shut down")
                        || matches.contains("shut down app")
                        || matches.contains("shut down application")) {

                    // close application
                    surfaceView.setEnabled(false);
                    speakText("Application closing");
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            finish();
                        }
                    }, 1000);
                } else {
                    // reinitialize voice engine
                    Log.e(TAG, "Ignoring non-commands; normal talking or "
                            + "noise inferred.");

                    // add short delay between reactivating voice engine
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            enableVoiceEngine();
                            Intent intent = new Intent(CameraActivity.this,
                                    Voice_Engine.class);
                            startActivityForResult(intent,
                                    VOICE_RECOGNITION_REQUEST_CODE);
                        }
                    }, 1000);
                }
            } // end if-statement: requestCode == VOICE_RECOGNITION_REQUEST_CODE
            // && resultCode != 0
        } else {
			/*
			 * Voice Engine interrupted. Affirm last action and then
			 * reinitialize TTS based on last action
			 */
            if ((data == null)&&(cameraBack != null || cameraFront != null)
                    &&(Voice_Engine.speakingInterrupted == true)) {
                Log.i(TAG, "Voice engine interrupted, surfaceView button was pressed");
                if (isSelfieController == true) {
                    cameraFront.takePicture(myShutterCallback, myPictureCallback_RAW,
                            myPictureCallback_JPG);
                } else {
                    cameraBack.takePicture(myShutterCallback, myPictureCallback_RAW,
                            myPictureCallback_JPG);
                }
            }
        }
    } // end onActivityForResult()

    private boolean isNetWorkConnection() {
		/*
		 * check connectivity and take special consideration for less stable
		 * connections such as WIFI. This is done so by making sure connecton
		 * strength is good enough to run our voice engine
		 */
        if (NetworkUtils.isNetworkActive(CameraActivity.this)
                && NetworkUtils.isConnectedFast(CameraActivity.this)) {
            Log.i(TAG,
                    "isConnectedWifi: "
                            + NetworkUtils.isConnectedWifi(CameraActivity.this)
                            + " // " + "isConnectedMobile: "
                            + NetworkUtils.isConnectedMobile(CameraActivity.this));

            if (isConnected == false) {
                isConnected = true;
            }
        }
        return isConnected;

    } // end isNetworkConnection()

    private void networkWarning() {

        try {
            isWarningSound = false;
            Uri notification = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            r = RingtoneManager.getRingtone(getApplicationContext(),
                    notification);
            v = (Vibrator) CameraActivity.this
                    .getSystemService(Context.VIBRATOR_SERVICE);
			/*
			 * suspend app using alert dialog. This allows us to suspend the
			 * main UI, and then proceed with connectivity checks, reconnection
			 * checks, and warning sounds before application closes for not
			 * detecting a proper network connection
			 */

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle("No Internet Connection Detected");
            dialogBuilder.setIcon(R.drawable.ic_launcher);
            dialogBuilder.setMessage("Please connect to the internet.")
                    .setCancelable(false);

            if (isVoice == true) {
                speakText("Please reconnect to the internet to continue using " +
                        "communication mode");
            } else {
                speakText("Communication mode requires an active network connecton. " +
                        "Please connect to the internet");
            }

            alertDialog = dialogBuilder.create();
            alertDialog.show();

            new CountDownTimer(6000, 1500) {

                /*
                 * The calls to onTick(long) are synchronized to this object so
                 * that one call to onTick(long) won't ever occur before the
                 * previous callback is complete
                 */
                public void onTick(long millisUntilFinished) {
                    Log.i(TAG, "seconds remaining (1): " + millisUntilFinished
                            / 1000);
                    if (isWarningSound == false) {
                        r.play();
                        v.vibrate(500);
                    }

                }

                public void onFinish() {
					/*
					 * recheck for internet connection every second for 10
					 * seconds. If no connection is detected after the allowed
					 * 12 seconds close application
					 */
                    new CountDownTimer(12000, 1000) {

                        @Override
                        public void onTick(long millisUntilFinished) {
                            // TODO Auto-generated method stub
                            if (NetworkUtils.isConnected(CameraActivity.this)
                                    && NetworkUtils
                                    .isConnectedFast(CameraActivity.this)) {
                                Log.i(TAG,
                                        "isConnectedWifi: "
                                                + NetworkUtils
                                                .isConnectedWifi(CameraActivity.this)
                                                + " isConnectedMobile: "
                                                + NetworkUtils
                                                .isConnectedMobile(CameraActivity.this)
                                                + "\nseconds remaining (2): "
                                                + millisUntilFinished / 1000);

                                // set this boolean only once
                                if (isConnected == false) {
                                    isConnected = true;
                                    surfaceView.setEnabled(true);
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
                                if (isVoice == true) {
                                    prefHelper.savePrefVoice("strVoice", false);
                                    isVoice = !isVoice;
                                    keyEventLv = false;
                                    keyEventPos = 0;
                                    speakText("Communication mode deactivated, main options");
                                } else {
                                    speakText("Unable to activate communication mode. " +
                                            "Internet connection is required");
                                }
                            } else {
                                alertDialog.dismiss();
                                if (isVoice == true) {
                                    if (isAdvance == true) {
                                        MessageHelper.ttsPath(99);
                                    } else if (isAdvance == false) {
                                        MessageHelper.ttsPath(4);
                                    }
                                } else {
                                    prefHelper.savePrefVoice("strVoice", true);
                                    isVoice = !isVoice;
                                    if (isAdvance == true) {
                                        MessageHelper.ttsPath(99);
                                    } else if (isAdvance == false) {
                                        MessageHelper.ttsPath(4);
                                    }
                                }
                            }
                        }

                    }.start();

                }
            }.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    } // end networkWarning()

    private Intent getShareIntent(String type, String subject, String text) {
        boolean found = false;
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");

        // gets the list of intents that can be loaded.
        List<ResolveInfo> resInfo = this.getPackageManager()
                .queryIntentActivities(share, 0);
        System.out.println("resinfo: " + resInfo);
        if (!resInfo.isEmpty()) {
            for (ResolveInfo info : resInfo) {
                if (info.activityInfo.packageName.toLowerCase().contains(type)
                        || info.activityInfo.name.toLowerCase().contains(type)) {
                    share.putExtra(Intent.EXTRA_SUBJECT, subject);
                    share.putExtra(Intent.EXTRA_TEXT, text);
                    share.setPackage(info.activityInfo.packageName);
                    found = true;
                    break;
                }
            }
            if (!found)
                return null;
            return share;
        }
        return null;
    } // end getShareIntent()

    class FaceDetection implements Camera.FaceDetectionListener {
        /**
         * Detects faces in the Camera view according to FaceDetectionListener.
         * All faces detected will be stored into a Face array to be passed to
         * be processed by this method. For one face it will detect if the face
         * is centered. For two faces detects centered if neither face is in the
         * center of the screen. For three or more faces it says multiple faces
         * detected.
         *
         * @param faces
         *            the number of faces detected by
         *            Camera.FaceDetectionListener
         * @param camera
         *            not used. Exists for overriding onFaceDetection from
         *            FaceDetectionListener
         */

        @Override
        public void onFaceDetection(Face[] faces, Camera camera) {

            Random rand = new Random();
            int randDesc = 0;

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
                    randDesc = rand.nextInt(2) + 1;

                    Rect realFaceRect = new Rect(left, top, right, bottom);

                    int halfWidth = surfaceView.getRight() / 2;
                    int halfHeight = surfaceView.getBottom() / 2;
                    Rect middle = new Rect(halfWidth - 50, halfHeight - 50,
                            halfWidth + 50, halfHeight + 50);


                    if (middle.intersect(realFaceRect) && balanced) {
                        Log.i(TAG, "People detected: " + faces.length);
                        if (randDesc <= 1) {
                            speakText("One person centered detected and your camera is level.");
                        } else if (randDesc == 2) {
                            speakText("One person centered detected. Take picture when ready.");
                        } else {
                            //no speaking, slows down constant talking spam
                        }

                    } else if (middle.intersect(realFaceRect) && !balanced) {
                        Log.i(TAG, "People detected: " + faces.length);
                        speakText("One person centered detected but your camera is not level.");
                    } else if (!balanced) {
                        Log.i(TAG,
                                "People detected: " + faces.length
                                        + " // realFaceRect: "
                                        + realFaceRect.toString());
                        if (randDesc <= 1) {
                            speakText("One person detected, not centered.");
                        } else if (randDesc == 2) {
                            speakText("One person detected, not centered and your camera is not level.");
                        } else {
                            //no speaking, slows down constant talking spam
                        }
                    }

                } else if (faces.length == 2 && balanced) {
                    Log.i(TAG, "People detected: " + faces.length
                            + " // faces[0].rect: " + faces[0].rect.toString());
                    if (randDesc <= 1) {
                        speakText("Two people detected, both are centered. Take picture when ready.");
                    } else if (randDesc == 2) {
                        speakText("Two people detected, both are centered and your camera is level.");
                    } else {
                        //no speaking, slows down constant talking spam
                    }
                } else if (faces.length == 2 && !balanced) {
                    Log.i(TAG, "People detected: " + faces.length
                            + " // faces[0].rect: " + faces[0].rect.toString());
                    if (randDesc <= 1) {
                        speakText("Two people detected, both are centered.");
                    } else if (randDesc == 2) {
                        speakText("Two people detected, both are centered but your camera is not level.");
                    } else {
                        //no speaking, slows down constant talking spam
                    }
                } else if (faces.length >= 3 && balanced) {
                    Log.i(TAG, "People detected: " + faces.length);
                    if (randDesc <= 1) {
                        speakText("Multiple people detected. Take picture when ready.");
                    } else if (randDesc == 2) {
                        speakText("Multiple people detected and your camera is level.");
                    } else {
                        //no speaking, slows down constant talking spam
                    }
                }
            }
        }
    } // end FaceDetection class

    /**
     * Disables the VoiceEngine
     */
    private void disableVoiceEngine() {
        Log.e(TAG, "disabled voice engine new helper method");
        try {
            VoiceEngineHelper.setVoiceController(true);
            //Voice_Engine.singletonVE.finish();
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e);
        }
    }

    /**
     * Enables the VoiceEngine. When 
     * VoiceEngineHelper.setVoiceController(false), voice engine
     * is active, otherwise inactive
     */
    private void enableVoiceEngine() {
        Log.e(TAG, "enable voice engine new helper method");
        VoiceEngineHelper.setVoiceController(false); 
    }

    /**
     * @return the isQualityImprove
     */
    public static boolean isQualityImprove() {
        return isQualityImprove;
    }

    /**
     * @return the lastPictureTakenUri
     */
    public static Uri getLastPictureTakenUri() {
        return lastPictureTakenUri;
    }

    /**
     * @param lastPictureTakenUri
     *            the lastPictureTakenUri to set
     */
    public static void setLastPictureTakenUri(Uri lastPictureTakenUri) {
        CameraActivity.lastPictureTakenUri = lastPictureTakenUri;
    }

    /**
     *
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
    }

    @Override
    public void onBackPressed() {
        // not only close Activity, but stop services as well

        if (isVoice == false) {
            finish();
        }

    }

    /**
     * Called after onCreate(Bundle) � or after onRestart() when the activity
     * had been stopped, but is now again being displayed to the user. It will
     * be followed by onResume().
     */
    @Override
    public void onStart() {
        Log.d(TAG, "onStart()" + "returnSocialMedia: " + returnSocialMedia);
        if (returnSocialMedia == true) {
            Log.i(TAG, "lastPictureTakenUri: " + lastPictureTakenUri);
            returnSocialMedia = false;
        }
        sensorListener.enable();
        if(textToSpeech == null){
            textToSpeech = new TextToSpeech(CameraActivity.this, this);
            textToSpeech.setLanguage(Locale.US);
            textToSpeech.setPitch(8/10);
            textToSpeech.setSpeechRate(15/12);
        }
        else{
            if(isVoice == true){
                if(isOptionController == true ){
                    isOptionController = false;
                }
                if(VoiceEngineHelper.getVoiceController() == true){
                    enableVoiceEngine();
                }
                MessageHelper.ttsPath(4);
            }
        }


        super.onStart();
    }

    /**
     * Called when you are no longer visible to the user.
     */
    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");

        super.onResume();
    }

    /**
     * Called when you are no longer visible to the user.
     */
    @Override
    public void onStop() {
        Log.d(TAG, "onStop()");
        sensorListener.disable();
        if((isVoice == true)&&(textToSpeech.isSpeaking() == true))
        {
            disableVoiceEngine();
            textToSpeech.stop();
        }
        if(textToSpeech != null){
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        super.onStop();

    }

    /**
     * Called as part of the activity lifecycle when an activity is going into
     * the background, but has not (yet) been killed.
     */
    @Override
    public void onPause() {
        Log.d(TAG, "onPause()");
        if((isVoice == true)&&(textToSpeech.isSpeaking() == true))
        {
            disableVoiceEngine();
            textToSpeech.stop();
        }
        super.onPause();

    }

    /**
     * Called by the system to remove the Service when it is no longer used.
     * Ends textToSpeech and Voice_Engine, as well as calling Activity's
     * onDestroy(). The service should clean up any resources it holds (threads,
     * registered receivers, etc) at this point. Upon return, there will be no
     * more calls in to this Service object and it is effectively dead. Do not
     * call this method directly.
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        disableVoiceEngine();
        sensorListener.disable();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        super.onDestroy();
    }

} // end CameraActivity class