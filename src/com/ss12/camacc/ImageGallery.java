package com.ss12.camacc;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Emboss;
import Catalano.Imaging.Filters.Grayscale;
import Catalano.Imaging.Filters.Invert;
import Catalano.Imaging.Filters.Sepia;
import Catalano.Imaging.IBaseInPlace;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.example.navigationdrawer.R;

public class ImageGallery extends Activity {
    private static final String TAG = "FilterActivity";

    private static final int RESULT_LOAD_IMAGE = 1;

    // Image Filters
    private static final int FILTER_SEPIA = 1;
    private static final int FILTER_GRAYSCALE = 2;
    private static final int FILTER_EMBOSS = 3;
    private static final int FILTER_INVERT = 4;

    Button imgButton, filterButton;
    ImageView image;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        addListenerOnButton();

    }

    public void addListenerOnButton() {

        image = (ImageView) findViewById(R.id.imageView);

        imgButton = (Button) findViewById(R.id.btnChangeImage);
        filterButton = (Button) findViewById(R.id.btnApplyFilter);
        filterButton.setEnabled(false);

        imgButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //image.setImageResource(R.drawable.action_search);

                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }

        });

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add in code to bring up filter results

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            Log.i(TAG, selectedImage.toString());
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);

            imageView.setImageBitmap(applyFilter(FILTER_INVERT, bitmap));
            saveFilter(picturePath, bitmap);
            // Since we now have an image, can apply a filter to it.
            filterButton.setEnabled(true);

        }
    }

    /**
     * @param path  Path of the image passed
     * @param bitmap Bitmap that will be saved that has filter applied
     */
    private static void saveFilter(String path, Bitmap bitmap) {
        Log.i(TAG, "Path: " + path);
        // Get the base name and extension
        String[] tokens = path.split("\\.(?=[^\\.]+$)");
        Log.i(TAG, "Tokens: " + tokens[0]);
    }
    /**
     *
     * @param   id      desired filter to be applied to a bitmap
     * @param   src     bitmap source image to be filtered
     * @return          src bitmap with filter applied
     */
    private static Bitmap applyFilter(int id, Bitmap src) {
        FastBitmap img = new FastBitmap(src);
        // Interface for generic filter
        IBaseInPlace filter = null;

        switch(id) {
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



    /* Prototype from http://xjaphx.wordpress.com/2011/06/21/image-processing-grayscale-image-on-the-fly
        Need to find a better library, but this is just an example.
     */
    public static Bitmap doGreyscale(Bitmap src) {
        // constant factors
        final double GS_RED = 0.299;
        final double GS_GREEN = 0.587;
        final double GS_BLUE = 0.114;

        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        // pixel information
        int A, R, G, B;
        int pixel;

        // get image size
        int width = src.getWidth();
        int height = src.getHeight();

        // scan through every single pixel
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get one pixel color
                pixel = src.getPixel(x, y);
                // retrieve color of all channels
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                // take conversion up to one single value
                R = G = B = (int)(GS_RED * R + GS_GREEN * G + GS_BLUE * B);
                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }



}
