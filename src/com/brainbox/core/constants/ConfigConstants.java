package com.brainbox.core.constants;

import android.location.LocationManager;

import com.brainbox.core.helper.DatabaseHelper;
import com.brainbox.core.http.HttpRequestHelperImpl;

/**
 * @author Pramod
 * 
 */
public class ConfigConstants {
	
	public static String TAG = "TAG";
	public static LocationManager locationManager;
	public static final String BROADCAST_EMAIL = "BROADCAST_EMAIL";
	public static final String ADDRESS = "ADDRESS";
	public static final String LONGITUDE = "LONGITUDE";
	public static final String LATITIDE = "LATITIDE";
	public static final String IMEI = "IMEI";
	public static final String TIMER_PERIOD = "TIMER_PERIOD";

	// IntentActions
	public static final String MESSAGE_ACTION = "MESSAGE_ACTION";
	public static final int DATABASE_VERSION = 2;
	public static DatabaseHelper DATABASE_HELPER = null;
	public static HttpRequestHelperImpl httpRequestHelper;

}
