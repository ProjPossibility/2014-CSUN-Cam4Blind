package com.camacc.helper;

import android.util.Log;

import com.camacc.activity.CameraActivity;


public class MessageHelper {
	private static String TAG = MessageHelper.class.getSimpleName();

	public static String metaString;
    // saves the last path in case the user exits the app

	/**
	 * Initializes textToSpeech and lays out the available options for the user,
	 * as well as creating a usage path for the user to take.
	 * 
	 * @param id
	 *            The program path that is being taken
	 */
	public static void ttsPath(final int id) {
		switch (id) {
			case 0: {
				Log.i(TAG, "ttsPath: case 0");
				metaString = "Welcome to CamAcc. Please say Picture to take a picture, "
						+ "or Detection to detect faces or Options to change "
						+ "settings or Help for a list of commands.";
				CameraActivity.speakText(metaString);
				break;
			}
			case 1: {
				Log.i(TAG, "ttsPath: case 1");
				metaString = "Your photo has been successfully saved. "
						+ "Say Picture at any time to take another picture, or say "
						+ "Detection to start face detection, or say "
						+ "Filter to apply a filter to the picture you just took.";
				CameraActivity.speakText(metaString);
				break;
			}
			case 2: {
				Log.i(TAG, "ttsPath: case 2");
				metaString = "What filter would you like to apply? "
						+ "Some examples are Sepia, Gray Scale or Emboss. "
						+ "You can even say Improve Quality to make the image better. "
						+ "To hear all filter commands say Help.";
				CameraActivity.speakText(metaString);
				break;
			}
			case 3: {
				Log.i(TAG, "ttsPath: case 3");
				metaString = "You can toggle these settings by saying advance mode "
						+ "On or Off. Auto detection On or Off. "
						+ " To exit options say Exit.";
				CameraActivity.speakText(metaString);
				break;
			}
			case 4: {
				Log.i(TAG, "ttsPath: case 4");
				metaString = "Please say a command.";
				CameraActivity.speakText(metaString);
				break;
			}
			case 5: {
				Log.i(TAG, "ttsPath: case 5");
				metaString = "Your photo has been saved.";
                CameraActivity.speakText(metaString);
				break;
			}
			case 6: {
				Log.i(TAG, "ttsPath: case 6");
				metaString = "What filter?";
				CameraActivity.speakText(metaString);
				break;
			}
			case 7: {
				Log.i(TAG, "ttsPath: case 7");
				metaString = "Advance mode is now turned on. To exit options "
						+ "say Exit.";
				CameraActivity.speakText(metaString);
				break;
			}
			case 8: {
				Log.i(TAG, "ttsPath: case 8");
				metaString = "Advance mode is now turned off. To exit options "
						+ "say Exit.";
				CameraActivity.speakText(metaString);
				break;
			}
			case 9: {
				Log.i(TAG, "ttsPath: case 9");
				metaString = "Auto social media is now turned on. To exit options"
						+ "say Exit.";
				CameraActivity.speakText(metaString);
				break;
			}
			case 10: {
				Log.i(TAG, "ttsPath: case 10");
				metaString = "Auto social media is now turned off. To exit options"
						+ "say Exit.";
				CameraActivity.speakText(metaString);
				break;
			}
			case 11: {
				Log.i(TAG, "ttsPath: case 11");
				metaString = "You must say Options first to toggle advance mode. "
						+ "Please say Options or a normal command.";
				CameraActivity.speakText(metaString);
				break;
			}
			case 12: {
				Log.i(TAG, "ttsPath: case 12");
				metaString = "You must say Options first to toggle auto social media. "
						+ "Please say Options or a normal command.";
				CameraActivity.speakText(metaString);
				break;
			}
			case 13: {
				Log.i(TAG, "ttsPath: case 13");
				metaString = "You are already in option settings. You can exit at any "
						+ "time by saying Exit.";
				CameraActivity.speakText(metaString);
				break;
			}
			case 14: {
				Log.i(TAG, "ttsPath: case 14");
				metaString = "You are not in option settings. If you want to enter"
						+ "option settings say Options, otherwise say a normal command.";
                CameraActivity.speakText(metaString);
                break;
			}
			case 15: {
				Log.i(TAG, "ttsPath: case 15");
				metaString = "To do this action, please leave options first by saying "
						+ "Leave Options.";
                CameraActivity.speakText(metaString);
				break;
			}
			case 16: {
				Log.i(TAG, "ttsPath: case 16");
				metaString = "Please say a filter command. Some examples are Sepia, "
						+ "Gray Scale or Emboss. You can even say Improve Quality to make "
						+ "the image better. To hear all filter commands say Help.";
				CameraActivity.speakText(metaString);
				break;
			}
			case 17: {
				Log.i(TAG, "ttsPath: case 17");
				metaString = "To do this action, please take a picture first.";
				CameraActivity.speakText(metaString);
				break;
			}
			case 18: {
				Log.i(TAG, "ttsPath: case 18");
				metaString = "Your selfie has been successfully saved. To apply a "
						+ "filter say Filter.";
				CameraActivity.speakText(metaString);
				break;
			}
			case 19: {
				Log.i(TAG, "ttsPath: case 19");
				metaString = "Advance mode is already turned on. To exit options"
						+ "say Exit.";
				CameraActivity.speakText(metaString);
				break;
			}
			case 20: {
				Log.i(TAG, "ttsPath: case 20");
				metaString = "Advance mode is already turned off. To exit options"
						+ "say Exit.";
				CameraActivity.speakText(metaString);
				break;
			}
			case 21: {
                /*
				Log.i(TAG, "ttsPath: case 21");
				metaString = "Auto social media is already turned on. To exit options"
						+ "say Exit.";
				TestActivity.speakText(metaString);
				//CameraActivity.speakText(metaString);*/
				break;
			}
			case 22: {

				Log.i(TAG, "ttsPath: case 22");
                metaString = "Your photo has been saved. "
                    + "Please say a command.";
				CameraActivity.speakText(metaString);
				break;
			}
			case 23: {
				Log.i(TAG, "ttsPath: case 23");
				metaString = "You must have face book installed to perform this action. "
						+ "Please say a command.";
				CameraActivity.speakText(metaString);
				break;
			}
			case 24: {
				Log.i(TAG, "ttsPath: case 24");
				metaString = "You must have twitter installed to perform this action. "
						+ "Please say a command.";
				CameraActivity.speakText(metaString);
				break;
			}
			case 25: {
				Log.i(TAG, "ttsPath: case 25");
				metaString = "You must have instegram installed to perform this action. "
						+ "Please say a command.";
				CameraActivity.speakText(metaString);
				break;
			}
			case 26: {
				Log.i(TAG, "ttsPath: case 26");
				metaString = "Your selfie has been successfully saved.";
				CameraActivity.speakText(metaString);
				break;
			}
            case 27: {
                Log.i(TAG, "ttsPath: case 27");
                metaString = "You must have face book installed to perform this action. ";
                CameraActivity.speakText(metaString);
                break;
            }
            case 28: {
                Log.i(TAG, "ttsPath: case 28");
                metaString = "You must have twitter installed to perform this action. ";
                CameraActivity.speakText(metaString);
                break;
            }
            case 29: {
                Log.i(TAG, "ttsPath: case 29");
                metaString = "You must have instegram installed to perform this action. ";
                CameraActivity.speakText(metaString);
                break;
            }
			case 97: {
				Log.i(TAG, "ttsPath: case 97");
				metaString = "Sorry, I did not get that. Can you please repeat "
						+ "your command?";
				CameraActivity.speakText(metaString);
				break;
			}
			case 98: {
				Log.i(TAG, "ttsPath: case 98");
				metaString = "Please take a picture first before applying filter.";
				CameraActivity.speakText(metaString);
				break;
			}
			case 99: {
				Log.i(TAG, "ttsPath: case 99");
				metaString = "Please say Picture, Detection " + "or Options.";
				CameraActivity.speakText(metaString);
				break;
			}

		}

	}
}
