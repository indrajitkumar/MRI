package com.brainbox.shopclues.milkrun.activity;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.brainbox.core.utils.LogUtils;
import com.brainbox.core.vo.Merchant;

import com.brainbox.milkrun.constants.MilkRunConstants;
import com.brainbox.shopclues.milkrun.R;
import com.brainbox.tracking.LocationService;

import com.brainbox.vo.enums.ActionEnum;

public class MRDashboardActivity extends MilkRunActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.home);
		super.onCreate(savedInstanceState);
		((TextView) findViewById(R.id.header_title)).setText(R.string.app_name);
		checkMileage();
	}

	private void checkMileage() {
		Boolean mileage = Boolean.valueOf(db.getValue(MilkRunConstants.MILEAGE_UPDATED));
		if (!mileage) {
			Intent intent = new Intent(this, MileageActivity.class);
			intent.putExtra(MileageActivity.MILEAGE_TYPE, "CHECK_IN");
			startActivityForResult(intent, REQUEST_MILEAGE_CHECK_IN);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_MILEAGE_CHECK_IN:
				db.insertValue(MilkRunConstants.MILEAGE_UPDATED, String.valueOf(true));
				break;
			case REQUEST_MILEAGE_CHECK_OUT:
				clean();
				break;

			}
		} else {
			switch (requestCode) {
			case REQUEST_MILEAGE_CHECK_IN:
				checkMileage();

				break;

			}

		}

	}

	public void refresh(View v) {
		LogUtils.debug(this.getClass().getName() + ": Refreshing POD");
		OnClickListener ocl = new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				new MRAsyncTaskHandler(activity, ActionEnum.REFRESH);

			}
		};
		createConfirmDialog("This will delete existing manifest.", "Download Manifest ?", ocl);

	}

	public void logout(View v) {
		OnClickListener submitMileage = new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(activity, MileageActivity.class);
				intent.putExtra(MileageActivity.MILEAGE_TYPE, "CHECK_OUT");
				startActivityForResult(intent, REQUEST_MILEAGE_CHECK_OUT);

			}
		};
		createConfirmDialog(getString(R.string.logout_confirm), getString(R.string.logout_title), submitMileage);
	}

	public void showMerchants(View v) {
		ArrayList<Merchant> merchants = db.getAllMerchants();
		//final Intent i = new Intent(getString(R.string.merchant_action));
		final Intent i = new Intent(getString(R.string.merchant_action));
		i.putExtra(Merchant.class.getName(), merchants);
		startActivity(i);
	}

	public void settings(View v) {
		final Intent i = new Intent(this, SettingsActivity.class);
		startActivity(i);
	}

	@Override
	public ArrayList<String> getItems() {
		return null;
	}

	@Override
	public void stopServices() {
		super.stopServices();
		stopService(new Intent(this, LocationService.class));
	}
}