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

    // Image Filters
    public static final int FILTER_NONE      = 0;
    public static final int FILTER_SEPIA     = 1;
    public static final int FILTER_GRAYSCALE = 2;
    public static final int FILTER_EMBOSS    = 3;
    public static final int FILTER_INVERT    = 4;
    public static final int FILTER_BLUR		 = 5;
    public static final int FILTER_SHARPEN   = 6;
    public static final int FILTER_MORPH     = 7;
    public static final int FILTER_BRIGHTNESS= 8;
    public static final int FILTER_GAUSSIAN  = 9;

    public static Uri filterUri;  

	/**
     *
     */
    public ImageProcess(){}

    /**
     *
     * @param context   Context for function
     * @param uri       Uri that will be filtered, and saved to same location
     * @param filter    Type of filter
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
    }
  
    /**
     * @param context   Current context
     * @param uri       Path of the image passed
     * @param bitmap    Bitmap that will be saved that has filter applied
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
    }
    
    /**
     *
     * @param   id      desired filter to be applied to a bitmap
     * @param   src     bitmap source image to be filtered
     * @return          src bitmap with filter applied
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
    }
    
    /**
	 * @return the filterUri
	 */
	public static Uri getFilterUri() {
		return filterUri;
	}

}
