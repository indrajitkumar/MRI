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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.Spinner;

import com.brainbox.core.utils.LogUtils;
import com.brainbox.core.vo.Item;
import com.brainbox.core.vo.Merchant;
import com.brainbox.core.vo.MilkRunJSONResponseVO;
import com.brainbox.core.vo.Order;
import com.brainbox.core.vo.ReasonVO;
import com.brainbox.core.widget.OrdersListAdapter;

import com.brainbox.milkrun.constants.MilkRunConstants;

import com.brainbox.shopclues.milkrun.R;
import com.brainbox.vo.enums.ActionEnum;
import com.brainbox.vo.enums.StatusEnum;

public class OrderListActivity extends MilkRunListActivity implements OnChildClickListener, OnGroupExpandListener {
	protected int groupPosition;
	protected int childPosition;
	ReasonVO[] reasons;
	protected ArrayList<Order> orders;
	protected ArrayList<Item> expandedItems;
	Item focusedItem;
	Order focusedOrder;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		LogUtils.v(getClass() + " : onCreate");
		setContentView(R.layout.ex_order_view);
		eListView = (ExpandableListView) findViewById(R.id.exlistView);
		merchant = (Merchant) this.getIntent().getSerializableExtra(Merchant.class.getName());
		super.onCreate(savedInstanceState);
		reasons = db.getReasons();
		if (!StatusEnum.CLOSED.name().equals(merchant.status)) {
			eListView.setOnChildClickListener(this);
		}
		eListView.setOnGroupExpandListener(this);
	}

	@Override
	protected void onResume() {
		LogUtils.v(getClass(), "onResume");
		updateData();
		super.onResume();
	}

	public void refreshData() {
		LogUtils.v(getClass(), "refreshData");
		orders.clear();
		orders.addAll(db.getOrdersByMerchantId(merchant.merchantId));
		expListAdapter.notifyDataSetChanged();
	}

	public void updateData() {
		super.refreshScreen();
		LogUtils.v(getClass(), "refreshScreen");
		orders = db.getOrdersByMerchantId(merchant.merchantId);
		expListAdapter = new OrdersListAdapter(orders, this);
		eListView.setAdapter(expListAdapter);
		showHideButtons();
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		this.groupPosition = groupPosition;
		this.childPosition = childPosition;
		focusedItem = (Item) expListAdapter.getChild(groupPosition, childPosition);
		action = ActionEnum.UPDATE_QUANTITY;
		showItemDialog();
		return true;
	}

	protected void updateQuantity(Item item) {
		db.updateItem(item);
		db.updateOrderItem(item);
		db.updateRecQuantity(item);
	}

	private void showItemDialog() {
		dialog = new Dialog(activity, R.style.BB_Theme_Dialog);
		dialog.setContentView(R.layout.update_qty);
		// builder.set
		dialog.findViewById(R.id.goButton).setOnClickListener(itemClickListener);
		EditText editText = (EditText) dialog.findViewById(R.id.input);
		if (!MilkRunConstants.STATUS_OPEN.equals(focusedItem.status)) {
			String text = String.valueOf(focusedItem.qtyRec);
			editText.setText(text);
			editText.setSelection(text.length());
		}
		dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		// Populate Reason codes
		Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner_reasons);
		// Create an ArrayAdapter using the string array and a default spinner
		// Get Mismatch Reasons
		ArrayAdapter<ReasonVO> reasons_adapter = new ArrayAdapter<ReasonVO>(this, android.R.layout.simple_spinner_item,
				reasons);
		// Specify the layout to use when the list of choices appears
		reasons_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(reasons_adapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
				focusedItem.reason = reasons[position].reasonCode;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// arg0.set
			}
		});
		//
		int selected = reasons_adapter.getPosition(db.getReason(focusedItem.reason));
		if (selected >= 0 && selected <= reasons_adapter.getCount()) {
			spinner.setSelection(selected);
		}
		// dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.FILL_PARENT;
		dialog.getWindow().setAttributes(lp);
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.show();
	}

	private OnClickListener itemClickListener = new OnClickListener() {
		public void onClick(View v) {
			CharSequence input = ((EditText) dialog.findViewById(R.id.input)).getText();
			// inputManager.hideSoftInputFromWindow();
			AlertDialog.Builder alert = new AlertDialog.Builder(dialog.getContext());
			if (input != null && !"".equals(input.toString()) && input.length() < 5) {
				int avlQty = Integer.valueOf(input.toString());
				if (avlQty != focusedItem.qtyExp && avlQty != 0) {
					alert.setMessage(getString(R.string.alert_invalid_qty1, 0, focusedItem.qtyExp));
					alert.setNeutralButton("OK", null);
					alert.show();
				} else if (avlQty != focusedItem.qtyExp) {
					Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner_reasons);
					int res = spinner.getSelectedItemPosition();
					if (res != 0) {
						dialog.dismiss();
						focusedItem.qtyRec = Integer.valueOf(input.toString());
						focusedItem.status = MilkRunConstants.STATUS_REVISED;
						String reasonCode = ((ReasonVO) spinner.getSelectedItem()).reasonCode;
						focusedItem.reason = reasonCode;
						updateQuantity(focusedItem);
						updateOrderStatus(focusedOrder);
					} else {
						alert.setMessage(getString(R.string.alert_qty_mismatch_reason));
						alert.setNeutralButton("OK", null);
						alert.show();
					}
				} else {
					dialog.dismiss();
					focusedItem.qtyRec = Integer.valueOf(input.toString());
					focusedItem.status = MilkRunConstants.STATUS_REVISED;
					focusedItem.reason = null;
					updateQuantity(focusedItem);
					updateOrderStatus(focusedOrder);
					showHideButtons();
				}
				expListAdapter.notifyDataSetChanged();
			} else {
				alert.setMessage(String.format(getString(R.string.alert_invalid_qty), 0, focusedItem.qtyExp));
				alert.setNeutralButton("OK", null);
				alert.show();
			}
		}
	};

	@Override
	public void onGroupExpand(int groupPosition) {
		this.groupPosition = groupPosition;
		int len = expListAdapter.getGroupCount();
		for (int i = 0; i < len; i++) {
			if (i != groupPosition) {
				eListView.collapseGroup(i);
			}
		}
		focusedOrder = (Order) expListAdapter.getGroup(groupPosition);
		expandedItems = db.getItemsByOrderId(focusedOrder.orderId);
	}

	protected void showHideButtons() {
		if (MilkRunConstants.STATUS_CLOSED.equals(merchant.status)) {
			findViewById(R.id.btn_skip_merchant).setVisibility(View.GONE);
			findViewById(R.id.upd_btn).setVisibility(View.GONE);
			findViewById(R.id.cnf_btn).setVisibility(View.GONE);
			return;
		}
		findViewById(R.id.btn_skip_merchant).setVisibility(View.GONE);
		findViewById(R.id.upd_btn).setVisibility(View.VISIBLE);
		findViewById(R.id.cnf_btn).setVisibility(View.GONE);
	}

	protected void confirm() {
		new UpdateServerHandler(this, ActionEnum.CONFIRM_PO);
	}

	class UpdateServerHandler extends Handler implements Runnable {
		ActionEnum action;
		private ProgressDialog pDialog;
		private Activity context;
		private MilkRunJSONResponseVO serverResponse;

		public UpdateServerHandler(Activity context, ActionEnum action) {
			Thread thread = new Thread(this);
			this.action = action;
			this.context = context;
			pDialog = ProgressDialog.show(context, null, "Please Wait... ");
			thread.start();
			eListView.collapseGroup(groupPosition);
		}

		@Override
		public void run() {
			switch (action) {
			case MR_VALIDATE:
			case UPDATE_PO:
				for (Order order : orders) {
					ArrayList<Item> items = db.getItemsByOrderId(order.orderId);
					for (Item item : items) {
						if (StatusEnum.OPEN.name().equals(item.status)) {
							item.qtyRec = 0;
							item.reason = reason.reasonCode;
							item.status = StatusEnum.SKIPPED.name();
							updateQuantity(item);
						}
					}
					updateOrderStatus(order);
				}
				serverResponse = updatePO(ActionEnum.UPDATE_PO);
				break;
			case CONFIRM_PO:
				serverResponse = updatePO(ActionEnum.CONFIRM_PO);
			}
			sendEmptyMessage(0);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			AlertDialog.Builder alert = new AlertDialog.Builder(context);
			if (serverResponse != null) {
				alert.setTitle(serverResponse.status);
				switch (action) {
				case UPDATE_PO:
					String message = "";
					if (serverResponse.message != null && !serverResponse.message.trim().equals("")) {
						message = serverResponse.message;
					} else if (MilkRunJSONResponseVO.SUCCESS.equalsIgnoreCase(serverResponse.status)) {
						int warnings = db.getWarningsByMerchantId(merchant.merchantId).size();
						message = warnings == 0 ? getString(R.string.msg_mr_validated)
								: getString(R.string.msg_mr_validated_with_warn);
					}
					alert.setMessage(message);
					if (MilkRunJSONResponseVO.SUCCESS.equalsIgnoreCase(serverResponse.status)) {

						refreshData();
						findViewById(R.id.upd_btn).setVisibility(View.GONE);
						findViewById(R.id.cnf_btn).setVisibility(View.VISIBLE);
					} else {

					}
					break;
				case CONFIRM_PO:
					if (MilkRunJSONResponseVO.SUCCESS.equalsIgnoreCase(serverResponse.status)) {
						alert.setMessage(getString(R.string.msg_mr_cnf));
						eListView.setOnChildClickListener(null);
						findViewById(R.id.cnf_btn).setVisibility(View.GONE);
						findViewById(R.id.close_btn).setVisibility(View.VISIBLE);
					}
					break;
				}
			} else {
				// alert
			}
			alert.setPositiveButton("OK", null);
			try {
				pDialog.dismiss();
				aDialog = alert.show();
				expListAdapter.notifyDataSetChanged();
				sendBroadcast(new Intent(getString(R.string.action_refresh)));
			} catch (Exception e) {
			}
		}
	}

	public MilkRunJSONResponseVO updatePO(ActionEnum action) {
		MilkRunJSONResponseVO response = null;
		try {
			Map<String, String> params = new HashMap<String, String>();
			params.put("action", action.value);
			params.put("merchantId", merchant.merchantId);
			ArrayList<Order> orders = db.getOrdersByMerchantId(merchant.merchantId);
			StringBuffer ords = new StringBuffer();
			int j = 1;
			for (Order order : orders) {
				ArrayList<Item> items = db.getItemsByOrderId(order.orderId);
				int i = 1;
				for (Item item : items) {
					ords.append(order.orderId);
					ords.append(",");
					ords.append(item.itemId);
					ords.append(",");
					ords.append(item.qtyRec);
					ords.append(",");
					ords.append(item.reason);
					if (i++ < items.size()) {
						ords.append("|");
					}
				}
				if (j++ < orders.size()) {
					ords.append("|");
				}
			}
			params.put("orders", ords.toString());
			String apiRes = httpRequestHelper.executeHttpRequest(params, HttpPost.METHOD_NAME);
			response = handleResponse(apiRes, MilkRunConstants.STATUS_REVISED);
		} catch (Exception e) {
			e.printStackTrace();
			response = new MilkRunJSONResponseVO();
			response.message = e.getMessage();
		}
		return response;
	}

	public void updateServer(View v) {
		eListView.collapseGroup(groupPosition);
		// orders = db.getOrdersByMerchantId(merchant.merchantId);
		for (Order order : orders) {
			if (StatusEnum.OPEN.equals(order.status)) {
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

	public void updateServer() {
		new UpdateServerHandler(this, ActionEnum.UPDATE_PO);
	}

	public void validate(Dialog d) {
		Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner_reasons);
		int res = spinner.getSelectedItemPosition();
		dialog.dismiss();
		if (res != Spinner.INVALID_POSITION) {
			String reasonCode = ((ReasonVO) spinner.getSelectedItem()).reasonCode;
			for (Order order : orders) {
				ArrayList<Item> items = db.getItemsByOrderId(order.orderId);
				for (Item item : items) {
					if (StatusEnum.OPEN.name().equals(item.status)) {
						item.qtyRec = 0;
						item.reason = reasonCode;
						item.status = StatusEnum.SKIPPED.name();
						updateQuantity(item);
					}
				}
				updateOrderStatus(order);
			}
			new UpdateServerHandler(this, ActionEnum.MR_VALIDATE);
		}
	}

	public void validateMR() {
		promptValidate();
	}

	public ArrayList<String> getItems() {
		final ArrayList<String> items = new ArrayList<String>();
		for (Order item : orders) {
			items.add(item.orderId);
		}
		return items;
	}

	private StatusEnum updateOrderStatus(Order order) {
		expandedItems = db.getItemsByOrderId(order.orderId);
		boolean skipped = true;
		StatusEnum orderstStatus = StatusEnum.FILLED;
		for (Item item : expandedItems) {
			if (item.qtyRec > 0) {
				skipped = false;
			}
			// if (MilkRunConstants.STATUS_OPEN.equals(item.status)) {
			// orderstStatus = StatusEnum.OPEN;
			// skipped = false;
			// break;
			// } else
			if (item.qtyExp != item.qtyRec) {
				orderstStatus = StatusEnum.PARTIAL_FILL;
				// break;
			}
		}
		if (skipped) {
			orderstStatus = StatusEnum.SKIPPED;
		}
		order.status = orderstStatus;
		db.updateOrderStatus(order, orderstStatus.name());

		return orderstStatus;
	}
}