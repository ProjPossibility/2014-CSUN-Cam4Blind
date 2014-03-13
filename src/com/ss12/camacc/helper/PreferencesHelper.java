package com.ss12.camacc.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Aids in the storing of user preferences.
 */
public class PreferencesHelper {

	public static String TAG = PreferencesHelper.class.getSimpleName();
    /**
     * SharedPreferences object.
     */
	private SharedPreferences sharedPreferences;
    /**
     * Editor object.
     */
	private Editor editor;
    /**
     * Name of the file.
     */
	private static final String APP_SHARED_PREFS = 
			PreferencesHelper.class.getSimpleName();

    /**
     * Stores preferences.
     *
     * @param context The context.
     */
	public PreferencesHelper(Context context) {
		this.sharedPreferences = context.getSharedPreferences
				(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
		this.editor = sharedPreferences.edit();
	}//end PreferencesHelper

    /**
     * Clears stored preferences.
     *
     * @param context The context.
     */
	public void clearPreferences(Context context) {
		this.sharedPreferences = context.getSharedPreferences
				(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
		this.editor.clear();
		this.editor.commit();
	}//end clearPreferences

    /**
     * Get string from prefs or return false.
     *
     * @param firstLaunch The first launch.
     * @return            String or returns false
     */
	public Boolean getPrefFirstLaunch(String firstLaunch) {
		//get string from prefs or return false
		return sharedPreferences.getBoolean("firstLaunch", false);
		
	}//end getPrefFirstLaunch

    /**
     * Saves the preferences from the first launch.
     *
     * @param firstLaunch The first launch.
     * @param bool        A Boolean value for the first launch.
     */
	public void savePrefFirstLaunch(String firstLaunch, Boolean bool) {
		editor.putBoolean(firstLaunch, bool);
		editor.commit();
	}//end savePrefFirstLaunch

    /**
     * Get string from prefs or return false.
     *
     * @param description The description.
     * @return            String or returns false
     */
	public Boolean getPrefDescription(String description) {
		//get string from prefs or return false
		return sharedPreferences.getBoolean("description", true);
		
	}//end getPrefDescription

    /**
     * Saves the preference description.
     *
     * @param description The description.
     * @param bool        A Boolean value for the description.
     */
	public void savePrefDescription(String description, Boolean bool) {
		editor.putBoolean(description, bool);
		editor.commit();
	}//end savePrefDescription

    /**
     * Get string from prefs or return false.
     *
     * @param autoSocial The autoSocial status.
     * @return           String or returns false
     */
	public Boolean getPrefAutoSocial(String autoSocial) {
		//get string from prefs or return false
		return sharedPreferences.getBoolean("autoSocial", false);
		
	}//end getPrefAutoSocial

    /**
     * Saves the preference for autoSocial.
     *
     * @param autoSocial The autoSocial status.
     * @param bool       A Boolean value for the AutoSocial.
     */
	public void savePrefAutoSocial(String autoSocial, Boolean bool) {
		editor.putBoolean(autoSocial, bool);
		editor.commit();
	}//end savePrefAutoSocial
	
	
} //end PreferenceHelper class
