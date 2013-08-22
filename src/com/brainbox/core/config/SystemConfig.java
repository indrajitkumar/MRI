package com.brainbox.core.config;

import android.content.pm.PackageInfo;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;

import com.brainbox.core.http.HttpRequestHelperImpl;

public class SystemConfig {

	public static ConnectivityManager connectivity ;
	public static TelephonyManager tManager;
	public static HttpRequestHelperImpl httpRequestHelper;
	public static PackageInfo packageInfo;
	public static int buildNumber;
	public static String appVersion;
	public static Location location;
	public static LocationManager locationManager;
	public static final int PORT = 80;
	public static final long TIMER_PERIOD = 1 * 60 * 1000;
	public static int CONNECTION_TIMEOUT = 30000;
	public static int SOCKET_TIMEOUT = 30000;

	public static String API_HOST;
	public static String API_PATH;
	public static String APP_DIR;

}
