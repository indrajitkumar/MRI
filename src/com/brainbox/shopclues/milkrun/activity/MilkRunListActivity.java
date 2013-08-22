package com.brainbox.shopclues.milkrun.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

public class MilkRunListActivity extends MilkRunActivity {
	protected ExpandableListView eListView;
	BaseExpandableListAdapter expListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mInflater =
				(LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		super.onCreate(savedInstanceState);
	}

	@Override
	public ArrayList<String> getItems() {
		return null;
	}
	
	public void confirm(View v) {
		promptConfirm();
	}
}