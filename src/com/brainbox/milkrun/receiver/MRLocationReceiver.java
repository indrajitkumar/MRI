package com.brainbox.milkrun.receiver;

import android.content.Context;
import android.location.Location;

import com.brainbox.core.constants.CommonConstants;
import com.brainbox.core.utils.LogUtils;
import com.brainbox.milkrun.helper.MilkRunDatabaseHelper;
import com.brainbox.tracking.LocationReceiver;

public class MRLocationReceiver extends LocationReceiver {

	MilkRunDatabaseHelper mrd;

	@Override
	protected void handleLocation(Context context, Location location) {
		super.handleLocation(context, location);
		try {
			if (location != null) {
				LogUtils.v("Location : " + location.getLatitude() + " ," + location.getLongitude());
				mrd = new MilkRunDatabaseHelper(context);
				mrd.insertValue(CommonConstants.LAT, String.valueOf(location.getLatitude()));
				mrd.insertValue(CommonConstants.LON, String.valueOf(location.getLongitude()));
				mrd.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
