package com.brainbox.shopclues.milkrun.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.brainbox.core.vo.Merchant;
import com.brainbox.milkrun.constants.MilkRunConstants;
import com.brainbox.shopclues.milkrun.R;


public class MerchantViewActivity extends MilkRunActivity implements
		OnItemClickListener, OnItemLongClickListener {
	public static final String MERCHANT = "MERCHANT";
	private LayoutInflater mInflater;
	private ArrayList<Merchant> data;
	

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		setContentView(R.layout.merchant_view);
		super.onCreate(savedInstanceState);
		mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		data = db.getAllMerchants();
	}

	CustomAdapter adapter;

	@Override
	protected void onStart() {
		super.onStart();
		if (data != null) {
			final ListView lv = (ListView) findViewById(R.id.listView);
			lv.setAdapter(new CustomAdapter(this, R.layout.list_item,
					R.id.topLeft, data));
			lv.setTextFilterEnabled(true);
			lv.setOnItemClickListener(this);
			lv.setOnItemLongClickListener(this);
			lv.setEmptyView(findViewById(R.id.emptyView));
		}
		((TextView) findViewById(R.id.header_title)).setText(String.format(
				getString(R.string.merchant_header),
				db.getValue(MilkRunConstants.MANIFEST_ID), data.size()));
	}

	int receiveByChoice;
	int position;

	public void onItemClick(final AdapterView<?> adapter, final View view,
			final int position, final long id) {
		merchant = data.get(position);
		showItems(view);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View view,
			int position, long arg3) {
		merchant = data.get(position);
		showMerchant(merchant);
		return true;
	}

	private void showMerchant(final Merchant merchant) {
		dialog = new Dialog(activity, R.style.BB_Theme_Dialog);
		dialog.setContentView(R.layout.merchant_detail_view);
		((TextView) dialog.findViewById(R.id.merchant_name))
				.setText(merchant.name);
		((TextView) dialog.findViewById(R.id.merchant_address))
				.setText(merchant.addressLine1);
		((TextView) dialog.findViewById(R.id.merchant_phone))
				.setText(merchant.phone);
		dialog.findViewById(R.id.merchant_phone).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View v) {
						call(v);
					}
				});
		dialog.findViewById(R.id.ok_button).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View v) {
						Spinner spn = (Spinner) dialog
								.findViewById(R.id.time_list);
						int pos = spn.getSelectedItemPosition();
						if (pos != 0) {
							String time = (String) spn.getSelectedItem();
							String message = getString(
									R.string.sms_template_notify,
									merchant.manifestId, time);
							sendSMS("tel:" + merchant.phone, message);
							dialog.dismiss();
						} else {
							showAlertDialog("Please select time to notify",
									null, null);

						}

					}
				});
		dialog.findViewById(R.id.cancelButton).setOnClickListener(
				closeDialogListener);
		dialog.show();
	}

	private class CustomAdapter extends ArrayAdapter<Merchant> {
		public CustomAdapter(final Context context, final int resource,
				final int textViewResourceId, final List<Merchant> objects) {
			super(context, resource, textViewResourceId, objects);
		}

		public CustomAdapter(final Context context, final int resource,
				final int textViewResourceId, final Merchant[] objects) {
			super(context, resource, textViewResourceId, objects);
		}

		@Override
		public View getView(final int position, View convertView,
				final ViewGroup parent) {
			final Merchant merchant = getItem(position);
			if (null == convertView) {
				convertView = mInflater.inflate(R.layout.merchant_list_item,
						null);
			}
			((TextView) convertView.findViewById(R.id.merchant_name))
					.setText(merchant.name);
			((TextView) convertView.findViewById(R.id.merchant_address))
					.setText(merchant.addressLine1);
			TextView tv = ((TextView) convertView
					.findViewById(R.id.merchant_phone));
			tv.setText(merchant.phone);
			tv.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					Intent callIntent = new Intent(Intent.ACTION_DIAL);
					callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					callIntent.setData(Uri.parse("tel:"
							+ ((TextView) v).getText()));
					startActivity(callIntent);
					return true;
				}
			});
			String qtyTxt = String.valueOf(merchant.qtyExp);
			if (MilkRunConstants.STATUS_CLOSED.equals(merchant.status)) {
				qtyTxt = merchant.qtyRec + "/" + qtyTxt;
				convertView.setBackgroundResource(R.drawable.bg_green);
			} else if (MilkRunConstants.STATUS_SKIPPED.equals(merchant.status)) {
				convertView.setBackgroundResource(R.drawable.bg_skip);
			} else {
				convertView.setBackgroundDrawable(null);
			}
			tv = (TextView) convertView.findViewById(R.id.qty);
			if (tv != null) {
				tv.setText(qtyTxt);
			}
			return convertView;
		}
	}

	private void sendSMS(String phoneNumber, String message) {
		PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()), 0);
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, pi, null);
	}

	public ArrayList<String> getItems() {
		final ArrayList<String> items = new ArrayList<String>();
		for (Merchant item : data) {
			items.add(item.name);
		}
		return items;
	}
}
