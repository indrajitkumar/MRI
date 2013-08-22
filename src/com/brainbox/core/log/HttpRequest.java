/*
 * This class was copied from this Stackoverflow Q&A:
 * http://stackoverflow.com/questions/2253061/secure-http-post-in-android/2253280#2253280
 * Thanks go to MattC!  
 */
package com.brainbox.core.log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import org.acra.util.FakeSocketFactory;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

import com.brainbox.core.utils.LogUtils;

public final class HttpRequest {

	private String login;
	private String password;
	private int connectionTimeOut = 3000;
	private int socketTimeOut = 3000;
	private int maxNrRetries = 3;

	public void setLogin(String login) {
		this.login = login;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setConnectionTimeOut(int connectionTimeOut) {
		this.connectionTimeOut = connectionTimeOut;
	}

	public void setSocketTimeOut(int socketTimeOut) {
		this.socketTimeOut = socketTimeOut;
	}

	/**
	 * The default number of retries is 3.
	 * 
	 * @param maxNrRetries
	 *            Max number of times to retry Request on failure due to
	 *            SocketTimeOutException.
	 */
	public void setMaxNrRetries(int maxNrRetries) {
		this.maxNrRetries = maxNrRetries;
	}

	/**
	 * Posts to a URL.
	 * 
	 * @param url
	 *            URL to which to post.
	 * @param parameters
	 *            Map of parameters to post to a URL.
	 * @throws IOException
	 *             if the data cannot be posted.
	 */
	public void sendPost(URL url, Map<?, ?> parameters) throws IOException {

		final HttpClient httpClient = getHttpClient();
		final HttpPost httpPost = getHttpPost(url, parameters);

		LogUtils.debug( "Sending request to " + url);
		
			LogUtils.debug( "HttpPost params : ");
		for (final Object key : parameters.keySet()) {
			
				LogUtils.debug( " key : '" + key + "'    value: '"
						+ parameters.get(key) + "'");
		}

		final HttpResponse response = httpClient.execute(httpPost,
				new BasicHttpContext());
		if (response != null) {
			final StatusLine statusLine = response.getStatusLine();
			if (statusLine != null) {
				final String statusCode = Integer.toString(response
						.getStatusLine().getStatusCode());
				if (statusCode.startsWith("4") || statusCode.startsWith("5")) {
					
						LogUtils.debug( "Could not send HttpPost : "
								+ httpPost);
					throw new IOException("Host returned error code "
							+ statusCode);
				}
			}

			
				LogUtils.debug( "HttpResponse Status : "
						+ (statusLine != null ? statusLine.getStatusCode()
								: "NoStatusLine#noCode"));
			final String content = EntityUtils.toString(response.getEntity());
			
				LogUtils.debug(
						"HttpResponse Content : "
								+ content.substring(0,
										Math.min(content.length(), 200)));

		} else {
			
				LogUtils.debug( "HTTP no Response!!");
		}
	}

	/**
	 * @return HttpClient to use with this HttpRequest.
	 */
	private HttpClient getHttpClient() {
		final HttpParams httpParams = new BasicHttpParams();
		httpParams.setParameter(ClientPNames.COOKIE_POLICY,
				CookiePolicy.RFC_2109);
		HttpConnectionParams
				.setConnectionTimeout(httpParams, connectionTimeOut);
		HttpConnectionParams.setSoTimeout(httpParams, socketTimeOut);
		HttpConnectionParams.setSocketBufferSize(httpParams, 8192);

		final SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", new PlainSocketFactory(), 80));
		registry.register(new Scheme("https", (new FakeSocketFactory()), 443));

		final ClientConnectionManager clientConnectionManager = new ThreadSafeClientConnManager(
				httpParams, registry);
		final DefaultHttpClient httpClient = new DefaultHttpClient(
				clientConnectionManager, httpParams);

		return httpClient;
	}

	/**
	 * @return Credentials to use with this HttpRequest or null if no
	 *         credentials were supplied.
	 */
	private UsernamePasswordCredentials getCredentials() {
		if (login != null || password != null) {
			return new UsernamePasswordCredentials(login, password);
		}

		return null;
	}

	private HttpPost getHttpPost(URL url, Map<?, ?> parameters)
			throws UnsupportedEncodingException {

		final HttpPost httpPost = new HttpPost(url.toString());

		final UsernamePasswordCredentials creds = getCredentials();
		if (creds != null) {
			httpPost.addHeader(BasicScheme.authenticate(creds, "UTF-8", false));
		}
		httpPost.setHeader("User-Agent", "Android");
		httpPost.setHeader(
				"Accept",
				"text/html,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
		httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

		final String paramsAsString = getParamsAsString(parameters);
		httpPost.setEntity(new StringEntity(paramsAsString, "UTF-8"));

		return httpPost;
	}

	/**
	 * Converts a Map of parameters into a URL encoded Sting.
	 * 
	 * @param parameters
	 *            Map of parameters to convert.
	 * @return URL encoded String representing the parameters.
	 * @throws UnsupportedEncodingException
	 *             if one of the parameters couldn't be converted to UTF-8.
	 */
	private String getParamsAsString(Map<?, ?> parameters)
			throws UnsupportedEncodingException {

		final StringBuilder dataBfr = new StringBuilder();
		for (final Object key : parameters.keySet()) {
			if (dataBfr.length() != 0) {
				dataBfr.append('&');
			}
			final Object preliminaryValue = parameters.get(key);
			final Object value = (preliminaryValue == null) ? ""
					: preliminaryValue;
			dataBfr.append(URLEncoder.encode(key.toString(), "UTF-8"));
			dataBfr.append('=');
			dataBfr.append(URLEncoder.encode(value.toString(), "UTF-8"));
		}

		return dataBfr.toString();
	}
}