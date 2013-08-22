package com.brainbox.shopclues.milkrun.activity;

import java.util.ArrayList;

import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

import com.brainbox.core.vo.Merchant;
import com.brainbox.milkrun.constants.MilkRunConstants;
import com.brainbox.shopclues.milkrun.R;


public class MRTabViewActivity extends MilkRunActivity {
	LocalActivityManager mlam;
	TabHost tabHost;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_order_view);
		Resources res = getResources();
		merchant = (Merchant) this.getIntent().getSerializableExtra(Merchant.class.getName());
		String tab = this.getIntent().getStringExtra("TAB");
		int i = db.getItemsByMerchantId(merchant.merchantId).size();
		int o = db.getOrdersByMerchantId(merchant.merchantId).size();
		mlam = new LocalActivityManager(this, false);
		tabHost = (TabHost) findViewById(android.R.id.tabhost);
		mlam.dispatchCreate(savedInstanceState);
		tabHost.setup(mlam);
		tabHost.getTabWidget().setDividerDrawable(R.drawable.tab_divider);
		TabHost.TabSpec spec;
		Intent intent;
		intent = new Intent(res.getString(R.string.merchant_item_action));
		intent.putExtras(getIntent().getExtras());
		spec =
				tabHost.newTabSpec(MilkRunConstants.TAB_ITEM).setIndicator(
						createTabView(this, "ITEMS [" + i + "]")).setContent(
						intent);
		tabHost.addTab(spec);
		intent = new Intent(res.getString(R.string.order_action));
		intent.putExtras(getIntent().getExtras());
		spec =
				tabHost.newTabSpec(MilkRunConstants.TAB_ORDER).setIndicator(
						createTabView(this, "ORDERS [" + o + "]")).setContent(
						intent);
		tabHost.addTab(spec);
		intent = new Intent(res.getString(R.string.intent_action_warning));
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtras(getIntent().getExtras());
		spec =
				tabHost.newTabSpec("Warning").setIndicator(
						createTabView(this, getString(R.string.warnings)))
						.setContent(intent);
		tabHost.addTab(spec);
		if (tab != null)
			tabHost.setCurrentTabByTag(tab);
	}

	@Override
	protected void onStart() {
		super.onStart();
		((TextView) findViewById(R.id.bc_merchant)).setText(merchant.name +" [ "+db.getValue(MilkRunConstants.MANIFEST_ID)+" ]");
	}

	private static View createTabView(final Context context, final String text) {
		View view = LayoutInflater.from(context).inflate(R.layout.tabs, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		return view;
	}

	@Override
	protected void onResume() {
		super.onResume();
		mlam.dispatchResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mlam.dispatchPause(isFinishing());
	}

	@Override
	public ArrayList<String> getItems() {
		return null;
	}
}
