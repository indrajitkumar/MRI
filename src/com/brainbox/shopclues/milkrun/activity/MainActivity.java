package com.brainbox.shopclues.milkrun.activity;

import static com.brainbox.core.config.SystemConfig.appVersion;
import static com.brainbox.core.config.SystemConfig.connectivity;
import static com.brainbox.core.config.SystemConfig.tManager;
import static com.brainbox.core.constants.CommonConstants.APP_VERSION;
import static com.brainbox.core.constants.CommonConstants.BUILD;
import static com.brainbox.core.constants.CommonConstants.IMEI;
import static com.brainbox.core.constants.CommonConstants.LOGGED_IN;
import static com.brainbox.core.constants.CommonConstants.SERVER_URL;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;

import com.brainbox.core.config.SystemConfig;
import com.brainbox.core.constants.IntentActions;
import com.brainbox.core.helper.DatabaseHelper;
import com.brainbox.core.utils.CommonUtils;
import com.brainbox.core.utils.LogUtils;
import com.brainbox.shopclues.milkrun.R;

public class MainActivity extends Activity {
	protected static final String YES = "Y";
	protected DatabaseHelper db;
	protected Activity activity;
	protected AlertDialog aDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		activity = this;
		if (db == null)
			db = new DatabaseHelper(this);
		new InitializeAppTask().execute();
	}

	public void startServices() {
	}

	public void initConstants() {
		LogUtils.TAG = getString(R.string.tag);
		connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		LogUtils.logLevel = getResources().getInteger(R.integer.log_level);
		SystemConfig.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		SystemConfig.APP_DIR = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/" + getString(R.string.app_name);
		try {
			SystemConfig.buildNumber = getResources().getInteger(
					R.integer.buildnum);
			SystemConfig.packageInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			appVersion = "v" + SystemConfig.packageInfo.versionName; // + " b" +
																		// SystemConfig.buildNumber;
			db.insertValue(APP_VERSION, appVersion);
			db.insertValue(BUILD, String.valueOf(SystemConfig.buildNumber));
			CommonUtils.makedirs(SystemConfig.APP_DIR);

		} catch (Exception e) {
			LogUtils.error("Error while seting package", e);
		}
	}

	public void init() {
		db.insertValue(SERVER_URL, getString(R.string.server_url));
		db.insertValue(IMEI, tManager.getDeviceId());

		// try {
		// Properties props = CommonUtils.loadConfig(getAssets().open(
		// "config.properties"));
		// for (String name : props.stringPropertyNames()) {
		// String value = props.getProperty(name);
		// db.insertValue(name, value);
		// LogUtils.v("Property : " + name + " Value : " + value);
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		if (YES.equals(db.getValue(LOGGED_IN))
				|| !getResources().getBoolean(R.bool.login_enabled)) {
			startActivity(new Intent(SystemConfig.packageInfo.packageName
					+ IntentActions.ACTION_HOME));
		} else {
			startActivity(new Intent(SystemConfig.packageInfo.packageName
					+ IntentActions.ACTION_LOGIN));
		}
		finish();
	}

	class InitializeAppTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			preProcess();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			initConstants();
			startServices();
			init();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			postProcess();
			super.onPostExecute(result);
		}

	}

	protected void postProcess() {
		// TODO Auto-generated method stub

	}

	protected void preProcess() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (aDialog != null) {
			aDialog.dismiss();
		}
	}
}
