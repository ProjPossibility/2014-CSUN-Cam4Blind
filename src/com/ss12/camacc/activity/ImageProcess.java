package com.ss12.camacc.activity;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Emboss;
import Catalano.Imaging.Filters.Grayscale;
import Catalano.Imaging.Filters.Invert;
import Catalano.Imaging.Filters.Sepia;
import Catalano.Imaging.Filters.Blur;
import Catalano.Imaging.Filters.Sharpen;
import Catalano.Imaging.Filters.Morph;
import Catalano.Imaging.Filters.BrightnessCorrection;
import Catalano.Imaging.Filters.GaussianBlur;
import Catalano.Imaging.IBaseInPlace;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * ImageProcess is the functionality for filtering and processing images.
 * Includes features to manage the saving and bitmap processing. Also
 * has the ability to share photos via a share intent.
 *
 * @author Leonard Tatum
 * @author Noah Anderson
 * @author Stefan Eng
 * @author Javier Pimentel
 * @author Kristoffer Larson
 *
 */
public class ImageProcess {
	
	//public static String TAG = ImageProcess.class.getSimpleName();
    /**
     * A tag for no filter
     */
    public static final int FILTER_NONE      = 0;
    /**
     * A tag for the Sepia filter
     */
    public static final int FILTER_SEPIA     = 1;
    /**
     * A tag for the Gray Scale filter
     */
    public static final int FILTER_GRAYSCALE = 2;
    /**
     * A tag for the Emboss filter
     */
    public static final int FILTER_EMBOSS    = 3;
    /**
     * A tag for the Invert filter
     */
    public static final int FILTER_INVERT    = 4;
    /**
     * A tag for the Blur filter
     */
    public static final int FILTER_BLUR		 = 5;
    /**
     * A tag for the Sharpen filter
     */
    public static final int FILTER_SHARPEN   = 6;
    /**
     * A tag for the Morph filter
     */
    public static final int FILTER_MORPH     = 7;
    /**
     * A tag for the Brightness filter
     */
    public static final int FILTER_BRIGHTNESS= 8;
    /**
     * A tag for the Gaussian filter
     */
    public static final int FILTER_GAUSSIAN  = 9;

    private static String filterStr;
    //public static Uri filterUri;
    
    /**
     * Blank constructor. Used by other classes for referencing the methods
     * it contains.
     */
    public ImageProcess(){}

    /**
     * Prompts user for desired filter choice then applies it to the image.
     * The image is then saved to the Android photo gallery.
     *
     * @param context   Context for function
     * @param uri       Uri that will be filtered, and saved to same location
     * @param filter    Type of filter
     */
    public void applyAndSave(Context context, Uri uri, int filter) {
    	//Log.d(TAG, "applyAndSave() ::" + "\ncontext: " + context +
    	//		"\nuri: " + uri +
    	//		"\nfilter: " + filter);
    	
        // BitmapFactory options
        Options options = new Options();
        options.inSampleSize = 2; //necessary to ensure img files are not too large
        
        String path = uri.toString();
        //String path = CameraActivity.getLastPictureTakenUri().toString();
        //File file = new File(path);
        //Log.i(TAG, "applyAndSave() ::" + "\npath: " + path +
        //		"\nfile: " + file);

        try {
        	/* decode the file path into a bitmap. If the specified file name
             * is null, or cannot be decoded into a bitmap, the function returns 
             * null */
        	Bitmap unfiltered = BitmapFactory.decodeFile(path, options);
        	//Log.d(TAG, "applyAndSave() ::" + "\nunfiltered: " + unfiltered);
        	Bitmap filteredBitmap = applyFilter(filter, unfiltered);
        	//Log.d(TAG, "applyAndSave() ::" + "\nfilteredBitmap: " + filteredBitmap);
        	saveFile(context, uri, filteredBitmap);
        } catch (Exception e) {
        	//Log.e(TAG, "Failure to decode Bitmap");
        	e.printStackTrace();
        }
    }
  
