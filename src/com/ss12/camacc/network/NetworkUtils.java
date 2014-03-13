package com.ss12.camacc.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Deals with the network.
 */
public class NetworkUtils {
	
	public static String TAG = NetworkUtils.class.getSimpleName();
    /**
     * Checks to see if there's a stable network connection.
     *
     * @param context The context.
     * @return        True if there is a stable connection,
     *                false otherwise.
     */
	public static boolean isNetworkActive(Context context) {
		//check general connectivity
		ConnectivityManager conMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (conMgr != null) {
			if (conMgr.getActiveNetworkInfo() != null &&
					conMgr.getActiveNetworkInfo().isConnected() &&
					conMgr.getActiveNetworkInfo().isAvailable()) {
				Log.d(TAG, "Active Connection");
				return true;
			}
		}
		Log.d(TAG, "No Connection");
		return false;
	}//end isNetworkActive

    /**
     * Gets the IP address for the system.
     *
     * @param context The context.
     * @return        The IP address
     */
	public static String getIPAddr(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();

		String strIPAddr = String.format("%d.%d.%d.%d", 
			(ip & 0xff),
			(ip >> 8 & 0xff),
			(ip >> 16 & 0xff),
			(ip >> 24 & 0xff));
		Log.d(TAG, strIPAddr);
		return strIPAddr;
	}//end getIPAddress
	
	/**
	 * Get the network info.
     *
	 * @param context The context.
	 * @return        The network info
	 */
	public static NetworkInfo getNetworkInfo(Context context) {
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    return cm.getActiveNetworkInfo();
	}//end getNetworkInfo
	
	/**
	 * Check if there is any connectivity.
     *
	 * @param context The context.
	 * @return        True if there's a connection, false
     *                otherwise.
	 */
	public static boolean isConnected(Context context) {
	    NetworkInfo info = NetworkUtils.getNetworkInfo(context);
	    return (info != null && info.isConnected());
	}//end isConnected
	
	/**
	 * Check if there is any connectivity to a WIFI network.
     *
	 * @param context The context.
	 * @return        True if connected to wifi, false otherwise.
	 */
	public static boolean isConnectedWifi(Context context) {
	    NetworkInfo info = NetworkUtils.getNetworkInfo(context);
	    return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
	}
	
	/**
	 * Check if there is any connectivity to a mobile network.
     *
	 * @param context The context.
	 * @return        True if it's connected to a mobile network,
     *                false otherwise.
	 */
	public static boolean isConnectedMobile(Context context) {
	    NetworkInfo info = NetworkUtils.getNetworkInfo(context);
	    return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
	}//end isConnectedMobile
	
	/**
	 * Check if there is fast connectivity.
     *
	 * @param context The context.
	 * @return        True if the connection is fast and connected,
     *                false otherwise.
	 */
	public static boolean isConnectedFast(Context context) {
	    NetworkInfo info = NetworkUtils.getNetworkInfo(context);
	    return (info != null && info.isConnected() && NetworkUtils.isConnectionFast(info.getType(),info.getSubtype()));
	}//end isConnectedFast
	
	/**
	 * Check if there is fast connectivity.
     *
	 * @param type    Type of connection
	 * @param subType Network type
	 * @return        True if the connection is fast and connected,
     *                false otherwise.
	 */
	public static boolean isConnectionFast(int type, int subType) {
		if(type == ConnectivityManager.TYPE_WIFI) {
			Log.i(TAG, "WIFI Connection");
			return true;
		} else if(type == ConnectivityManager.TYPE_MOBILE) {
			switch(subType){
			case TelephonyManager.NETWORK_TYPE_1xRTT:
				Log.i(TAG, "WEAK: ~ 50-100 kbps");
				return false; // ~ 50-100 kbps
			case TelephonyManager.NETWORK_TYPE_CDMA:
				Log.i(TAG, "WEAK: ~ 14-64 kbps");
				return false; // ~ 14-64 kbps
			case TelephonyManager.NETWORK_TYPE_EDGE:
				Log.i(TAG, "WEAK: ~ 50-100 kbps");
				return false; // ~ 50-100 kbps
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
				Log.i(TAG, "STRONG: ~ 400-1000 kbps");
				return true; // ~ 400-1000 kbps
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
				Log.i(TAG, "STRONG: ~ 600-1400 kbps");
				return true; // ~ 600-1400 kbps
			case TelephonyManager.NETWORK_TYPE_GPRS:
				Log.i(TAG, "WEAK: ~ 100 kbps");
				return false; // ~ 100 kbps
			case TelephonyManager.NETWORK_TYPE_HSDPA:
				Log.i(TAG, "STRONG: ~ 2-14 Mbps");
				return true; // ~ 2-14 Mbps
			case TelephonyManager.NETWORK_TYPE_HSPA:
				Log.i(TAG, "STRONG: ~ 700-1700 kbps");
				return true; // ~ 700-1700 kbps
			case TelephonyManager.NETWORK_TYPE_HSUPA:
				Log.i(TAG, "STRONG: ~ 1-23 Mbps");
				return true; // ~ 1-23 Mbps
			case TelephonyManager.NETWORK_TYPE_UMTS:
				Log.i(TAG, "STRONG: ~ 400-7000 kbps");
				return true; // ~ 400-7000 kbps
			/*
			 * Above API level 7, make sure to set android:targetSdkVersion 
			 * to appropriate level to use these
			 */
			case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
				Log.i(TAG, "STRONG: ~ 1-2 Mbps");
				return true; // ~ 1-2 Mbps
			case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
				Log.i(TAG, "STRONG: ~ 5 Mbps");
				return true; // ~ 5 Mbps
			case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
				Log.i(TAG, "STRONG: ~ 10-20 Mbps");
				return true; // ~ 10-20 Mbps
			case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
				Log.i(TAG, "WEAK: ~25 kbps");
				return false; // ~25 kbps 
			case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
				Log.i(TAG, "STRONG: ~ 10+ Mbps");
				return true; // ~ 10+ Mbps
			// Unknown
			case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			default:
				return false;
			}
		} else {
			Log.d(TAG, "No Network Info");
			return false;
		}
	}//end isConnectionFast
}//end NetworkUtils