package com.brainbox.shopclues.milkrun.activity;

import java.util.ArrayList;

import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

import com.brainbox.milkrun.constants.MilkRunConstants;
import com.brainbox.shopclues.milkrun.R;

public class MerchantTabViewActivity extends MilkRunActivity {
	LocalActivityManager mlam;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_view);
		((TextView) findViewById(R.id.header_title)).setText("Manifest ID : "
		        + db.getValue(MilkRunConstants.MANIFEST_ID));
		mlam = new LocalActivityManager(this, false);
		TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
		mlam.dispatchCreate(savedInstanceState);
		tabHost.setup(mlam);
		tabHost.getTabWidget().setDividerDrawable(R.drawable.tab_divider);
		TabHost.TabSpec spec;
		Intent intent = null;
		intent = new Intent(this, MerchantViewActivity.class);
		// intent.putExtras(getIntent().getExtras());
		spec =
		        tabHost.newTabSpec("Merchant").setIndicator(
		                createTabView("MERCHANTS ["
		                        + db.getAllMerchants().size() + "]"))
		                .setContent(intent);
		tabHost.addTab(spec);
		// intent = new Intent(this, DashboardActivity.class);
		// spec = tabHost.newTabSpec("MAp")
		// .setIndicator(createTabView("MAP VIEW"))
		// .setContent(R.id.map_view);
		// tabHost.addTab(spec);
	}

	private View createTabView(final String text) {
		View view = LayoutInflater.from(this).inflate(R.layout.tabs, null);
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
