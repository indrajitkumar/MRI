package com.brainbox.core.utils;

import java.util.Arrays;

import com.brainbox.core.constants.ErrorConstants;
import com.brainbox.core.vo.LoginResponseVO;
import com.brainbox.mobile.exception.SystemException;
import com.google.gson.GsonBuilder;

public class JSONReader {

	public static String TIME_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";
	public static final String SUCCESS = "SUCCESS";
	protected static GsonBuilder builder;

	static {
		builder = new GsonBuilder();
	}

	public static LoginResponseVO getLoginResponse(String jsonString, Class<? extends LoginResponseVO> clazz)
			throws SystemException {
		LoginResponseVO vo;

		try {
			vo = builder.create().fromJson(jsonString, clazz);
		} catch (Exception e) {
			LogUtils.error("Error while reading response", e);
			throw new SystemException(ErrorConstants.JSON_ERROR, e.getMessage());
		}
		vo.isLoggedIn = SUCCESS.equalsIgnoreCase(vo.status);
		return vo;
	}

	public static <T> T getResponse(String jsonString, Class<T> clazz) throws SystemException {
		T vo;
		try {
			vo = builder.create().fromJson(jsonString, clazz);
		} catch (Exception e) {
			LogUtils.error("Error while reading response", e);
			throw new SystemException(ErrorConstants.JSON_ERROR, e.getMessage());
		}
		return vo;
	}

}
