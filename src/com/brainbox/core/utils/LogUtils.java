package com.brainbox.core.utils;

import org.acra.ACRA;

import android.util.Log;

public class LogUtils {
	public static int logLevel;
	public static String TAG;

	public static void error(String message, Exception e) {
		Log.e(TAG, message, e);
		ACRA.getErrorReporter().handleSilentException(e);
	}

	public static void error(Exception e) {
		String message = e.getMessage();
		if (message == null) {
			message = "";
		}
		Log.e(TAG, message, e);
		ACRA.getErrorReporter().handleSilentException(e);
	}

	public static void error(String message) {
		Log.e(TAG, message);
	}

	public static void debug(String message) {
		if (logLevel <= Log.DEBUG)
			Log.d(TAG, message);
	}

	public static void v(String message) {
		if (logLevel <= Log.VERBOSE)
			Log.v(TAG, message);
	}

	public static void v(Class<?> clazz, String message) {
		if (logLevel <= Log.VERBOSE)
			Log.v(TAG, clazz.getSimpleName() + " : " + message);
	}
}
