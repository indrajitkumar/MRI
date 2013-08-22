package com.brainbox.shopclues.milkrun.activity;

import static com.brainbox.core.config.SystemConfig.connectivity;

import java.util.Date;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.TextView;

import com.brainbox.core.config.SystemConfig;
import com.brainbox.core.constants.CommonConstants;
import com.brainbox.core.constants.IntentActions;
import com.brainbox.core.helper.DatabaseHelper;
import com.brainbox.core.http.HttpRequestHelperImpl;
import com.brainbox.core.utils.LogUtils;
import com.brainbox.core.widget.StateButton;
import com.brainbox.gesture.BBSimpleOnGestureListener;
import com.brainbox.gesture.SwipeTypeEnum;
import com.brainbox.shopclues.milkrun.R;

public abstract class ParentActivity extends ActivityGroup {
	protected static final int DIALOG_LOGOUT = 0;
	protected Dialog dialog;
	protected static AlertDialog aDialog;
	protected static ProgressDialog pDialog;
	protected InputMethodManager inputManager;
	protected DatabaseHelper db;
	protected String alertMessage = "";
	protected Activity activity;
	private GestureDetector gestureDetector;
	public static HttpRequestHelperImpl httpRequestHelper;
	LocationManager locationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// groupContext = this;
		activity = this;
		SimpleOnGestureListener simpleOnGestureListener = new BBSimpleOnGestureListener(this);
		gestureDetector = new GestureDetector(simpleOnGestureListener);
		inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
	}

	public void back(View v) {
		finish();
	}

	public void enableGPS(View v) {
		try {
			if (!SystemConfig.locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
				Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(myIntent);
			}
		} catch (Exception e) {
			LogUtils.error(getClass().getSimpleName() + " : " + "Exception while changing State", e);
		}
	}

	public void enableData(View v) {
		connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity.getActiveNetworkInfo() == null) {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setClassName("com.android.phone", "com.android.phone.Settings");
			startActivity(intent);
		}
	}

	public void onHome(View v) {
		Intent intent = new Intent(getPackageName() + IntentActions.ACTION_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	public void logout(View v) {
		createConfirmDialog(getString(R.string.logout_confirm), getString(R.string.logout_title), logoutConfirm);
	}

	public void stopServices() {
	}

	protected void createConfirmDialog(String message, String title, OnClickListener ocl) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setCancelable(false);
		builder.setTitle(title);
		builder.setPositiveButton(R.string.label_ok, ocl);
		builder.setNegativeButton(R.string.label_cancel, null);
		aDialog = builder.show();
	}

	@Deprecated
	/**
	 * User showAlertDialog instead
	 */
	protected void createAlertDialog(String message, String title, OnClickListener ocl) {
		showAlertDialog(message, title, ocl);
	}

	public void showAlertDialog(String message, String title, OnClickListener ocl) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setCancelable(false);
		builder.setTitle(title);
		builder.setPositiveButton(R.string.label_ok, ocl);
		aDialog = builder.show();
	}

	protected OnClickListener logoutConfirm = new OnClickListener() {

		public void onClick(DialogInterface arg0, int arg1) {
			clean();
		}

	};

	protected void clean() {
		try {
			createAlertDialog(getString(R.string.logout_message), null, closeApp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected OnClickListener closeApp = new OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			stopServices();
			//unregisterReceiver(mConnReceiver);
			for (String db : databaseList()) {
				deleteDatabase(db);
			}
			finish();
		}
	};

	@Override
	protected void onDestroy() {
		if (db != null) {
			db.close();
		}
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		LogUtils.debug("Unregistering Receivers");
		try {
			unregisterReceiver(mConnReceiver);
		} catch (Exception e) {
			LogUtils.error("Error while Unregistering Receivers", e);
		}
		if (dialog != null)
			dialog.dismiss();
		if (aDialog != null)
			aDialog.dismiss();
		if (db != null) {
			db.close();
		}
	}

	protected BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BBSimpleOnGestureListener.SWIPE_ACTION.equals(action)) {
				SwipeTypeEnum swipeType = (SwipeTypeEnum) intent
						.getSerializableExtra(BBSimpleOnGestureListener.SWIPE_TYPE);
				swipe(swipeType);
			} else {
				Address address = intent.getParcelableExtra(CommonConstants.ADDRESS);
				if (address != null && address.getAddressLine(0) != null) {
					DatabaseHelper db = new DatabaseHelper(context);
					db.insertValue(CommonConstants.ADDRESS, address.getAddressLine(0));
					db.close();
				}
				handleBrodcast();
			}
		}
	};

	protected void onResume() {
		registerReceiver(mConnReceiver, new IntentFilter(getString(R.string.message_action)));
		registerReceiver(mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		registerReceiver(mConnReceiver, new IntentFilter(BBSimpleOnGestureListener.SWIPE_ACTION));
		super.onResume();
	};

	public void refreshScreen() {
		try {
			DatabaseHelper db = new DatabaseHelper(this);
			TextView tv = (TextView) findViewById(R.id.address);
			if (tv != null) {

				if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
						|| locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

					String address = db.getValue(CommonConstants.ADDRESS);
					if (address != null && !"".equals(address)) {
						tv.setText(address);
					} else {
						tv.setText("Getting adress from your location, Please wait..");
					}
				} else {
					tv.setText("Location update is disabled. Please enable!");
				}
			}
			db.close();
			tv = (TextView) findViewById(R.id.version);
			if (tv != null) {
				tv.setText(db.getValue(CommonConstants.APP_VERSION));
			}
			StateButton vn = (StateButton) findViewById(R.id.nw);
			if (vn != null && SystemConfig.connectivity != null)
				vn.setOn(SystemConfig.connectivity.getActiveNetworkInfo() != null);
		} catch (Exception e) {
			LogUtils.error("Error while refrshing", e);
		}
	}

	public void call(View v) {
		Intent callIntent = new Intent(Intent.ACTION_DIAL);
		callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		callIntent.setData(Uri.parse("tel:" + ((TextView) v).getText()));
		startActivity(callIntent);
	}

	public void handleBrodcast() {
		try {
			refreshScreen();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
		// when dialog box is closed, below method will be called.
		public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
			setDate(new Date(selectedYear - 1900, selectedMonth, selectedDay));
		}
	};

	protected void setDate(Date date) {
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		return super.dispatchTouchEvent(event);
	}

	protected void swipe(SwipeTypeEnum swipe) {
	}
}
