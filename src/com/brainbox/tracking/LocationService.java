package com.brainbox.tracking;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import com.brainbox.core.config.SystemConfig;
import com.brainbox.core.utils.LocationDAO;
import com.brainbox.core.utils.LogUtils;
import com.brainbox.mobile.exception.SystemException;

import com.brainbox.shopclues.milkrun.R;


public class LocationService extends IntentService {
	public static String LOCATION_READY = "com.brainbox.tracking.LOCATION_READY";
	private static final String GPS_MIN_DIST_KEY = "GPS_MIN_DIST";
	private static final String GPS_POLL_KEY = "GPS_POLL";
	private static final String NW_POLL_KEY = "NW_POLL";
	private static float NW_MIN_DIST = 0;
	private static float GPS_MIN_DIST = 0;
	private static long GPS_POLL = 0;
	private long NW_POLL = 0;
	private static PendingIntent pendingIntent;
	protected static LocationDAO dao;
	private Context context;
	private Timer timer;
	private long period;

	@Override
	public IBinder onBind(Intent intent) {
		LogUtils.debug(LocationService.class.getSimpleName() + " : Bind");
		return (null);
	}

	public LocationService() {
		super("BrainBox-LocationService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		LogUtils.debug(getClass().getSimpleName() + " : " + "Initializing Location Service");
		context = getApplicationContext();
		timer = new Timer();
		LOCATION_READY = getString(R.string.location_intent);
		GPS_MIN_DIST = getResources().getInteger(R.integer.gps_min_dist);
		NW_MIN_DIST = getResources().getInteger(R.integer.nw_min_dist);
		GPS_POLL = getResources().getInteger(R.integer.gps_poll);
		NW_POLL = getResources().getInteger(R.integer.nw_poll);
		if (getResources().getBoolean(R.bool.location_log)) {
			period = getResources().getInteger(R.integer.timer_poll)*1000;
			timer.schedule(task, 0, period);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtils.debug(getClass().getSimpleName() + " : " + "Start Location Service");
		GPS_MIN_DIST = intent.getFloatExtra(GPS_MIN_DIST_KEY, GPS_MIN_DIST)*1000;
		GPS_POLL = intent.getLongExtra(GPS_POLL_KEY, GPS_POLL)*1000;
		NW_POLL = intent.getLongExtra(NW_POLL_KEY, NW_POLL)*1000;
		SystemConfig.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		startTracking();
		return (START_STICKY);
	}

	@Override
	public void onDestroy() {
		Log.i(LocationService.class.getSimpleName(), "Location Service Destroyed");
		timer.cancel();
	}

	private void startTracking() {
		try {
			Intent intent = new Intent(LOCATION_READY);
			pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent,
					PendingIntent.FLAG_CANCEL_CURRENT);
			LogUtils.debug(getClass().getSimpleName() + " : " + "Registering Location updates from GPS");
			SystemConfig.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_POLL, GPS_MIN_DIST,
					pendingIntent);
			SystemConfig.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, NW_POLL, NW_MIN_DIST,
					pendingIntent);
		} catch (Exception e) {
			LogUtils.error(getClass().getSimpleName() + " : " + "Exception while setting up Location Tracker ", e);
		}
	}

	private TimerTask task = new TimerTask() {
		@Override
		public void run() {

			postLocations();

		}

	};

	protected void postLocations() {
		dao = new LocationDAO(context);
		ArrayList<LocationVO> locations = dao.getLocations();
		for (LocationVO location : locations) {
			try {
				LogUtils.v("Posting Location to server");
				String res = postLocation(location);
				dao.deleteLocation(location);
			} catch (SystemException e) {
				LogUtils.error("Error while posting location on server", e);
			}
		}
	}

	protected String postLocation(LocationVO location) throws SystemException {
		LocationServerUtils http = new LocationServerUtils(context);
		String res = http.postLocation(location);
		LogUtils.v("Posting Location response : " + res);
		return res;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		LogUtils.debug("Handle INTENT");
	}
}