package com.brainbox.core.utils;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.brainbox.core.utils.LogUtils;
import com.brainbox.shopclues.milkrun.R;
import com.brainbox.tracking.LocationVO;


public class LocationDAO extends SQLiteOpenHelper {
	public static final String COL_SERVER_UPDATED = "SERVER_UPDATED";
	public static final String COL_ID = "ID";
	public static final String COL_LAT = "LAT";
	public static final String COL_LNG = "LNG";
	public static final String COL_SPEED = "SPEED";
	public static final String COL_ACCURACY = "ACCURACY";
	public static final String COL_PROVIDER = "PROVIDER";
	public static final String COL_LOCATION_TIME = "LOCATION_TIME";
	public static final String COL_ADDRESS = "ADDRESS";
	protected Resources resources;

	public LocationDAO(Context context) {
		super(context, "LOCATION-DB", null, context.getResources().getInteger(R.integer.database_version));
		resources = context.getResources();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String query = resources.getString(R.string.create_table_location);
		LogUtils.debug("creating Location Table : " + query);
		db.execSQL(query);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		LogUtils.debug("Upgrading database");
		db.execSQL(resources.getString(R.string.drop_table_location));
		db.execSQL(resources.getString(R.string.create_table_location));
	}

	public void saveLocation(LocationVO location) {
		LogUtils.v("Inserting New location ");
		SQLiteDatabase db = this.getWritableDatabase();
		Object[] params =
		        new Object[] { location.getLatitude(), location.getLongitude(), location.getAccuracy(),
		                location.getProvider(), location.getLocationTime(), location.getAddress() };
		db.execSQL(resources.getString(R.string.sql_insert_location), params);
		db.close();
	}

	public ArrayList<LocationVO> getLocations() {
		LogUtils.v("Reading location log from DB");
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery(resources.getString(R.string.sql_select_location), null);
		ArrayList<LocationVO> locations = new ArrayList<LocationVO>();
		while (c.moveToNext()) {
			LocationVO location = new LocationVO();
			location.latitude = c.getDouble(c.getColumnIndex(COL_LAT));
			location.longitude = c.getDouble(c.getColumnIndex(COL_LNG));
			location.accuracy = c.getFloat(c.getColumnIndex(COL_ACCURACY));
			location.address = c.getString(c.getColumnIndex(COL_ADDRESS));
			location.id = c.getLong(c.getColumnIndex("ROW_ID"));
			location.locationTime = c.getLong(c.getColumnIndex(COL_LOCATION_TIME));
			location.provider = c.getString(c.getColumnIndex(COL_PROVIDER));
			location.speed = c.getDouble(c.getColumnIndex(COL_SPEED));
			locations.add(location);
		}
		c.close();
		db.close();
		return locations;
	}

	public void deleteLocation(LocationVO location) {
		LogUtils.v("Deleting location ");
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL(resources.getString(R.string.sql_delete_location), new Object[] { location.id });
		db.close();
		LogUtils.v("Deleted location ");
	}
}