package com.brainbox.tracking;

import android.location.Location;

public class LocationVO {

	public long id;
	public double latitude;
	public double longitude;
	public long locationTime;
	public double speed;
	public String provider;
	public float accuracy;
	public String address;

	public LocationVO() {

	}

	public LocationVO(Location location) {
		if (location != null) {
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			locationTime = location.getTime();
			speed = location.getSpeed();
			provider = location.getProvider();
			accuracy = location.getAccuracy();
		}
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public long getLocationTime() {
		return locationTime;
	}

	public void setLocationTime(long locationTime) {
		this.locationTime = locationTime;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	/**
	 * @return the provider
	 */
	public final String getProvider() {
		return provider;
	}

	/**
	 * @param provider
	 *            the provider to set
	 */
	public final void setProvider(String provider) {
		this.provider = provider;
	}

	/**
	 * @return the accuracy
	 */
	public final float getAccuracy() {
		return accuracy;
	}

	/**
	 * @param accuracy
	 *            the accuracy to set
	 */
	public final void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}

}
