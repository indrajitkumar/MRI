package com.brainbox.shopclues.milkrun.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.HttpPost;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;
import android.widget.Spinner;

import com.brainbox.core.utils.LogUtils;
import com.brainbox.core.vo.Item;
import com.brainbox.core.vo.Merchant;
import com.brainbox.core.vo.MilkRunJSONResponseVO;
import com.brainbox.core.vo.ReasonVO;
import com.brainbox.milkrun.constants.MilkRunConstants;

import com.brainbox.shopclues.milkrun.R;
import com.brainbox.vo.enums.ActionEnum;
import com.brainbox.vo.enums.StatusEnum;

public class MerchantItemActivity extends ItemViewActivity {
	@Override
	protected void onStart() {
		LogUtils.v(getClass() + " : Start ");
		merchant = (Merchant) this.getIntent().getSerializableExtra(Merchant.class.getName());
		super.onStart();
		final ListView lv = (ListView) findViewById(R.id.listView);
		if (!MilkRunConstants.STATUS_CLOSED.equals(merchant.status)) {
			lv.setOnItemClickListener(this);
		}
	}

	/**
	 * this method is used for displaying item list
	 * 
	 * @param position
	 */
	public void showList(int position) {
		showItems(data);
		showHideButtons();
	}

	@Override
	protected void onResume() {
		updateData();
		super.onResume();
	}

	protected void updateData() {
		LogUtils.v(getClass() + " : updateData");
		data = db.getItemsByMerchantId(merchant.merchantId);
		adapter = new CustomAdapter(this, R.id.topLeft, data);
		adapter.notifyDataSetChanged();
		showHideButtons();
		super.onResume();
	}

	@Override
	public void refreshData() {
		LogUtils.v(getClass() + " : refreshScreen");
		data.clear();
		data.addAll(db.getItemsByMerchantId(merchant.merchantId));
		adapter.notifyDataSetChanged();
	}

	protected void showHideButtons() {
		if (MilkRunConstants.STATUS_CLOSED.equals(merchant.status)) {
			findViewById(R.id.btn_skip_merchant).setVisibility(View.GONE);
			findViewById(R.id.upd_btn).setVisibility(View.GONE);
			findViewById(R.id.cnf_btn).setVisibility(View.GONE);
			return;
		} else {
			boolean skipMerchant = !MilkRunConstants.STATUS_SKIPPED.equals(merchant.status);
			for (Item item : data) {
				if (item.qtyRec != 0) {
					skipMerchant = false;
					break;
				}
			}
			if (!skipMerchant) {
				findViewById(R.id.btn_skip_merchant).setVisibility(View.GONE);
				findViewById(R.id.upd_btn).setVisibility(View.VISIBLE);
			} else {
				findViewById(R.id.btn_skip_merchant).setVisibility(View.VISIBLE);
				findViewById(R.id.upd_btn).setVisibility(View.GONE);
			}
			findViewById(R.id.cnf_btn).setVisibility(View.GONE);
		}
	}

	public void updateServer(View v) {
		for (Item item : data) {
			if (MilkRunConstants.STATUS_OPEN.equals(item.status)) {
				action = ActionEnum.MR_VALIDATE;
				Builder builder = getAlertDialog();
				builder.setSingleChoiceItems(getReasons(), 0, selectItem);
				builder.setTitle(getString(R.string.skip_item_reason));
				aDialog = builder.show();
				reason = db.getReasons()[1];
				return;
			}
		}
		promptValidate();
	}

