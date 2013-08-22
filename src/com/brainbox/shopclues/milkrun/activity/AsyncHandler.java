package com.brainbox.shopclues.milkrun.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;

import com.brainbox.core.constants.ConfigConstants;
import com.brainbox.milkrun.constants.MilkRunConstants;
import com.brainbox.milkrun.helper.MilkRunDatabaseHelper;
import com.brainbox.shopclues.milkrun.R;
import com.brainbox.vo.enums.ActionEnum;

public class AsyncHandler extends Handler implements Runnable {
	protected ActionEnum action;
	protected MilkRunDatabaseHelper db;
	protected ProgressDialog pDialog;
	protected AlertDialog aDialog;
	protected Activity context;

	public AsyncHandler(Activity context, ActionEnum action) {
		Thread thread = new Thread(this);
		this.action = action;
		this.context = context;
		db = (MilkRunDatabaseHelper) ConfigConstants.DATABASE_HELPER;
		pDialog = ProgressDialog.show(context, null, "Please Wait... ");
		thread.start();
	}

	@Override
	public void run() {
		switch (action) {
		case REFRESH:
			refresh();
			break;
		default:
			refresh();
			break;
		}
		sendEmptyMessage(0);
	}

	protected void refresh() {
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
		switch (action) {
		case UPDATE_QUANTITY:
			alert.setMessage("Server Updated");
			break;
		case REFRESH:
			alert.setMessage(context.getString(R.string.msg_manifest_loaded, db
			        .getValue(MilkRunConstants.MANIFEST_ID), db
			        .getAllMerchants().size()));
			break;
		default:
			alert.setMessage("Action Completed");
			break;
		}
		alert.setPositiveButton("OK", null);
		try {
			pDialog.dismiss();
			pDialog = null;
			aDialog = alert.show();
		} catch (Exception e) {
		}
	}
}
