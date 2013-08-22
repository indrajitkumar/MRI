package com.brainbox.shopclues.milkrun.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.brainbox.core.constants.ConfigConstants;
import com.brainbox.core.vo.Item;
import com.brainbox.core.vo.Merchant;
import com.brainbox.milkrun.helper.MilkRunDatabaseHelper;
import com.brainbox.shopclues.milkrun.R;


public class WarningsActivity extends MilkRunActivity {
	private LayoutInflater mInflater;
	private ArrayList<Item> data;
	private MilkRunDatabaseHelper db;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		db = (MilkRunDatabaseHelper) ConfigConstants.DATABASE_HELPER;
		setContentView(R.layout.warning_view);
		super.onCreate(savedInstanceState);
		mInflater =
		        (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		merchant = (Merchant) this.getIntent().getSerializableExtra(Merchant.class.getName());
	}

	CustomAdapter adapter;

	@Override
	protected void onStart() {
		super.onStart();
	}

	private class CustomAdapter extends ArrayAdapter<Item> {
		public CustomAdapter(final Context context, final int resource,
		        final int textViewResourceId, final List<Item> objects) {
			super(context, resource, textViewResourceId, objects);
		}

		public CustomAdapter(final Context context, final int resource,
		        final int textViewResourceId, final Item[] objects) {
			super(context, resource, textViewResourceId, objects);
		}

		@Override
		public View getView(final int position, View convertView,
		        final ViewGroup parent) {
			final Item item = getItem(position);
			if (null == convertView) {
				convertView = mInflater.inflate(R.layout.warning_item, null);
			}
			((TextView) convertView.findViewById(R.id.topLeft))
			        .setText(item.name);
			((TextView) convertView.findViewById(R.id.middle))
			        .setText(item.warning);
			return convertView;
		}
	}

	@Override
	protected void onResume() {
		data = db.getWarningsByMerchantId(merchant.merchantId);
		if (data != null) {
			final ListView lv = (ListView) findViewById(R.id.listView);
			lv.setAdapter(new CustomAdapter(this, R.layout.warning_item,
			        R.id.topLeft, data));
			lv.setTextFilterEnabled(true);
			lv.setEmptyView(findViewById(R.id.emptyView));
		}
		super.onResume();
	}

	@Override
	public ArrayList<String> getItems() {
		// TODO Auto-generated method stub
		return null;
	}
}
