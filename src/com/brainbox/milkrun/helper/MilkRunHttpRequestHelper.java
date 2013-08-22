package com.brainbox.milkrun.helper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;

import com.brainbox.core.helper.DatabaseHelper;
import com.brainbox.core.http.HttpRequestHelperImpl;
import com.brainbox.core.utils.LogUtils;
import com.brainbox.core.utils.MRJSONParser;
import com.brainbox.core.vo.Merchant;
import com.brainbox.core.vo.ReasonVO;
import com.brainbox.milkrun.constants.MilkRunConstants;

import com.brainbox.mobile.exception.SystemException;

import com.brainbox.vo.enums.ActionEnum;
import com.brainbox.vo.enums.PinTypeEnum;

public class MilkRunHttpRequestHelper extends HttpRequestHelperImpl {
	public static final String PARAM_MERCHANT_PIN = "merchant_pin";
	public static final String PARAM_PICKUPBOY_PIN = "pickupboy_pin";
	public static final String PARAM_MERCHANT_ID = "merchantId";
	public static final String PARAM_REASON_ID = "reasonId";
	public static final String PARAM_MANIFEST_ID = "manifest_id";
	public static final String PARAM_PICKUP_BOY_ID = "pickupboyid";
	public static final String PARAM_TYPE = "type";
	public static final String ACTION_DOWNLOAD = "download";
	public static final String ACTION_LOAD_REASONS = "reasonids";

	public String loadOrders() throws Exception {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
//		pairs.add(new BasicNameValuePair(PARAM_PICKUP_BOY_ID, db
//		        .getValue(MilkRunConstants.PICKUP_BOY_ID)));
		pairs.add(new BasicNameValuePair(PARAM_ACTION, ACTION_DOWNLOAD));
		InputStream is = executeHttpGet(pairs);
		String res = null;
		res = readResponse(is);
		return res;
	}

	public MilkRunHttpRequestHelper(Context ctx, DatabaseHelper db) {
		super(ctx, db);
	}

	public String executeHttpPost(Map<String, String> params)
	        throws SystemException {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		for (String key : params.keySet()) {
			pairs.add(new BasicNameValuePair(key, params.get(key)));
		}
		InputStream is =executeHttpPost(pairs);
		String res = readResponse(is);
		return res;
	}

	public String executeHttpGet(Map<String, String> params)
	        throws SystemException {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		for (String key : params.keySet()) {
			pairs.add(new BasicNameValuePair(key, params.get(key)));
		}
		InputStream is = executeHttpGet(pairs);
		String res = null;
		res = readResponse(is);
		return res;
	}

	public void addParams(List<NameValuePair> pairs) {
		pairs.add(new BasicNameValuePair(PARAM_MANIFEST_ID, db
		        .getValue(MilkRunConstants.MANIFEST_ID)));
		pairs.add(new BasicNameValuePair(PARAM_PICKUP_BOY_ID, db
		        .getValue(MilkRunConstants.PICKUP_BOY_ID)));
		super.addParams(pairs);
	}

	public String validatePIN(CharSequence pin, PinTypeEnum pinType,
	        Merchant merchant) throws SystemException {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair(PARAM_ACTION,
		        ActionEnum.CLOSE_PO.value));
		if (pinType == PinTypeEnum.TYPE_MERCHANT) {
			pairs.add(new BasicNameValuePair(PARAM_MERCHANT_PIN, pin.toString()));
		} else {
			pairs.add(new BasicNameValuePair(PARAM_PICKUPBOY_PIN, pin
			        .toString()));
		}
		pairs.add(new BasicNameValuePair(PARAM_MERCHANT_ID, merchant.merchantId));
		InputStream is = executeHttpGet(pairs);
		String res = readResponse(is);
		return res;
	}

	public String skipMerchant(Merchant merchant, ReasonVO reason)
	        throws SystemException {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair(PARAM_ACTION,
		        ActionEnum.SKIP_MERCHANT.value));
		pairs.add(new BasicNameValuePair(PARAM_MERCHANT_ID, merchant.merchantId));
		pairs.add(new BasicNameValuePair(PARAM_REASON_ID, reason.reasonCode));
		InputStream is = executeHttpGet(pairs);
		String res = readResponse(is);
		return res;
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