    /**
     * Saves image into the Android gallery.
     *
     * @param context   Current context
     * @param uri       Path of the image passed
     * @param bitmap    Bitmap that will be saved that has filter applied
     */
    public void saveFile(Context context, Uri uri, Bitmap bitmap) {
    	//Log.d(TAG, "saveFile() ::" +
    	//		"\ncontext: " + context + "\nuri: " + uri +
    	//		"\nbitmap: " + bitmap);

    	/* Legend: File name convention
    	 * @filterStr =
    	 * _se means sepia filter applied
    	 * _gr means grayscale filter applied
    	 * _em means emboss filter applied
    	 * _in means invert filter applied
    	 * _bl means blue filter applied
    	 * _sh means sharpen filter applied
    	 * _mo means morph filter applied
    	 * _br means brightness correction filter applied
    	 * _ga means gaussian filter applied
    	 */
    	
    	String imagePath = filterStr + uri.toString();
        //Log.i(TAG, "saveFile() ::" +
        //		"\nimagePath: " + imagePath + "\nfilterStr: " + filterStr);
        
        File file = new File(imagePath);
        //if (file.exists()) file.delete(); //delete old file
        try {
        	//Log.e(TAG, "Saving file in progress");
            FileOutputStream fOut = new FileOutputStream(file);
            //bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Applies a filter to the source image.
     *
     * @param   id      desired filter to be applied to a bitmap
     * @param   src     bitmap source image to be filtered
     * @return          the src bitmap with filter applied
     */
    public static Bitmap applyFilter(int id, Bitmap src) {
        FastBitmap img = new FastBitmap(src);
        img.toRGB();
        // Interface for generic filter
        IBaseInPlace filter = null;

        switch(id) {
            case FILTER_NONE:
                break;
            case FILTER_SEPIA:
            	filterStr = "se_";
                filter = new Sepia();
                break;
            case FILTER_GRAYSCALE:
            	filterStr = "gr_";
                filter = new Grayscale();
                break;
            case FILTER_EMBOSS:
            	filterStr = "em_";
                filter = new Emboss();
                break;
            case FILTER_INVERT:
            	filterStr = "in_";
                filter = new Invert();
                break;
            case FILTER_BLUR:
            	filterStr = "bl_";
                filter = new Blur();
                break;
            case FILTER_SHARPEN:
            	filterStr = "sh_";
                filter = new Sharpen();
                break;
            case FILTER_MORPH:
            	filterStr = "mo_";
                filter = new Morph();
                break;
            case FILTER_BRIGHTNESS:
            	filterStr = "br_";
                filter = new BrightnessCorrection();
                break;
            case FILTER_GAUSSIAN:
            	filterStr = "ga_";
                filter = new GaussianBlur();
                break;    
            
            default:
                break;
        }
        if(filter != null) {
        	//Log.e(TAG, "Bitmap applyFilter ::" + "\nfilter: " + filter);
            filter.applyInPlace(img);
        }
        return img.toBitmap();
    }

//    /**
//     *
//     * @param context       Context for function
//     * @param uri           Uri of the image to share
//     */
//    public void createShareIntentFromUri(Context context, Uri uri) {
//        createShareIntentFromPath(context,getRealPathFromURI(context, uri));
//    }
//
//    /**
//     *
//     * @param context       Context for function
//     * @param pathToImage   Path of image to share
//     */
//    public void createShareIntentFromPath(Context context, String pathToImage) {
//        final PackageManager pm = context.getPackageManager();
//        final Intent intent = new Intent(Intent.ACTION_SEND);
//        if (pm != null) {
//            List<ResolveInfo> riList = pm.queryIntentActivities(intent, 0);
//
//            for (ResolveInfo ri : riList) {
//                ActivityInfo ai = ri.activityInfo;
//                if (ai != null) {
//                    String pkg = ai.packageName;
//
//                    if (pkg.equals("com.facebook") || pkg.equals("com.twitter")) {
//                        //TTS apps available for sharing
//
//
//                        // Add to the list of accepted activities.
//
//                        // There's a lot of info available in the
//                        // ResolveInfo and ActivityInfo objects: the name, the icon, etc.
//
//                    }
//                }
//            }
//            //Ask for user choice
//            for (ResolveInfo ri : riList) {
//                ActivityInfo ai = ri.activityInfo;
//
//                if (ai != null) {
//                    //ComponentName cmp = new ComponentName(ai.packageName, ai.name);
//                    //if (what the user asked for) {
//                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
//                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//                    shareIntent.setType("image/*");
//
//                    // For a file in shared storage.  For data in private storage, use a ContentProvider.
//                    File file = new File(pathToImage);
//                    Uri uri = Uri.fromFile(file);
//                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
//                    shareIntent.setPackage(ai.packageName);
//                    context.startActivity(shareIntent);
//                }
//            }
//        }
//    }
}//end ImageProcess class
