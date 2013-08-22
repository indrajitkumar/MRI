package com.brainbox.shopclues.milkrun.activity;

import java.util.List;

import android.app.Activity;

import com.brainbox.core.config.SystemConfig;
import com.brainbox.core.utils.LogUtils;
import com.brainbox.core.utils.MilkRunUtils;
import com.brainbox.core.vo.ReasonVO;

import com.brainbox.milkrun.constants.MilkRunConstants;
import com.brainbox.milkrun.helper.MilkRunHttpRequestHelper;

import com.brainbox.vo.enums.ActionEnum;

public class MRAsyncTaskHandler extends AsyncHandler {

	public MRAsyncTaskHandler(Activity context, ActionEnum action) {
		super(context, action);
	}

	public void refresh() {
		MilkRunHttpRequestHelper httpHelper = (MilkRunHttpRequestHelper) SystemConfig.httpRequestHelper;
		try {
			db.cleanMR();
			LogUtils.debug("Indrajit Database Name=========="+db.getDatabaseName());
			String res = httpHelper.loadOrders();
			MilkRunUtils.readManifest(res, MilkRunConstants.STATUS_OPEN);
			List<ReasonVO> reasons = httpHelper.loadReasons();
			db.updateReasons(reasons);
			LogUtils.debug("Indrajit Updated Database Named=========="+db.getDatabaseName());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
