package com.brainbox.tracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.brainbox.core.config.SystemConfig;
import com.brainbox.core.constants.CommonConstants;
import com.brainbox.core.utils.CommonUtils;
import com.brainbox.core.utils.LogUtils;
import com.brainbox.shopclues.milkrun.R;


public class LocationReceiver extends BroadcastReceiver {
	protected static Intent messageIintent = new Intent();
	protected LocationDAO dao;

	@Override
	public void onReceive(Context context, Intent intent) {
		// Do this when the system sends the intent
		LogUtils.v(getClass().getSimpleName() + " : "
				+ "New Broadcast Message !!");
		Bundle b = intent.getExtras();
		if (b == null) {
			return;
		}
		Location location = (Location) b
				.get(LocationManager.KEY_LOCATION_CHANGED);
		if (location != null) {
			handleLocation(context, location);
		}
	}

	protected void handleLocation(Context context, Location location) {
		SystemConfig.location = location;
		Address address = CommonUtils.getAddressFromLocation(location, context);
		if (context.getResources().getBoolean(R.bool.location_log)) {
			LocationVO locationVO = new LocationVO(location);
			if (address != null) {
				locationVO.setAddress(address.getAddressLine(0));
			}
			saveLocation(context, locationVO);

		}
		messageIintent.setAction(context.getResources().getString(
				R.string.message_action));
		messageIintent.putExtra(CommonConstants.ADDRESS, address);
		context.sendBroadcast(messageIintent);

	}

	protected void saveLocation(Context context, LocationVO locationVO) {
		dao = new LocationDAO(context);
		dao.saveLocation(locationVO);
		dao.close();

	}

	private static final int TWO_MINUTES = 1000 * 60 * 2;

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 */
	protected boolean isBetterLocation(Location location,
			Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}
		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;
		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}
		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;
		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());
		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate
				&& isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
}
