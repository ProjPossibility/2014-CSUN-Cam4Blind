package com.example.ImageSharing;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;

import java.io.File;
import java.util.List;

public class MyActivity extends Activity {

    public static final String path = "C:/Users/Kristoffer/Documents/Android/ImageSharing" +
            "/res/drawable/spaceshipspacecityfutureclouds.jpg"; //Full file path

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        createShareIntent(path);
    }

    private void createShareIntent(String pathToImage) {
        final PackageManager pm = getPackageManager();
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

                        // You could get a component name like this:

                    }
                }
            }
            //Ask for user choice
            for (ResolveInfo ri : riList) {
                ActivityInfo ai = ri.activityInfo;

                if (ai != null) {
                    //ComponentName cmp = new ComponentName(ai.packageName, ai.name);

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    shareIntent.setType("image/*");

                    // For a file in shared storage.  For data in private storage, use a ContentProvider.
                    File file = new File(pathToImage);
                    Uri uri = Uri.fromFile(file);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent.setPackage(ai.packageName);
                    startActivity(shareIntent);
                }
            }
        }
    }
}
