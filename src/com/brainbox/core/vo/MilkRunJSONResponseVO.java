package com.brainbox.core.vo;

import org.json.JSONException;
import org.json.JSONObject;

import com.brainbox.core.utils.LogUtils;

public class MilkRunJSONResponseVO {

	public static final String STATUS = "status";
	public static final String SUCCESS = "Success";
	public static final String FAILED = "Failed";
	public static final String MESSAGE = "Reason";
	public static final String PICKUP_BOY_ID = "pid";

	public String status = FAILED;
	public String message;

	public MilkRunJSONResponseVO(String jsonString) {

		try {
			JSONObject json = new JSONObject(jsonString);
			if (json.has(STATUS)) {
				status = json.getString(STATUS);
			}
			if (json.has(MESSAGE)) {
				message = json.getString(MESSAGE);

			}
		} catch (JSONException e) {
			message = "Invalid Response from server";
			LogUtils.error("Error while parsing Login JSON response : "
					+ jsonString, e);
		}
	}

	public MilkRunJSONResponseVO() {
		status = FAILED;
	}

}
