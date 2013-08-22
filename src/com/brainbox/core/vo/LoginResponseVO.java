package com.brainbox.core.vo;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.annotations.SerializedName;

public class LoginResponseVO implements Serializable {

	private static final long serialVersionUID = 1L;

	public String status;
	public String name;
	public String message;
	@Deprecated
	public String comment;

	@SerializedName("user")
	public String userID;
	public boolean isLoggedIn;

	public static final String STATUS = "status";
	public static final String SUCCESS = "SUCCESS";
	public static final String LOGIN_FAILED = "Failed";
	public static final String REASON = "reason";
	public static final String USER_ID = "user";

	public LoginResponseVO() {

	}

	@Deprecated
	public LoginResponseVO(String jsonString) {

		try {
			JSONObject json = new JSONObject(jsonString);
			if (json.has(STATUS)) {
				status = json.getString(STATUS);
				if (SUCCESS.equalsIgnoreCase(status) && json.has(USER_ID)) {
					isLoggedIn = true;
					userID = json.getString(USER_ID);
				} else {
					isLoggedIn = false;
					message = json.getString(REASON);
				}
			}
		} catch (JSONException e) {
			isLoggedIn = false;
			message = "Invalid Response from server";
		}
	}

}
