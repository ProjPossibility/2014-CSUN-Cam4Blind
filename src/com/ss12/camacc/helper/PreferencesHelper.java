package com.ss12.camacc.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferencesHelper {
	public static String TAG = PreferencesHelper.class.getSimpleName();
	
	private SharedPreferences sharedPreferences;
	private Editor editor;
	private static final String APP_SHARED_PREFS = 
			PreferencesHelper.class.getSimpleName(); //name of the file
	
	public PreferencesHelper(Context context) {
		this.sharedPreferences = context.getSharedPreferences
				(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
		this.editor = sharedPreferences.edit();
	}

	public void clearPreferences(Context context) {
		this.sharedPreferences = context.getSharedPreferences
				(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
		this.editor.clear();
		this.editor.commit();
	}
	
	public Boolean getPrefFirstLaunch(String firstLaunch) {
		//get string from prefs or return false
		return sharedPreferences.getBoolean("firstLaunch", false);
		
	}
	
	public void savePrefFirstLaunch(String firstLaunch, Boolean bool) {
		editor.putBoolean(firstLaunch, bool);
		editor.commit();
	}
	
	public Boolean getPrefDescription(String description) {
		//get string from prefs or return false
		return sharedPreferences.getBoolean("description", true);
		
	}
	
	public void savePrefDescription(String description, Boolean bool) {
		editor.putBoolean(description, bool);
		editor.commit();
	}
	
	public Boolean getPrefAutoSocial(String autoSocial) {
		//get string from prefs or return false
		return sharedPreferences.getBoolean("autoSocial", false);
		
	}
	
	public void savePrefAutoSocial(String autoSocial, Boolean bool) {
		editor.putBoolean(autoSocial, bool);
		editor.commit();
	}
	
	
} //end PreferenceHelper class
