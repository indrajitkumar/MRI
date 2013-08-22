package com.brainbox.core.http;

import static com.brainbox.core.constants.CommonConstants.LAT;
import static com.brainbox.core.constants.CommonConstants.LON;
import static com.brainbox.core.constants.CommonConstants.PASSWORD;
import static com.brainbox.core.constants.CommonConstants.UID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;


import com.brainbox.core.config.SystemConfig;
import com.brainbox.core.constants.CommonConstants;
import com.brainbox.core.constants.ErrorConstants;
import com.brainbox.core.helper.DatabaseHelper;
import com.brainbox.core.log.LogField;
import com.brainbox.core.utils.CommonUtils;
import com.brainbox.core.utils.LogUtils;
import com.brainbox.core.vo.LoginResponseVO;
import com.brainbox.log.GoogleFormSender;
import com.brainbox.log.LogSender;
import com.brainbox.log.vo.AppLogData;
import com.brainbox.mobile.exception.SystemException;
import com.brainbox.shopclues.milkrun.R;

public class HttpRequestHelperImpl extends HttpRequestHelper {
	public static final String ACTION_LOGIN = "login";
	public static final String ACTION_PING = "ping";
	protected DatabaseHelper db;
	protected static String PARAM_USER = "uid";

	public static final String PARAM_ACTION = "action";
	private static final String ACTION_UPDATE_REGID = "updateRegID";
	private static final String REG_ID = "reg_id";
	public static final String PARAM_VERSION = "ver";
	public static final String PARAM_BUILD = "build";
	public static Context context;
	public static String PARAM_IMEI = "imei";
	public LogSender logSender;

	public HttpRequestHelperImpl(Context context, DatabaseHelper db) {
		this.db = db;
		HttpRequestHelperImpl.context = context;
		logSender = new GoogleFormSender("dEhKbENzYUhjNWlxV2JSa3JXZ2dHcGc6MQ");
	}

