package com.ss12.camacc.helper;

/**
 * Aids in Voice Engine control.
 */
public class VoiceEngineHelper {
    /**
     * A boolean that is set to true when a voice commands can be processed.
     */
    public static boolean isVoiceController = false;
	
	/**
	 * @param isVoiceController the isVoiceController to set
	 */
	public static void setVoiceController(boolean isVoiceController) {
		VoiceEngineHelper.isVoiceController = isVoiceController;
	}
	
    /**
	 * @return the isVoiceController
	 */
	public static boolean getVoiceController() {
		return isVoiceController;
	}
	

}
