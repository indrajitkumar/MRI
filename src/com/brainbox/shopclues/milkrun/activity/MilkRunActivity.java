package com.brainbox.shopclues.milkrun.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.brainbox.core.utils.LogUtils;
import com.brainbox.core.utils.MilkRunUtils;
import com.brainbox.core.vo.Item;
import com.brainbox.core.vo.Merchant;
import com.brainbox.core.vo.MilkRunJSONResponseVO;
import com.brainbox.core.vo.Order;
import com.brainbox.core.vo.ReasonVO;
import com.brainbox.milkrun.constants.MilkRunConstants;
import com.brainbox.milkrun.helper.MilkRunDatabaseHelper;
import com.brainbox.milkrun.helper.MilkRunHttpRequestHelper;
import com.brainbox.mobile.exception.SystemException;
import com.brainbox.shopclues.milkrun.R;
import com.brainbox.vo.enums.ActionEnum;
import com.brainbox.vo.enums.PinTypeEnum;
import com.brainbox.vo.enums.StatusEnum;

public abstract class MilkRunActivity extends ParentActivity {

	public static final int REQUEST_MILEAGE_CHECK_IN = 0;
	public static final int REQUEST_MILEAGE_CHECK_OUT = 1;
	public static final int REQUEST_MERCHANT_LOCATION = 2;

