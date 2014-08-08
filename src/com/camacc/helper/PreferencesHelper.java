package com.camacc.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferencesHelper {

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
	
	public Boolean getPrefFirstLaunch(String strFirstLaunch) {
		//get string from prefs or return false
		return sharedPreferences.getBoolean("strFirstLaunch", false);
		
	}
	
	public void savePrefFirstLaunch(String strFirstLaunch, Boolean bool) {
		editor.putBoolean(strFirstLaunch, bool);
		editor.commit();
	}

//	public Boolean getPrefDescription(String strDescription) {
//		//get string from prefs or return false
//		return sharedPreferences.getBoolean("strDescription", false);
//		
//	}
//	
//	public void savePrefDescription(String strDescription, Boolean bool) {
//		editor.putBoolean(strDescription, bool);
//		editor.commit();
//	}

	public Boolean getPrefVoice(String strVoice) {
		//get string from prefs or return false
		return sharedPreferences.getBoolean("strVoice", true);
		
	}
	
	public void savePrefVoice(String strVoice, Boolean bool) {
		editor.putBoolean(strVoice, bool);
		editor.commit();
	}	
	
	public Boolean getPrefAdvance(String strAdvance) {
		//get string from prefs or return false
		return sharedPreferences.getBoolean("strAdvance", false);
		
	}
	
	public void savePrefAdvance(String strAdvance, Boolean bool) {
		editor.putBoolean(strAdvance, bool);
		editor.commit();
	}
	
	public Boolean getPrefAutoDetect(String strAutoDetect) {
		//get string from prefs or return false
		return sharedPreferences.getBoolean("strAutoDetect", false);
		
	}
	
	public void savePrefAutoDetect(String strAutoDetect, Boolean bool) {
		editor.putBoolean(strAutoDetect, bool);
		editor.commit();
	}

} //end PreferenceHelper class
