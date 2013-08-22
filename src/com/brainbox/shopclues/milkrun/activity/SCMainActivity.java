package com.brainbox.shopclues.milkrun.activity;

import com.brainbox.core.config.SystemConfig;
import com.brainbox.milkrun.constants.MilkRunConstants;
import com.brainbox.shopclues.milkrun.http.SCHttpRequestHelper;




public class SCMainActivity extends MilkRunMainActivity {
	public void init() {
		super.init();
		SystemConfig.httpRequestHelper =
		        MilkRunConstants.httpRequestHelper =
		                new SCHttpRequestHelper(this, db);
	}
}