	public void validate(Dialog d) {
		Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner_reasons);
		int res = spinner.getSelectedItemPosition();
		dialog.dismiss();
		if (res != 0) {
			String reasonCode = ((ReasonVO) spinner.getSelectedItem()).reasonCode;
			for (Item item : data) {
				if (StatusEnum.OPEN.name().equals(item.status)) {
					item.qtyRec = 0;
					item.reason = reasonCode;
					db.updateItem(item);
				}
			}
		}
	}

	public void validateMR() {
		for (Item item : data) {
			if (StatusEnum.OPEN.name().equals(item.status)) {
				item.qtyRec = 0;
				item.reason = reason.reasonCode;
				db.updateItem(item);
			}
		}
		promptValidate();
	}

	public void updateServer() {
		new UpdateServerHandler(this, ActionEnum.UPDATE_PO);
	}

	public void confirm(View v) {
		promptConfirm();
	}

	protected void confirm() {
		new UpdateServerHandler(this, ActionEnum.CONFIRM_PO);
	}

	class UpdateServerHandler extends Handler implements Runnable {
		ActionEnum action;
		private ProgressDialog pDialog;
		private Activity context;
		private MilkRunJSONResponseVO responseVO;

		public UpdateServerHandler(Activity context, ActionEnum action) {
			Thread thread = new Thread(this);
			this.action = action;
			this.context = context;
			pDialog = ProgressDialog.show(context, null, "Please Wait... ");
			thread.start();
		}

		@Override
		public void run() {
			switch (action) {
			case UPDATE_PO:
				updatePO(ActionEnum.UPDATE_PO);
				break;
			case CONFIRM_PO:
				updatePO(ActionEnum.CONFIRM_PO);
			}
			sendEmptyMessage(0);
		}

		public void updatePO(ActionEnum action) {
			try {
				ArrayList<Item> items = db.getItemsByMerchantId(merchant.merchantId);
				Map<String, String> params = new HashMap<String, String>();
				params.put("action", action.value);
				params.put("merchantId", merchant.merchantId);
				params.put("manifest_id", merchant.manifestId);
				params.put("pickupboyid", db.getValue(MilkRunConstants.PICKUP_BOY_ID));
				String itms = "";
				int i = 1;
				for (Item item : items) {
					itms += item.itemId + "," + item.qtyRec;
					itms += "," + item.reason;
					if (i++ < items.size()) {
						itms += "|";
					}
				}
				params.put("item", itms);
				String apiRes = httpRequestHelper.executeHttpRequest(params, HttpPost.METHOD_NAME);
				responseVO = handleResponse(apiRes, MilkRunConstants.STATUS_REVISED);

			} catch (Exception e) {
				responseVO = new MilkRunJSONResponseVO();
				responseVO.message = "Error while updating on server";
			}
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			AlertDialog.Builder alert = new AlertDialog.Builder(context);
			try {
				if (responseVO != null && MilkRunJSONResponseVO.SUCCESS.equals(responseVO.status)) {

					switch (action) {

					case UPDATE_PO:

						int warnings = db.getWarningsByMerchantId(merchant.merchantId).size();
						String message = warnings == 0 ? getString(R.string.msg_mr_validated)
								: getString(R.string.msg_mr_validated_with_warn);
						alert.setMessage(message);
						refreshData();
						findViewById(R.id.upd_btn).setVisibility(View.GONE);
						findViewById(R.id.cnf_btn).setVisibility(View.VISIBLE);
						adapter.notifyDataSetChanged();

						break;
					case CONFIRM_PO:
						alert.setMessage(getString(R.string.msg_mr_cnf));
						final ListView lv = (ListView) findViewById(R.id.listView);
						lv.setOnItemClickListener(null);
						findViewById(R.id.cnf_btn).setVisibility(View.GONE);
						findViewById(R.id.close_btn).setVisibility(View.VISIBLE);
						break;
					}
				} else {
					alert.setMessage(responseVO.message);
				}
				sendBroadcast(new Intent(getString(R.string.action_refresh)));
			} catch (Exception e) {
				LogUtils.error("Unkown error", e);
				alert.setMessage("Unkown Error");
			}
			alert.setPositiveButton("OK", null);
			aDialog = alert.show();
			pDialog.dismiss();
		}
	}

}
