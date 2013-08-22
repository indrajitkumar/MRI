package com.brainbox.shopclues.milkrun.activity;

import android.content.Intent;
import android.os.Bundle;


import com.brainbox.core.config.SystemConfig;
import com.brainbox.core.constants.ConfigConstants;
import com.brainbox.milkrun.constants.MilkRunConstants;
import com.brainbox.milkrun.helper.MilkRunDatabaseHelper;
import com.brainbox.milkrun.helper.MilkRunHttpRequestHelper;
import com.brainbox.tracking.LocationService;

public class MilkRunMainActivity extends MainActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ConfigConstants.DATABASE_HELPER = db = new MilkRunDatabaseHelper(this);
		SystemConfig.httpRequestHelper = MilkRunConstants.httpRequestHelper = new MilkRunHttpRequestHelper(this, db);
		super.onCreate(savedInstanceState);
	}

	public void startServices() {
		Intent locationIntent = new Intent(this, LocationService.class);
		startService(locationIntent);
	}

}
