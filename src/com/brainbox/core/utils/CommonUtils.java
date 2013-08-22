package com.brainbox.core.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Environment;
import android.widget.Toast;


import com.brainbox.core.config.BatteryInfo;
import com.brainbox.core.config.SystemConfig;
import com.brainbox.core.constants.CommonConstants;
import com.brainbox.core.constants.ErrorConstants;
import com.brainbox.core.constants.IntentActions;
import com.brainbox.core.constants.RequestCodes;
import com.brainbox.core.helper.DatabaseHelper;
import com.brainbox.core.http.HttpRequestHelperImpl;
import com.brainbox.mobile.exception.SystemException;
import com.brainbox.shopclues.milkrun.R;

public class CommonUtils {
	private static final String NUMBER_FORMAT = "-?\\d+(\\.\\d+)?";
	private static Geocoder gc;

	public static Properties loadConfig(InputStream is) throws SystemException {
		String dir = SystemConfig.APP_DIR;
		Properties props = new Properties();
		File configFile = new File(dir, "config.properties");
		try {
			if (!configFile.exists()) {
				configFile.createNewFile();
				BufferedWriter bos = new BufferedWriter(new FileWriter(
						configFile));
				BufferedReader bis = new BufferedReader(new InputStreamReader(
						is));
				String line = null;
				while ((line = bis.readLine()) != null) {
					bos.write(line + "\n");
				}
				bis.close();
				bos.flush();
				bos.close();
			}
			props.load(new FileInputStream(configFile));
		} catch (FileNotFoundException e) {
			LogUtils.error(configFile.getAbsolutePath() + " Does not exit");
			throw new SystemException(ErrorConstants.IO_ERROR,
					"file config.properties does not exit in Dir " + dir);
		} catch (IOException e) {
			throw new SystemException(ErrorConstants.IO_ERROR);
		}
		return props;
	}

	public static Address getAddressFromLatLng(double lat, double lng,
			Context context) {
		Address address = null;
		try {
			if (gc == null)
				gc = new Geocoder(context, new Locale("en", "IN"));
			List<Address> addressList = gc.getFromLocation(lat, lng, 1);
			if (addressList != null && addressList.size() != 0) {
				address = addressList.get(0);
			} else {
				LogUtils.debug("Can not get Address from Geocoder");
			}
		} catch (Exception e) {
			LogUtils.debug("Can not get Address from Geocoder");
		}
		return address;
	}

	public static Address getAddressFromLocation(Location loc, Context context) {
		if (loc == null) {
			return null;
		}
		return getAddressFromLatLng(loc.getLatitude(), loc.getLongitude(),
				context);
	}

	public static void updateBatteryInfo(Context context) {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent battery = context.registerReceiver(null, ifilter);
		int status = battery.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		BatteryInfo.isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
				|| status == BatteryManager.BATTERY_STATUS_FULL;
		BatteryInfo.level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		BatteryInfo.scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		int chargePlug = battery.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		BatteryInfo.usbCharging = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
		BatteryInfo.acCharging = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
	}

	public static String getErrorMessage(String errorCode, Context context) {
		String message = null;
		if (errorCode == ErrorConstants.NETWORK_ERROR) {
			message = context.getString(R.string.err_nw);
		} else if (errorCode == ErrorConstants.INVALID_LOGIN) {
			message = context.getString(R.string.err_invalid_login);
		} else if (errorCode == ErrorConstants.CONNECTION_TIMEOUT) {
			message = context.getString(R.string.err_conn_timeout);
		} else if (errorCode == ErrorConstants.DATA_ERROR) {
			message = context.getString(R.string.err_data);
		} else {
			message = context.getString(R.string.err_default);
		}
		return message;
	}

	/**
	 * This method installs the application from the given url.
	 * 
	 * @param apkurl
	 *            the url of package,
	 */
	public static void updateAPK(Context context, String apkurl) {
		try {
			String fileName = "tmp.apk";
			DatabaseHelper db = new DatabaseHelper(context);
			FileOutputStream fos = context.openFileOutput(fileName,
					Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
			InputStream is = new HttpRequestHelperImpl(context, db)
					.executeHttpGet(apkurl);
			byte[] buffer = new byte[1024];
			int len1 = 0;
			while ((len1 = is.read(buffer)) != -1) {
				fos.write(buffer, 0, len1);
			}
			fos.close();
			is.close();
			File fileLocation = new File(context.getFilesDir(), fileName);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setDataAndType(Uri.fromFile(fileLocation),
					"application/vnd.android.package-archive");
			context.startActivity(intent);
			db.insertValue(CommonConstants.LATEST_APP, CommonConstants.YES);
		} catch (Exception e) {
			Toast.makeText(context, "Update error!", Toast.LENGTH_LONG).show();
		}
	}

	public static void requestSign(Activity activity) {
		Intent intent = new Intent(activity.getPackageName()
				+ IntentActions.ACTION_SIGN);
		activity.startActivityForResult(intent, RequestCodes.REQ_SIGN);
	}

	public static boolean isNumeric(String str) {
		return str.matches(NUMBER_FORMAT);
	}

	public static String getVersion() {
		String version = "NA";
		PackageInfo packageInfo;
		try {
			packageInfo = SystemConfig.packageInfo;
			version = "v" + packageInfo.versionName + " b"
					+ SystemConfig.buildNumber;
		} catch (Exception e) {
			// LogUtils.error("Error while gettig package info", e);
		}
		return version;
	}

	public static void createShortcut(Context context) {
		Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
		Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
				context.getString(R.string.app_name));
		context.sendBroadcast(intent);
	}

	public static AlertDialog createAlertDialog(Activity activity,
			String message, String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage(message);
		builder.setNeutralButton("OK", null);
		return builder.create();
	}

	public static boolean makedirs(String dir) {
		File tempdir = new File(dir);
		if (tempdir.exists() && !tempdir.isDirectory()) {
			tempdir.delete();
		}
		if (!tempdir.exists()) {
			tempdir.mkdirs();
			LogUtils.debug("Dir created");
		}
		return (tempdir.isDirectory());
	}

	public static String formatDateTime(Date date) {
		String format = "yyyy-MM-dd HH:mm:ss:SSS";
		String newDate = new SimpleDateFormat(format).format(date);
		return newDate;
	}

	public static String moveFile(String fileName, String toDir) {
		boolean move = false;
		File newFile = null;

		if (fileName != null && toDir != null) {
			File file = new File(fileName);
			String dest = Environment.getExternalStorageDirectory()
					+ File.separator + toDir;
			makedirs(dest);
			if (file.exists()) {
				newFile = new File(dest, file.getName());
				move = file.renameTo(newFile);
			}
		}
		if (move && newFile != null) {
			return newFile.getAbsolutePath();
		}
		return null;
	}

}
