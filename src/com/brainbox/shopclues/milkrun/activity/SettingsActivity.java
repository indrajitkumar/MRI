package com.brainbox.shopclues.milkrun.activity;

import com.brainbox.shopclues.milkrun.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class SettingsActivity extends MilkRunActivity {

	public static final String MILEAGE_TYPE = "MILEAGE_TYPE";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.settings);
		super.onCreate(savedInstanceState);

	}

	public void recordLocation(View v) {
		startActivity(new Intent(this, MerchantLocationActivity.class));
	}

}