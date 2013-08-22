package com.brainbox.tracking;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;

import com.brainbox.core.constants.CommonConstants;
import com.brainbox.core.helper.DatabaseHelper;
import com.brainbox.core.http.HttpRequestHelperImpl;
import com.brainbox.mobile.exception.SystemException;
import com.brainbox.shopclues.milkrun.R;
import com.brainbox.tracking.LocationVO;


public class LocationServerUtils extends HttpRequestHelperImpl {
	private static final String ACTION_UPDATE_LOCATION = "updateLocation";
	private static final String PARAM_ADD = "add";
	private static final String PARAM_SPEED = "speed";
	private static final String PARAM_TIME = "time";
	private static final String PARAM_PROVIDER = "provider";
	private static final String PARAM_LON = "lon";
	private static final String PARAM_LAT = "lat";
	private static final String PARAM_ACCURACY = "accuracy";

	public LocationServerUtils(Context ctx) {
		super(ctx, new DatabaseHelper(ctx));
	}

	public String postLocation(LocationVO location) throws SystemException {

		if (location == null) {
			return "ERROR";
		}
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair(PARAM_ACCURACY, String
				.valueOf(location.getAccuracy())));
		pairs.add(new BasicNameValuePair(PARAM_LAT, String.valueOf(location
				.getLatitude())));
		pairs.add(new BasicNameValuePair(PARAM_LON, String.valueOf(location
				.getLongitude())));
		pairs.add(new BasicNameValuePair(PARAM_PROVIDER, location.getProvider()));
		pairs.add(new BasicNameValuePair(PARAM_TIME, String.valueOf(location
				.getLocationTime())));
		pairs.add(new BasicNameValuePair(PARAM_SPEED, String.format("%2f",
				location.getSpeed() * 3.6)));
		pairs.add(new BasicNameValuePair(PARAM_ADD, location.address));
		pairs.add(new BasicNameValuePair(PARAM_ACTION, ACTION_UPDATE_LOCATION));
		InputStream is = executeHttpPost(pairs);
		return readResponse(is);
	}
	public String getHost() {
		return context.getString(R.string.location_api);
	}

}