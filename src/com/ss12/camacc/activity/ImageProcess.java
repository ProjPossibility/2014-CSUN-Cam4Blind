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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * ImageProcess is the functionality for filtering and processing images.
 * Includes features to manage the saving and bitmap processing. Also
 * has the ability to share photos via a share intent.
 */
public class ImageProcess {
	
	public static String TAG = ImageProcess.class.getSimpleName();

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
    /**
     * A Uri object for filters
     */
    public static Uri filterUri;

    /**
     * Blank constructor. Used by other classes for referencing this class.
     */
    public ImageProcess(){}

    /**
     * Finds the desired filter choice then applies it to the image.
     * The image is then saved to the Android photo gallery.
     *
     * @param context Context for function
     * @param uri     Uri that will be filtered, and saved to same location
     * @param filter  Type of filter
     */
    public void applyAndSave(Context context, Uri uri, int filter) {
        // BitmapFactory options
        Options options = new Options();
        options.inSampleSize = 2; //necessary to ensure img files are not too large
        
        String path = uri.toString();
  //      File file = new File(path);

        try {
        	/* decode the file path into a bitmap. If the specified file name
             * is null, or cannot be decoded into a bitmap, the function returns 
             * null */
        	Bitmap unfiltered = BitmapFactory.decodeFile(path, options);
        	Bitmap filteredBitmap = applyFilter(filter, unfiltered);
        	saveFile(context, uri, filteredBitmap);
        } catch (Exception e) {
        	Log.e(TAG, "Failure to decode Bitmap");
        	e.printStackTrace();
        }
    }//end applyAndSave

    /**
     * Saves image into the Android gallery.
     *
     * @param context Current context
     * @param uri Path of the image passed
     * @param bitmap Bitmap that will be saved that has filter applied
     */
    public void saveFile(Context context, Uri uri, Bitmap bitmap) {
    	Log.d(TAG, "saveFile() ::" + 
    			"\ncontext: " + context + "\nuri: " + uri + 
    			"\nbitmap: " + bitmap);

    	String imagePath = uri.toString();
        Log.i(TAG, "saveFile() ::" +
        		"\nimagePath: " + imagePath);
        
        File file = new File(imagePath);
        filterUri = Uri.parse(imagePath);

        /* boolean isQualityImprove applies multiple filters. Only want to 
         * save file until after all filters are applied */
        if (CameraActivity.isQualityImprove() == false) {
        	try {
            	Log.e(TAG, "Saving filtered file in progress");
                FileOutputStream fOut = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
                fOut.flush();
                fOut.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
    	}
    }//end saveFile

    /**
     * Applies a filter to the source image.
     *
     * @param id Desired filter to be applied to a bitmap
     * @param src Bitmap source image to be filtered
     * @return The src bitmap with filter applied
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
                filter = new Sepia();
                break;
            case FILTER_GRAYSCALE:
                filter = new Grayscale();
                break;
            case FILTER_EMBOSS:
                filter = new Emboss();
                break;
            case FILTER_INVERT:
                filter = new Invert();
                break;
            case FILTER_BLUR:
                filter = new Blur();
                break;
            case FILTER_SHARPEN:
                filter = new Sharpen();
                break;
            case FILTER_MORPH:
                filter = new Morph();
                break;
            case FILTER_BRIGHTNESS:
                filter = new BrightnessCorrection();
                break;
            case FILTER_GAUSSIAN:
                filter = new GaussianBlur();
                break;    
            
            default:
                break;
        }
        if(filter != null) {
            filter.applyInPlace(img);
        }
        return img.toBitmap();
    }//end applyFilter
    
    /**
     * Gets the Filter Uri.
     *
	 * @return the filterUri
	 */
	public static Uri getFilterUri() {
		return filterUri;
	}

}//end ImageProcess class
