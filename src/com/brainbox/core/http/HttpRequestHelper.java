package com.brainbox.core.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;


import com.brainbox.core.constants.ErrorConstants;
import com.brainbox.core.utils.LogUtils;
import com.brainbox.mobile.exception.SystemException;
import com.brainbox.shopclues.milkrun.R;

/**
 * @author Pramod Bindal
 * 
 */
public class HttpRequestHelper {
	public static Context context;

	public InputStream execute(HttpUriRequest request, List<NameValuePair> pairs) throws SystemException {
		InputStream is = null;
		try {
			request.addHeader("Accept-Encoding", "gzip,deflate");
			LogUtils.v(" Requesting Server Not Coming Here" + request.getRequestLine());

			HttpClient client = getHttpClient();
			HttpResponse response = client.execute(request);
			LogUtils.v(": Response Received ");
			Header contentEncoding = response.getFirstHeader("Content-Encoding");
			is = response.getEntity().getContent();
			LogUtils.v("Content Encoding  Not Coming Here: " + contentEncoding);
			if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
				is = new GZIPInputStream(is);
			}
			// client.getConnectionManager().shutdown();
		} catch (IOException e) {
			LogUtils.error(HttpRequestHelper.class.getName() + "Error ", e);
			throw new SystemException(ErrorConstants.NETWORK_ERROR);
		} finally {
		}
		return is;
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

	public String executeHttpRequest(Map<String, String> data, String requestType) throws SystemException {
		LogUtils.debug(HttpRequestHelper.class.getName() + ": Executing  Request");

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		// addParams(pairs);
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

	public String getHost() {
		return null;
	}

	protected static String readResponse(InputStream is) throws SystemException {
		StringBuffer res = new StringBuffer();
		try {
			byte[] buff = new byte[1024];
			int read = is.available();
			while ((read = is.read(buff)) != -1) {
				res.append(new String(buff, 0, read));
			}
			LogUtils.v(HttpRequestHelper.class, "Http Response is : " + res);
			return res.toString();
		} catch (IOException e) {
			LogUtils.error("Error while reading Stream", e);
			throw new SystemException(ErrorConstants.IO_ERROR, e.getMessage());
		}
	}

}
