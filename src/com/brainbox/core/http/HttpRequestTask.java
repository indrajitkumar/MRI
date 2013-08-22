package com.brainbox.core.http;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.brainbox.core.helper.DatabaseHelper;
import com.brainbox.core.http.HttpRequestHelperImpl;
import com.brainbox.core.utils.JSONReader;
import com.brainbox.core.vo.HttpRequestVO;
import com.brainbox.core.vo.JSONResponseVO;
import com.brainbox.mobile.exception.SystemException;


public class HttpRequestTask extends
		AsyncTask<HttpRequestVO, Integer, JSONResponseVO> {
	DatabaseHelper db1;
	HttpRequestHelperImpl httpHelper;
	ProgressDialog dialog;
	Activity activity;
	Handler handler;

	public HttpRequestTask(Activity activity, HttpRequestHelperImpl httpHelper,
			Handler handler) {
		this.activity = activity;
		this.handler = handler;
		db1 = new DatabaseHelper(activity);
		this.httpHelper = httpHelper;
	}

	@Override
	protected void onPreExecute() {
		dialog = ProgressDialog.show(activity, null, "Please Wait. . .");
	}

	@Override
	protected JSONResponseVO doInBackground(HttpRequestVO... request) {

		JSONResponseVO response = null;
		try {
//			String respString = httpHelper.executeHttpRequest(
//					request[0].params, HttpPost.METHOD_NAME);
			String respString = httpHelper.executeHttpRequest(
					request[0].params,HttpGet.METHOD_NAME);
			response = (JSONResponseVO) JSONReader.getResponse(respString,
					JSONResponseVO.class);

		} catch (SystemException e) {
			e.printStackTrace();
		}
		return response;

	}

	@Override
	protected void onPostExecute(JSONResponseVO result) {
		dialog.dismiss();
		Message msg = handler.obtainMessage();
		Bundle b = new Bundle();
		b.putSerializable("DATA", result);
		msg.setData(b);
		handler.sendMessage(msg);
	}
}
