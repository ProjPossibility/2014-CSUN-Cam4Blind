package com.ss12.camacc.helper;

public class VoiceEngineHelper {
	
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