	public HttpClient getHttpClient() {
		HttpClient client;

		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams
				.setConnectionTimeout(httpParams, context.getResources().getInteger(R.integer.conn_timeout));
		HttpConnectionParams.setSoTimeout(httpParams, context.getResources().getInteger(R.integer.conn_timeout));

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HostnameVerifier hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
		SchemeRegistry registry = new SchemeRegistry();
		SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
		;
		socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
		registry.register(new Scheme("https", socketFactory, 443));
		SocketFactory sf = new PlainSocketFactory();
		registry.register(new Scheme("http", sf, 80));
		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2965);
		SingleClientConnManager mgr = new SingleClientConnManager(httpClient.getParams(), registry);
		client = new DefaultHttpClient(mgr, httpClient.getParams());
		// Set verifier
		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
		return client;
	}

	public InputStream execute(HttpUriRequest request, List<NameValuePair> pairs) throws SystemException {
		InputStream is = null;
		try {
			request.addHeader("Accept-Encoding", "gzip,deflate");
			LogUtils.v(" Requesting Server Not Coming Here " + request.getRequestLine());

			HttpClient client = getHttpClient();
			HttpResponse response = client.execute(request);
			LogUtils.v(": Response Received ");
			Header contentEncoding = response.getFirstHeader("Content-Encoding");
			is = response.getEntity().getContent();
			LogUtils.v("Content Encoding Not Coming Here: " + contentEncoding);
			if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
				is = new GZIPInputStream(is);
			}
			// client.getConnectionManager().shutdown();
		} catch (IOException e) {
			LogUtils.error(HttpRequestHelper.class.getName() + "Error ", e);
			throw new SystemException(ErrorConstants.NETWORK_ERROR, CommonUtils.getErrorMessage(ErrorConstants.NETWORK_ERROR, context));
		} finally {
		}
		return is;
	}

	public String getHost() {
		return db.getValue(CommonConstants.SERVER_URL);
	}

	public InputStream executeHttpGet(List<NameValuePair> pairs) throws SystemException {
		LogUtils.debug(HttpRequestHelper.class.getName() + ": Executing Get Request");
		return executeHttpGet(pairs, getHost());
	}

	public InputStream executeHttpGet(List<NameValuePair> pairs, String uri) throws SystemException {
		LogUtils.debug(HttpRequestHelper.class.getName() + ": Executing Get Request");
		addParams(pairs);
		HttpGet request = new HttpGet(uri + "?" + URLEncodedUtils.format(pairs, "UTF-8"));
		return execute(request, null);
	}

	public InputStream executeHttpGet(String url) throws SystemException {
		LogUtils.debug(HttpRequestHelper.class.getName() + ": Executing Get Request");
		HttpGet request = new HttpGet(url);
		return execute(request, null);
	}

	protected void addParams(List<NameValuePair> pairs) {
		pairs.add(new BasicNameValuePair(PARAM_IMEI, db.getValue(CommonConstants.IMEI)));
		if (SystemConfig.location != null) {
			pairs.add(new BasicNameValuePair(LAT, String.valueOf(SystemConfig.location.getLatitude())));
			pairs.add(new BasicNameValuePair(LON, String.valueOf(SystemConfig.location.getLongitude())));
		} else {
			pairs.add(new BasicNameValuePair(LAT, db.getValue(LAT)));
			pairs.add(new BasicNameValuePair(LON, db.getValue(LON)));
		}
		pairs.add(new BasicNameValuePair(PARAM_USER, db.getValue(CommonConstants.UID)));
		pairs.add(new BasicNameValuePair(PARAM_BUILD, db.getValue(CommonConstants.BUILD)));
		pairs.add(new BasicNameValuePair(PARAM_VERSION, db.getValue(CommonConstants.APP_VERSION)));

	}

	protected void addParams(MultipartEntity entity) throws UnsupportedEncodingException {
		entity.addPart(CommonConstants.IMEI, new StringBody(db.getValue(CommonConstants.IMEI)));
		entity.addPart(PARAM_BUILD, new StringBody(db.getValue(CommonConstants.BUILD)));
		entity.addPart(PARAM_VERSION, new StringBody(db.getValue(CommonConstants.APP_VERSION)));
		String uid = db.getValue(CommonConstants.UID);
		if (uid != null) {
			entity.addPart(PARAM_USER, new StringBody(db.getValue(CommonConstants.UID)));
		}
		if (SystemConfig.location != null) {
			entity.addPart(LAT, new StringBody(String.valueOf(SystemConfig.location.getLatitude())));
			entity.addPart(LON, new StringBody(String.valueOf(SystemConfig.location.getLongitude())));
		}
	}

	public InputStream executeHttpPost(List<NameValuePair> pairs) throws SystemException {
		LogUtils.debug(HttpRequestHelper.class.getName() + ": Executing POST Request");
		addParams(pairs);
		HttpPost request = new HttpPost(getHost());
		try {
			request.setEntity(new UrlEncodedFormEntity(pairs));
		} catch (UnsupportedEncodingException e) {
			LogUtils.error("Error while processing request", e);
			throw new SystemException(ErrorConstants.NETWORK_ERROR);
		}
		return execute(request, pairs);
	}

	public String executeHttpRequest(Map<String, String> data, String requestType) throws SystemException {
		LogUtils.debug(HttpRequestHelper.class.getName() + ": Executing POST Request");

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		addParams(pairs);
		for (String key : data.keySet()) {
			pairs.add(new BasicNameValuePair(key, data.get(key)));
		}

		HttpUriRequest request;

		if (HttpPost.METHOD_NAME.equalsIgnoreCase(requestType)) {
			request = new HttpPost(getHost());
			try {
				((HttpPost) request).setEntity(new UrlEncodedFormEntity(pairs));
			} catch (UnsupportedEncodingException e) {
				LogUtils.error("Error while processing request", e);
				throw new SystemException(ErrorConstants.FORMAT_ERROR);
			}
		} else {
			request = new HttpGet(getHost());
		}

		InputStream is = execute(request, pairs);
		return readResponse(is);
	}

	public String executeHttpPost(MultipartEntity entity) throws SystemException {
		HttpPost httpPost = new HttpPost(getHost());
		String resp = null;
		try {
			addParams(entity);
			httpPost.setEntity(entity);
			httpPost.addHeader("Accept-Encoding", "gzip,deflate");
			InputStream is = execute(httpPost, null);
			resp = readResponse(is);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SystemException(ErrorConstants.IO_ERROR);
		}
		return resp;

	}

	public InputStream executeHttpPut(List<NameValuePair> pairs) throws SystemException {
		return executeHttpPut(pairs, getHost());
	}

	public InputStream executeHttpPut(List<NameValuePair> pairs, String uri) throws SystemException {
		LogUtils.debug(HttpRequestHelper.class.getName() + ": Executing Put Request");
		HttpPut request = new HttpPut(uri);
		addParams(pairs);
		try {
			request.setEntity(new UrlEncodedFormEntity(pairs));
		} catch (UnsupportedEncodingException e) {
			LogUtils.error("Error while processing request", e);
			throw new SystemException(ErrorConstants.NETWORK_ERROR);
		}
		return execute(request, pairs);
	}

	protected static String readResponse1(InputStream is) throws SystemException {
		LogUtils.v("Reading response from Input Stream");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = null;
		StringBuffer response = new StringBuffer();
		try {
			while ((line = br.readLine()) != null) {
				response.append(line + CommonConstants.NEW_LINE);
			}
			LogUtils.v("Http Response received is :" + response);
			is.close();
			br.close();
		} catch (IOException e) {
			LogUtils.error("Error while reading resposne", e);
		}
		return response.toString();
	}

	public LoginResponseVO login(String uid, String pwd) throws SystemException {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair(PASSWORD, pwd));
		pairs.add(new BasicNameValuePair(PARAM_ACTION, ACTION_LOGIN));
		db.insertValue(UID, uid);
		InputStream is = executeHttpPost(pairs);
		String response = readResponse(is);
		return handleLogin(response);
	}

	public String ping(String message) throws SystemException {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair(PARAM_ACTION, ACTION_PING));
		pairs.add(new BasicNameValuePair("msg", message));
		return readResponse(executeHttpPost(pairs));
	}

	public LoginResponseVO handleLogin(String response) throws SystemException {
		LoginResponseVO loginResponse = new LoginResponseVO(response);
		db.insertValue(CommonConstants.USER, loginResponse.userID);
		return loginResponse;
	}

	public String updateRegId(String regId) throws SystemException {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(REG_ID, regId));
		params.add(new BasicNameValuePair(PARAM_ACTION, ACTION_UPDATE_REGID));
		InputStream is = executeHttpPost(params);
		String resString = readResponse(is);
		return resString;
	}
}
