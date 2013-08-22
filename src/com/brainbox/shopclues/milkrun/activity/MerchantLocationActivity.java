package com.brainbox.shopclues.milkrun.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.brainbox.core.constants.CommonConstants;
import com.brainbox.core.http.HttpRequestHelperImpl;
import com.brainbox.core.http.HttpRequestTask;
import com.brainbox.core.vo.HttpRequestVO;
import com.brainbox.core.vo.JSONResponseVO;
import com.brainbox.core.vo.Merchant;

import com.brainbox.milkrun.constants.MilkRunConstants;

import com.brainbox.shopclues.milkrun.R;
import com.brainbox.tracking.GeoPointActivity;
import com.brainbox.vo.enums.ActionEnum;

public class MerchantLocationActivity extends MilkRunListActivity implements OnItemClickListener {
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
			lv.setAdapter(new CustomAdapter(this, R.layout.list_item, R.id.topLeft, data));
			lv.setOnItemClickListener(this);
			lv.setEmptyView(findViewById(R.id.emptyView));
		}
		((TextView) findViewById(R.id.header_title)).setText(String.format(getString(R.string.merchant_header),
				db.getValue(MilkRunConstants.MANIFEST_ID), data.size()));
	}

	public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
		merchant = data.get(position);
		startActivityForResult(new Intent(this, GeoPointActivity.class), REQUEST_MERCHANT_LOCATION);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_MERCHANT_LOCATION:
				merchant.lat = intent.getDoubleExtra(CommonConstants.LAT, 0);
				merchant.lon = intent.getDoubleExtra(CommonConstants.LON, 0);
				HttpRequestVO req = new HttpRequestVO();

				req.params.put("mer_lat", String.valueOf(merchant.lat));
				req.params.put("mer_lon", String.valueOf(merchant.lon));
				req.params.put("company_id", String.valueOf(merchant.merchantId));
				req.params.put(HttpRequestHelperImpl.PARAM_ACTION, ActionEnum.RECORD_LOCATION.value);

				new HttpRequestTask(this, httpRequestHelper, new Handler(callback)).execute(req);

				break;

			}
		}
	}

	private Callback callback = new Callback() {
		public boolean handleMessage(Message msg) {
			JSONResponseVO vo = (JSONResponseVO) msg.getData().getSerializable("DATA");
			if (vo != null && "SUCCESS".equals(vo.status)) {
				showAlertDialog("Merchant Location updated successfully", null, null);
			} else {
				showAlertDialog("Merchant Location not updated on server", "Error", null);
			}
			return true;
		};
	};

	private class CustomAdapter extends ArrayAdapter<Merchant> {
		public CustomAdapter(final Context context, final int resource, final int textViewResourceId,
				final List<Merchant> objects) {
			super(context, resource, textViewResourceId, objects);
		}

		public CustomAdapter(final Context context, final int resource, final int textViewResourceId,
				final Merchant[] objects) {
			super(context, resource, textViewResourceId, objects);
		}

		@Override
		public View getView(final int position, View convertView, final ViewGroup parent) {
			final Merchant merchant = getItem(position);
			if (null == convertView) {
				convertView = mInflater.inflate(R.layout.merchant_list_item, null);
			}
			((TextView) convertView.findViewById(R.id.merchant_name)).setText(merchant.name);
			((TextView) convertView.findViewById(R.id.merchant_address)).setText(merchant.addressLine1);
			TextView tv = ((TextView) convertView.findViewById(R.id.merchant_phone));
			tv.setText(merchant.phone);
			tv.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					Intent callIntent = new Intent(Intent.ACTION_DIAL);
					callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					callIntent.setData(Uri.parse("tel:" + ((TextView) v).getText()));
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

	public ArrayList<String> getItems() {
		final ArrayList<String> items = new ArrayList<String>();
		for (Merchant item : data) {
			items.add(item.name);
		}
		return items;
	}
}
