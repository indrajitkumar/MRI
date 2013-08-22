package com.brainbox.core.config;

public class BatteryInfo {
	public static int level;
	public static int scale = 100;
	public static boolean isCharging;
	public static boolean usbCharging;
	public static boolean acCharging;

	public static float getBatteryPercentage() {
		return level * 100.0f / (float) scale;
	}
}
