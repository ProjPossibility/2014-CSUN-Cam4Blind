package com.ss12.camacc;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Emboss;
import Catalano.Imaging.Filters.Grayscale;
import Catalano.Imaging.Filters.Invert;
import Catalano.Imaging.Filters.Sepia;
import Catalano.Imaging.IBaseInPlace;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.content.Context;
import com.example.navigationdrawer.R;

/**
 * Created by stefan on 2/8/14.
 */
public class FilterFragment extends Fragment {
    private static final String TAG = "FilterActivity";
    // TTS_ENGINE

    private static final int RESULT_LOAD_IMAGE = 1;

    // Image Filters
    private static final int FILTER_SEPIA = 1;
    private static final int FILTER_GRAYSCALE = 2;
    private static final int FILTER_EMBOSS = 3;
    private static final int FILTER_INVERT = 4;

    Button imgButton, filterButton;
    ImageView image;
    ViewGroup viewGroup;
    //Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewGroup = container;
        addListenerOnButton();

        return inflater.inflate(R.layout.fragment_filter, container, false);

    }

    public void addListenerOnButton() {

        image = (ImageView) viewGroup.findViewById(R.id.imageViewFrag);

        imgButton = (Button) viewGroup.findViewById(R.id.btnChangeImageFrag);
        filterButton = (Button) viewGroup.findViewById(R.id.btnApplyFilterFrag);
        filterButton.setEnabled(false);

//        imgButton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//                image.setImageResource(R.drawable.action_search);
//
//                Intent i = new Intent(
//                        Intent.ACTION_PICK,
//                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//
////                startActivityForResult(i, RESULT_LOAD_IMAGE);
//                changeImage(i, RESULT_LOAD_IMAGE);
//            }
//
//        });

    }

    protected void changeImage(Intent data, int requestCode) {

        if (requestCode == RESULT_LOAD_IMAGE && null != data) {
            Uri selectedImage = data.getData();
            Log.i(TAG, selectedImage.toString());
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = viewGroup.getContext().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            ImageView imageView = (ImageView) viewGroup.findViewById(R.id.imageView);
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);

            imageView.setImageBitmap(applyFilter(FILTER_INVERT, bitmap));

            // Since we now have an image, can apply a filter to it.
            imgButton.setEnabled(true);

        }
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





}