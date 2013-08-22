package com.brainbox.shopclues.milkrun.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;

import com.brainbox.core.config.SystemConfig;
import com.brainbox.core.constants.CommonConstants;
import com.brainbox.core.constants.ErrorConstants;
import com.brainbox.core.helper.DatabaseHelper;
import com.brainbox.core.utils.CommonUtils;
import com.brainbox.core.utils.LogUtils;
import com.brainbox.core.vo.LoginResponseVO;
import com.brainbox.mobile.exception.SystemException;
import com.brainbox.shopclues.milkrun.R;

public class LoginActivity extends ParentActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_screen);
		init();
	}

	private void init() {
		try {
			if (SystemConfig.connectivity.getActiveNetworkInfo() == null) {
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setMessage(CommonUtils.getErrorMessage(ErrorConstants.NETWORK_ERROR, getApplicationContext()));
				alert.setCancelable(false);
				alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						LoginActivity.this.finish();
					}
				});
				alert.show();
			}
			if (db == null)
				db = new DatabaseHelper(this);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (aDialog != null)
			aDialog.dismiss();
	}

	public void login(View v) {
		String uid = ((EditText) (findViewById(R.id.uid))).getText().toString();
		String pwd = ((EditText) (findViewById(R.id.pwd))).getText().toString();
		LogUtils.debug("Logging in with user ID" + uid);
		new LoginHandler(uid, pwd);
	}

	protected class LoginHandler extends Handler implements Runnable {
		String loginMessage = "Error";
		LoginResponseVO login = null;
		String uid, pwd;
		protected ProgressDialog pDialog;

		public LoginHandler(String uid, String pwd) {
			this.uid = uid;
			this.pwd = pwd;
			Thread thread = new Thread(this);
			thread.start();
			pDialog = ProgressDialog.show(LoginActivity.this, null, "Logging In");
		}

		@Override
		public void run() {
			try {
				login = SystemConfig.httpRequestHelper.login(uid, pwd);
				// db.insertValue(CommonConstants.UID, login.userID);
			} catch (SystemException e) {
				login = new LoginResponseVO();
				login.message = CommonUtils.getErrorMessage(e.errorCode, activity);
			}
			sendEmptyMessage(0);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (login.isLoggedIn) {
				db.insertValue(CommonConstants.UID, uid);
				db.insertValue(CommonConstants.PASSWORD, pwd);
				db.insertValue(CommonConstants.LOGGED_IN, CommonConstants.YES);
				Intent intent = new Intent(getResources().getString(R.string.dashboard_action));
				startActivity(intent);
				finish();
			} else {
				AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
				alert.setTitle("Error");
				alert.setMessage(login.message);
				alert.setNeutralButton("OK", null);
				aDialog = alert.show();
			}
			pDialog.dismiss();

		}
	}

}