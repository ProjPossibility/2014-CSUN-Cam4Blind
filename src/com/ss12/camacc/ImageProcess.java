package com.ss12.camacc;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Emboss;
import Catalano.Imaging.Filters.Grayscale;
import Catalano.Imaging.Filters.Invert;
import Catalano.Imaging.Filters.Sepia;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * ImageProcess is the functionality for filtering and processing images.
 * Includes features to manage the saving and bitmap processing. Also
 * has the ability to share photos via a share intent.
 */
public class ImageProcess {
    private static final String TAG = "ImageProcess";

    // Image Filters
    public static final int FILTER_NONE      = 0;
    public static final int FILTER_SEPIA     = 1;
    public static final int FILTER_GRAYSCALE = 2;
    public static final int FILTER_EMBOSS    = 3;
    public static final int FILTER_INVERT    = 4;

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
        options.inSampleSize = 2;
        //options.inJustDecodeBounds = true;
        String path = getRealPathFromURI(context, uri);
        Bitmap unfiltered = BitmapFactory.decodeFile(path, options);
        Bitmap filteredBitmap = applyFilter(filter, unfiltered);
        saveFile(context, uri, filteredBitmap);
    }

    /**
     *
     * @param context    Context
     * @param contentUri Uri for the desired path
     * @return           Full path of the given Uri
     */
    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * @param context   Current context
     * @param uri       Path of the image passed
     * @param bitmap    Bitmap that will be saved that has filter applied
     */
    public void saveFile(Context context, Uri uri, Bitmap bitmap) {
        String imagePath = getRealPathFromURI(context, uri);
        File file = new File(imagePath);
        try {
            FileOutputStream fOut = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
            default:
                break;
        }
        if(filter != null) {
            filter.applyInPlace(img);
        }
        return img.toBitmap();
    }

    /**
     *
     * @param context       Context for function
     * @param uri           Uri of the image to share
     */
    public void createShareIntentFromUri(Context context, Uri uri) {
        createShareIntentFromPath(context,getRealPathFromURI(context, uri));
    }

    /**
     *
     * @param context       Context for function
     * @param pathToImage   Path of image to share
     */
    public void createShareIntentFromPath(Context context, String pathToImage) {
        final PackageManager pm = context.getPackageManager();
        final Intent intent = new Intent(Intent.ACTION_SEND);
        if (pm != null) {
            List<ResolveInfo> riList = pm.queryIntentActivities(intent, 0);

            for (ResolveInfo ri : riList) {
                ActivityInfo ai = ri.activityInfo;
                if (ai != null) {
                    String pkg = ai.packageName;

                    if (pkg.equals("com.facebook") || pkg.equals("com.twitter")) {
                        //TTS apps available for sharing


                        // Add to the list of accepted activities.

                        // There's a lot of info available in the
                        // ResolveInfo and ActivityInfo objects: the name, the icon, etc.

                    }
                }
            }
            //Ask for user choice
            for (ResolveInfo ri : riList) {
                ActivityInfo ai = ri.activityInfo;

                if (ai != null) {
                    //ComponentName cmp = new ComponentName(ai.packageName, ai.name);
                    //if (what the user asked for) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    shareIntent.setType("image/*");

                    // For a file in shared storage.  For data in private storage, use a ContentProvider.
                    File file = new File(pathToImage);
                    Uri uri = Uri.fromFile(file);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent.setPackage(ai.packageName);
                    context.startActivity(shareIntent);
                }
            }
        }
    }
}