	public LayoutInflater mInflater;
	protected static Merchant merchant;
	protected Order order;
	protected static CharSequence input;
	protected static PinTypeEnum pinType;
	protected static MilkRunDatabaseHelper db;
	protected ActionEnum action;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		db = new MilkRunDatabaseHelper(this);
		httpRequestHelper = new MilkRunHttpRequestHelper(this, db);
	}

	@Override
	protected void onResume() {
		super.onResume();
		//Need to ask about this, Hope this would not be using
		registerReceiver(
				mReceiver,
				new IntentFilter(getResources().getString(
						R.string.action_refresh)));
	}

	@Override
	protected void onPause() {
		super.onPause();
		try {
			unregisterReceiver(mReceiver);
		} catch (Exception e) {
			LogUtils.error("Error while Unregistering Receivers", e);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (db != null)
			db.close();
	}

	public void showMerchants(View v) {
		final Intent intent = new Intent(getString(R.string.action_merchant));
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	public void showOrders(View v) {
		final Intent intent = new Intent(getString(R.string.action_tab));
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("TAB", MilkRunConstants.TAB_ORDER);
		intent.putExtra(Order.class.getName(), order);
		intent.putExtra(Merchant.class.getName(), merchant);
		startActivity(intent);
	}

	public void showItems(View v) {
		final Intent intent = new Intent(getString(R.string.action_tab));
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("TAB", MilkRunConstants.TAB_ITEM);
		intent.putExtra(Merchant.class.getName(), merchant);
		startActivity(intent);
	}

	public ArrayList<String> getItems() {
		return null;
	};


	public class HttpResponseHanlder extends Handler implements Runnable {
		private ProgressDialog pDialog;
		private Activity context;
		private String response;

		// protected ActionEnum action;
		public HttpResponseHanlder(Activity context, ActionEnum actionParam) {
			action = actionParam;
			this.context = context;
		}

		public void getResponse() {
			Thread thread = new Thread(this);
			pDialog = ProgressDialog.show(context, null, "Please Wait... ");
			thread.start();
		}

		@Override
		public void run() {
			switch (action) {
			case CLOSE_PO:
				try {
					response = validatePINonServer(input, pinType);
				} catch (SystemException e) {
					response = "Error While Validating PIN";
				}
				break;
			case SKIP_MERCHANT:
				try {
					response = skipMerchant();
				} catch (Exception e) {
					response = "Error while skipping merchant";
				}
			}
			sendEmptyMessage(0);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			OnClickListener ocl2 = null;
			AlertDialog.Builder alert = new AlertDialog.Builder(context);
			MilkRunJSONResponseVO responseVO = new MilkRunJSONResponseVO(
					response);
			switch (action) {
			case CLOSE_PO:
				if (MilkRunJSONResponseVO.SUCCESS
						.equalsIgnoreCase(responseVO.status)) {
					db.closePO(merchant.merchantId);
				} else {
					action = ActionEnum.VALIDATE_PIN;
				}
				alert.setMessage(responseVO.message);
				ocl2 = dcl;
				break;
			case SKIP_MERCHANT:
				ocl2 = dcl;
				if (MilkRunJSONResponseVO.SUCCESS
						.equalsIgnoreCase(responseVO.status)) {
					db.skipMerchant(merchant, reason.reasonCode);
					action = ActionEnum.GO_HOME;
				} else {
					// action = ActionEnum.SKIP_MERCHANT;
					ocl2 = null;
				}
				alert.setMessage(responseVO.message);
				break;
			}
			alert.setPositiveButton("OK", ocl2);
			pDialog.dismiss();
			aDialog = alert.show();
		}
	}

	protected void promptValidate() {

		ArrayList<Order> orders = db.getOrdersByMerchantId(merchant.merchantId);
		for (Order order : orders) {
			if (StatusEnum.PARTIAL_FILL.equals(order.status)) {
				showAlertDialog(getString(R.string.alert_ord_partial_fill),
						getString(R.string.label_error), null);
				return;
			}
		}

		action = ActionEnum.PROMPT_VALIDATE;
		prompt(getString(R.string.prompt_mr_validate));
	};

	protected void promptConfirm() {
		action = ActionEnum.PROMPT_CONFIRM;
		prompt(getString(R.string.prompt_mr_cnf));
	};

	protected void promptClose() {
		action = ActionEnum.PROMPT_CLOSE;
		prompt(getString(R.string.prompt_mr_close));
	};

	protected void prompt(String message) {
		List<Item> items = db.getItemsByMerchantId(merchant.merchantId);
		int qtyExp = 0, qtyRec = 0;
		for (Item item : items) {
			qtyRec += item.qtyRec;
			qtyExp += item.qtyExp;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setNegativeButton(R.string.label_no, null);
		builder.setPositiveButton(R.string.label_yes, dcl);
		builder.setMessage(String.format(message, qtyRec, qtyExp));
		aDialog = builder.show();
	};

	public static String validatePINonServer(CharSequence pin,
			PinTypeEnum pinType) throws SystemException {
		return MilkRunConstants.httpRequestHelper.validatePIN(pin, pinType,
				merchant);
	}

	protected android.content.DialogInterface.OnClickListener dcl = new android.content.DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog1, int which) {
			switch (action) {
			case MR_VALIDATE:
				validateMR();
				break;
			case PROMPT_VALIDATE:
				updateServer();
				break;
			case PROMPT_CONFIRM:
			case CONFIRM_PO:
				confirm();
				break;
			case PROMPT_CLOSE:
				List<Item> items = db.getItemsByMerchantId(merchant.merchantId);
				int qtyExp = 0,
				qtyRec = 0;
				for (Item item : items) {
					qtyRec += item.qtyRec;
					qtyExp += item.qtyExp;
				}
				dialog = new Dialog(activity, R.style.AppTheme);
				dialog.setTitle(getResources().getString(R.string.close_po));
				dialog.setContentView(R.layout.alert_pin_layout);
				((TextView) dialog.findViewById(R.id.mr_summary))
						.setText(qtyRec + "/" + qtyExp);
				dialog.findViewById(R.id.goButton).setOnClickListener(cl);
				dialog.findViewById(R.id.cancelButton).setOnClickListener(
						new View.OnClickListener() {
							public void onClick(View v) {
								dialog.dismiss();
							}
						});
				action = ActionEnum.VALIDATE_PIN;
				dialog.getWindow().setSoftInputMode(
						WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
				lp.copyFrom(dialog.getWindow().getAttributes());
				lp.width = WindowManager.LayoutParams.FILL_PARENT;
				lp.gravity = Gravity.CENTER_HORIZONTAL
						| Gravity.CENTER_VERTICAL;
				dialog.getWindow().setAttributes(lp);
				dialog.show();
				break;
			case CLOSE_PO:
				Intent intent = new Intent(getResources().getString(
						R.string.home_action));
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				break;
			case SKIP_MERCHANT:
				getResponse(ActionEnum.SKIP_MERCHANT);
				break;
			case GO_HOME:
				intent = new Intent(getResources().getString(
						R.string.home_action));
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		}
	};

	protected void confirm() {
	}

	protected void updateServer() {
	}

	public void closePO(View v) {
		promptClose();
	}

	protected View.OnClickListener cl = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			input = ((EditText) dialog.findViewById(R.id.input)).getText();
			dialog.dismiss();
			inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
			switch (action) {
			case VALIDATE_PIN:
				Spinner spinner = (Spinner) dialog
						.findViewById(R.id.spn_pin_typ);
				String type = (String) spinner.getSelectedItem();
				if (getString(R.string.option_merchant).equals(type)) {
					pinType = PinTypeEnum.TYPE_MERCHANT;
				} else {
					pinType = PinTypeEnum.TYPE_PICKUP_BOY;
				}
				getResponse(ActionEnum.CLOSE_PO);
				break;
			case SKIP_MERCHANT:
				break;
			}
		}
	};

	public void getResponse(ActionEnum action) {
		new HttpResponseHanlder(this, action).getResponse();
	}

	protected ReasonVO reason;

	public void skipMerchant(View v) {
		action = ActionEnum.SKIP_MERCHANT;
		Builder builder = getAlertDialog();
		OnClickListener icl = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				reason = db.getReasons()[which + 1];
			}
		};
		reason = db.getReasons()[1];
		builder.setSingleChoiceItems(getReasons(), 0, icl);
		builder.setTitle(getString(R.string.skip_merchant_reason));
		aDialog = builder.show();
	}

	public String[] getReasons() {
		ReasonVO[] reasonArray = db.getReasons();
		String[] reasons = new String[reasonArray.length - 1];
		for (int i = 1; i < reasonArray.length; i++) {
			reasons[i - 1] = reasonArray[i].toString();
		}
		return reasons;
	}

	public String skipMerchant() throws SystemException {
		return MilkRunConstants.httpRequestHelper
				.skipMerchant(merchant, reason);
	}

	public AlertDialog.Builder getAlertDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setNegativeButton("Cancel", null);
		builder.setPositiveButton("OK", dcl);
		return builder;
	}

	public void showCustomDialog(int layoutId) {
		dialog = new Dialog(activity, R.style.AppTheme);
		dialog.setContentView(layoutId);
		dialog.findViewById(R.id.goButton).setOnClickListener(cl);
		dialog.findViewById(R.id.cancelButton).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
	}

	public MilkRunJSONResponseVO handleResponse(String apiRes, String status)
			throws IOException {
		MilkRunJSONResponseVO response = null;
		try {
			if (apiRes.trim().startsWith("{")) {
				response = new MilkRunJSONResponseVO(apiRes);
			} else {
				MilkRunUtils.readManifest(apiRes, status);
				merchant.status = status;
				db.updatePO(merchant.merchantId, merchant.status);
				response = new MilkRunJSONResponseVO();
				response.status = MilkRunJSONResponseVO.SUCCESS;
			}
		} catch (Exception e) {
			response = new MilkRunJSONResponseVO();
			response.status = MilkRunJSONResponseVO.FAILED;
			response.message = "INVALID RESPONSE FROM SERVER.";

		}
		return response;
	}

	public View.OnClickListener closeDialogListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			dialog.dismiss();
		}
	};

	public void validate(Dialog d) {
	}

	public void validateMR() {
	}

	public DialogInterface.OnClickListener selectItem = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (action) {
			case MR_VALIDATE:
				reason = db.getReasons()[which + 1];
				break;
			}
		}
	};
	protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (getString(R.string.action_refresh).equals(action)) {
				refreshData();
			}
		}
	};

	public void refreshData() {
	}
}
