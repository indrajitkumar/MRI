package com.brainbox.shopclues.milkrun.http;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;

import com.brainbox.core.helper.DatabaseHelper;
import com.brainbox.core.utils.JSONReader;
import com.brainbox.core.utils.LogUtils;
import com.brainbox.core.utils.MRJSONParser;
import com.brainbox.core.vo.LoginResponseVO;
import com.brainbox.core.vo.MRLoginResponseVO;
import com.brainbox.core.vo.ReasonVO;
import com.brainbox.milkrun.constants.MilkRunConstants;
import com.brainbox.milkrun.helper.MilkRunHttpRequestHelper;

import com.brainbox.mobile.exception.SystemException;


public class SCHttpRequestHelper extends MilkRunHttpRequestHelper {

	public SCHttpRequestHelper(Context ctx, DatabaseHelper db) {
		super(ctx, db);
	}

	public LoginResponseVO handleLogin(String response) throws SystemException {
		MRLoginResponseVO loginResponse = (MRLoginResponseVO) JSONReader
				.getLoginResponse(response, MRLoginResponseVO.class);
		db.insertValue(MilkRunConstants.PICKUP_BOY_ID, loginResponse.pickUpBoyId);
		db.insertValue(MilkRunConstants.MILEAGE_UPDATED, String.valueOf(loginResponse.mileageUpdated));
		loginResponse.message = loginResponse.reason;
		return loginResponse;
	}

	public List<ReasonVO> loadReasons() throws SystemException {
		LogUtils.debug("Loading reasons");
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(PARAM_ACTION, ACTION_LOAD_REASONS));
		InputStream is = executeHttpGet(params);
		String resString = readResponse(is);
		return MRJSONParser.readReasons(resString);

	}

}
